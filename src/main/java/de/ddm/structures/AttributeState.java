package de.ddm.structures;

import de.ddm.structures.Table.Column;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AttributeState {
    public ColumnArray currentSegmentArray;
    public ColumnArray oldSegmentArray;
    public ColumnSet oldSegmentSet;
    public Metadata metadata = new Metadata();

    public AttributeState(ColumnArray.Factory arrayFactory, ColumnSet.Factory setFactory){
        this.currentSegmentArray = arrayFactory.create();
        this.oldSegmentArray = arrayFactory.create();
        this.oldSegmentSet = setFactory.create();
    }

    public SetDiff mergeSegments() {
        List<Value> oldValues = this.oldSegmentArray.setValues(this.currentSegmentArray.streamValues());

        Map<Value, Long> countDeltas = Utility.calulateCountDeltas(this.currentSegmentArray.streamValues().map(val -> val.value), oldValues.stream());
        Map<Value, Long> countTotals = this.oldSegmentSet.applyCountDeltas(countDeltas);

        SetDiff diff = Utility.calculateSetDiff(countDeltas, countTotals);
        this.metadata.update(this.oldSegmentSet, diff);
        return diff;
    }
}
