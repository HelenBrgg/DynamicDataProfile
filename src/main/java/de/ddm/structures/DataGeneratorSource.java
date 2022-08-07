package de.ddm.structures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataGeneratorSource implements Source {
    private final BufferedReader procReader;
    private boolean finished = false;

    public DataGeneratorSource() throws IOException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = { "scripts/datagen.py", "data/TPCH", "-batch-size 1000", "--total-rows 10000" };
        Process proc = rt.exec(commands);
        procReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    }

    @Override
    public boolean isFinished(){
        return this.finished;
    }

    @Override
    public Optional<Table> nextTable() {
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
                this.finished = true;
                this.procReader.close();
                return Optional.empty(); // not a full batch
            }

            String csv = String.join("\n", lines);

            return Optional.of(Table.parseCSV(csv, "T"));
        } catch (Exception e) {
            System.out.println("ERROR: failed to read table: " + e.toString());

            this.finished = true;
            try { this.procReader.close(); } catch (IOException _e) {}
            return Optional.empty();
        }
    }
}
