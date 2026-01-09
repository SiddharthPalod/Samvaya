# Event Service – Sharding & Caching Strategy

## 1. Overview

The **Event Service** is responsible for managing events (creation, querying, details) for the Eventverse platform. As the platform scales (multiple cities, large organizers, high traffic around popular events), we need a clear plan for:

* **Horizontal data partitioning (sharding)**
* **Efficient read performance via caching**
* **Predictable cache invalidation rules**

This document describes the **intended sharding design** (even if not fully implemented yet) and the **current + planned caching strategy**.

---

## 2. Data Model Recap

Core entity: `Event`

Key fields:

* `id` (PK)
* `title`, `description`
* `city`
* `time` (event start time, `OffsetDateTime`)
* `organizerId`
* `capacity`
* `venue`
* `category`
* `publicEvent`
* `version` (optimistic locking)
* `popularityScore` (used for POPULAR sorting)

Indexes:

* `idx_events_city_time (city, time)` — supports city + time range queries
* `idx_events_organizer_id (organizer_id)` — supports organizer-based queries

---

## 3. Access Patterns

Typical read/write patterns:

### Writes

* `POST /events`

  * Create a new event.
  * Writes happen in the event’s **home shard** (see sharding section).
* `PUT /events/{id}`

  * Full update of an event.
* `DELETE /events/{id}`

  * Delete event (or soft-delete in future).

### Reads

* `GET /events/{id}`

  * Read by event ID (primary key).
  * Heavy traffic for popular events.
  * Cached via `eventDetails` cache.
* `GET /events`

  * Filtered list:

    * By city
    * By organizerId
    * By time range
    * Sorted by time or popularity
  * POPULAR queries cached via `popularEvents` cache.

These patterns drive the sharding and caching design.

---

## 4. Sharding Strategy

### 4.1 Goals

* **Scale reads & writes** as number of events and users grow.
* **Localize data** that is typically queried together.
* **Avoid cross-shard joins** from the Event Service (each query should target a single shard where possible).
* Keep the design **simple but extensible** so the monolithic single-DB setup can evolve into a sharded one without big code changes.

### 4.2 Shard Key Choice

Two natural candidates:

1. **By `city` (region-based sharding)**

   * Users usually browse events in a specific city.
   * `GET /events?city=Bangalore` can be routed to the **Bangalore shard**.
   * Good for localized traffic spikes (e.g., a big festival in one city).

2. **By `organizerId`**

   * All events of a large organizer are colocated.
   * Good if large B2B organizers dominate traffic.

For this system, we choose:

> **Primary shard key: `city`**
> Secondary dimension for potential future sub-sharding: `organizerId`.

Reason: the **most common queries** are city + time based.

### 4.3 Logical Shard Layout

Example logical layout (can be adapted):

* **Shard 1**: Cities in Region A (e.g., `Bangalore`, `Hyderabad`)
* **Shard 2**: Cities in Region B (e.g., `Mumbai`, `Pune`)
* **Shard 3**: Cities in Region C (e.g., `Delhi`, `Gurgaon`)
* **Shard N**: “Long tail” or new cities

Each shard can be:

* A **separate Postgres database**, or
* **Separate schemas** in the same Postgres cluster (e.g., `events_shard_1`, `events_shard_2`).

### 4.4 Shard Routing

Introduce a **Shard Routing Layer** (logical component; can be inside event-service or a shared library):

```java
public enum ShardId {
    SHARD_1, SHARD_2, SHARD_3, DEFAULT
}

public interface ShardResolver {
    ShardId resolveByCity(String city);
    ShardId resolveByEventId(Long eventId);
}
```

Example simplistic implementation:

```java
public class CityBasedShardResolver implements ShardResolver {

    @Override
    public ShardId resolveByCity(String city) {
        if (city == null) return ShardId.DEFAULT;

        String normalized = city.trim().toLowerCase();
        switch (normalized) {
            case "bangalore":
            case "hyderabad":
                return ShardId.SHARD_1;
            case "mumbai":
            case "pune":
                return ShardId.SHARD_2;
            case "delhi":
            case "gurgaon":
                return ShardId.SHARD_3;
            default:
                return ShardId.DEFAULT;
        }
    }

    // For eventId-based lookup, you can use ID ranges or metadata tables.
    @Override
    public ShardId resolveByEventId(Long eventId) {
        // Example using ranges: (this is just illustrative)
        if (eventId < 1_000_000L) return ShardId.SHARD_1;
        if (eventId < 2_000_000L) return ShardId.SHARD_2;
        return ShardId.DEFAULT;
    }
}
```

### 4.5 Datasource Routing (Future Implementation)

Use **Spring’s AbstractRoutingDataSource** to route SQL calls:

```java
public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<ShardId> CURRENT_SHARD = new ThreadLocal<>();

    public static void setShard(ShardId shardId) {
        CURRENT_SHARD.set(shardId);
    }

    public static void clearShard() {
        CURRENT_SHARD.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return CURRENT_SHARD.get();
    }
}
```

Then in service methods (conceptually):

```java
public EventResponse createEvent(EventCreateRequest req) {
    ShardId shard = shardResolver.resolveByCity(req.getCity());
    try {
        ShardRoutingDataSource.setShard(shard);
        Event saved = eventRepository.save(eventMapper.toEntity(req));
        return eventMapper.toResponse(saved);
    } finally {
        ShardRoutingDataSource.clearShard();
    }
}
```

> **Note**: In the current implementation, we still use a **single datasource**. The routing layer can be added later without changing controller contracts.

### 4.6 Sharding & Caching Interaction

* Cache keys must be **shard-aware** for aggregate queries to avoid collisions.
* For example, `popularEvents` cache for Bangalore shard should be distinct from Mumbai shard.

Example:

```java
// Current annotation:
@Cacheable(
    value = "popularEvents",
    key = "T(java.util.Objects).hash(#filter.city, #filter.fromTime, #filter.toTime, #filter.page, #filter.size)",
    condition = "#filter.sort != null && #filter.sort.equalsIgnoreCase('POPULAR')"
)
```

In a sharded world, we conceptually treat `city` as part of shard identity. The key already includes `city`, so once shards are in place, the cache naturally separates per city.

---

## 5. Caching Strategy

### 5.1 Goals

* Reduce database load for **hot endpoints**:

  * Event details for frequently viewed events.
  * Popular events list.
* Keep consistency **good enough** for user experience (event changes reflected within seconds to minutes).
* Avoid full reliance on cache (DB is always the source of truth).

### 5.2 Technologies

* **Redis** as distributed cache
* **Spring Cache abstraction** with `CacheManager`
* JSON serialization using `GenericJackson2JsonRedisSerializer` with proper `ObjectMapper` configuration (Java time, etc.)

Configured in `RedisConfig`:

* Default `entryTtl(Duration.ofMinutes(10))` for cache entries.
* Values are serialized as JSON.

---

## 6. Cache Types

### 6.1 `eventDetails` Cache

**What:** Cache for `GET /events/{id}` responses.

**Key:** `id` (Long) → `EventResponse`

**Usage:**

* **Read:**

  * `EventService.getEventById(id)` first tries Redis:

    * If hit and deserialization succeeds → return cached `EventResponse`.
    * If hit but deserialization fails (old format / version mismatch) → evict this key and fall back to DB.
    * If miss → read from DB and then populate cache.
* **Write (create/update/delete):**

  * On **create**: no direct eventDetails entry yet (only when first read).
  * On **update**: we evict `eventDetails` for that id.
  * On **delete**: we evict `eventDetails` for that id.

This guarantees that **after an update or delete, the next read is consistent** with the database.

### 6.2 `popularEvents` Cache

**What:** Cache for `GET /events` when sorted by popularity (`sort=POPULAR`).

**Key (current):**

```java
T(java.util.Objects).hash(
    #filter.city,
    #filter.fromTime,
    #filter.toTime,
    #filter.page,
    #filter.size
)
```

Only active when:

```java
condition = "#filter.sort != null && #filter.sort.equalsIgnoreCase('POPULAR')"
```

So only **POPULAR** queries are cached.

**Usage:**

* **Read:**

  * First call with given city/time/page computes result from DB, caches it.
  * Subsequent calls for the same filter within TTL hit Redis.
* **Write:**

  * On **createEvent**, we currently:

    ```java
    @CacheEvict(value = {"popularEvents"}, allEntries = true)
    public EventResponse createEvent(EventCreateRequest req) { ... }
    ```

    This ensures new events can appear in popular lists.

  * On **updateEvent** / **deleteEvent**:

    * We may either:

      * Evict **all `popularEvents`** (simple but effective), or
      * Evict only affected cities/time ranges (complex; only needed at massive scale).

---

## 7. Popularity Calculation

### 7.1 Current Behavior

* `sort=POPULAR` currently uses:

```java
private Sort resolveSort(String sort) {
    if (sort != null && sort.equalsIgnoreCase("POPULAR")) {
        return Sort.by(
            Sort.Order.desc("popularityScore"),
            Sort.Order.asc("time")
        );
    }
    return Sort.by(Sort.Direction.ASC, "time");
}
```

* `popularityScore` is a numeric column on `events` table, default `0`.

### 7.2 Future Integration with Booking Service

Planned data sources for calculating `popularityScore`:

* Number of **bookings** for the event.
* Number of **views** (page impressions).
* Average **rating** or engagement score.

Example formula:

```text
popularityScore = w1 * log(1 + bookingsCount)
                 + w2 * log(1 + viewsCount)
                 + w3 * rating
```

A background job or streaming consumer will periodically update `events.popularity_score` in Postgres.

> **Note:** Event Service remains a simple reader of `popularityScore` and does not compute it at request time.

---

## 8. Cache Invalidation Rules

**eventDetails**

* On `create`:

  * Nothing to evict; entry will be created on first GET.
* On `update`:

  * Evict `eventDetails` for that `event.id`.
* On `delete`:

  * Evict `eventDetails` for that `event.id`.

**popularEvents**

* On `create`:

  * `@CacheEvict(value = "popularEvents", allEntries = true)`
* On `update`:

  * Either:

    * Same: clear all popularEvents (simple), or
    * City-scoped eviction (future optimization).
* On `delete`:

  * Same strategy as update.

TTL (e.g. 10 minutes) provides a **second layer of eventual consistency**, even if some edge case misses explicit eviction.

---

## 9. Future Improvements

1. **Shard-Aware Metrics**

   * Monitor QPS per shard.
   * Cache hit/miss metrics for `eventDetails` and `popularEvents`.

2. **Per-City Popular Cache**

   * Separate Redis keys like `popularEvents:Bangalore:hash(...)`.
   * Fine-grained eviction per city.

3. **Soft Deletes & Status**

   * Use `status` field (`DRAFT`, `PUBLISHED`, `CANCELLED`, `DELETED`).
   * Adjust cache invalidation and queries accordingly.

4. **Full-Text Search**

   * Integrate with a search engine (e.g., Elasticsearch/OpenSearch) for text search on title/description.
   * Event Service becomes the source of truth; search index is a read-optimized view.

---

## 10. TL;DR

* **Sharding (design):**

  * Shard by **city**.
  * Use a **shard resolver** + potential `AbstractRoutingDataSource`.
  * Cache keys already implicitly separate by city.

* **Caching (implemented):**

  * `eventDetails` for `GET /events/{id}` — single-event cache, evicted on update/delete.
  * `popularEvents` for `GET /events?sort=POPULAR` — cached per filter, evicted on create (and later update/delete).

* **Popularity:**

  * Now: `popularityScore` + sort.
  * Later: computed from bookings/engagement data by background jobs.


