package de.ddm.profiler;

import java.util.HashSet;
import java.util.Set;

public class NaiveCandidateGenerator implements CandidateGenerator {
    Set<String> attributes = new HashSet<>();

    @Override
    public void addChange(String attribute, SetChange change) {
        if (attributes.contains(attribute)) {
            allCandidates.removeIf(candidate -> {
                return candidate.referencedAttibute == attribute || candidate.dependentAttribute == attribute;
            });
        }
        this.attributes.add(attribute);
    }

    Set<Candidate> allCandidates = new HashSet<>();

    @Override
    public Set<Candidate> generateCandidates() {
        Set<Candidate> generatedCandidateSet = new HashSet<>();
        for (String firstAttribute : attributes) {
            for (String secondAttribute : attributes) {
                Candidate candidate = new Candidate(firstAttribute, secondAttribute);
                if (firstAttribute != secondAttribute && !allCandidates.contains(candidate)) {
                    generatedCandidateSet.add(candidate);
                }
            }
        }
        allCandidates.addAll(generatedCandidateSet);
        return generatedCandidateSet;
    }

    @Override
    public void updateCandidateStatus(Candidate candidate, CandidateStatus cs) {

        // TODO Auto-generated method stub

    }

}
