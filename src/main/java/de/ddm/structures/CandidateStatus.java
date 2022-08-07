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

    @AllArgsConstructor
    public static class Cardinality implements Reason {
    }

    @AllArgsConstructor
    public static class Datatype implements Reason {
    }

    @AllArgsConstructor
    public static class Extrema implements Reason {
    }

    @AllArgsConstructor
    public static class Bloomfilter implements Reason {
    }

    @AllArgsConstructor
    public static class FailedCheck implements Reason {
    }

    @AllArgsConstructor
    public static class SucceededCheck implements Reason {
    }

    @AllArgsConstructor
    public static class LogicalImplication implements Reason {
        public List<Candidate> involvedCandidates;
    }

    public static CandidateStatus ruledOutByCardinality() {
        return new CandidateStatus(false, new Cardinality());
    }

    public static CandidateStatus ruledOutByDatatype() {
        return new CandidateStatus(false, new Datatype());
    }

    public static CandidateStatus ruledOutByExtrema() {
        return new CandidateStatus(false, new Extrema());
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
