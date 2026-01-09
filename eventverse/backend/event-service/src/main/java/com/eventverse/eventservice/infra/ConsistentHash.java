package com.eventverse.eventservice.infra;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash {

    private final SortedMap<Integer, String> ring = new TreeMap<>();

    public void addNode(String node) {
        ring.put(node.hashCode(), node);
    }

    public String getNode(String key) {
        int hash = key.hashCode();
        SortedMap<Integer, String> tail = ring.tailMap(hash);
        return tail.isEmpty()
                ? ring.get(ring.firstKey())
                : tail.get(tail.firstKey());
    }
}

