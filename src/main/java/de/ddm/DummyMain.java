package de.ddm;

import de.ddm.profiler.*;
import java.util.*;
import java.util.stream.Collectors;

public class DummyMain {
        static Source createSource() {
                try {
                        Table table = Table.parseCSV(
                                        "$,Origin,Destination,Day\n" +
                                                        "0,Mercury,Venus,2\n" +
                                                        "1,Mercury,Venus,2\n" +
                                                        "2,Venus,Earth,3\n" +
                                                        "3,Earth,Earth,4\n" +
                                                        "4,Moon,Earth,2\n" +
                                                        "5,Mars,Moon,1",
                                        "test.csv");

                        Source source = new DummySource(new ArrayList<>(Arrays.asList(table)));
                        return source;
                } catch (Exception e) {
                        assert (false);
                }
                return null;
        }

        public static void main(String[] args) {
                Source source = createSource();
                InputSplitter inputSplitter = new InputSplitter(source, 1);

                Map<String, ColumnArray> columnArrays = new HashMap<>();
                Map<String, ColumnSet> columnSets = new HashMap<>();
                CandidateGenerator generator = new NaiveCandidateGenerator();

                Table nextTable = source.nextTable();
                while (nextTable != null) {
                        System.out.println("run mainloop");

                        /* initialize column-array and column-set if necessary */
                        for (String attribute : nextTable.attributes) {
                                columnArrays.putIfAbsent(attribute, new HeapColumnArray());
                                columnSets.putIfAbsent(attribute, new HeapColumnSet());
                        }

                        List<SetCommand> setCommands = inputSplitter.splitTable(nextTable);

                        /* process set commands */
                        for (SetCommand setCommand : setCommands) {
                                List<Value> changedValues = columnArrays.get(setCommand.attribute)
                                                .setValue(setCommand.values);

                                /* calculate count delta */
                                Map<Value, Long> decreasedCounts = changedValues.stream().collect(Collectors.groupingBy(
                                                v -> v, Collectors.counting()));
                                Map<Value, Long> deltaCounts = setCommand.values.stream().collect(Collectors.groupingBy(
                                                v -> v.value, Collectors.counting()));
                                for (Map.Entry<Value, Long> entry : decreasedCounts.entrySet()) {
                                        deltaCounts.merge(entry.getKey(), -entry.getValue(), (positiveCount,
                                                        negativeCount) -> positiveCount - negativeCount);
                                        // TODO remove if delta count is 0
                                }

                                Map<Value, Long> totalCounts = columnSets.get(setCommand.attribute)
                                                .applyCounts(deltaCounts);

                                Set<Value> addedValues = totalCounts.entrySet().stream()
                                                .filter(entry -> entry.getValue() == deltaCounts.get(entry.getKey()))
                                                .map(Map.Entry::getKey)
                                                .collect(Collectors.toSet());
                                Set<Value> removedValues = totalCounts.entrySet().stream()
                                                .filter(entry -> entry.getValue() == 0)
                                                .map(Map.Entry::getKey)
                                                .collect(Collectors.toSet());
                                SetChange change = new SetChange(addedValues, removedValues);

                                generator.addChange(setCommand.attribute, change);
                        }
                        nextTable = source.nextTable();
                }

                Set<Candidate> candidates = generator.generateCandidates();

                System.out.println("# Candidates");
                candidates.forEach(cand -> System.out.println(cand));

        }
}
