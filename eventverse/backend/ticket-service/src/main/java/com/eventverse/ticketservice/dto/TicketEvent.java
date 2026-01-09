package com.eventverse.ticketservice.dto;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

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

    public TicketEvent() { }

    public TicketEvent(String eventId, Type type, String ticketId, String eventIdRef, String userId, Long amount, Instant occurredAt, Map<String, Object> payload) {
        this.eventId = eventId;
        this.type = type;
        this.ticketId = ticketId;
        this.eventIdRef = eventIdRef;
        this.userId = userId;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }

    // getters/setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getEventIdRef() { return eventIdRef; }
    public void setEventIdRef(String eventIdRef) { this.eventIdRef = eventIdRef; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    @Override
    public boolean equals(Object o) { /* kept short */
        if (this == o) return true;
        if (!(o instanceof TicketEvent)) return false;
        TicketEvent that = (TicketEvent) o;
        return Objects.equals(eventId, that.eventId);
    }
    @Override
    public int hashCode() { return Objects.hash(eventId); }

    @Override
    public String toString() {
        return "TicketEvent{" + "eventId='" + eventId + '\'' + ", type=" + type + ", ticketId='" + ticketId + '\'' + '}';
    }
}
