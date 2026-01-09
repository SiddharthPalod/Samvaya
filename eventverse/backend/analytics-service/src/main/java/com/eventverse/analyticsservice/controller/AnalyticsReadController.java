package com.eventverse.analyticsservice.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/analytics/realtime")
@RequiredArgsConstructor
public class AnalyticsReadController {
    private final RedisTemplate<String, String> redis;

    @GetMapping("/event/{eventId}")
    public Map<String, Object> eventSnapshot(@PathVariable String eventId) {
        String tickets = redis.opsForValue().get("rt:event:" + eventId + ":tickets");
        String revenue = redis.opsForValue().get("rt:event:" + eventId + ":revenue");
        return Map.of(
                "eventId", eventId,
                "tickets", tickets != null ? Long.parseLong(tickets) : 0L,
                "revenue", revenue != null ? Long.parseLong(revenue) : 0L
        );
    }
}
