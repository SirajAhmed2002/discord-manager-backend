package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SpeechSequenceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AudioFileManagerTest {

    private AudioFileManager audioFileManager;

    @BeforeEach
    void setUp() {
        audioFileManager = new AudioFileManager();
    }

    @Test
    void saveTemporaryAudioFiles_EmptyList_ReturnsEmptyMap(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> emptyEntries = new ArrayList<>();

        // When
        Map<Integer, File> result = audioFileManager.saveTemporaryAudioFiles(emptyEntries, tempDir.toFile());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void saveTemporaryAudioFiles_SingleEntry_CreatesOneFile(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> entries = new ArrayList<>();
        SpeechSequenceEntry entry = createTestEntry(1, "user1", "123456789",
                1000L, 2000L, createTestAudioData(1000));
        entries.add(entry);

        // When
        Map<Integer, File> result = audioFileManager.saveTemporaryAudioFiles(entries, tempDir.toFile());

        // Then
        assertEquals(1, result.size());
        assertTrue(result.containsKey(1));
        File audioFile = result.get(1);
        assertTrue(audioFile.exists());
        assertTrue(audioFile.getName().contains("seq_1"));
        assertTrue(audioFile.getName().endsWith(".wav"));
        assertTrue(Files.size(audioFile.toPath()) > 0);
    }

    @Test
    void saveTemporaryAudioFiles_MultipleEntries_CreatesMultipleFiles(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> entries = new ArrayList<>();
        entries.add(createTestEntry(1, "user1", "123", 1000L, 2000L, createTestAudioData(1000)));
        entries.add(createTestEntry(2, "user2", "456", 2000L, 3000L, createTestAudioData(500)));
        entries.add(createTestEntry(3, "user3", "789", 3000L, 4000L, createTestAudioData(1500)));

        // When
        Map<Integer, File> result = audioFileManager.saveTemporaryAudioFiles(entries, tempDir.toFile());

        // Then
        assertEquals(3, result.size());
        for (int i = 1; i <= 3; i++) {
            assertTrue(result.containsKey(i));
            File audioFile = result.get(i);
            assertTrue(audioFile.exists());
            assertTrue(audioFile.getName().contains("seq_" + i));
            assertTrue(audioFile.getName().endsWith(".wav"));
        }
    }

    @Test
    void saveTemporaryAudioFiles_EntriesWithEmptyAudioData_SkipsEmptyEntries(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> entries = new ArrayList<>();
        entries.add(createTestEntry(1, "user1", "123", 1000L, 2000L, createTestAudioData(1000)));
        entries.add(createTestEntry(2, "user2", "456", 2000L, 3000L, new byte[0])); // Empty audio data
        entries.add(createTestEntry(3, "user3", "789", 3000L, 4000L, createTestAudioData(500)));

        // When
        Map<Integer, File> result = audioFileManager.saveTemporaryAudioFiles(entries, tempDir.toFile());

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1));
        assertTrue(result.containsKey(3));
        assertFalse(result.containsKey(2)); // No file created for entry with empty audio data
    }

    @Test
    void createTemporaryTimelineFile_EmptyList_CreatesEmptyTimelineFile(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> emptyEntries = new ArrayList<>();

        // When
        File timelineFile = audioFileManager.createTemporaryTimelineFile(emptyEntries, tempDir.toFile());

        // Then
        assertTrue(timelineFile.exists());
        assertTrue(timelineFile.getName().startsWith("timeline_"));
        assertTrue(timelineFile.getName().endsWith(".csv"));

        // Read file content and verify header only
        List<String> lines = Files.readAllLines(timelineFile.toPath());
        assertEquals(1, lines.size()); // Should contain only the header
        assertEquals("SequenceId,UserId,Username,StartTime,EndTime,DurationMs,AudioDataSize", lines.get(0));
    }

    @Test
    void createTemporaryTimelineFile_SingleEntry_CreatesTimelineWithOneDataRow(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> entries = new ArrayList<>();
        entries.add(createTestEntry(1, "123", "user1", 1000L, 2000L, new byte[500]));

        // When
        File timelineFile = audioFileManager.createTemporaryTimelineFile(entries, tempDir.toFile());

        // Then
        assertTrue(timelineFile.exists());

        // Read file content and verify
        List<String> lines = Files.readAllLines(timelineFile.toPath());
        assertEquals(2, lines.size()); // Header + 1 data row
        assertEquals("SequenceId,UserId,Username,StartTime,EndTime,DurationMs,AudioDataSize", lines.get(0));

        // Verify data row format
        String dataRow = lines.get(1);
        assertTrue(dataRow.startsWith("1,123,user1,1000,2000,1000,500"));
    }

    @Test
    void createTemporaryTimelineFile_MultipleEntries_CreatesTimelineWithMultipleRows(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> entries = new ArrayList<>();
        entries.add(createTestEntry(1, "123", "user1", 1000L, 2000L, new byte[500]));
        entries.add(createTestEntry(2, "456", "user2", 2100L, 3000L, new byte[800]));
        entries.add(createTestEntry(3, "789", "user3", 3100L, 4000L, new byte[1200]));

        // When
        File timelineFile = audioFileManager.createTemporaryTimelineFile(entries, tempDir.toFile());

        // Then
        assertTrue(timelineFile.exists());

        // Read file content and verify
        List<String> lines = Files.readAllLines(timelineFile.toPath());
        assertEquals(4, lines.size()); // Header + 3 data rows

        // Check each data row contains expected values
        assertTrue(lines.get(1).startsWith("1,123,user1,1000,2000,1000,500"));
        assertTrue(lines.get(2).startsWith("2,456,user2,2100,3000,900,800"));
        assertTrue(lines.get(3).startsWith("3,789,user3,3100,4000,900,1200"));
    }

    @Test
    void createTemporaryTimelineFile_EntriesWithDifferentTimestamps_SortsEntriesByStartTime(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> entries = new ArrayList<>();
        // Adding entries in non-chronological order
        entries.add(createTestEntry(2, "456", "user2", 5000L, 6000L, new byte[500]));
        entries.add(createTestEntry(1, "123", "user1", 1000L, 2000L, new byte[500]));
        entries.add(createTestEntry(3, "789", "user3", 3000L, 4000L, new byte[500]));

        // When
        File timelineFile = audioFileManager.createTemporaryTimelineFile(entries, tempDir.toFile());

        // Then
        assertTrue(timelineFile.exists());

        // Read file content and verify entries are sorted by startTime
        List<String> lines = Files.readAllLines(timelineFile.toPath());
        assertEquals(4, lines.size()); // Header + 3 data rows

        // Check that entries are sorted by startTime (not by sequenceId)
        assertTrue(lines.get(1).contains("1,123,user1,1000"));
        assertTrue(lines.get(2).contains("3,789,user3,3000"));
        assertTrue(lines.get(3).contains("2,456,user2,5000"));
    }

    @Test
    void createTemporaryTimelineFile_SpecialCharactersInUsername_HandlesCorrectly(@TempDir Path tempDir) throws Exception {
        // Given
        List<SpeechSequenceEntry> entries = new ArrayList<>();
        entries.add(createTestEntry(1, "123", "user,with\"commas\"", 1000L, 2000L, new byte[500]));

        // When
        File timelineFile = audioFileManager.createTemporaryTimelineFile(entries, tempDir.toFile());

        // Then
        assertTrue(timelineFile.exists());

        // Read file content and verify
        List<String> lines = Files.readAllLines(timelineFile.toPath());
        assertEquals(2, lines.size()); // Header + 1 data row

        // Check that CSV special characters are handled properly
        String dataRow = lines.get(1);
        assertTrue(dataRow.contains("user,with\"commas\"") ||
                dataRow.contains("\"user,with\"\"commas\"\"\""));  // Depending on CSV escaping implementation
    }


    // Helper method to create a test entry with constructor
    private SpeechSequenceEntry createTestEntry(int sequenceId, String userId, String username,
                                                long startTime, long endTime, byte[] audioData) {
        return new SpeechSequenceEntry(sequenceId, userId, username, startTime, endTime, audioData);
    }

    // Helper method to create test audio data
    private byte[] createTestAudioData(int size) {
        byte[] data = new byte[size];
        // Fill with some sample data
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 256);
        }
        return data;
    }
}