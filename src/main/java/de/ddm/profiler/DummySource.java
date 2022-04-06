package de.ddm.profiler;

import java.util.Optional;

import lombok.AllArgsConstructor;

/**
    * implements the source, is a source that contains the tables
    * @see Source
    */

@AllArgsConstructor 
public class DummySource implements Source {
    private Table table;

    /**
     * Returns the next table of the source in case it is available.
     * @return  the new table or none if non is available
     */
    @Override
    public Optional<Table> nextTable() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
