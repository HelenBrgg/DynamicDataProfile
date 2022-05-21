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

    // Every implementor needs to provide either of these one
    default List<Partition> partitionStream(String attribute, IntStream positions){
        IntSummaryStatistics stats = positions.collect(Collectors.summarizingInt(Integer::intValue));
        return this.partitionRange(attribute, stats.getMin(), stats.getMax());
    }
    default List<Partition> partitionRange(String attribute, int rangeBegin, rangeEndInclusive){
        return this.partitionStream(attribute, IntStream.range(rangeBegin, rangeEndInclusive));
    }

    default void partitionTable(Table table, BiConsumer<Partition, List<ValueWithPosition>>> consumer){
        // go through all columns
        for (int col = 0; col < table.attributes.size(); col++) {
            String attribute = table.attributes.get(col);
            Column column = table.columns.get(col);

            // go through all partitions for current column
            for (Partition partition : this.partitionRange(attribute, table.positions.stream())) {

                // for the current partition, gather the values
                List<ValueWithPositions> partitionValues = new ArrayList<>();
                for (int index = 0; index < table.positions.size()) {
                    int position = table.positions.get(index);
                    if (position >= partition.rangeBegin && position <= partition.rangeEndInclusive){
                        partitionValues.add(new ValueWithPosition(column.get(index), position));
                    }
                }
                // finally, pass partition and partition values to the consumer
                consumer.accept(partition, partitionValues);
            }
        }
    }
}

