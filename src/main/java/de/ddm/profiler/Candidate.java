package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class Candidate {

    public String dependentAttribute;
    public String referencedAttibute;

    public String toString() {
        return dependentAttribute + " c " + referencedAttibute;
    }
}
