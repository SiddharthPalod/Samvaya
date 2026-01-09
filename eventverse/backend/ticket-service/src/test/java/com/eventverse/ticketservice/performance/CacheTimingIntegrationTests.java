package com.eventverse.ticketservice.performance;

import com.eventverse.ticketservice.service.cache.TtlLruCache;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheTimingIntegrationTests {

    @Test
    void ttlLruCacheIntroducesOverheadUnderExpiryHeavyLoad() {
        int capacity = 512;
        int iterations = 900;
        int keySpace = 450;

        PureLruCache<Integer, Integer> lruCache = new PureLruCache<>(capacity);
        TtlLruCache<Integer, Integer> ttlCache = new TtlLruCache<>(capacity, Duration.ofMillis(5));

        long lruDuration = runWorkload(lruCache::put, lruCache::get, lruCache::size, iterations, keySpace, false);
        long ttlDuration = runWorkload(ttlCache::put, ttlCache::get, ttlCache::size, iterations, keySpace, true);

        long padding = Duration.ofMillis(40).toNanos(); // tolerate host variance
        assertTrue(ttlDuration > lruDuration + padding,
                () -> "TTL cache should be slower under expiry churn. lru=" + lruDuration
                        + "ns ttl=" + ttlDuration + "ns");
    }

    private long runWorkload(PutOp<Integer, Integer> put,
                             GetOp<Integer, Integer> get,
                             SizeOp size,
                             int iterations,
                             int keySpace,
                             boolean addExpiryPauses) {
        long start = System.nanoTime();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < iterations; i++) {
            int key = random.nextInt(keySpace);
            put.put(key, i);
            get.get(key);
            size.size();

            if (addExpiryPauses && i % 75 == 0) {
                try {
                    Thread.sleep(2); // allow TTL to expire entries, increases cleanup cost
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return System.nanoTime() - start;
    }

    private interface PutOp<K, V> {
        void put(K key, V value);
    }

    private interface GetOp<K, V> {
        V get(K key);
    }

    private interface SizeOp {
        int size();
    }

    /**
     * Minimal LRU cache using access-order LinkedHashMap for comparison.
     */
    private static class PureLruCache<K, V> {
        private final Map<K, V> delegate;
        private final int maxSize;

        PureLruCache(int maxSize) {
            this.maxSize = maxSize;
            this.delegate = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > PureLruCache.this.maxSize;
                }
            };
        }

        void put(K key, V value) {
            delegate.put(key, value);
        }

        V get(K key) {
            return delegate.get(key);
        }

        int size() {
            return delegate.size();
        }
    }
}


