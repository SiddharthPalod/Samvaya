package com.eventverse.notificationservice.service;

import com.eventverse.notificationservice.model.WebhookSubscription;
import com.eventverse.notificationservice.repository.WebhookSubscriptionRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UserWebhookService {
    private final WebhookSubscriptionRepository subscriptionRepo;

    public UserWebhookService(WebhookSubscriptionRepository subscriptionRepo) {
        this.subscriptionRepo = subscriptionRepo;
    }

    /**
     * Ensures a user has a webhook subscription for notifications.
     * Creates one if it doesn't exist, returns existing if it does.
     */
    public WebhookSubscription ensureUserWebhookSubscription(String userId) {
        String partnerId = "user-" + userId;
        
        return subscriptionRepo.findByPartnerId(partnerId)
            .orElseGet(() -> {
                WebhookSubscription subscription = new WebhookSubscription();
                subscription.setPartnerId(partnerId);
                subscription.setUrl("internal://user-notifications/" + userId);
                subscription.setSecret(UUID.randomUUID().toString());
                subscription.setActive(true);
                return subscriptionRepo.save(subscription);
            });
    }

    /**
     * Gets the user's webhook subscription ID, creating it if needed.
     */
    public Long getUserWebhookSubscriptionId(String userId) {
        return ensureUserWebhookSubscription(userId).getId();
    }
}
