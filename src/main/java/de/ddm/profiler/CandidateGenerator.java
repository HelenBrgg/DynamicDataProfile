package de.ddm.profiler;

import java.util.Set;

public interface CandidateGenerator {
    public void addChange(String attribute, SetChange change);

    public Set<Candidate> generateCandidates();

    public void updateCandidateStatus(Candidate candidate, CandidateStatus cs);
}
