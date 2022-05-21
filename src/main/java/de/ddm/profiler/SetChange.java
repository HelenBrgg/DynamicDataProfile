package de.ddm.profiler;

import java.util.Set;
import java.util.HashSet;

public class SetChange {
    public Set<Value> inserted;
    public Set<Value> removed;

    public SetChange(Set<Value> inserted, Set<Value> removed) {
        // we ensure the two sets are distinct (element was added and removed = no
        // change)
        this.inserted = new HashSet<>(inserted);
        this.inserted.removeAll(removed);
        this.removed = new HashSet<>(removed);
        this.removed.removeAll(inserted);
    }
}
