package de.ddm.profiler;
import java.util.Optional;

public interface Source {
    Optional<Table> NextTable();
    
}
