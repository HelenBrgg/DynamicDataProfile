package de.ddm.profiler;

import java.util.Map;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HeapColumnSet implements ColumnSet {

    @Override
    public Map<Value, Integer> applyCounts(Map<Value, Integer> counts) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<Value> queryRange(Value from, Value to) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int cardinality() {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
