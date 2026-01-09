package com.eventverse.ticketservice.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "seat_inventory")
public class SeatInventory {
    @Id
    private Long eventId;
    private Integer totalSeats;
    private Integer availableSeats;

    @Version
    private Long version;
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    // getters & setters

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
