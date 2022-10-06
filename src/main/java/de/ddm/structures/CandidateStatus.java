package de.ddm.structures;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@AllArgsConstructor
@Getter
public class CandidateStatus {
    public static interface Reason {
        default String additionalInfo(){ return ""; }
    }

    private boolean isValid;
    private Reason reason;

    @AllArgsConstructor
    public static class Cardinality implements Reason {
        private int attrA;
        private int attrB;

        @Override
        public String toString(){ return "cardinality"; }

        @Override
        public String additionalInfo(){ return this.attrA + " > " + this.attrB; }
    }

    @AllArgsConstructor
    public static class Datatype implements Reason {
        @Override
        public String toString(){ return "datatype"; }
    }

    @AllArgsConstructor
    public static class Extrema implements Reason {
        private Value minA;
        private Value maxA;
        private Value minB;
        private Value maxB;

        @Override
        public String toString(){ return "extrema"; }

        @Override
        public String additionalInfo(){ return minA + ".." + maxA + " X " + minB + ".." + maxB; }
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

    public static CandidateStatus ruledOutByCardinality(int attrA, int attrB) {
        return new CandidateStatus(false, new Cardinality(attrA, attrB));
    }

    public static CandidateStatus ruledOutByDatatype() {
        return new CandidateStatus(false, new Datatype());
    }

    public static CandidateStatus ruledOutByExtrema(Value minA, Value maxA, Value minB, Value maxB) {
        return new CandidateStatus(false, new Extrema(minA, maxA, minB, maxB));
    }

    public static CandidateStatus ruledOutByBloomfilter() {
        return new CandidateStatus(false, new Bloomfilter());
    }

    public static CandidateStatus failedCheck() {
        return new CandidateStatus(false, new FailedCheck());
    }

    public static CandidateStatus succeededCheck() {
        return new CandidateStatus(true, new SucceededCheck());
    }

    public static CandidateStatus logicalImplication(boolean isValid, List<Candidate> involvedCandidates) {
        return new CandidateStatus(isValid, new LogicalImplication(involvedCandidates));
    }
}
