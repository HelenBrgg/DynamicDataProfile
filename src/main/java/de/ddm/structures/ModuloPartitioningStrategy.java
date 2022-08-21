package de.ddm.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModuloPartitioningStrategy implements PartitioningStrategy {
    private Map<Table.Attribute, Integer> workerIdByAttribute = new HashMap<>();
    private int numAttributes = 0;
    private int numWorkers;

    public ModuloPartitioningStrategy(int numWorkers){
        this.numWorkers = numWorkers;
    }

    @Override
    public Optional<Integer> getAttributeWorkerId(Table.Attribute attr) {
        if (this.workerIdByAttribute.containsKey(attr))
            return Optional.of(this.workerIdByAttribute.get(attr));
        return Optional.empty();
    }

    @Override
    public void beforePartitionTable(Table table) {
        table.attributes.forEach(attribute -> {
            if (!this.workerIdByAttribute.containsKey(attribute)) {
                int workerId = this.numAttributes % this.numWorkers;
                this.workerIdByAttribute.put(attribute, workerId);
                this.numAttributes += 1;
            }
        });
    }
}
