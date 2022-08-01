package de.ddm.profiler;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Utility {
    public static Map<Value, Long> calulateValueCountDelta(List<ValueWithPosition> newValues, List<Value> oldValues) {
        Map<Value, Long> decreasedCounts = oldValues.stream().collect(Collectors.groupingBy(
                v -> v, Collectors.counting()));
        Map<Value, Long> deltaCounts = newValues.stream().collect(Collectors.groupingBy(
                v -> v.value, Collectors.counting()));

        for (Map.Entry<Value, Long> entry : decreasedCounts.entrySet()) {
            deltaCounts.merge(
                    entry.getKey(),
                    -entry.getValue(),
                    (positiveCount, negativeCount) -> positiveCount - negativeCount);
            // TODO remove if delta count is 0?
        }

        return deltaCounts;
    }

    public static SetDiff calculateSetDiff(Map<Value, Long> deltaCounts, Map<Value, Long> totalCounts) {
        Set<Value> addedValues = totalCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == deltaCounts.get(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<Value> removedValues = totalCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return new SetDiff(addedValues, removedValues);
    }
}
