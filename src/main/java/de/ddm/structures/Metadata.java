package de.ddm.structures;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@NoArgsConstructor
public class Metadata {
    private int cardinality = 0;
    private List<Value> minMax = List.of();
    public Optional<Datatype> datatype = Optional.empty();
    // public Bloomfilter bloomfilter; TODO

    public void update(ColumnSet set, SetDiff diff){
        this.cardinality = set.getCardinality();

        this.minMax = set.getMinMax();

        // find lowest datatype
        diff.getInserted().forEach(elem -> {
            Datatype elemType = Datatype.inferType(elem);
            if (this.datatype.isEmpty()
            || elemType.isSubtype(this.datatype.get())) {
                this.datatype = Optional.of(elemType);
            }
        });
    }

    public Optional<CandidateStatus> precheckPossibleSubset(Metadata other){
        if (other.cardinality > this.cardinality)
            return Optional.of(CandidateStatus.ruledOutByCardinality(other.cardinality, this.cardinality));

        if (this.datatype.isPresent() && other.datatype.isPresent()) {
            if (!other.datatype.get().isSubtype(this.datatype.get())) {
                return Optional.of(CandidateStatus.ruledOutByDatatype());
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
