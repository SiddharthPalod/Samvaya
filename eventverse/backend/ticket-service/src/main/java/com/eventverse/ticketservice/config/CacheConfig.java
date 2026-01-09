package com.eventverse.ticketservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final String EVENT_PRICING_CACHE = "eventPricing";

    /**
     * Runtime cache manager backed by Redis.
     * Uses no TTL to favor speed (server eviction policy handles memory/LRU).
     */
    @Bean
    @Profile("!test")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ZERO) // prefer speed; eviction handled by Redis policy (e.g., allkeys-lru)
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .initialCacheNames(java.util.Set.of(EVENT_PRICING_CACHE))
                .build();
    }

    /**
     * Lightweight in-memory cache manager for tests to avoid external Redis.
     */
    @Bean
    @Profile("test")
    public CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager(EVENT_PRICING_CACHE);
    }
}


