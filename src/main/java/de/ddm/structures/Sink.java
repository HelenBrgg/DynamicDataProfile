package de.ddm.structures;

public interface Sink {
    void putResult(Candidate candidate, CandidateStatus status);
    void finish();
}
