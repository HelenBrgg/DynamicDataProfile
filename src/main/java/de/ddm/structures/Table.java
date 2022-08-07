package de.ddm.structures;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.HashCodeExclude;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Contains:
 * - a list of values with their positions.
 * - the attribute that the values belong to.
 * - the ID of the worker that saves all values of that attribute
 * They have to be put to the column of the worker that saves the column.
 * 
 * @see ValueWithPosition
 */

@AllArgsConstructor
public class Table {
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Attribute {
        public String tableName;
        public String attribute;
    }

    /**
     * represents a column in a table
     * saves the values of one column in a list
     * 
     * @see Value
     *
     */
    @NoArgsConstructor
    public static class Column {
        public List<Value> values = new ArrayList<>();
    }

    public String name;
    public List<Attribute> attributes = new ArrayList<>();
    public List<Column> columns = new ArrayList<>();
    public List<Integer> positions = new ArrayList<>();

    public static Table parseCSV(String csvString, String tableName) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new StringReader(csvString));
        List<String[]> lines = reader.readAll();
        reader.close();

        List<Attribute> tableAttributes = List.of(lines.get(0)).stream()
            .map(attr -> new Attribute(tableName, attr))
            .collect(Collectors.toList());
        tableAttributes.remove(0); // remove position column

        List<Integer> tablePositions = new ArrayList<>();
        List<Column> tableColumns = Stream.generate(Column::new).limit(tableAttributes.size())
                .collect(Collectors.toList());

        for (int i = 1; i < lines.size(); i++) {
            tablePositions.add(Integer.parseInt(lines.get(i)[0]));
            for (int j = 1; j < lines.get(i).length; j++) {
                tableColumns.get(j - 1).values.add(Value.fromString(lines.get(i)[j]));
            }
        }
        return new Table(tableName, tableAttributes, tableColumns, tablePositions);
    }

    public IntStream streamPositions(){
        return this.positions.stream().mapToInt(i -> i);
    }

    public Stream<Value.WithPosition> streamColumnWithPositions(int colIndex, int rangeBegin, int rangeEndInclusive) {
        Column column = this.columns.get(colIndex);
        return IntStream.range(0, this.positions.size())
                .filter(i -> this.positions.get(i) > rangeBegin && this.positions.get(i) <= rangeEndInclusive)
                .mapToObj(i -> new Value.WithPosition(column.values.get(i), this.positions.get(i)));
    }
    public Stream<Value.WithPosition> streamColumnWithPositions(int colIndex){
        return this.streamColumnWithPositions(colIndex, 0, this.columns.get(colIndex).values.size());
    }
}
