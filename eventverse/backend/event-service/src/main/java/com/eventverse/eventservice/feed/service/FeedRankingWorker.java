package com.eventverse.eventservice.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FeedRankingWorker {


    private final RedisTemplate<String, Object> redisTemplate;


    @Async
    public void registerView(String eventId, String city) {
        redisTemplate.opsForZSet()
                .incrementScore("feed:trending:city:" + city, eventId, 1.0);
    }
}
