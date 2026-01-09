package com.eventverse.eventservice.feed.dto;

import com.eventverse.eventservice.feed.model.FeedType;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;


@Data
@AllArgsConstructor
public class FeedResponse {
    private FeedType type;
    private boolean cached;
    private List<EventScore> events;


    public static FeedResponse cached(FeedType type, List<EventScore> events) {
        return new FeedResponse(type, true, events);
    }


    public static FeedResponse db(FeedType type, List<EventScore> events) {
        return new FeedResponse(type, false, events);
    }
}
