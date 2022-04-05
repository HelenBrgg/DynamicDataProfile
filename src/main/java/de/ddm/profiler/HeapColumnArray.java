package de.ddm.profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HeapColumnArray implements ColumnArray{
    private List<Value> values = new ArrayList<>();  

    @Override
    public List<Value> SetValue(List<ValueWithPosition> ValuesWithPosition) {
        return ValuesWithPosition.stream().map(a-> values.set(a.position, a.value) ).collect(Collectors.toList());
    }

    @Override
    public int rowCount() {
        return values.size();
    }
    
}
