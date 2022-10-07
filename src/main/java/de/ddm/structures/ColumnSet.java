package de.ddm.structures;

import java.util.*;
import java.util.stream.Stream;
import orestes.bloomfilter.BloomFilter;

public interface ColumnSet {
    @FunctionalInterface
    static interface Factory {
        ColumnSet create();
    }

    boolean containsAll(Stream<Value> values);

    int getCardinality();
    List<Value> getMinMax();

    // BloomFilter<Value> generateBloomFilter();
    BitSet generateBloomFilter();

    Stream<Value> queryChunk(Optional<Value> from);

    Stream<Value> queryAll();

    Map<Value, Long> applyCountDeltas(Map<Value, Long> countDeltas);
}