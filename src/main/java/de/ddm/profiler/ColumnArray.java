package de.ddm.profiler;
import java.util.List;

/**
    * Interface of one Column Array for saving a column
    */
  

public interface ColumnArray {
    /**
     * Adds a new list of values to their positions in the Column Array
     * @param valuesWithPosition list of the new values together with their positions
     * @return a list of the old values
     */
    List<Value> setValue(List<ValueWithPosition> ValuesWithPosition);
    int rowCount();

    
}
