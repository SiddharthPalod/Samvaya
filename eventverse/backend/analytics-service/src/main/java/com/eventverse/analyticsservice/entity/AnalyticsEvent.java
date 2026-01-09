package com.eventverse.analyticsservice.entity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "analytics_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of analytics event, e.g. TICKET_PURCHASED, TICKET_CANCELLED.
     */
    private String eventType;

    /**
     * ID of the related domain entity (currently eventId).
     *
     * NOTE: This is stored as BIGINT in the database, so we use Long here
     * to avoid type-mismatch errors when persisting.
     */
    private Long entityId;

    /**
     * ID of the user associated with the event.
     * Stored as string to allow future non-numeric identifiers.
     */
    private String userId;

    /**
     * Numeric value to aggregate on (e.g. revenue amount, count).
     */
    private Double value;

    /**
     * Event timestamp in epoch millis.
     */
    private Long timestamp;

    /**
     * Optional city for geo-based analytics.
     */
    private String city;
}
