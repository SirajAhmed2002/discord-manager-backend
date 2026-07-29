package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.timelinereader;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SequenceTimelineEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface for reading timeline data from files.
 * Defines methods to read and parse speech sequence timeline information.
 */
public interface TimelineReader {

    /**
     * Reads a timeline file and converts it to a list of sequence timeline entries.
     *
     * @param timelineFile The CSV file containing timeline data
     * @return A list of sequence timeline entries parsed from the file
     * @throws IOException If an error occurs while reading the file
     */
    List<SequenceTimelineEntry> readTimelineFile(File timelineFile) throws IOException;
}
