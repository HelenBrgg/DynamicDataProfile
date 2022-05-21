package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

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
    public String name;
    public List<String> attributes = new ArrayList<>();
    public List<Column> columns = new ArrayList<>();
    public List<Integer> positions = new ArrayList<>();

    public static Table parseCSV(String csvString, String tableName) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new StringReader(csvString));
        List<String[]> lines = reader.readAll();
        reader.close();

        List<String> tableAttributes = new ArrayList<>(Arrays.asList(lines.get(0)));
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

    public Stream<ValueWithPosition> streamColumnWithPositions(int colIndex, int rangeBegin, int rangeEndInclusive) {
        Column column = this.columns.get(colIndex);
        return IntStream.range(0, this.positions.size())
                .filter(i -> this.positions.get(i) > rangeBegin && this.positions.get(i) <= rangeEndInclusive)
                .mapToObj(i -> new ValueWithPosition(column.values.get(i), this.positions.get(i)));
    }
}
