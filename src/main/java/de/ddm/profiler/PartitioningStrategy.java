import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.List;

public interface PartitioningStrategy {
    @AllArgsConstructor
    public static class Partition {
        public String attribute;
        public int rangeBegin;
        public int rangeEndInclusive;
        public int targetWorkerID;
    }

    /// Implementors MUST provide either partitonStream or partitionRange.
    default List<Partition> partitionStream(String attribute, int numWorkers, IntStream positions){
        IntSummaryStatistics stats = positions.collect(Collectors.summarizingInt(Integer::intValue));
        return self.partitionRange(attribute, numWorkers, stats.getMin(), stats.getMax());
    }
    default List<Partition> partitionRange(String attribute, int numWorkers, int rangeBegin, int rangeEndInclusive){
        return this.partitionStream(attribute, numWorkers, IntStream.range(rangeBegin, rangeEndInclusive));
    }

    default void partitionTable(Table table, BiConsumer<Partition, Stream<ValueWithPosition>>> consumer){
        // go through all columns
        for (int colIndex = 0; colIndex < table.attributes.size(); colIndex++) {
            String attribute = table.attributes.get(colIndex);
            Column column = table.columns.get(colIndex);

            // go through all partitions for current column
            for (Partition partition : this.partitionRange(attribute, table.positions.stream())) {
                // retrieve values for partition
                Stream<ValueWithPosition> partitionValues = table.streamColumn(col, partition.rangeBegin, partition.rangeEndInclusive);

                // finally, pass partition and partition values to the consumer
                consumer.accept(partition, partitionValues);
            }
        }
    }
}
