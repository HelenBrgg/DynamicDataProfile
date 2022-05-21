package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * represents a column in a table
 * saves the values of one column in a list
 * 
 * @see Value
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {
    public List<Value> values = new ArrayList<>();
}
