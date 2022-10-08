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
        public String additionalInfo(){ return this.attrA + " ≮ " + this.attrB; }
    }

    @AllArgsConstructor
    public static class Datatype implements Reason {
        private de.ddm.structures.Datatype typeA;
        private de.ddm.structures.Datatype typeB;

        @Override
        public String toString(){ return "datatype"; }

        @Override
        public String additionalInfo(){ return this.typeA + " ⊄ " + this.typeB; }
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
        public String additionalInfo(){ return "extremaA=[" + minA + "," + maxA + "],extremaB=[" + minB + "," + maxB + "]"; }
    }

    @AllArgsConstructor
    public static class Bloomfilter implements Reason {
        private int sizeA;
        private int sizeB;
        private int outliersA;

        @Override
        public String toString(){ return "bloomfilter"; }

        @Override
        public String additionalInfo(){ return "sizeA=" + sizeA + ",sizeB=" + sizeB + ",outliersA=" + outliersA; }
    }

    @AllArgsConstructor
    public static class FailedCheck implements Reason {
        private int workerId;

        @Override
        public String toString(){ return "failed_check"; }

        @Override
        public String additionalInfo(){ return "workerId=" + workerId; }
    }

    @AllArgsConstructor
    public static class SucceededCheck implements Reason {
        private int workerId;

        @Override
        public String toString(){ return "succeeded_check"; }

        @Override
        public String additionalInfo(){ return "workerId=" + workerId; }
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

    public static CandidateStatus ruledOutByDatatype(de.ddm.structures.Datatype typeA, de.ddm.structures.Datatype typeB) {
        return new CandidateStatus(false, new Datatype(typeA, typeB));
    }

    public static CandidateStatus ruledOutByExtrema(Value minA, Value maxA, Value minB, Value maxB) {
        return new CandidateStatus(false, new Extrema(minA, maxA, minB, maxB));
    }

    public static CandidateStatus ruledOutByBloomfilter(int sizeA, int sizeB, int outliersA) {
        return new CandidateStatus(false, new Bloomfilter(sizeA, sizeB, outliersA));
    }

    public static CandidateStatus failedCheck(int workerId) {
        return new CandidateStatus(false, new FailedCheck(workerId));
    }

    public static CandidateStatus succeededCheck(int workerId) {
        return new CandidateStatus(true, new SucceededCheck(workerId));
    }

    public static CandidateStatus logicalImplication(boolean isValid, List<Candidate> involvedCandidates) {
        return new CandidateStatus(isValid, new LogicalImplication(involvedCandidates));
    }
}
