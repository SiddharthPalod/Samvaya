package com.eventverse.eventservice.service;
import com.eventverse.eventservice.domain.Event;
import com.eventverse.eventservice.dto.EventCreateRequest;
import com.eventverse.eventservice.dto.EventFilterRequest;
import com.eventverse.eventservice.dto.EventResponse;
import com.eventverse.eventservice.dto.SeatAvailabilityResponse;
import com.eventverse.eventservice.mapper.EventMapper;
import com.eventverse.eventservice.repository.EventRepository;
import com.eventverse.eventservice.search.EventSearchService;
import com.eventverse.eventservice.sharding.ShardContext;
import com.eventverse.eventservice.sharding.ShardId;
import com.eventverse.eventservice.sharding.ShardResolver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final CacheManager cacheManager;
    private final ShardResolver shardResolver;
    private final BloomFilter bloomFilter;
    private final EventSearchService searchService;
    private final TicketInventoryClient ticketInventoryClient;

    public EventService(EventRepository eventRepository,
                        EventMapper eventMapper,
                        CacheManager cacheManager,
                        ShardResolver shardResolver,
                        BloomFilter bloomFilter,
                        EventSearchService searchService,
                        TicketInventoryClient ticketInventoryClient) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.cacheManager = cacheManager;
        this.shardResolver = shardResolver;
        this.bloomFilter = bloomFilter;
        this.searchService = searchService;
        this.ticketInventoryClient = ticketInventoryClient;
    }

    @Transactional
    @CacheEvict(value = {"popularEvents"}, allEntries = true)
    public EventResponse createEvent(EventCreateRequest req) {
        ShardId shard = shardResolver.resolveByCity(req.getCity());
        try {
            ShardContext.set(shard);
            Event event = eventMapper.toEntity(req);
            if (event.getPrice() == null) {
                event.setPrice(BigDecimal.ZERO);
            }
            Event saved = eventRepository.save(event);
            SeatAvailabilityResponse seatInfo = syncCapacity(saved);
            saved = reconcileCapacity(saved, seatInfo);
            // populate bloom filter for fast existence checks
            bloomFilter.add(String.valueOf(saved.getId()));
            // index into Elasticsearch
            try { searchService.index(saved); } catch (Exception ignored) {}
            EventResponse response = eventMapper.toResponse(saved);
            applySeatData(response, seatInfo);
            return response;
        } finally {
            ShardContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        // Try to get from cache first, but handle deserialization errors gracefully
        ShardId shard = shardResolver.resolveByEventId(id);
        try {
            ShardContext.set(shard);
            // Bloom filter: if it definitively doesn't contain the id, short-circuit
            if (!bloomFilter.mightContain(String.valueOf(id))) {
                throw new EntityNotFoundException("Event not found: " + id);
            }
            Cache cache = cacheManager.getCache("eventDetails");
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(id);
                if (wrapper != null) {
                    try {
                        Object cached = wrapper.get();
                        if (cached instanceof EventResponse) {
                            EventResponse copied = copyEventResponse((EventResponse) cached);
                            return attachSeatAvailability(copied);
                        }
                    } catch (Exception e) {
                        // If deserialization fails (old cache format), evict and continue
                        cache.evict(id);
                    }
                }
            }
        } catch (Exception e) {
            // If cache access fails, just continue to database lookup
        }
        finally {
            ShardContext.clear();
        }
        
        // Fall back to database lookup
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));
        EventResponse baseResponse = eventMapper.toResponse(event);
        // ensure bloom filter is warmed for future checks
        bloomFilter.add(String.valueOf(id));
        try { searchService.index(event); } catch (Exception ignored) {}
        
        // Cache the result for future requests
        try {
            Cache cache = cacheManager.getCache("eventDetails");
            if (cache != null) {
                cache.put(id, baseResponse);
            }
        } catch (Exception e) {
            // If caching fails, just continue without caching
        }
        
        return attachSeatAvailability(baseResponse);
    }

    @Transactional(readOnly = true)
    public BigDecimal getPriceForEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));
        return event.getPrice() != null ? event.getPrice() : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> searchFullText(String query, String city, int page, int size) {
        try {
            Page<EventResponse> es = searchService.search(query, city, page, size);
            // Fallback to DB if index is empty or not yet warmed
            if (!es.isEmpty()) return es.map(this::attachSeatAvailability);
        } catch (Exception ignored) {
            // fall back to JPA search if ES not available
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "popularityScore").and(Sort.by("time")));
        Specification<Event> spec = (root, q, cb) -> {
            var predicates = cb.conjunction();
            String like = "%" + query.toLowerCase() + "%";
            var or = cb.disjunction();
            or.getExpressions().add(cb.like(cb.lower(root.get("title")), like));
            or.getExpressions().add(cb.like(cb.lower(root.get("description")), like));
            or.getExpressions().add(cb.like(cb.lower(root.get("venue")), like));
            or.getExpressions().add(cb.like(cb.lower(root.get("category")), like));
            or.getExpressions().add(cb.like(cb.lower(root.get("city")), like));
            predicates.getExpressions().add(or);
            predicates.getExpressions().add(cb.equal(root.get("publicEvent"), true));
            if (city != null && !city.isBlank()) {
                predicates.getExpressions().add(cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
            }
            return predicates;
        };
        return eventRepository.findAll(spec, pageable)
                .map(eventMapper::toResponse)
                .map(this::attachSeatAvailability);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "popularEvents",
            key = "T(java.util.Objects).hash(#filter.city, #filter.fromTime, #filter.toTime, #filter.page, #filter.size)",
            condition = "#filter.sort != null && #filter.sort.equalsIgnoreCase('POPULAR')"
    )
    public Page<EventResponse> searchEvents(EventFilterRequest filter) {
        ShardId shard = shardResolver.resolveByCity(filter.getCity());
        try {
            ShardContext.set(shard);
            Pageable pageable = PageRequest.of(
                    filter.getPage(),
                    filter.getSize(),
                    resolveSort(filter.getSort())
            );
            Specification<Event> spec = buildSpecification(filter);
            Page<Event> page = eventRepository.findAll(spec, pageable);
            return page.map(eventMapper::toResponse)
                    .map(this::attachSeatAvailability);
        } finally {
            ShardContext.clear();
        }
    }

    @Transactional
    @CacheEvict(value = {"popularEvents"}, allEntries = true)
    public EventResponse updateEvent(Long id, EventCreateRequest req, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));

        // optional: only organizer can update
        if (!event.getOrganizerId().equals(userId)) {
            throw new IllegalStateException("You are not the organizer of this event");
        }

        // update fields
        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setCity(req.getCity());
        event.setTime(req.getTime());
        event.setCapacity(req.getCapacity());
        event.setVenue(req.getVenue());
        event.setCategory(req.getCategory());
        event.setPublicEvent(req.getPublicEvent());
        event.setImageUrl(req.getImageUrl());
        if (req.getPrice() != null) {
            event.setPrice(req.getPrice());
        }

        Event saved = eventRepository.save(event);
        SeatAvailabilityResponse seatInfo = syncCapacity(saved);
        saved = reconcileCapacity(saved, seatInfo);

        // evict eventDetails cache entry for this id
        Cache detailsCache = cacheManager.getCache("eventDetails");
        if (detailsCache != null) {
            detailsCache.evict(id);
        }

        // ensure bloom filter contains updated event id
        bloomFilter.add(String.valueOf(saved.getId()));
        try { searchService.index(saved); } catch (Exception ignored) {}

        EventResponse response = eventMapper.toResponse(saved);
        applySeatData(response, seatInfo);
        return response;
    }

    // ---------- Admin (superuser) operations ----------
    @Transactional
    @CacheEvict(value = {"popularEvents"}, allEntries = true)
    public EventResponse adminCreateEvent(EventCreateRequest req) {
        if (req.getOrganizerId() == null) {
            throw new IllegalArgumentException("organizerId is required for admin create");
        }
        return createEvent(req);
    }

    @Transactional
    @CacheEvict(value = {"popularEvents"}, allEntries = true)
    public EventResponse adminUpdateEvent(Long id, EventCreateRequest req) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));

        event.setTitle(req.getTitle());
        event.setDescription(req.getDescription());
        event.setCity(req.getCity());
        event.setTime(req.getTime());
        event.setCapacity(req.getCapacity());
        event.setVenue(req.getVenue());
        event.setCategory(req.getCategory());
        event.setImageUrl(req.getImageUrl());
        if (req.getPublicEvent() != null) {
            event.setPublicEvent(req.getPublicEvent());
        }
        if (req.getPrice() != null) {
            event.setPrice(req.getPrice());
        }
        if (req.getOrganizerId() != null) {
            event.setOrganizerId(req.getOrganizerId());
        }

        Event saved = eventRepository.save(event);
        SeatAvailabilityResponse seatInfo = syncCapacity(saved);
        saved = reconcileCapacity(saved, seatInfo);
        Cache detailsCache = cacheManager.getCache("eventDetails");
        if (detailsCache != null) {
            detailsCache.evict(id);
        }
        bloomFilter.add(String.valueOf(saved.getId()));
        try { searchService.index(saved); } catch (Exception ignored) {}
        EventResponse response = eventMapper.toResponse(saved);
        applySeatData(response, seatInfo);
        return response;
    }

    @Transactional
    @CacheEvict(value = {"popularEvents"}, allEntries = true)
    public void deleteEvent(Long id, Long userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found: " + id));

        // optional: only organizer can delete
        if (!event.getOrganizerId().equals(userId)) {
            throw new IllegalStateException("You are not the organizer of this event");
        }

        eventRepository.delete(event);

        // evict eventDetails cache entry for this id
        Cache detailsCache = cacheManager.getCache("eventDetails");
        if (detailsCache != null) {
            detailsCache.evict(id);
        }
        try { searchService.delete(id); } catch (Exception ignored) {}
    }

    private SeatAvailabilityResponse syncCapacity(Event event) {
        try {
            return ticketInventoryClient.syncCapacity(event.getId(), event.getCapacity());
        } catch (Exception ignored) {
            return null;
        }
    }

    private EventResponse applySeatData(EventResponse response, SeatAvailabilityResponse seatInfo) {
        if (response == null || seatInfo == null) return response;
        response.setTotalSeats(seatInfo.getTotalSeats());
        response.setAvailableSeats(seatInfo.getAvailableSeats());
        response.setCapacity(seatInfo.getTotalSeats());
        return response;
    }

    private Event reconcileCapacity(Event saved, SeatAvailabilityResponse seatInfo) {
        if (saved == null || seatInfo == null || seatInfo.getTotalSeats() == null) {
            return saved;
        }
        if (!seatInfo.getTotalSeats().equals(saved.getCapacity())) {
            saved.setCapacity(seatInfo.getTotalSeats());
            return eventRepository.save(saved);
        }
        return saved;
    }

    private EventResponse attachSeatAvailability(EventResponse response) {
        if (response == null || response.getId() == null) return response;
        try {
            SeatAvailabilityResponse seatInfo = ticketInventoryClient.fetchAvailability(response.getId());
            applySeatData(response, seatInfo);
        } catch (Exception ignored) {
            // keep base event response if inventory service is unavailable
        }
        return response;
    }

    private EventResponse copyEventResponse(EventResponse source) {
        if (source == null) return null;
        EventResponse copy = new EventResponse();
        copy.setId(source.getId());
        copy.setTitle(source.getTitle());
        copy.setDescription(source.getDescription());
        copy.setCity(source.getCity());
        copy.setTime(source.getTime());
        copy.setCapacity(source.getCapacity());
        copy.setTotalSeats(source.getTotalSeats());
        copy.setAvailableSeats(source.getAvailableSeats());
        copy.setOrganizerId(source.getOrganizerId());
        copy.setVenue(source.getVenue());
        copy.setCategory(source.getCategory());
        copy.setPublicEvent(source.isPublicEvent());
        copy.setImageUrl(source.getImageUrl());
        copy.setPopularityScore(source.getPopularityScore());
        copy.setPrice(source.getPrice());
        return copy;
    }

    private Sort resolveSort(String sort) {
        if (sort != null) {
            if (sort.equalsIgnoreCase("POPULAR")) {
                return Sort.by(
                        Sort.Order.desc("popularityScore"),
                        Sort.Order.asc("time")
                );
            }
            if (sort.equalsIgnoreCase("TIME_DESC")) {
                return Sort.by(Sort.Direction.DESC, "time");
            }
        }
        return Sort.by(Sort.Direction.ASC, "time");
    }

    private Specification<Event> buildSpecification(EventFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (filter.getCity() != null && !filter.getCity().isBlank()) {
                predicates.getExpressions().add(
                        cb.equal(cb.lower(root.get("city")), filter.getCity().toLowerCase())
                );
            }
            if (filter.getOrganizerId() != null) {
                predicates.getExpressions().add(
                        cb.equal(root.get("organizerId"), filter.getOrganizerId())
                );
            }
            if (filter.getFromTime() != null) {
                predicates.getExpressions().add(
                        cb.greaterThanOrEqualTo(root.get("time"), filter.getFromTime())
                );
            }
            if (filter.getToTime() != null) {
                predicates.getExpressions().add(
                        cb.lessThanOrEqualTo(root.get("time"), filter.getToTime())
                );
            } else {
                predicates.getExpressions().add(
                        cb.greaterThanOrEqualTo(root.get("time"), OffsetDateTime.now())
                );
            }
            return predicates;
        };
    }
}