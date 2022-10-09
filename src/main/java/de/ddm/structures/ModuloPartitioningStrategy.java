package de.ddm.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ModuloPartitioningStrategy implements PartitioningStrategy {
    private Map<Table.Attribute, Integer> workerIdByAttribute = new HashMap<>();
    private List<Integer> workerIds = new ArrayList<>();

    @Override
    public void addWorker(int workerId){
        this.workerIds.add(workerId);
    }
    @Override
    public void hasWorkers(){
        this.workerIds.isEmpty();
    }

    @Override
    public Optional<Integer> getAttributeWorkerId(Table.Attribute attr) {
        if (this.workerIdByAttribute.containsKey(attr))
            return Optional.of(this.workerIdByAttribute.get(attr));
        return Optional.empty();
    }

    @Override
    public void beforePartitionTable(Table table) {
        assert !this.workerIds.isEmpty() : "no registered workers";

        table.attributes.forEach(attribute -> {
            if (!this.workerIdByAttribute.containsKey(attribute)) {
                int workerId = this.workerIds.get((int) (Math.abs((long) attribute.hashCode()) % this.workerIds.size()));
                this.workerIdByAttribute.put(attribute, workerId);
            }
        });
    }
}
