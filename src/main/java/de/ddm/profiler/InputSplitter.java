package de.ddm.profiler;

import java.util.ArrayList;
import java.util.List;

/**
 * receives tables from the source and distribute them to the workers
 * 
 * @see Source
 */

public class InputSplitter {
    public Source source;
    public int numWorker;

    public InputSplitter(Source source, int numWorker) {
        this.source = source;
        this.numWorker = numWorker;
    }

    /**
     * Splits a given table into its columns and creates setCommands for the
     * workers.
     * If there are more columns than worker, the workers receive multiple columns.
     * They get distributed with a modulo-function.
     * 
     * @param table the given table with the columns to be distributed
     * @return a List of SetCommands
     * @see SetCommand
     */

    public List<SetCommand> splitTable(Table table) {
        List<SetCommand> setCommands = new ArrayList<>();
        for (int col = 0; col < table.attributes.size(); col++) {
            SetCommand setCommand = new SetCommand();
            setCommand.values = new ArrayList<>();
            setCommand.attribute = table.attributes.get(col);
            Column column = table.columns.get(col);
            for (int row = 0; row < column.values.size(); row++) {
                Value value = column.values.get(row);
                int position = table.positions.get(row);
                setCommand.values.add(new ValueWithPosition(value, position));
            }
            setCommand.workerID = (col - 1) % numWorker;
            setCommands.add(setCommand);
        }
        return setCommands;
    }

}
