package de.ddm.structures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import com.opencsv.CSVWriter;

import akka.actor.Status;

public class CsvLogSink implements Sink {
    private final Instant begin;
    private final CSVWriter writer;

    private static String getTimestamp() {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return date.format(new Date());
    }

    public CsvLogSink(String csvFile) throws IOException {
        this.begin = Instant.now();
        this.writer = new CSVWriter(new FileWriter(new File(csvFile)));
        // header row
        this.writer.writeNext(new String[]{"timestamp", "attribute_a", "attribute_b", "is_valid", "reason", "comparision_count"});
    }

    public CsvLogSink() throws IOException {
        this("results-" + getTimestamp() + ".csv");
    }

    @Override
    public void putResult(Candidate candidate, CandidateStatus status) {
        this.writer.writeNext(new String[]{
            "" + (Instant.now().getEpochSecond() - this.begin.getEpochSecond()),
            candidate.getAttributeA().toString(),
            candidate.getAttributeB().toString(),
            "" + status.isValid(),
            status.getReason().toString(),
            "" + status.getComparsionCount()
        });
        this.writer.flushQuietly(); // dont do this on every write...
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