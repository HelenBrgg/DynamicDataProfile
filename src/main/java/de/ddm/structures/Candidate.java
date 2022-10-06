package de.ddm.structures;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Candidate {
    private Table.Attribute attributeA;
    private Table.Attribute attributeB;

    @Override
    public String toString() {
        return this.attributeA.tableName + ".csv → " + this.attributeB.tableName + ".csv: " 
            + this.attributeA.attribute + " ⊆ " + this.attributeB.attribute;
    }
}
