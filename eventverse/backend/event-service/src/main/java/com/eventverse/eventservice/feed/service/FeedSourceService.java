package com.eventverse.eventservice.feed.service;

import com.eventverse.eventservice.domain.Event;
import com.eventverse.eventservice.feed.dto.EventScore;
import com.eventverse.eventservice.feed.model.FeedType;
import com.eventverse.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedSourceService {

    private final EventRepository eventRepository;

    /**
     * Fetch feed candidates from the database with simple scoring rules:
     * - TRENDING: highest popularityScore, city-scoped when provided
     * - RECOMMENDED: bias to user city if available, otherwise global trending
     * - UPCOMING: soonest upcoming events (time ascending), city-scoped when provided
     */
    public List<EventScore> fetchFromDb(FeedType type, String userId, String city) {
        int limit = 100; // warm a healthy page of results
        switch (type) {
            case TRENDING -> {
                return toScores(fetchTrending(city, limit), false);
            }
            case RECOMMENDED -> {
                // Basic recommender: prioritize user city trending; fallback to global trending
                List<Event> cityTrending = fetchTrending(city, limit);
                if (cityTrending.isEmpty()) {
                    cityTrending = fetchTrending(null, limit);
                }
                return toScores(cityTrending, true);
            }
            case UPCOMING -> {
                return toScores(fetchUpcoming(city, limit), false);
            }
            default -> throw new IllegalArgumentException("Unsupported feed type: " + type);
        }
    }

    private List<Event> fetchTrending(String city, int limit) {
        Pageable pageable = PageRequest.of(0, limit,
                Sort.by(Sort.Order.desc("popularityScore"), Sort.Order.asc("time")));
        if (city != null && !city.isBlank()) {
            return eventRepository.findByCityAndPublicEventTrueOrderByPopularityScoreDescTimeAsc(city, pageable);
        }
        return eventRepository.findByPublicEventTrueOrderByPopularityScoreDescTimeAsc(pageable);
    }

    private List<Event> fetchUpcoming(String city, int limit) {
        OffsetDateTime now = OffsetDateTime.now();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.asc("time")));
        if (city != null && !city.isBlank()) {
            return eventRepository.findByCityAndPublicEventTrueAndTimeAfterOrderByTimeAsc(city, now, pageable);
        }
        return eventRepository.findByPublicEventTrueAndTimeAfterOrderByTimeAsc(now, pageable);
    }

    private List<EventScore> toScores(List<Event> events, boolean addRecencyBoost) {
        OffsetDateTime now = OffsetDateTime.now();
        return events.stream()
                .map(ev -> new EventScore(
                        String.valueOf(ev.getId()),
                        computeScore(ev, now, addRecencyBoost)
                ))
                // extra safety: highest score first for the warmed cache
                .sorted(Comparator.comparingDouble(EventScore::getScore).reversed())
                .collect(Collectors.toList());
    }

    private double computeScore(Event ev, OffsetDateTime now, boolean addRecencyBoost) {
        double popularity = ev.getPopularityScore() != null ? ev.getPopularityScore() : 0d;
        double recencyBoost = 0d;
        if (addRecencyBoost && ev.getTime() != null) {
            // earlier events (sooner) get a slight boost for recommendations
            long hoursUntil = java.time.Duration.between(now, ev.getTime()).toHours();
            recencyBoost = Math.max(0, 48 - Math.abs(hoursUntil)) * 0.5; // capped modest boost
        }
        return popularity + recencyBoost;
    }
}
