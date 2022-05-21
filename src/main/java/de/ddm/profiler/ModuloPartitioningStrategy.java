import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.List;

@NoArgsConstructor
public class ModuloPartitioningStrategy implements PartitioningStrategy {
    List<String> attributesByWorkerId;

    List<PartitioningStrategy.Partition> partitionRange(String attribute, int rangeBegin, rangeEndInclusive){
        int workerId = this.attributesByWorkerId.indexOf(attribute);
        if (workerId == -1){
            this.attributesByWorkerId.add(attribute);
            workerId = this.attributesByWorkerId.size() - 1;
        }
        return Arrays.asList(new PartitioningStrategy.Partition(attribute, rangeBegin, rangeEnd, workerId));
    }
}
