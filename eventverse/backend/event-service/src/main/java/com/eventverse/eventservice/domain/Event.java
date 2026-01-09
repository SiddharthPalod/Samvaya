package com.eventverse.eventservice.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_city_time", columnList = "city,event_time"),
        @Index(name = "idx_events_organizer_id", columnList = "organizer_id")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 64)
    private String city;

    @Column(name = "event_time", nullable = false)
    private OffsetDateTime time;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @Column(length = 255)
    private String venue;

    @Column(length = 64)
    private String category;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "is_public", nullable = false)
    private boolean publicEvent = true;

    @Column(name = "price", precision = 12, scale = 2, nullable = false, columnDefinition = "numeric(12,2) default 0")
    private BigDecimal price = BigDecimal.ZERO;

    @Version
    private Long version;

    @Column(name = "popularity_score", nullable = false)
    private Long popularityScore = 0L;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public OffsetDateTime getTime() { return time; }
    public void setTime(OffsetDateTime time) { this.time = time; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isPublicEvent() { return publicEvent; }
    public void setPublicEvent(boolean publicEvent) { this.publicEvent = publicEvent; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Long getPopularityScore() { return popularityScore;}

    public void setPopularityScore(Long popularityScore) { this.popularityScore = popularityScore;}
}
