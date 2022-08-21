package de.ddm.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class CandidateStatus {
    public static interface Reason {
    }

    private boolean isValid;
    private Reason reason;
    private int comparsionCount;

    @AllArgsConstructor
    public static class Cardinality implements Reason {
        @Override
        public String toString(){ return "cardinality"; }
    }

    @AllArgsConstructor
    public static class Datatype implements Reason {
        @Override
        public String toString(){ return "datatype"; }
    }

    @AllArgsConstructor
    public static class Extrema implements Reason {
        @Override
        public String toString(){ return "extrema"; }
    }

    @AllArgsConstructor
    public static class Bloomfilter implements Reason {
        @Override
        public String toString(){ return "bloomfilter"; }
    }

    @AllArgsConstructor
    public static class FailedCheck implements Reason {
        @Override
        public String toString(){ return "failed_check"; }
    }

    @AllArgsConstructor
    public static class SucceededCheck implements Reason {
        @Override
        public String toString(){ return "succeeded_check"; }
    }

    @AllArgsConstructor
    public static class LogicalImplication implements Reason {
        public List<Candidate> involvedCandidates;

        @Override
        public String toString(){ return "logical_implication"; }
    }

    public static CandidateStatus ruledOutByCardinality() {
        return new CandidateStatus(false, new Cardinality(), 0);
    }

    public static CandidateStatus ruledOutByDatatype() {
        return new CandidateStatus(false, new Datatype(), 0);
    }

    public static CandidateStatus ruledOutByExtrema() {
        return new CandidateStatus(false, new Extrema(), 0);
    }

    public static CandidateStatus ruledOutByBloomfilter() {
        return new CandidateStatus(false, new Bloomfilter(), 0);
    }

    public static CandidateStatus failedCheck(int comparisonCount) {
        return new CandidateStatus(false, new FailedCheck(), comparisonCount);
    }

    public static CandidateStatus succeededCheck(int comparisonCount) {
        return new CandidateStatus(true, new SucceededCheck(), comparisonCount);
    }

    public static CandidateStatus logicalImplication(boolean isValid, List<Candidate> involvedCandidates) {
        return new CandidateStatus(isValid, new LogicalImplication(involvedCandidates), 0);
    }
}
