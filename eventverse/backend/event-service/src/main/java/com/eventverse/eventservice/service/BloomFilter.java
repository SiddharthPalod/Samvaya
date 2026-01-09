package com.eventverse.eventservice.service;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Objects;

@Component
public class BloomFilter {

    private final BitSet bitSet = new BitSet(1_000_000);
    private final int size = 1_000_000;

    private int hash(String value, int seed) {
        return Math.abs(Objects.hash(value, seed)) % size;
    }

    public void add(String value) {
        bitSet.set(hash(value, 1));
        bitSet.set(hash(value, 2));
        bitSet.set(hash(value, 3));
    }

    public boolean mightContain(String value) {
        return bitSet.get(hash(value, 1))
                && bitSet.get(hash(value, 2))
                && bitSet.get(hash(value, 3));
    }
}

