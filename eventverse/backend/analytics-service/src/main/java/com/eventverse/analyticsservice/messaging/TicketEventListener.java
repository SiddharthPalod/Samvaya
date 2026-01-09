package com.eventverse.analyticsservice.messaging;

import com.eventverse.analyticsservice.dto.TicketEvent;
import com.eventverse.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketEventListener {
    private static final Logger log = LoggerFactory.getLogger(TicketEventListener.class);
    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "ticket-events", groupId = "analytics-service")
    public void onMessage(TicketEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received ticket event from Kafka: type={}, eventIdRef={}, ticketId={}, key={}",
                event.getType(), event.getEventIdRef(), event.getTicketId(), key);
        analyticsService.ingestTicketEvent(event);
    }
}
