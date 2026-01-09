package com.eventverse.analyticsservice.service;
import com.eventverse.analyticsservice.dto.AnalyticsEventRequest;
import com.eventverse.analyticsservice.dto.TicketEvent;
import com.eventverse.analyticsservice.entity.AnalyticsEvent;
import com.eventverse.analyticsservice.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final AnalyticsEventRepository repository;
    private final RedisTemplate<String, String> redis;

    public void ingest(AnalyticsEventRequest req) {
        AnalyticsEvent event = new AnalyticsEvent(
                null,
                req.getEventType(),
                req.getEntityId(),
                req.getUserId(),
                req.getValue(),
                req.getTimestamp(),
                req.getCity()
        );
        repository.save(event);
        long ticketDelta = 0L;
        if ("TICKET_PURCHASED".equals(req.getEventType())) {
            ticketDelta = 1L;
        } else if ("TICKET_CANCELLED".equals(req.getEventType())) {
            ticketDelta = -1L;
        }
        if (ticketDelta != 0L) {
            String keySuffix = String.valueOf(req.getEntityId());
            redis.opsForValue().increment("rt:event:" + keySuffix + ":tickets", ticketDelta);
            redis.opsForValue().increment("rt:event:" + keySuffix + ":revenue", req.getValue().longValue());
        }
    }

    /**
     * Kafka entry-point: map ticket events to analytics events.
     */
    public void ingestTicketEvent(TicketEvent ticketEvent) {
        if (ticketEvent == null || ticketEvent.getEventIdRef() == null) {
            return;
        }
        String eventType = ticketEvent.getType() == TicketEvent.Type.TICKET_CANCELLED
                ? "TICKET_CANCELLED"
                : "TICKET_PURCHASED";

        double value = ticketEvent.getAmount() == null ? 0D : ticketEvent.getAmount();
        // cancellations subtract value to keep aggregates accurate
        if (ticketEvent.getType() == TicketEvent.Type.TICKET_CANCELLED) {
            value = -value;
        }

        AnalyticsEventRequest req = new AnalyticsEventRequest();
        req.setEventType(eventType);
        try {
            // eventIdRef is serialized as a string, but represents a numeric eventId.
            req.setEntityId(Long.parseLong(ticketEvent.getEventIdRef()));
        } catch (NumberFormatException ex) {
            // If we for some reason can't parse the event id, skip ingest for this record
            return;
        }
        req.setUserId(ticketEvent.getUserId());
        req.setValue(value);
        req.setTimestamp(ticketEvent.getOccurredAt() != null ? ticketEvent.getOccurredAt().toEpochMilli() : Instant.now().toEpochMilli());
        req.setCity(null);
        ingest(req);
    }
}
