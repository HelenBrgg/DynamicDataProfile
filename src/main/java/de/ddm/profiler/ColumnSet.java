package de.ddm.profiler;

import java.util.*;
import java.util.stream.Stream;


public interface ColumnSet{

    public Map<Value,Integer> applyCounts(Map<Value,Integer> counts);

    public Stream<Value> queryRange(Value from, Value to);

    public int cardinality();
}
