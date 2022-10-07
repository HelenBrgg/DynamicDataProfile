package de.ddm.structures;

import lombok.Getter;
import java.util.Set;
import java.util.HashSet;

@Getter
public class SetDiff {
    private Set<Value> inserted;
    private Set<Value> removed;

    public SetDiff(Set<Value> inserted, Set<Value> removed) {
        // here we ensure the two sets are distinct
        // (element was added and removed = no change)
        this.inserted = new HashSet<>(inserted);
        this.inserted.removeAll(removed);
        this.removed = new HashSet<>(removed);
        this.removed.removeAll(inserted);
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        this.inserted.forEach(s -> { b.append('+'); b.append(s); b.append('\n'); });
        this.removed.forEach(s -> { b.append('-'); b.append(s); b.append('\n'); });
        return b.toString();
    }
}
