package com.eventverse.eventservice.dto;
import java.time.OffsetDateTime;

public class EventFilterRequest {

    private String city;
    private Long organizerId;
    private OffsetDateTime fromTime;
    private OffsetDateTime toTime;
    private String sort; // POPULAR or TIME_ASC
    private Integer page = 0;
    private Integer size = 20;

    // getters and setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }

    public OffsetDateTime getFromTime() { return fromTime; }
    public void setFromTime(OffsetDateTime fromTime) { this.fromTime = fromTime; }

    public OffsetDateTime getToTime() { return toTime; }
    public void setToTime(OffsetDateTime toTime) { this.toTime = toTime; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}
