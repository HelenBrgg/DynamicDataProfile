package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data @AllArgsConstructor @NoArgsConstructor
public class Table {
    public String Name;
    public List<String> Attributes;
    public List<Column> Columns; 
}