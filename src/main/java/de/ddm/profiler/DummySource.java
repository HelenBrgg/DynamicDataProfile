package de.ddm.profiler;

import java.util.Optional;

import lombok.AllArgsConstructor;

@AllArgsConstructor 
public class DummySource implements Source {
    private Table table;

    @Override
    public Optional<Table> NextTable() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
