package com.eventverse.ticketservice.messaging;

import com.eventverse.ticketservice.dto.TicketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class TicketEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(TicketEventProducer.class);
    private static final String TOPIC = "ticket-events";
    
    private final KafkaTemplate<String, TicketEvent> kafkaTemplate;

    public TicketEventProducer(KafkaTemplate<String, TicketEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTicketConfirmed(String ticketId, String eventId, Long userId, Long amount, Map<String, Object> payload) {
        TicketEvent event = new TicketEvent(
            UUID.randomUUID().toString(),
            TicketEvent.Type.TICKET_CONFIRMED,
            ticketId,
            eventId,
            userId.toString(),
            amount,
            Instant.now(),
            payload
        );
        sendEvent(event, ticketId);
    }

    public void publishTicketCancelled(String ticketId, String eventId, Long userId, Long amount, Map<String, Object> payload) {
        TicketEvent event = new TicketEvent(
            UUID.randomUUID().toString(),
            TicketEvent.Type.TICKET_CANCELLED,
            ticketId,
            eventId,
            userId.toString(),
            amount,
            Instant.now(),
            payload
        );
        sendEvent(event, ticketId);
    }

    private void sendEvent(TicketEvent event, String key) {
        try {
            logger.info("Attempting to send ticket event [{}] to topic [{}] with key [{}]", 
                event.getEventId(), TOPIC, key);
            
            if (kafkaTemplate == null) {
                logger.error("KafkaTemplate is null! Cannot send event [{}]", event.getEventId());
                return;
            }
            
            CompletableFuture<SendResult<String, TicketEvent>> future = kafkaTemplate.send(TOPIC, key, event);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Successfully sent ticket event [{}] with offset=[{}] to partition=[{}]", 
                        event.getEventId(), 
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
                } else {
                    logger.error("Failed to send ticket event [{}] due to: {}", event.getEventId(), 
                        ex.getMessage(), ex);
                }
            });
            
            // Also wait a bit to catch immediate errors (but don't block too long)
            try {
                future.get(5, TimeUnit.SECONDS); // Wait up to 5 seconds for completion
            } catch (TimeoutException e) {
                logger.warn("Ticket event [{}] send is taking longer than expected, but continuing...", event.getEventId());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error waiting for ticket event [{}] send completion: {}", 
                    event.getEventId(), e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error("Exception while sending ticket event [{}]: {}", event.getEventId(), e.getMessage(), e);
        }
    }
}
