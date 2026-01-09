package com.eventverse.eventservice.feed.dto;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class EventScore {
    private String eventId;
    private double score;
}
