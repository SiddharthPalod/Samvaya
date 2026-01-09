package com.eventverse.analyticsservice.dto;
import lombok.Data;


@Data
public class AnalyticsEventRequest {
    private String eventType;
    /**
     * ID of the related domain entity (currently eventId).
     * Stored as Long to match the BIGINT column in the database.
     */
    private Long entityId;
    private String userId;
    private Double value;
    private Long timestamp;
    private String city;
}