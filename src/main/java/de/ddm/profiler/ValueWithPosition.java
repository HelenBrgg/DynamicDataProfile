package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class ValueWithPosition {
    public Value value;
    public int position;
}
