package com.eventverse.notificationservice.controller;
import com.eventverse.notificationservice.model.WebhookSubscription;
import com.eventverse.notificationservice.model.WebhookDelivery;
import com.eventverse.notificationservice.repository.WebhookSubscriptionRepository;
import com.eventverse.notificationservice.repository.WebhookDeliveryRepository;
import com.eventverse.notificationservice.dto.RegisterRequest;
import com.eventverse.notificationservice.dto.WebhookSubscriptionResponse;
import com.eventverse.notificationservice.service.UserWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    private final WebhookSubscriptionRepository repo;
    private final WebhookDeliveryRepository deliveryRepo;
    private final ObjectMapper objectMapper;
    private final UserWebhookService userWebhookService;
    
    public WebhookController(WebhookSubscriptionRepository repo, WebhookDeliveryRepository deliveryRepo, 
                            ObjectMapper objectMapper, UserWebhookService userWebhookService){
        this.repo = repo;
        this.deliveryRepo = deliveryRepo;
        this.objectMapper = objectMapper;
        this.userWebhookService = userWebhookService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody RegisterRequest req){
        String secret = UUID.randomUUID().toString();
        WebhookSubscription s = new WebhookSubscription();
        s.setPartnerId(req.getPartnerId());
        s.setUrl(req.getUrl());
        s.setSecret(secret);
        repo.save(s);
        // Return secret once (partners must store)
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", s.getId(), "secret", secret));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerWithPath(@RequestBody RegisterRequest req){
        return register(req);
    }

    @GetMapping("/list")
    public ResponseEntity<List<WebhookSubscriptionResponse>> list(){
        List<WebhookSubscriptionResponse> responses = repo.findAll().stream()
                .map(s -> new WebhookSubscriptionResponse(
                        s.getId(),
                        s.getPartnerId(),
                        s.getUrl(),
                        s.isActive(),
                        s.getCreatedAt(),
                        s.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/deliveries")
    public ResponseEntity<List<WebhookDelivery>> getDeliveries(
            @RequestParam(required = false) Long subscriptionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<WebhookDelivery> deliveries;
        if (subscriptionId != null) {
            deliveries = deliveryRepo.findAll().stream()
                    .filter(d -> d.getSubscriptionId().equals(subscriptionId))
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(size)
                    .collect(Collectors.toList());
        } else {
            deliveries = deliveryRepo.findAll(pageable).getContent();
        }
        return ResponseEntity.ok(deliveries);
    }

    @GetMapping("/deliveries/{subscriptionId}")
    public ResponseEntity<List<WebhookDelivery>> getDeliveriesBySubscription(
            @PathVariable Long subscriptionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<WebhookDelivery> deliveries = deliveryRepo.findAll().stream()
                .filter(d -> d.getSubscriptionId().equals(subscriptionId))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
        return ResponseEntity.ok(deliveries);
    }

    @PostMapping("/user/{userId}/ensure")
    public ResponseEntity<Map<String, Object>> ensureUserWebhook(@PathVariable String userId) {
        WebhookSubscription subscription = userWebhookService.ensureUserWebhookSubscription(userId);
        return ResponseEntity.ok(Map.of(
            "subscriptionId", subscription.getId(),
            "partnerId", subscription.getPartnerId(),
            "created", subscription.getCreatedAt().equals(subscription.getUpdatedAt())
        ));
    }

    @GetMapping("/notifications/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            // Ensure user has a webhook subscription (creates if doesn't exist)
            Long subscriptionId = userWebhookService.getUserWebhookSubscriptionId(userId);
            
            if (subscriptionId == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            // Get deliveries for this user's subscription
            List<WebhookDelivery> deliveries = deliveryRepo.findAll().stream()
                    .filter(d -> d.getSubscriptionId() != null && d.getSubscriptionId().equals(subscriptionId))
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .skip(page * size)
                    .limit(size)
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> notifications = new ArrayList<>();
            for (WebhookDelivery delivery : deliveries) {
                try {
                    Map<String, Object> eventData = objectMapper.readValue(delivery.getPayload(), Map.class);
                    
                    // The payload contains the serialized TicketEvent object
                    // Extract fields from the event structure
                    String eventIdRef = null;
                    if (eventData.containsKey("eventIdRef")) {
                        eventIdRef = String.valueOf(eventData.get("eventIdRef"));
                    }
                    
                    String ticketId = null;
                    if (eventData.containsKey("ticketId")) {
                        ticketId = String.valueOf(eventData.get("ticketId"));
                    }
                    
                    String eventUserId = null;
                    if (eventData.containsKey("userId")) {
                        eventUserId = String.valueOf(eventData.get("userId"));
                    }
                    
                    Long amount = null;
                    if (eventData.containsKey("amount")) {
                        Object amountObj = eventData.get("amount");
                        if (amountObj instanceof Number) {
                            amount = ((Number) amountObj).longValue();
                        } else if (amountObj instanceof String) {
                            try {
                                amount = Long.parseLong((String) amountObj);
                            } catch (NumberFormatException e) {
                                // Skip if can't parse
                            }
                        }
                    }
                    
                    // Also check if there's a nested payload field (from the original payload map)
                    Map<String, Object> nestedPayload = null;
                    if (eventData.containsKey("payload") && eventData.get("payload") instanceof Map) {
                        nestedPayload = (Map<String, Object>) eventData.get("payload");
                        // Use nested payload values if eventIdRef is not at top level
                        if (eventIdRef == null && nestedPayload.containsKey("eventId")) {
                            eventIdRef = String.valueOf(nestedPayload.get("eventId"));
                        }
                        if (ticketId == null && nestedPayload.containsKey("ticketId")) {
                            ticketId = String.valueOf(nestedPayload.get("ticketId"));
                        }
                        if (eventUserId == null && nestedPayload.containsKey("userId")) {
                            eventUserId = String.valueOf(nestedPayload.get("userId"));
                        }
                    }
                    
                    // Build the notification payload with the expected structure
                    Map<String, Object> notificationPayload = new java.util.HashMap<>();
                    if (ticketId != null) {
                        notificationPayload.put("ticketId", ticketId);
                    }
                    if (eventIdRef != null) {
                        notificationPayload.put("eventIdRef", eventIdRef);
                    }
                    if (eventUserId != null) {
                        notificationPayload.put("userId", eventUserId);
                    }
                    // Only include amount if it's not null and not zero
                    if (amount != null && amount != 0) {
                        notificationPayload.put("amount", amount);
                    }
                    if (eventData.containsKey("type")) {
                        notificationPayload.put("type", eventData.get("type"));
                    }
                    if (eventData.containsKey("occurredAt")) {
                        notificationPayload.put("occurredAt", eventData.get("occurredAt"));
                    }
                    
                    Map<String, Object> notification = Map.of(
                        "id", delivery.getId(),
                        "type", delivery.getDomainEventType(),
                        "eventId", delivery.getDomainEventId(),
                        "status", delivery.getStatus(),
                        "createdAt", delivery.getCreatedAt().toString(),
                        "payload", notificationPayload
                    );
                    notifications.add(notification);
                } catch (Exception e) {
                    // Skip invalid payloads, but log for debugging
                    System.err.println("Error parsing delivery payload for delivery " + delivery.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            // Return empty list on error rather than failing
            System.err.println("Error fetching notifications for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}