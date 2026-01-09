package com.eventverse.eventservice.sharding;

import org.springframework.stereotype.Component;

@Component
public class CityBasedShardResolver implements ShardResolver {
    @Override
    public ShardId resolveByCity(String city) {
        if (city == null) {
            return ShardId.DEFAULT;
        }

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

    @Override
    public ShardId resolveByEventId(Long eventId) {
        if (eventId == null) {
            return ShardId.DEFAULT;
        }
        // Very simple example based on ID ranges (you can refine later)
        if (eventId < 1_000_000L) {
            return ShardId.SHARD_1;
        } else if (eventId < 2_000_000L) {
            return ShardId.SHARD_2;
        } else {
            return ShardId.SHARD_3;
        }
    }
}
