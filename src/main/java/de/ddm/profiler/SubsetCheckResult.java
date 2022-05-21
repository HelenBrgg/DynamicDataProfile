package de.ddm.profiler;

import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class SubsetCheckResult {
    public static interface Reason {
    }

    public boolean isSubset;
    public Reason reason;

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
        // sample of counterexamples and their counts in subset
        public Map<Value, Integer> notInSuperset;
    }

    @AllArgsConstructor
    public static class PassedCheck implements Reason {
    }

    @AllArgsConstructor
    public static class LogicalImplication implements Reason {
        public List<Candidate> involvedCandidates;
    }

    public static SubsetCheckResult ruledOutByCardinality() {
        return new SubsetCheckResult(false, new Cardinality());
    }

    public static SubsetCheckResult ruledOutByDatatype() {
        return new SubsetCheckResult(false, new Datatype());
    }

    public static SubsetCheckResult ruledOutByExtrema() {
        return new SubsetCheckResult(false, new Extrema());
    }

    public static SubsetCheckResult ruledOutByBloomfilter() {
        return new SubsetCheckResult(false, new Bloomfilter());
    }

    public static SubsetCheckResult failedCheck(Map<Value, Integer> notInSuperset) {
        return new SubsetCheckResult(false, new FailedCheck(notInSuperset));
    }

    public static SubsetCheckResult passedCheck() {
        return new SubsetCheckResult(true, new PassedCheck());
    }

    public static SubsetCheckResult logicalImplication(boolean isSubset, List<Candidate> involvedCandidates) {
        return new SubsetCheckResult(isSubset, new LogicalImplication(involvedCandidates));
    }
}
