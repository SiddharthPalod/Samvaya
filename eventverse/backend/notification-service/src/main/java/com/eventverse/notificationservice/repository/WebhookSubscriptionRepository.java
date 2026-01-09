package com.eventverse.notificationservice.repository;
import com.eventverse.notificationservice.model.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {
    List<WebhookSubscription> findByActiveTrue();
    Optional<WebhookSubscription> findByPartnerId(String partnerId);
}
