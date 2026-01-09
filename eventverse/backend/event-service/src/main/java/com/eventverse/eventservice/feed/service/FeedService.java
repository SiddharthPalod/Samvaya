package com.eventverse.eventservice.feed.service;
import com.eventverse.eventservice.feed.dto.EventScore;
import com.eventverse.eventservice.feed.dto.FeedResponse;
import com.eventverse.eventservice.feed.model.FeedType;
import com.eventverse.eventservice.feed.repository.FeedRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRedisRepository redisRepository;
    private final FeedSourceService feedSourceService;
    public FeedResponse getFeed(
            FeedType type,
            String userId,
            String city,
            int page,
            int size
    ) {
        String key = buildKey(type, userId, city);
        List<EventScore> cached = redisRepository.fetch(key, page, size);
        if (!cached.isEmpty()) {
            return FeedResponse.cached(type, cached);
        }
        List<EventScore> fresh = feedSourceService.fetchFromDb(type, userId, city);
        redisRepository.warm(key, fresh);
        return FeedResponse.db(type, fresh);
    }

    private String buildKey(FeedType type, String userId, String city) {
        return switch (type) {
            case TRENDING -> "feed:trending:city:" + city;
            case RECOMMENDED -> "feed:recommended:user:" + userId;
            case UPCOMING -> "feed:upcoming";
        };
    }
}
