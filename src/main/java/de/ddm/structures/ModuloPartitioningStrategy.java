package de.ddm.structures;
import lombok.NoArgsConstructor;
import java.util.List;

@NoArgsConstructor
public class ModuloPartitioningStrategy implements PartitioningStrategy {
    private List<Table.Attribute> attributesByWorkerId;

    @Override
    public List<PartitioningStrategy.Partition> partitionRange(Table.Attribute attribute, int rangeBegin, int rangeEndInclusive) {
        int workerId = this.attributesByWorkerId.indexOf(attribute);
        if (workerId == -1) {
            this.attributesByWorkerId.add(attribute);
            workerId = this.attributesByWorkerId.size() - 1;
        }
        return List.of(new PartitioningStrategy.Partition(attribute, rangeBegin, rangeEndInclusive, workerId));
    }
}
