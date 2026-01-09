package com.eventverse.eventservice.feed.controller;

import com.eventverse.eventservice.feed.dto.FeedResponse;
import com.eventverse.eventservice.feed.model.FeedType;
import com.eventverse.eventservice.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/events/feed")
@RequiredArgsConstructor
public class FeedController {


    private final FeedService feedService;


    @GetMapping
    public FeedResponse getFeed(
            @RequestParam FeedType type,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader("X-User-Id") String userId
    ) {
        return feedService.getFeed(type, userId, city, page, size);
    }
}
