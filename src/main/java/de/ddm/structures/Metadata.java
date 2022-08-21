package de.ddm.structures;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jnr.ffi.annotations.Meta;

@Getter
@NoArgsConstructor
public class Metadata {
    private int cardinality = 0;
    private List<Value> minMax = List.of();
    // public Map<Datatype, Integer> datatypeCounts = new HashMap<>();
    // public Bloomfilter bloomfilter; TODO

    public void update(ColumnSet set, SetDiff diff){
        this.cardinality = set.getCardinality();
        this.minMax = set.getMinMax();
    }

    public Metadata combineWith(Metadata other){
        // TODO
        return this;
    }

    public Optional<CandidateStatus> precheckPossibleSubset(Metadata other){
        if (this.cardinality > other.cardinality) return Optional.of(CandidateStatus.ruledOutByCardinality());

        if (this.minMax.size() == 0) return Optional.empty();

        Value minA = this.minMax.get(0);
        Value maxA = (this.minMax.size() == 2) ? this.minMax.get(1) : minA;

        Value minB = other.minMax.get(0);
        Value maxB = (other.minMax.size() == 2) ? other.minMax.get(1) : minB;

        if (minA.compareTo(minB) < 0) return Optional.of(CandidateStatus.ruledOutByExtrema());
        if (maxA.compareTo(maxB) > 0) return Optional.of(CandidateStatus.ruledOutByExtrema());

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
