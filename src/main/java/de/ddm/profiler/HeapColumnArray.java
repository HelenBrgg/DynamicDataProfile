package de.ddm.profiler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;

/**
 * implements ColumnArray
 */

@NoArgsConstructor
public class HeapColumnArray implements ColumnArray {
    private List<Value> values = new ArrayList<>();

    /**
     * Adds a new list of values to their positions in the Column Array
     * 
     * @param valuesWithPosition list of the new values together with their
     *                           positions
     * @return a list of the old values
     */

    @Override
    public List<Value> setValue(List<ValueWithPosition> valuesWithPosition) {
        int maxPosition = valuesWithPosition.stream().map(a -> a.position).max((o1, o2) -> {
            if (o1 > o2)
                return 1;
            if (o1 == o2)
                return 0;
            if (o1 < o2)
                return -1;
            else
                return 0;
        }).orElse(0);

        int padding = (maxPosition + 1) - values.size();
        for (int i = 0; i < padding; i++) {
            values.add(Value.NULL);
        }
        return valuesWithPosition.stream().map(a -> values.set(a.position, a.value)).collect(Collectors.toList());
    }

    /**
     * returns the number of values in the Column Array
     * 
     * @return number of values
     */

    @Override
    public int rowCount() {
        return values.size();
    }

}
