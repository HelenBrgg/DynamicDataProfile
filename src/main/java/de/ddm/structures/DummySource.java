package de.ddm.structures;

import java.util.Optional;
import java.util.List;

import lombok.AllArgsConstructor;

/**
 * implements the source, is a source that contains the tables
 * 
 * @see Source
 */
@AllArgsConstructor
public class DummySource implements Source {
    private List<Table> dummyTables;

    @Override
    public boolean isFinished() {
        return this.dummyTables.isEmpty();
    }

    /**
     * Returns the next table of the source in case it is available.
     * 
     * @return the new table or none if non is available
     */
    @Override
    public Optional<Table> nextTable() {
        if (this.isFinished()) {
            return Optional.empty();
        }
        return Optional.of(this.dummyTables.remove(0));
    }
}
