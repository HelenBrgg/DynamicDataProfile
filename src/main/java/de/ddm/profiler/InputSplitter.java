package de.ddm.profiler;

import java.util.ArrayList;
import java.util.List;

/**
 * receives tables from the source and distribute them to the workers
 */

public class InputSplitter {
    public int numWorker;

    public InputSplitter(int numWorker) {
        this.numWorker = numWorker;
    }

    /**
     * Splits a given table into its columns and creates ArrayChanges for the
     * workers.
     * If there are more columns than worker, the workers receive multiple columns.
     * They get distributed with a modulo-function.
     * 
     * @param table the given table with the columns to be distributed
     * @return a List of ArrayChanges
     * @see ArrayChange
     */

    public List<ArrayChange> splitTable(Table table) {
        List<ArrayChange> changes = new ArrayList<>();

        for (int col = 0; col < table.attributes.size(); col++) {
            int workerID = (col - 1) % numWorker;
            String attribute = table.attributes.get(col);
            List<ValueWithPosition> values = new ArrayList<>();

            Column column = table.columns.get(col);
            for (int row = 0; row < column.values.size(); row++) {
                Value value = column.values.get(row);
                int position = table.positions.get(row);
                ArrayChange.values.add(new ValueWithPosition(value, position));
            }
            changes.add(new ArrayChange(attribute, values, worker_id));
        }
        return ArrayChanges;
    }

}
