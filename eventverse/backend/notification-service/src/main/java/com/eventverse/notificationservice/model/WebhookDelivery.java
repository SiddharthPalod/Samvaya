package com.eventverse.notificationservice.model;
import com.eventverse.notificationservice.converter.JsonbStringType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import java.time.Instant;

@Entity
@Table(name = "webhook_delivery",
        uniqueConstraints = @UniqueConstraint(columnNames = {"subscription_id","domain_event_id"}))
public class WebhookDelivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "subscription_id")
    private Long subscriptionId;
    private String domainEventId;
    private String domainEventType;
    @Type(JsonbStringType.class)
    @Column(columnDefinition = "jsonb")
    private String payload; // store raw JSON
    private int attempt = 0;
    private String status;
    private String lastError;
    private Instant nextAttemptAt;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    // constructors, getters, setters

    public WebhookDelivery() {
        // Default constructor required by JPA
    }

    public WebhookDelivery(Long id) {
        this.id = id;
    }

    public WebhookDelivery(Long id, Long subscriptionId, String domainEventId, String domainEventType, String payload, int attempt, String status, String lastError, Instant nextAttemptAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.domainEventId = domainEventId;
        this.domainEventType = domainEventType;
        this.payload = payload;
        this.attempt = attempt;
        this.status = status;
        this.lastError = lastError;
        this.nextAttemptAt = nextAttemptAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getDomainEventId() {
        return domainEventId;
    }

    public void setDomainEventId(String domainEventId) {
        this.domainEventId = domainEventId;
    }

    public String getDomainEventType() {
        return domainEventType;
    }

    public void setDomainEventType(String domainEventType) {
        this.domainEventType = domainEventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(Instant nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

