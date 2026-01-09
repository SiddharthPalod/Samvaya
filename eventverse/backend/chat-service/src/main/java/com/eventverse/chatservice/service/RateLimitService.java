package com.eventverse.chatservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Rate limiting service for chat messages.
 * Currently commented out in application.properties - can be enabled for testing.
 * 
 * Reference: api-gateway RateLimiterConfig for similar implementation.
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${chat.rate-limit.enabled:false}")
    private boolean enabled;

    @Value("${chat.rate-limit.messages-per-minute:30}")
    private int messagesPerMinute;

    @Value("${chat.rate-limit.burst-capacity:10}")
    private int burstCapacity;

    /**
     * Check if user can send a message (rate limit check).
     * @param userId User ID
     * @return true if allowed, false if rate limited
     */
    public boolean isAllowed(String userId) {
        // Rate limiting is disabled by default
        // Uncomment the code below to enable rate limiting
        /*
        if (!enabled) {
            return true;
        }

        String key = "rate-limit:chat:" + userId;
        String countKey = key + ":count";
        String windowKey = key + ":window";

        // Get current count
        Long currentCount = redisTemplate.opsForValue().increment(countKey);
        if (currentCount == null || currentCount == 1) {
            // First message in window, set expiration
            redisTemplate.expire(countKey, 1, TimeUnit.MINUTES);
        }

        // Check if over limit
        if (currentCount != null && currentCount > messagesPerMinute) {
            return false;
        }

        // Burst capacity check (sliding window)
        Long burstCount = redisTemplate.opsForValue().increment(windowKey);
        if (burstCount == null || burstCount == 1) {
            redisTemplate.expire(windowKey, 10, TimeUnit.SECONDS);
        }

        if (burstCount != null && burstCount > burstCapacity) {
            return false;
        }
        */

        return true; // Always allow when disabled
    }

    /**
     * Reset rate limit for a user (for testing).
     */
    public void reset(String userId) {
        String key = "rate-limit:chat:" + userId;
        redisTemplate.delete(key + ":count");
        redisTemplate.delete(key + ":window");
    }
}
