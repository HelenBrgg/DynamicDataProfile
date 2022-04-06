package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

/**
    * A setCommand is a list of commands for values to be added to a list, it contains of
    * - a list of values with their positions. 
    * - the attribute that the values belong to. 
    * - and the ID of the worker that saves all values of that attribute 
    * They have to be put to the column of the worker that saves the column.
    * @see  ValueWithPosition
    */

@Data @AllArgsConstructor @NoArgsConstructor
public class SetCommand {
    public List<ValueWithPosition> values;
    public int workerID;
    public String attribute;
}
