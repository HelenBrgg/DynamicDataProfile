package de.ddm.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.IntSummaryStatistics;
import java.util.List;

public interface PartitioningStrategy {
    @Getter
    @AllArgsConstructor
    static class Partition {
        private Table.Attribute attribute;
        private int rangeBegin;
        private int rangeEndInclusive;
        private int workerId;
    }

    /// Implementors MUST provide either partitonStream or partitionRange.
    default List<Partition> partitionStream(Table.Attribute attr, IntStream positions) {
        IntSummaryStatistics stats = positions.summaryStatistics();
        return this.partitionRange(attr, stats.getMin(), stats.getMax());
    }

    default List<Partition> partitionRange(Table.Attribute attr, int rangeBegin, int rangeEndInclusive) {
        return this.partitionStream(attr, IntStream.range(rangeBegin, rangeEndInclusive));
    }

    default void partitionTable(Table table, BiConsumer<Partition, Stream<Value.WithPosition>> consumer) {
        // go through all columns
        IntStream.range(0, table.attributes.size()).forEach(columnIndex -> {
            Table.Attribute attribute = table.attributes.get(columnIndex);

            // go through all partitions for current column
            this.partitionStream(attribute, table.streamPositions()).forEach(partition -> {
                // retrieve values for partition
                Stream<Value.WithPosition> partitionValues = table
                    .streamColumnWithPositions(columnIndex, partition.rangeBegin, partition.rangeEndInclusive);

                // finally, pass partition and partition values to the consumer
                consumer.accept(partition, partitionValues);
            });
        });
    }
}
