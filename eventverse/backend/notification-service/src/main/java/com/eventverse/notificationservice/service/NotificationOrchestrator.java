package com.eventverse.notificationservice.service;
import com.eventverse.notificationservice.dto.TicketEvent;
import com.eventverse.notificationservice.model.WebhookDelivery;
import com.eventverse.notificationservice.model.WebhookSubscription;
import com.eventverse.notificationservice.repository.WebhookDeliveryRepository;
import com.eventverse.notificationservice.repository.WebhookSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
@Service
public class NotificationOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(NotificationOrchestrator.class);
    private final WebhookSubscriptionRepository subscriptionRepo;
    private final WebhookDeliveryRepository deliveryRepo;
    private final ObjectMapper mapper;
    private final UserWebhookService userWebhookService;

    public NotificationOrchestrator(WebhookSubscriptionRepository subscriptionRepo, WebhookDeliveryRepository deliveryRepo, 
                                   ObjectMapper mapper, UserWebhookService userWebhookService) {
        this.subscriptionRepo = subscriptionRepo; 
        this.deliveryRepo = deliveryRepo; 
        this.mapper = mapper;
        this.userWebhookService = userWebhookService;
    }

    public void handleTicketEvent(TicketEvent event) {
        try {
            logger.info("Handling ticket event: type={}, userId={}, ticketId={}, eventId={}", 
                event.getType(), event.getUserId(), event.getTicketId(), event.getEventId());

            // Ensure the user has a webhook subscription for notifications
            WebhookSubscription userSubscription = userWebhookService.ensureUserWebhookSubscription(event.getUserId());
            logger.info("User subscription ensured: subscriptionId={}, partnerId={}", 
                userSubscription.getId(), userSubscription.getPartnerId());

            String payloadJson;
            try { 
                payloadJson = mapper.writeValueAsString(event); 
                logger.debug("Serialized event payload: {}", payloadJson);
            } catch(Exception e) { 
                logger.error("Failed to serialize event payload", e);
                payloadJson = "{}"; 
            }

            // Create webhook deliveries for ALL active webhook subscriptions (including user subscriptions)
            List<WebhookSubscription> subs = subscriptionRepo.findByActiveTrue();
            logger.info("Found {} active subscriptions, creating deliveries", subs.size());
            
            for (WebhookSubscription sub : subs) {
                try {
                    WebhookDelivery d = new WebhookDelivery();
                    d.setSubscriptionId(sub.getId());
                    d.setDomainEventId(event.getEventId());
                    d.setDomainEventType(event.getType().name());
                    d.setPayload(payloadJson);
                    
                    // User subscriptions (internal://) are marked as SUCCESS immediately
                    // External webhooks are PENDING and will be delivered
                    if (sub.getUrl().startsWith("internal://")) {
                        d.setStatus("SUCCESS");
                        d.setNextAttemptAt(null);
                        logger.debug("Created SUCCESS delivery for user subscription: subscriptionId={}", sub.getId());
                    } else {
                        d.setStatus("PENDING");
                        d.setNextAttemptAt(Instant.now());
                        logger.debug("Created PENDING delivery for external subscription: subscriptionId={}", sub.getId());
                    }
                    deliveryRepo.save(d);
                    logger.info("Saved delivery: id={}, subscriptionId={}, eventType={}", 
                        d.getId(), d.getSubscriptionId(), d.getDomainEventType());
                } catch (Exception e) {
                    logger.error("Failed to create delivery for subscription: subscriptionId={}", sub.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling ticket event: eventId={}, userId={}", event.getEventId(), event.getUserId(), e);
        }
    }
}
