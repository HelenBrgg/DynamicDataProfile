package de.ddm.structures;

import lombok.NoArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import orestes.bloomfilter.FilterBuilder;
import orestes.bloomfilter.CountingBloomFilter;

@NoArgsConstructor
public class HeapColumnSet implements ColumnSet {
    // NOTE: 0 counts are removed in applyCountDeltas
    private SortedMap<Value, Long> countMap = new TreeMap<>();
    private CountingBloomFilter<Value> bloomfilter =
        new FilterBuilder(16_000, 2)
            .buildCountingBloomFilter();
            // TODO set hash functions


    @Override
    public boolean containsAll(Stream<Value> values) {
        // TODO instead of collecting values to set, consume and compare step-by-step
        return this.countMap.keySet().containsAll(values.collect(Collectors.toSet()));
    }

    /**
     * The size of the set
     * 
     * @return size
     */
    @Override
    public int getCardinality() {
        return this.countMap.size();
    }

    @Override
    public List<Value> getMinMax() {
        if (this.getCardinality() == 0) return List.of();
        if (this.getCardinality() == 1) return List.of(this.countMap.firstKey());
        return List.of(this.countMap.firstKey(), this.countMap.lastKey());
    }

    @Override
    public BitSet generateBloomFilter(){
        BitSet ret = new BitSet(16000);

        this.bloomfilter.getCountMap().forEach((idx, count) -> {
            if (count == 0) return;
            ret.set(idx, true);
        });

        assert (ret.cardinality() == 0) == (this.getCardinality() == 0): "bloomfilter generated incorrectly";

        return ret;
    }

    @Override
    public Stream<Value> queryChunk(Optional<Value> from){
        Set<Value> subset = this.countMap.keySet();
        if (from.isPresent()) {
            subset = this.countMap.subMap(from.get(), this.countMap.lastKey()).keySet();
        }
        return subset.stream().limit(1000);
    }

    /**
     * 
     * @param from begin of the query range
     * @param to   end of the query range
     */
    @Override
    public Stream<Value> queryAll() {
        return this.countMap.keySet().stream();
    }

    /**
     * Applies(Increases/ Decreases) the changes of counts for a given set of values
     * 
     * @param counts the set of values and their countdifferences
     * @return the total number of counts for the given values
     */
    @Override
    public Map<Value, Long> applyCountDeltas(Map<Value, Long> countDeltas) {
        Map<Value, Long> totalCounts = new HashMap<>();
        countDeltas.forEach((k, v) -> {
            Long old = this.countMap.get(k);
            if (old == null)
                old = 0L;
            this.countMap.put(k, v + old);
            totalCounts.put(k, v + old);
        });

        // NOTE changes in the value-set are reflected back in the collection
        // see https://docs.oracle.com/javase/6/docs/api/java/util/Map.html#keySet%28%29
        this.countMap.values().removeIf(count -> count == 0);

        totalCounts.forEach((value, count) -> {
            if (count == 0) {
                this.bloomfilter.remove(value);
            } else if (count.equals(countDeltas.get(value))) {
                this.bloomfilter.add(value);
            }
        });
        
        return totalCounts;
    }
}
