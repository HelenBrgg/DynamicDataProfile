package de.ddm.structures;

import java.util.List;
import java.util.stream.Stream;

/**
    * Interface of one Column Array for saving a column
    */
public interface ColumnArray {
    @FunctionalInterface
    static interface Factory {
        ColumnArray create();
    }

    int getRowCount();

    Stream<Value.WithPosition> streamValues();

    /**
     * Adds a new list of values to their positions in the Column Array
     * @param newValues list of the new values together with their positions
     * @return a list of the old values
     */
    List<Value> setValues(Stream<Value.WithPosition> newValues);

    void clear();
}
