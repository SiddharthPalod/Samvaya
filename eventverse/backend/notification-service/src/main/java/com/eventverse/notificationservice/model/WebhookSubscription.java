package com.eventverse.notificationservice.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "webhook_subscription")
public class WebhookSubscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String partnerId;
    private String url;
    private String secret;
    private boolean active = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    // constructors, getters, setters

    public WebhookSubscription() {
        // Default constructor required by JPA
    }

    public WebhookSubscription(Long id) {
        this.id = id;
    }

    public WebhookSubscription(Long id, String partnerId, String url, String secret, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.url = url;
        this.secret = secret;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

