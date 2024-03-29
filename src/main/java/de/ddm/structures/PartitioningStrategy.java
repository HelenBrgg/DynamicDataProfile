package de.ddm.structures;

import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface PartitioningStrategy {
    void addWorker(int workerId);
    void hasWorkers();

    Optional<Integer> getAttributeWorkerId(Table.Attribute attr);

    void beforePartitionTable(Table table);

    default Map<Integer, Table> partitionTable(Table table){
        this.beforePartitionTable(table);

        Map<Integer, Table> partitionTables = new HashMap<>();

        // go through all columns
        IntStream.range(0, table.attributes.size()).forEach(columnIndex -> {
            Table.Attribute attribute = table.attributes.get(columnIndex);

            Integer workerId = this.getAttributeWorkerId(attribute).get();

            Table partitionTable = partitionTables.computeIfAbsent(workerId, _workerId -> {
                Table newTable = new Table(table.name);
                newTable.positions = new ArrayList<>(table.positions);
                return newTable;
            });

            partitionTable.attributes.add(attribute);
            partitionTable.columns.add(new Table.Column(new ArrayList<>(table.columns.get(columnIndex).values)));
        });

        return partitionTables;
    }
}
