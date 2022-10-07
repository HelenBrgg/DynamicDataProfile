package de.ddm.structures;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
// import org.apache.hadoop.util.bloom.BloomFilter;
import orestes.bloomfilter.BloomFilter;
// import orestes.bloomfilter.memory.BloomFilterMemory;

@Getter
@NoArgsConstructor
public class Metadata {
    private int cardinality = 0;
    private List<Value> minMax = List.of();
    private Optional<Datatype> datatype = Optional.empty();
    private Optional<BitSet> bloomfilter = Optional.empty();

    public void update(ColumnSet set, SetDiff diff){
        this.cardinality = set.getCardinality();

        // find most abstract datatype
        diff.getInserted().forEach(elem -> {
            Datatype elemType = Datatype.inferType(elem);
            if (this.datatype.isEmpty()
            || elemType.isSubtype(this.datatype.get())) {
                this.datatype = Optional.of(elemType);
            }
        });

        this.minMax = set.getMinMax();

        this.bloomfilter = Optional.of(set.generateBloomFilter());
    }

    public Optional<CandidateStatus> precheckPossibleSubset(Metadata other){
        if (other.cardinality > this.cardinality
        || this.cardinality == 0
        || other.cardinality == 0)
            return Optional.of(CandidateStatus.ruledOutByCardinality(other.cardinality, this.cardinality));

        if (this.datatype.isPresent() && other.datatype.isPresent()) {
            Datatype typeA = other.datatype.get();
            Datatype typeB = this.datatype.get();

            if (!typeA.isSubtype(typeB)){
                return Optional.of(CandidateStatus.ruledOutByDatatype(typeA, typeB));
            }
        }

        // TODO check cardinality == 0 instead?
        if (this.getMin().isPresent() && other.getMin().isPresent()) {
            Value minA = other.getMin().get();
            Value maxA = other.getMax().get();
            Value minB = this.getMin().get();
            Value maxB = this.getMax().get();

            if (minA.compareTo(minB) < 0) return Optional.of(CandidateStatus.ruledOutByExtrema(minA, maxA, minB, maxB));
            if (maxA.compareTo(maxB) > 0) return Optional.of(CandidateStatus.ruledOutByExtrema(minA, maxA, minB, maxB));
        }

        if (this.bloomfilter.isPresent() && other.bloomfilter.isPresent()) {
            assert this.bloomfilter.get().cardinality() != 0;
            assert other.bloomfilter.get().cardinality() != 0;

            BitSet tmp = (BitSet) other.bloomfilter.get().clone();
            tmp.andNot(this.bloomfilter.get());

            if (tmp.cardinality() != 0) {
                return Optional.of(CandidateStatus.ruledOutByBloomfilter(other.bloomfilter.get().cardinality(), this.bloomfilter.get().cardinality(), tmp.cardinality()));
            }

            // assert this.bloomfilter.get().compatible(other.bloomfilter.get());

            // BloomFilter<Value> tmp = other.bloomfilter.get().clone();
            // tmp.intersect(this.bloomfilter.get());

            // if (tmp.isEmpty()) {
            //     return Optional.of(CandidateStatus.ruledOutByBloomfilter(other.bloomfilter.get().getBitSet().cardinality(), this.bloomfilter.get().getBitSet().cardinality(), tmp.getBitSet().cardinality()));
            // }
            // if (!tmp.equals(other.bloomfilter.get())) {
            //     return Optional.of(CandidateStatus.ruledOutByBloomfilter(other.bloomfilter.get().getExpectedElements(), this.bloomfilter.get().getExpectedElements(), tmp.getExpectedElements()));
            // }
        }

        return Optional.empty();
    }

    public Optional<Value> getMin(){
        if (minMax.isEmpty()) return Optional.empty();
        return Optional.of(this.minMax.get(0));
    }
    public Optional<Value> getMax(){
        if (minMax.isEmpty()) return Optional.empty();
        return Optional.of(this.minMax.get(this.minMax.size() - 1));
    }
}
