package de.ddm.profiler;
import java.util.List;

public interface ColumnArray {
    List<Value> SetValue(List<ValueWithPosition> ValuesWithPosition);
    int rowCount();

    
}
