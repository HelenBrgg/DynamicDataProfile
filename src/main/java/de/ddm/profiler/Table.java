package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains:
 * - a list of values with their positions.
 * - the attribute that the values belong to.
 * - the ID of the worker that saves all values of that attribute
 * They have to be put to the column of the worker that saves the column.
 * 
 * @see ValueWithPosition
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Table {
    public String name;
    public List<String> attributes;
    public List<Column> columns;
    public List<Integer> positions = new ArrayList<>();
}