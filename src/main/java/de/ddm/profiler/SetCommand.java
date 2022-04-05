package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data @AllArgsConstructor @NoArgsConstructor
public class SetCommand {
    public List<ValueWithPosition> values;
    public int workerID;
    public String attribute;
}
