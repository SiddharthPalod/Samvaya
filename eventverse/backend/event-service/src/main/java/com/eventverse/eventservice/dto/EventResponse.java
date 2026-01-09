package com.eventverse.eventservice.dto;
import java.time.OffsetDateTime;
import java.math.BigDecimal;


public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String city;
    private OffsetDateTime time;
    private Integer capacity;
    private Integer totalSeats;
    private Integer availableSeats;
    private Long organizerId;
    private String venue;
    private String category;
    private boolean publicEvent;
    private String imageUrl;

    private Long popularityScore;
    private BigDecimal price;


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

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isPublicEvent() { return publicEvent; }
    public void setPublicEvent(boolean publicEvent) { this.publicEvent = publicEvent; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getPopularityScore() { return popularityScore; }
    public void setPopularityScore(Long popularityScore) { this.popularityScore = popularityScore;}

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    }