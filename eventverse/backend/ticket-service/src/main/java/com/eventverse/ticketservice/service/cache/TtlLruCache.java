package com.eventverse.ticketservice.service.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple in-memory cache that combines TTL expiration with LRU eviction.
 * Thread-safe via coarse-grained synchronization because the use cases are light.
 */
public class TtlLruCache<K, V> {

    private final long ttlMillis;
    private final int maxSize;
    private final Map<K, CacheEntry<V>> store;

    public TtlLruCache(int maxSize, Duration ttl) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        Objects.requireNonNull(ttl, "ttl is required");
        this.maxSize = maxSize;
        this.ttlMillis = ttl.toMillis();
        this.store = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return size() > TtlLruCache.this.maxSize;
            }
        };
    }

    public synchronized void put(K key, V value) {
        store.put(key, new CacheEntry<>(value, Instant.now().plusMillis(ttlMillis)));
    }

    public synchronized V get(K key) {
        CacheEntry<V> entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            store.remove(key);
            return null;
        }
        return entry.value();
    }

    public synchronized int size() {
        evictExpired();
        return store.size();
    }

    private void evictExpired() {
        store.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    private record CacheEntry<V>(V value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}


