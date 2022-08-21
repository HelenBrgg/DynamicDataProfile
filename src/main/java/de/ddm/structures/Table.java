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

public class Table {
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Attribute {
        public String tableName;
        public String attribute;

        @Override
        public String toString(){
            return String.format("%s[%s]", this.tableName, this.attribute);
        }
    }

    /**
     * represents a column in a table
     * saves the values of one column in a list
     * 
     * @see Value
     *
     */
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Column {
        public List<Value> values = new ArrayList<>();
    }

    public final String name;
    public List<Attribute> attributes = new ArrayList<>();
    public List<Column> columns = new ArrayList<>();
    public List<Integer> positions = new ArrayList<>();

    public Table(String name){
        this.name = name;
    }

    public static Table parseCSV(String csvString, String tableName) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new StringReader(csvString));
        List<String[]> rawRows = reader.readAll();
        reader.close();

        Table table = new Table(tableName);

        table.attributes = Stream.of(rawRows.get(0))
            .skip(1) // skip position column
            .map(attr -> new Attribute(tableName, attr))
            .collect(Collectors.toList());

        table.positions = rawRows.stream()
            .skip(1) // skip header row
            .map(rawRow -> Integer.parseInt(rawRow[0]))
            .collect(Collectors.toList());
        table.columns = Stream.generate(Column::new)
            .limit(table.attributes.size())
            .collect(Collectors.toList());

        for (int i = 1; i < rawRows.size(); i++) { // each row
            for (int j = 1; j < table.attributes.size(); j++) { // each column
                Value value = Value.fromString(rawRows.get(i)[j]);
                table.columns.get(j - 1).values.add(value);
            }
        }

        return table;
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

    public void insertValues(Attribute attribute, Stream<Value.WithPosition> values) {
        
    }
}
