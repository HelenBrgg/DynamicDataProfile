package de.ddm.profiler;

import java.util.*;
import java.util.stream.Stream;

public interface ColumnSet {

    public Map<Value, Long> applyCounts(Map<Value, Long> counts);

    public Stream<Value> queryRange(Value from, Value to);

    public Stream<Value> queryRange();

    public int cardinality();

    public void merge(ColumnSet other);

    public boolean containsAll(Stream<Value> values);
}
