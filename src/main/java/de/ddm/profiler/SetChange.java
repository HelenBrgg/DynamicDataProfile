package de.ddm.profiler;

import lombok.AllArgsConstructor;
import java.util.Set;

@AllArgsConstructor
public class SetChange {
    public Set<Value> inserted;
    public Set<Value> removed;

}
