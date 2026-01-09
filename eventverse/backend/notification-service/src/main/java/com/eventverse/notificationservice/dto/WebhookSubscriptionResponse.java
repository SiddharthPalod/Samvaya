package com.eventverse.notificationservice.dto;

import java.time.Instant;

public class WebhookSubscriptionResponse {
    private Long id;
    private String partnerId;
    private String url;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public WebhookSubscriptionResponse() {
    }

    public WebhookSubscriptionResponse(Long id, String partnerId, String url, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.url = url;
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
