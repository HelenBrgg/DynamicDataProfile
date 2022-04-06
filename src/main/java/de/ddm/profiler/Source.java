package de.ddm.profiler;
import java.util.Optional;

 /**
    * Interface of the source of the tables
    */

public interface Source {

    /**
    * returns the next table if one is available
    * @return returns the table or returns none if no table is available
    */
    Optional<Table> nextTable();
    
}
