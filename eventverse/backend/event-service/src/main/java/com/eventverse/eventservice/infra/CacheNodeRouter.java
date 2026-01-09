package com.eventverse.eventservice.infra;
import org.springframework.stereotype.Component;

@Component
public class CacheNodeRouter {

    private final ConsistentHash hash;

    public CacheNodeRouter() {
        hash = new ConsistentHash();
        hash.addNode("redis-1");
        hash.addNode("redis-2");
        hash.addNode("redis-3");
    }

    public String route(String eventId) {
        return hash.getNode(eventId);
    }
}

