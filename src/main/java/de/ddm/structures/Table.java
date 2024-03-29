package de.ddm.structures;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvException;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
        // FIXME better way to supply separator?
        // CSVReader reader = new CSVReader(new StringReader(csvString));

        CSVReader reader =
            new CSVReaderBuilder(new StringReader(csvString))
                .withCSVParser(new CSVParserBuilder()
                    .withSeparator(';')
                    .build())
                .build();
                
        List<String[]> rawRows = reader.readAll();
        reader.close();

        Table table = new Table(tableName);

        table.attributes = Stream.of(rawRows.get(0))
            .skip(1) // skip position column
            .map(attr -> new Attribute(tableName, attr))
            .collect(Collectors.toList());
        try {
            table.positions = rawRows.stream()
                .skip(1) // skip header row
                .map(rawRow -> Integer.parseInt(rawRow[0]))
                .collect(Collectors.toList());
        } catch (NumberFormatException ex) {
            throw new IOException("Expected position cells of " + tableName + " to be integers: " + ex.getMessage() + "; TABLE\n" + csvString);
        }
        table.columns = Stream.generate(Column::new)
            .limit(table.attributes.size())
            .collect(Collectors.toList());

        for (int i = 1; i < rawRows.size(); ++i) { // each row
            String[] rawRow = rawRows.get(i);
            for (int j = 0; j < table.attributes.size(); j++) { // each column
                if (rawRow.length <= j + 1) {
                    assert false : "table " + tableName + ", attributes: " + table.attributes + ", row: " + String.join("|", rawRow);
                }
                Value value = Value.fromString(rawRow[j + 1]);
                table.columns.get(j).values.add(value);
            }
        }

        return table;
    }

    public IntStream streamPositions(){
        return this.positions.stream().mapToInt(i -> i);
    }

    public Stream<Value.WithPosition> streamColumnWithPositions(int colIndex){
        Column column = this.columns.get(colIndex);
        return IntStream.range(0, this.positions.size())
                .mapToObj(i -> new Value.WithPosition(column.values.get(i), this.positions.get(i)));
    }
}
