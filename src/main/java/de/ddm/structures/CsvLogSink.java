package de.ddm.structures;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class CsvLogSink implements Sink {
    private final String beginTimestamp;
    private final Instant begin;
    private final CSVWriter writer;

    public CsvLogSink() throws IOException {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        this.beginTimestamp = date.format(new Date());

        this.begin = Instant.now();
        // File file = new File("live-results-" + this.beginTimestamp + ".csv";
        File file = new File("live-results.csv");
        file.createNewFile();
        this.writer = new CSVWriter(new FileWriter(file));
        // header row
        this.writer.writeNext(new String[]{"timestamp", "attribute_a", "attribute_b", "is_valid", "reason", "info"});
    }

    @Override
    public void putLiveResult(Candidate candidate, CandidateStatus status) {
        this.writer.writeNext(new String[]{
            "" + (Instant.now().getEpochSecond() - this.begin.getEpochSecond()),
            candidate.getAttributeA().toString(),
            candidate.getAttributeB().toString(),
            "" + status.isValid(),
            status.getReason().toString(),
            status.getReason().additionalInfo(),
        });
        this.writer.flushQuietly();
    }

    @Override
    public void putFinalResults(Map<Candidate, CandidateStatus> results){
        try {
            // File file = new File("final-results-" + this.beginTimestamp + ".txt");
            File file = new File("final-results.csv");
            file.createNewFile();
            PrintWriter finalWriter = new PrintWriter(new FileWriter(file));
            
            results.entrySet().stream()
                .filter(entry -> entry.getValue().isValid())
                .map(entry -> entry.getKey().toString())
                .sorted()
                .forEachOrdered(str -> finalWriter.println(str));

            finalWriter.flush();
            finalWriter.close();
        } catch (IOException err) {
            System.out.println("failed to write to csv log sink: " + err.toString());
        }
    }

    @Override
    public void finish() {
        try {
            this.writer.close();
        } catch (IOException err) {
            System.out.println("failed to close csv log sink: " + err.toString());
        }
    }
}
