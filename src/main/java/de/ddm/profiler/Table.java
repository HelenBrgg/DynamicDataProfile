package de.ddm.profiler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Table {
    public String name;
    public List<String> attributes;
    public List<Column> columns;
    public List<Integer> positions = new ArrayList<>();

    public static Table parseCSV(String csvString, String tableName) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new StringReader(csvString));
        List<String[]> lines = reader.readAll();
        List<Integer> tablePositions = new ArrayList<>();
        List<String> tableAttributes = new ArrayList<>();
        List<Column> tableColumns = new ArrayList<>();

        tableAttributes = Arrays.asList(lines.get(0));

        for (int i = 1; i < lines.size(); i++) {
            tablePositions.add(Integer.parseInt(lines.get(i)[0]));
            for (int j = 1; j < lines.get(i).length; j++) {
                if (i == 1) {
                    tableColumns.add(new Column(new ArrayList<>()));
                }
                tableColumns.get(j - 1).values.add(Value.fromOriginal(lines.get(i)[j]));
            }
        }
        reader.close();
        return new Table(tableName, tableAttributes, tableColumns, tablePositions);
    }
}