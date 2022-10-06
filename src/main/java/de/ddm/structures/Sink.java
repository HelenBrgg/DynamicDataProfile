package de.ddm.structures;

import java.util.Map;

public interface Sink {
    void putLiveResult(Candidate candidate, CandidateStatus status);
    void putFinalResults(Map<Candidate, CandidateStatus> results);
    void finish();
}
