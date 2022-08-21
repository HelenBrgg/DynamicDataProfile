package de.ddm.structures;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CandidateGenerator {
    private Map<Table.Attribute, Metadata> attributes = new HashMap<>();
    private Map<Candidate, Optional<CandidateStatus>> candidates = new HashMap<>();

    public void updateCandidate(Candidate candidate, Optional<CandidateStatus> status) {
        this.candidates.put(candidate, status); // don't return because it may be used by others

        List<Candidate> resetCandidates = new ArrayList<>();

        this.candidates.forEach((implicatedCandidate, implicatedStatus) -> {
            if (implicatedStatus.isEmpty()) return;
            if (!(implicatedStatus.get().getReason() instanceof CandidateStatus.LogicalImplication)) return;

            CandidateStatus.LogicalImplication implication = (CandidateStatus.LogicalImplication) implicatedStatus.get().getReason();

            if (implication.involvedCandidates.remove(candidate)) {
                resetCandidates.add(implicatedCandidate);
            }
        });

        resetCandidates.forEach(resetCandidate -> this.updateCandidate(resetCandidate, Optional.empty()));
    }

    public void updateAttribute(Table.Attribute attr, boolean additions, boolean removals, Metadata meta){
        if (this.attributes.put(attr, meta) == null) return;

        List<Candidate> resetCandidates = new ArrayList<>();
        if (additions) {
            // additions only matter for A of A c B
            this.candidates.forEach((candidate, status) -> {
                if (status.isEmpty())
                    return; // candidate is still being checked
                if (!candidate.getAttributeA().equals(attr))
                    return; // attribute is not A of A c B
                if (status.get().getReason() instanceof CandidateStatus.LogicalImplication)
                    return; // don't need to recheck
                resetCandidates.add(candidate);
            });
        }
        if (removals) {
            // removals only matter for B of A c B
            this.candidates.forEach((candidate, status) -> {
                if (status.isEmpty())
                    return; // candidate is still being checked
                if (!candidate.getAttributeB().equals(attr))
                    return; // attribute is not B of A c B
                if (status.get().getReason() instanceof CandidateStatus.LogicalImplication)
                    return; // don't need to recheck
                resetCandidates.add(candidate);
            });
        }

        resetCandidates.forEach(resetCandidate -> this.updateCandidate(resetCandidate, Optional.empty()));
    }

    public Map<Candidate, Optional<CandidateStatus>> generateCandidates() {
        Map<Candidate, Optional<CandidateStatus>> returnCandidates = new HashMap<>();

        this.attributes.forEach((attrA, metaA) -> {
            this.attributes.forEach((attrB, metaB) -> {
                if (attrA == attrB) return;

                Candidate candidate = new Candidate(attrA, attrB);

                // we abort if the candidate has already been (successfully/unsuccessfully) validated
                if (this.candidates.getOrDefault(candidate, Optional.empty()).isPresent()) return;

                Optional<CandidateStatus> precheck = metaB.precheckPossibleSubset(metaA);
                this.candidates.put(candidate, precheck);
                returnCandidates.put(candidate, precheck);
            });
        });

        return returnCandidates;
    }

    public Set<Candidate> generateCandidatesUnpruned() {
        return this.generateCandidates().entrySet().stream()
            .filter(entry -> entry.getValue().isEmpty())
            .map(entry -> entry.getKey())
            .collect(Collectors.toSet());
    }
}
