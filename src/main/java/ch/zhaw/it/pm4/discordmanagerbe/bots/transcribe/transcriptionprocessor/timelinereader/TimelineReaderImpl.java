package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.timelinereader;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SequenceTimelineEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the TimelineReader interface.
 * Reads and parses timeline data from CSV files to create sequence timeline entries.
 */
@Component
public class TimelineReaderImpl implements TimelineReader {

    /** the logger instance for this class */
    private static final Logger logger = LoggerFactory.getLogger(TimelineReaderImpl.class);

    /**
     * Reads a CSV timeline file and converts each row to a SequenceTimelineEntry.
     * Skips the header row and filters out invalid rows.
     *
     * @param timelineFile The CSV file containing timeline data
     * @return A list of sequence timeline entries parsed from the file
     * @throws IOException If an error occurs while reading the file
     */
    @Override
    public List<SequenceTimelineEntry> readTimelineFile(File timelineFile) throws IOException {
        logger.info("Reading timeline file: {}", timelineFile.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(timelineFile))) {
            return reader.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length >= 7)
                    .map(parts -> new SequenceTimelineEntry(
                            Integer.parseInt(parts[0]),
                            parts[1],
                            parts[2],
                            Long.parseLong(parts[3]),
                            Long.parseLong(parts[4]),
                            Integer.parseInt(parts[6]),
                            null))
                    .collect(Collectors.toList());
        }
    }
}
