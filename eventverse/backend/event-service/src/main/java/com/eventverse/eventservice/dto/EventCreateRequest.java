package com.eventverse.eventservice.dto;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

public class EventCreateRequest {
    @NotBlank
    @Size(max = 140)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotBlank
    private String city;

    @NotNull
    private OffsetDateTime time;

    @NotNull
    @Min(1)
    private Integer capacity;

    // will be overridden from JWT header
    private Long organizerId;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal price = BigDecimal.ZERO;

    private String venue;
    private String category;
    private Boolean publicEvent = true;
    private String imageUrl;

    // getters and setters
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

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getPublicEvent() { return publicEvent; }
    public void setPublicEvent(Boolean publicEvent) { this.publicEvent = publicEvent; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
