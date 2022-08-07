package de.ddm.structures;

import java.util.Optional;

/**
 * Interface of the source of the tables
 */

public interface Source {
    boolean isFinished();

    /**
     * returns the next table if one is available
     * 
     * @return returns the table or returns null if no table is available
     */
    Optional<Table> nextTable();
}
