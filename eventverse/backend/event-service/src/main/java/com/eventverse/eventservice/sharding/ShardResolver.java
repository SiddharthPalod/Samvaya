package com.eventverse.eventservice.sharding;

public interface ShardResolver {
    ShardId resolveByCity(String city);
    default ShardId resolveByEventId(Long eventId) {
        return ShardId.DEFAULT;
    }
}
