package de.ddm.structures;

import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * implements ColumnArray
 */

@NoArgsConstructor
public class HeapColumnArray implements ColumnArray {
    private List<Value> values = new ArrayList<>();

    /**
     * returns the number of values in the Column Array
     * 
     * @return number of values
     */
    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public Stream<Value.WithPosition> streamValues() {
        return IntStream.range(0, this.values.size())
            .mapToObj(pos -> new Value.WithPosition(this.values.get(pos), pos));
    }

    private void grow(int size){
        if (this.values.size() >= size) return;

        int padding = size - values.size();
        for (int i = 0; i < padding; i++) {
            this.values.add(Value.EMPTY);
        }
    }

    /**
     * Adds a new list of values to their positions in the Column Array
     * 
     * @param valuesWithPosition list of the new values together with their
     *                           positions
     * @return a list of the old values
     */
    @Override
    public List<Value> setValues(Stream<Value.WithPosition> newValues) {
        return newValues
            .map(val -> {
                this.grow(val.position + 1);
                return values.set(val.position, val.value);
            })
            .collect(Collectors.toList());
    }

    @Override
    public void clear(){
        this.values.clear();
    }
}
