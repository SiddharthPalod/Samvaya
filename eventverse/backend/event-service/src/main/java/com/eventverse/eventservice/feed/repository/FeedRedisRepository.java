package com.eventverse.eventservice.feed.repository;
import com.eventverse.eventservice.feed.dto.EventScore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Repository
@RequiredArgsConstructor
public class FeedRedisRepository {


    private final RedisTemplate<String, Object> redisTemplate;


    public List<EventScore> fetch(String key, int page, int size) {
        int start = page * size;
        int end = start + size - 1;


        Set<ZSetOperations.TypedTuple<Object>> data =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);


        List<EventScore> result = new ArrayList<>();
        if (data == null) return result;


        for (ZSetOperations.TypedTuple<Object> tuple : data) {
            result.add(new EventScore(
                    tuple.getValue().toString(),
                    tuple.getScore()
            ));
        }
        return result;
    }


    public void warm(String key, List<EventScore> scores) {
        for (EventScore es : scores) {
            redisTemplate.opsForZSet().add(key, es.getEventId(), es.getScore());
        }
        redisTemplate.expire(key, java.time.Duration.ofMinutes(10));
    }
}
