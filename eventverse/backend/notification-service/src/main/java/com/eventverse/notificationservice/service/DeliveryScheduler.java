package com.eventverse.notificationservice.service;
import com.eventverse.notificationservice.model.WebhookDelivery;
import com.eventverse.notificationservice.repository.WebhookDeliveryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

@Component
public class DeliveryScheduler {
    private final WebhookDeliveryRepository deliveryRepo;
    private final DeliveryService deliveryService;

    public DeliveryScheduler(WebhookDeliveryRepository deliveryRepo, DeliveryService deliveryService) {
        this.deliveryRepo = deliveryRepo; this.deliveryService = deliveryService;
    }

    @Scheduled(fixedDelayString = "${notification.poll-ms:3000}")
    public void pollAndDeliver() {
        List<WebhookDelivery> due = deliveryRepo.findDueDeliveries(Instant.now(), PageRequest.of(0, 50));
        for (WebhookDelivery d : due) {
            try { deliveryService.attemptDelivery(d); } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
