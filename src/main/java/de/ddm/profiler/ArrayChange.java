package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

/**
    * An ArrayChange is a list of values to be applied to an ColumnArray, it contains of
    * - the attribute that the values belong to. 
    * - a list of values with their positions. 
    * - and the ID of the worker that saves all values of that attribute 
    * They have to be put to the column of the worker that saves the column.
    * @see  ValueWithPosition
    */

@AllArgsConstructor
public class ArrayChange {
    public String attribute;
    public List<ValueWithPosition> values;
    public int workerID;
}
