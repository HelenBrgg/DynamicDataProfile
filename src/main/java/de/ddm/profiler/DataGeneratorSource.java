package de.ddm.profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataGeneratorSource implements Source {
    private final BufferedReader procReader;

    public DataGeneratorSource() throws IOException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = { "scripts/datagen.py", "data/TPCH", "-batch-size 1000", "--total-rows 10000" };
        Process proc = rt.exec(commands);
        procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    }

    @Override
    public Table nextTable() {
        List<String> lines = new ArrayList<>();

        try {
            while (true) {
                String line = procReader.readLine();
                if (line == null)
                    break; // process ended
                if (line.isEmpty())
                    break; // batch ended
                lines.add(line);
            }
            if (lines.size() < 2) {
                return null; // not a full batch
            }

            return Table.parseCSV(String.join("\n", lines), "T");
        } catch (Exception e) {
            System.out.println("ERROR: failed to read table: " + e.toString());
            return null;
        }
    }
}
