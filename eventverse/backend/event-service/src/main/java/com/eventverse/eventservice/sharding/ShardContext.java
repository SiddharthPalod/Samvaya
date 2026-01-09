package com.eventverse.eventservice.sharding;

public final class ShardContext {

    private static final ThreadLocal<ShardId> CURRENT_SHARD = new ThreadLocal<>();

    private ShardContext() {
    }

    public static void set(ShardId shardId) {
        CURRENT_SHARD.set(shardId);
    }

    public static ShardId get() {
        ShardId id = CURRENT_SHARD.get();
        return id != null ? id : ShardId.DEFAULT;
    }

    public static void clear() {
        CURRENT_SHARD.remove();
    }
}
