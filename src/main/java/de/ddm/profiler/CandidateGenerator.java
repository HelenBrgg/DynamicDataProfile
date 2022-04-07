package de.ddm.profiler;

import java.util.List;

public interface CandidateGenerator {
    public void addChange(String attribute, SetChange change);

    public List<Candidate> generateCandidates();

    public void updateCAndidateStatus(Candidate candidate, CandidateStatus cs);
}
