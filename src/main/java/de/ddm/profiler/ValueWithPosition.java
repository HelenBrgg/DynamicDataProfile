package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
    * Contains: 
    * - a value
    * - the position of the value in a column 
    * @see  Value
    */

@Data @AllArgsConstructor @NoArgsConstructor
public class ValueWithPosition {
    public Value value;
    public int position;
}
