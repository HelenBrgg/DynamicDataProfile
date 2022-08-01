package de.ddm.profiler;

import java.util.Set;

public interface CandidateGenerator {
    public void addChange(String attribute, SetDiff change);

    public Set<Candidate> generateCandidates();

    public void updateCandidateStatus(Candidate candidate, SubsetCheckResult scr);
}
