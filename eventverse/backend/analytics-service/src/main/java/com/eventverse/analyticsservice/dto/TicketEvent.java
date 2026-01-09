package com.eventverse.analyticsservice.dto;

import java.time.Instant;
import java.util.Map;

public class TicketEvent {
    public enum Type { TICKET_CONFIRMED, TICKET_CANCELLED }

    private String eventId;
    private Type type;
    private String ticketId;
    private String eventIdRef;
    private String userId;
    private Long amount;
    private Instant occurredAt;
    private Map<String, Object> payload;

    public TicketEvent() {}

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getEventIdRef() {
        return eventIdRef;
    }

    public void setEventIdRef(String eventIdRef) {
        this.eventIdRef = eventIdRef;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
