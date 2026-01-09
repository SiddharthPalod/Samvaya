package com.eventverse.notificationservice.repository;
import com.eventverse.notificationservice.model.WebhookDelivery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    @Query("select d from WebhookDelivery d where d.status in ('PENDING','RETRYING') and d.nextAttemptAt <= :now")
    List<WebhookDelivery> findDueDeliveries(@Param("now") Instant now, Pageable p);
}