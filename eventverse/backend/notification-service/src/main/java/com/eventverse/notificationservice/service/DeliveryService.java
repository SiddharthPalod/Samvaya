package com.eventverse.notificationservice.service;
import com.eventverse.notificationservice.model.WebhookDelivery;
import com.eventverse.notificationservice.model.WebhookSubscription;
import com.eventverse.notificationservice.repository.WebhookDeliveryRepository;
import com.eventverse.notificationservice.repository.WebhookSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
@Service
public class DeliveryService {
    private final WebhookDeliveryRepository deliveryRepo;
    private final WebhookSubscriptionRepository subscriptionRepo;
    private final WebClient webClient = WebClient.create();
    private final int MAX_ATTEMPTS = 5;
    private final long BASE_SECONDS = 2;
    private final long CAP_SECONDS = 3600;

    public DeliveryService(WebhookDeliveryRepository deliveryRepo, WebhookSubscriptionRepository subscriptionRepo) {
        this.deliveryRepo = deliveryRepo; this.subscriptionRepo = subscriptionRepo;
    }

    public void attemptDelivery(WebhookDelivery delivery) {
        Optional<WebhookSubscription> subOpt = subscriptionRepo.findById(delivery.getSubscriptionId());
        if (subOpt.isEmpty()) { markFailed(delivery, "subscription missing"); return; }
        WebhookSubscription sub = subOpt.get();

        delivery.setAttempt(delivery.getAttempt() + 1);
        deliveryRepo.save(delivery);

        String body = delivery.getPayload();
        String signature;
        try {
            signature = HmacUtil.hmacSha256Hex(sub.getSecret(), body);
        } catch (Exception e) {
            markFailed(delivery, "hmac-failure:" + e.getMessage());
            return;
        }

        try {
            int status = webClient.post()
                    .uri(sub.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Event-Type", delivery.getDomainEventType())
                    .header("Idempotency-Key", delivery.getDomainEventId())
                    .header("X-Event-Signature", "sha256=" + signature)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .map(resp -> resp.getStatusCode().value())
                    .onErrorReturn(500)
                    .block(Duration.ofSeconds(5));

            if (status >= 200 && status < 300) {
                delivery.setStatus("SUCCESS");
                delivery.setNextAttemptAt(null);
                deliveryRepo.save(delivery);
                return;
            } else {
                // failure: schedule retry
                handleRetry(delivery, "HTTP " + status);
            }
        } catch (Exception e) {
            handleRetry(delivery, e.getMessage());
        }
    }

    private void handleRetry(WebhookDelivery delivery, String error) {
        delivery.setLastError(error);
        if (delivery.getAttempt() >= MAX_ATTEMPTS) {
            delivery.setStatus("FAILED");
            deliveryRepo.save(delivery);
            // push to DLQ or emit metric / alert
        } else {
            delivery.setStatus("RETRYING");
            long delay = Math.min(CAP_SECONDS, BASE_SECONDS * (1L << (delivery.getAttempt() - 1)));
            delivery.setNextAttemptAt(Instant.now().plusSeconds(delay));
            deliveryRepo.save(delivery);
        }
    }

    private void markFailed(WebhookDelivery d, String err) {
        d.setStatus("FAILED"); d.setLastError(err); deliveryRepo.save(d);
    }
}
