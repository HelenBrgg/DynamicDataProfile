package de.ddm.structures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Strings;
import com.opencsv.exceptions.CsvException;

import de.ddm.singletons.InputConfigurationSingleton;

public class DataGeneratorSource implements Source {
    private final Logger logger;
    private String tableName;
    private final BufferedReader procReader;
    private boolean finished = false;

    public DataGeneratorSource(String[] env, String[] command) throws IOException {
        this.logger = LoggerFactory.getLogger("data-generator");

        // String[] command = InputConfigurationSingleton.get().getDataGeneratorCommands().get(0);
        // String[] env = InputConfigurationSingleton.get().getDataGeneratorEnv();

        this.logger.info("spawning data-generator process with: {} {}", Strings.join(" ", env), Strings.join(" ", command));

        this.tableName = Paths.get(command[1]).getFileName().toString().replaceFirst("[.][^.]+$", "");

        ProcessBuilder builder = new ProcessBuilder(List.of(command));
        builder.environment().put("CSV_SEPARATOR", ";"); // TODO
        builder.redirectErrorStream(true);

        Process proc = builder.start();
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
            String line = null;
            while ((line = procReader.readLine()) != null) {
                if (line.isEmpty())
                    break; // batch ended
                lines.add(line);
            }

            if (lines.size() < 2) {
                this.logger.info("closing data-generator process for {}", this.tableName);

                this.finished = true;
                this.procReader.close();
                return Optional.empty(); // not a full batch
            }

            this.logger.debug("read batch from {}", this.tableName);

            String csv = String.join("\n", lines);
            return Optional.of(Table.parseCSV(csv, this.tableName));
        } catch (IOException | CsvException e) {
            this.logger.error("failed to read table {}: {}", this.tableName, e.toString());

            this.finished = true;
            try { this.procReader.close(); } catch (IOException _e) {}
            return Optional.empty();
        }
    }
}