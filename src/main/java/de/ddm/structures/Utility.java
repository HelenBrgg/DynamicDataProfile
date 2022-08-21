package de.ddm.structures;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO move these methods into the appropriate classes, if possible
public abstract class Utility {
    public static Map<Value, Long> calulateCountDeltas(Stream<Value> newValues, Stream<Value> oldValues) {
        Map<Value, Long> decreasedCounts = oldValues
                .filter(val -> !val.equals(Value.EMPTY))
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));
        Map<Value, Long> deltaCounts = newValues
                .filter(val -> !val.equals(Value.EMPTY))
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        for (Map.Entry<Value, Long> entry : decreasedCounts.entrySet()) {
            deltaCounts.merge(
                    entry.getKey(),
                    -entry.getValue(),
                    (positiveCount, negativeCount) -> positiveCount - negativeCount);
        }

        deltaCounts.values().removeIf(count -> count == 0);

        return deltaCounts;
    }

    public static SetDiff calculateSetDiff(Map<Value, Long> deltaCounts, Map<Value, Long> totalCounts) {
        Set<Value> addedValues = totalCounts.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(Value.EMPTY))
                .filter(entry -> entry.getValue() == deltaCounts.get(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<Value> removedValues = totalCounts.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(Value.EMPTY))
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        return new SetDiff(addedValues, removedValues);
    }
}
