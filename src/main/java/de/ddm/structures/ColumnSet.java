package de.ddm.structures;

import java.util.*;
import java.util.stream.Stream;

public interface ColumnSet {
    @FunctionalInterface
    static interface Factory {
        ColumnSet create();
    }

    boolean containsAll(Stream<Value> values);

    int getCardinality();
    List<Value> getMinMax();

    Stream<Value> queryChunk(Optional<Value> from);

    //TODO
    // List<Value> queryChunk
    Stream<Value> queryAll();

    Map<Value, Long> applyCountDeltas(Map<Value, Long> countDeltas);
}