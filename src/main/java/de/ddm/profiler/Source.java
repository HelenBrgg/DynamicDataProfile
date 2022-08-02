package de.ddm.profiler;

/**
 * Interface of the source of the tables
 */

public interface Source {

    /**
     * returns the next table if one is available
     * 
     * @return returns the table or returns null if no table is available
     */
    Table nextTable();

}
