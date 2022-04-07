package de.ddm.profiler;

import java.util.*;
import java.util.TreeMap;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HeapColumnSet implements ColumnSet {
    public SortedMap<Value,Integer> set = new TreeMap<>();

    /**
     * Applies(Increases/ Decreases) the changes of counts for a given set of values
     * @param counts the set of values and their countdifferences
     * @return the total number of counts for the given values
     */

    @Override
    public Map<Value, Integer> applyCounts(Map<Value, Integer> counts) {
        Map<Value,Integer> totalCounts= new TreeMap<>();
        counts.forEach((k,v)-> {
            Integer old = set.get(k);
            if (old == null) old=0;
            set.put(k,v + old);
            totalCounts.put(k,v + old);
        });
        return totalCounts;
    }

    /**
     * 
     * @param from begin of the query range
     * @param to end of the query range
     */

    @Override
    public Stream<Value> queryRange(Value from, Value to) {
        return set.subMap(from,to).entrySet().stream().filter(entry-> entry.getValue()>0).map(entry->entry.getKey());
    }
    /**
     * The size of the set
     * @return size
     */

    @Override
    public int cardinality() {
        return set.size();
    }
    
}


