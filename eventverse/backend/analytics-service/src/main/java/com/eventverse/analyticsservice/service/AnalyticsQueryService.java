package com.eventverse.analyticsservice.service;

import com.eventverse.analyticsservice.repository.AnalyticsEventRepository;
import com.eventverse.analyticsservice.repository.view.DailyActiveUsersView;
import com.eventverse.analyticsservice.repository.view.DailyRevenueView;
import com.eventverse.analyticsservice.repository.view.TopEventView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {
    private final AnalyticsEventRepository repository;
    private final RestTemplate restTemplate;

    @Value("${event-service.base-url:http://localhost:8082}")
    private String eventServiceBaseUrl;

    public List<DailyRevenueView> dailyRevenue(LocalDate startDate, LocalDate endDate, String eventId) {
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusSeconds(1);
        return repository.aggregateDailyRevenue(start, end, eventId);
    }

    public List<DailyActiveUsersView> dailyActiveUsers(LocalDate startDate, LocalDate endDate, String eventId) {
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).minusSeconds(1);
        return repository.aggregateDailyActiveUsers(start, end, eventId);
    }

    public List<Map<String, Object>> topEvents(int days, int limit) {
        Instant since = Instant.now().minusSeconds((long) days * 24 * 3600);
        List<TopEventView> rows = repository.topEventsSince(since, limit);
        Map<String, String> titles = fetchEventTitles(
                rows.stream().map(TopEventView::getEventId).collect(Collectors.toSet())
        );
        return rows.stream()
                .map(r -> Map.<String, Object>of(
                        "eventId", r.getEventId(),
                        "title", titles.getOrDefault(r.getEventId(), r.getEventId()),
                        "revenue", r.getRevenue(),
                        "tickets", r.getTickets()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, String> fetchEventTitles(Iterable<String> eventIds) {
        Map<String, String> result = new java.util.HashMap<>();
        for (String id : eventIds) {
            try {
                Map<?, ?> resp = restTemplate.getForObject(eventServiceBaseUrl + "/events/" + id, Map.class);
                Object title = resp != null ? resp.get("title") : null;
                result.put(id, title != null ? title.toString() : id);
            } catch (Exception ex) {
                result.put(id, id);
            }
        }
        return result;
    }
}
