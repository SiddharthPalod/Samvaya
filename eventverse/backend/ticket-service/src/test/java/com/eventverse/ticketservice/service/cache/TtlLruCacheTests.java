package com.eventverse.ticketservice.service.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TtlLruCacheTests {

    @Test
    void evictsLeastRecentlyUsedWhenCapacityExceeded() {
        TtlLruCache<String, String> cache = new TtlLruCache<>(2, Duration.ofMinutes(5));

        cache.put("A", "alpha");
        cache.put("B", "bravo");
        // touch A so B becomes LRU
        assertEquals("alpha", cache.get("A"));

        cache.put("C", "charlie");

        assertNull(cache.get("B"), "B should be evicted as the LRU entry");
        assertEquals("alpha", cache.get("A"));
        assertEquals("charlie", cache.get("C"));
    }

    @Test
    void evictsEntriesAfterTtl() throws InterruptedException {
        TtlLruCache<String, String> cache = new TtlLruCache<>(5, Duration.ofMillis(50));

        cache.put("temp", "value");
        Thread.sleep(70);

        assertNull(cache.get("temp"), "Entry should be evicted after TTL expires");
        assertEquals(0, cache.size(), "Cache should clean up expired entries");
    }
}


