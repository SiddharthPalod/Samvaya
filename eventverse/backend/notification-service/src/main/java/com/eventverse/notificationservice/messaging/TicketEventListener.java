package com.eventverse.notificationservice.messaging;
import com.eventverse.notificationservice.dto.TicketEvent;
import com.eventverse.notificationservice.service.NotificationOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.KafkaHeaders;

@Component
public class TicketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TicketEventListener.class);
    private final NotificationOrchestrator orchestrator;
    
    public TicketEventListener(NotificationOrchestrator orchestrator) { 
        this.orchestrator = orchestrator;
        logger.info("TicketEventListener initialized - listening to topic 'ticket-events'");
    }

    @KafkaListener(topics = "ticket-events", groupId = "notification-service")
    public void onMessage(TicketEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            logger.info("Received ticket event from Kafka: type={}, userId={}, ticketId={}, eventId={}, key={}", 
                event.getType(), event.getUserId(), event.getTicketId(), event.getEventId(), key);
            orchestrator.handleTicketEvent(event);
        } catch (Exception e) {
            logger.error("Error processing ticket event: eventId={}, userId={}", 
                event != null ? event.getEventId() : "null", 
                event != null ? event.getUserId() : "null", e);
        }
    }
}

