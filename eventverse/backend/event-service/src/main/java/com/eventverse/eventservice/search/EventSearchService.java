package com.eventverse.eventservice.search;

import com.eventverse.eventservice.domain.Event;
import com.eventverse.eventservice.dto.EventResponse;
import com.eventverse.eventservice.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventSearchService {

    private final ElasticsearchOperations operations;
    private final EventMapper mapper;

    public Page<EventResponse> search(String query, String city, int page, int size) {
        if (query == null || query.isBlank()) {
            return Page.empty();
        }
        Criteria text = new Criteria("title").matches(query)
                .or(new Criteria("description").matches(query))
                .or(new Criteria("venue").matches(query))
                .or(new Criteria("category").matches(query))
                .or(new Criteria("city").matches(query));

        Criteria criteria = text;
        if (city != null && !city.isBlank()) {
            criteria = criteria.and(new Criteria("city").is(city));
        }

        Pageable pageable = PageRequest.of(page, size);
        CriteriaQuery cq = new CriteriaQuery(criteria, pageable);

        SearchHits<EventDocument> hits = operations.search(cq, EventDocument.class);
        List<EventResponse> responses = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResponse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, PageRequest.of(page, size), hits.getTotalHits());
    }

    public void index(Event event) {
        operations.save(toDocument(event));
    }

    public void delete(Long id) {
        operations.delete(id.toString(), EventDocument.class);
    }

    private Optional<EventResponse> toResponse(EventDocument doc) {
        if (doc == null || doc.getId() == null) return Optional.empty();
        EventResponse resp = new EventResponse();
        resp.setId(doc.getId());
        resp.setTitle(doc.getTitle());
        resp.setDescription(doc.getDescription());
        resp.setCity(doc.getCity());
        resp.setVenue(doc.getVenue());
        resp.setCategory(doc.getCategory());
        resp.setTime(doc.getTime());
        resp.setImageUrl(doc.getImageUrl());
        return Optional.of(resp);
    }

    private EventDocument toDocument(Event ev) {
        return EventDocument.builder()
                .id(ev.getId())
                .title(ev.getTitle())
                .description(ev.getDescription())
                .city(ev.getCity())
                .venue(ev.getVenue())
                .category(ev.getCategory())
                .time(ev.getTime())
                .imageUrl(ev.getImageUrl())
                .popularityScore(ev.getPopularityScore())
                .build();
    }
}
