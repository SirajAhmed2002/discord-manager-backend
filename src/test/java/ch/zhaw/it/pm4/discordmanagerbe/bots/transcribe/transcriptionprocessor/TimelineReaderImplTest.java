package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.timelinereader.TimelineReader;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.timelinereader.TimelineReaderImpl;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SequenceTimelineEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimelineReaderImplTest {

    private TimelineReader timelineReader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        timelineReader = new TimelineReaderImpl();
    }

    @Test
    void testReadValidTimelineFile() throws IOException {
        // arrange
        File timelineFile = createValidTimelineFile();

        // act
        List<SequenceTimelineEntry> entries = timelineReader.readTimelineFile(timelineFile);

        // assert
        assertEquals(2, entries.size());

        SequenceTimelineEntry entry1 = entries.getFirst();
        assertEquals(1, entry1.getSequenceId());
        assertEquals("user1", entry1.getUserId());
        assertEquals("username1", entry1.getUsername());
        assertEquals(1000L, entry1.getStartTime());
        assertEquals(2000L, entry1.getEndTime());
        assertEquals(1024, entry1.getAudioDataSize());
        assertNull(entry1.getAudioFileName());

        SequenceTimelineEntry entry2 = entries.get(1);
        assertEquals(2, entry2.getSequenceId());
        assertEquals("user2", entry2.getUserId());
        assertEquals("username2", entry2.getUsername());
        assertEquals(2000L, entry2.getStartTime());
        assertEquals(3000L, entry2.getEndTime());
        assertEquals(2048, entry2.getAudioDataSize());
        assertNull(entry2.getAudioFileName());
    }

    @Test
    void testReadEmptyTimelineFile() throws IOException {
        // arrange
        File timelineFile = createEmptyTimelineFile();

        // act
        List<SequenceTimelineEntry> entries = timelineReader.readTimelineFile(timelineFile);

        // assert
        assertTrue(entries.isEmpty());
    }

    @Test
    void testReadTimelineFileWithOnlyHeader() throws IOException {
        // arrange
        File timelineFile = createTimelineFileWithOnlyHeader();

        // act
        List<SequenceTimelineEntry> entries = timelineReader.readTimelineFile(timelineFile);

        // assert
        assertTrue(entries.isEmpty());
    }

    @Test
    void testReadTimelineFileWithInvalidData() throws IOException {
        // arrange
        File timelineFile = createTimelineFileWithInvalidData();

        // act
        List<SequenceTimelineEntry> entries = timelineReader.readTimelineFile(timelineFile);

        // assert
        assertEquals(1, entries.size());
    }

    @Test
    void testReadNonExistentFile() {
        // arrange
        File nonExistentFile = new File(tempDir.toFile(), "nonexistent.csv");

        // act & assert
        assertThrows(IOException.class, () -> timelineReader.readTimelineFile(nonExistentFile));
    }

    @Test
    void testNumericParsingExceptions() throws IOException {
        // arrange
        File timelineFile = createTimelineFileWithParsingErrors();

        // act & assert
        assertThrows(NumberFormatException.class, () -> timelineReader.readTimelineFile(timelineFile));
    }

    private File createValidTimelineFile() throws IOException {
        File file = new File(tempDir.toFile(), "valid_timeline.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("sequenceId,userId,username,startTime,endTime,transcription,audioDataSize\n");
            writer.write("1,user1,username1,1000,2000,Hello World,1024\n");
            writer.write("2,user2,username2,2000,3000,Another message,2048\n");
        }
        return file;
    }

    private File createEmptyTimelineFile() throws IOException {
        File file = new File(tempDir.toFile(), "empty_timeline.csv");
        try (FileWriter writer = new FileWriter(file)) {
            // Leere Datei
        }
        return file;
    }

    private File createTimelineFileWithOnlyHeader() throws IOException {
        File file = new File(tempDir.toFile(), "header_only_timeline.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("sequenceId,userId,username,startTime,endTime,transcription,audioDataSize\n");
        }
        return file;
    }

    private File createTimelineFileWithInvalidData() throws IOException {
        File file = new File(tempDir.toFile(), "invalid_timeline.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("sequenceId,userId,username,startTime,endTime,transcription,audioDataSize\n");
            writer.write("1,user1,username1,1000,2000,Hello World,1024\n");
            writer.write("invalid line with not enough columns\n");
        }
        return file;
    }

    private File createTimelineFileWithParsingErrors() throws IOException {
        File file = new File(tempDir.toFile(), "parsing_error_timeline.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("sequenceId,userId,username,startTime,endTime,transcription,audioDataSize\n");
            writer.write("not_a_number,user1,username1,1000,2000,Hello World,1024\n");
        }
        return file;
    }
}