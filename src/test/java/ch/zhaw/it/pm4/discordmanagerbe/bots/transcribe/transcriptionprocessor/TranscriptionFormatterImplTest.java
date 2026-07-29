package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionformatter.TranscriptionFormatter;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionformatter.TranscriptionFormatterImpl;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SequenceTimelineEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranscriptionFormatterImplTest {

    private TranscriptionFormatter transcriptionFormatter;

    @BeforeEach
    void setUp() {
        transcriptionFormatter = new TranscriptionFormatterImpl();
    }

    @Test
    void formatTranscription_shouldReturnCorrectFormattedTranscription_whenValidInput() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                3661000, 3671000, 20000, "audio1.wav"));
        timeline.add(new SequenceTimelineEntry(2, "user2Id", "User2",
                7322000, 7332000, 18000, "audio2.wav"));

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "Hello, how are you?");
        transcriptions.put(2, "I'm doing fine, thanks!");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [01:01:01] **User1**: Hello, how are you?
                
                [02:02:02] **User2**: I'm doing fine, thanks!
                
                """;
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldSkipEmptyTranscriptions() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                3661000, 3671000, 20000, "audio1.wav"));
        timeline.add(new SequenceTimelineEntry(2, "user2Id", "User2",
                7322000, 7332000, 18000, "audio2.wav"));
        timeline.add(new SequenceTimelineEntry(3, "user3Id", "User3",
                10983000, 10993000, 22000, "audio3.wav"));

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "Hello, how are you?");
        transcriptions.put(2, ""); // Empty transcription
        transcriptions.put(3, "I'm doing great!");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [01:01:01] **User1**: Hello, how are you?
                
                [03:03:03] **User3**: I'm doing great!
                
                """;
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldSkipFailedTranscriptions() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                3661000, 3671000, 20000, "audio1.wav"));
        timeline.add(new SequenceTimelineEntry(2, "user2Id", "User2",
                7322000, 7332000, 18000, "audio2.wav"));

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "Hello, how are you?");
        transcriptions.put(2, "[Transcription failed]");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [01:01:01] **User1**: Hello, how are you?
                
                """;
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldHandleMissingTranscriptions() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                3661000, 3671000, 20000, "audio1.wav"));
        timeline.add(new SequenceTimelineEntry(2, "user2Id", "User2",
                7322000, 7332000, 18000, "audio2.wav"));
        timeline.add(new SequenceTimelineEntry(3, "user3Id", "User3",
                10983000, 10993000, 22000, "audio3.wav"));

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "Hello, how are you?");
        // Missing transcription for sequenceId 2
        transcriptions.put(3, "I'm doing great!");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [01:01:01] **User1**: Hello, how are you?
                
                [03:03:03] **User3**: I'm doing great!
                
                """;
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldHandleEmptyTimelineAndTranscriptions() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        Map<Integer, String> transcriptions = new HashMap<>();

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = "# Discord Conversation Transcript\n\n";
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldTrimTranscriptions() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                3661000, 3671000, 20000, "audio1.wav"));

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "  Text with spaces around  ");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [01:01:01] **User1**: Text with spaces around
                
                """;
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldFormatTimeCorrectly() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                59000, 69000, 5000, "audio1.wav")); // 00:00:59
        timeline.add(new SequenceTimelineEntry(2, "user2Id", "User2",
                60000, 70000, 6000, "audio2.wav")); // 00:01:00
        timeline.add(new SequenceTimelineEntry(3, "user3Id", "User3",
                3600000, 3610000, 7000, "audio3.wav")); // 01:00:00
        timeline.add(new SequenceTimelineEntry(4, "user4Id", "User4",
                3661000, 3671000, 8000, "audio4.wav")); // 01:01:01

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "Message at 59 seconds");
        transcriptions.put(2, "Message at 1 minute");
        transcriptions.put(3, "Message at 1 hour");
        transcriptions.put(4, "Message at 1 hour, 1 minute, 1 second");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [00:00:59] **User1**: Message at 59 seconds
                
                [00:01:00] **User2**: Message at 1 minute
                
                [01:00:00] **User3**: Message at 1 hour
                
                [01:01:01] **User4**: Message at 1 hour, 1 minute, 1 second
                
                """;
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldHandleMultipleMessagesFromSameUser() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                60000, 65000, 5000, "audio1.wav")); // 00:01:00
        timeline.add(new SequenceTimelineEntry(2, "user1Id", "User1",
                70000, 75000, 6000, "audio2.wav")); // 00:01:10
        timeline.add(new SequenceTimelineEntry(3, "user2Id", "User2",
                80000, 85000, 7000, "audio3.wav")); // 00:01:20

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "First message from User1");
        transcriptions.put(2, "Second message from User1");
        transcriptions.put(3, "Message from User2");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [00:01:00] **User1**: First message from User1
                
                [00:01:10] **User1**: Second message from User1
                
                [00:01:20] **User2**: Message from User2
                
                """;
        assertEquals(expected, result);
    }

    @Test
    void formatTranscription_shouldHandleSpecialCharactersInUsernames() {
        // Arrange
        List<SequenceTimelineEntry> timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User_1@Special",
                3661000, 3671000, 20000, "audio1.wav"));

        Map<Integer, String> transcriptions = new HashMap<>();
        transcriptions.put(1, "Message from user with special characters in name");

        // Act
        String result = transcriptionFormatter.formatTranscription(timeline, transcriptions);

        // Assert
        String expected = """
                # Discord Conversation Transcript
                
                [01:01:01] **User_1@Special**: Message from user with special characters in name
                
                """;
        assertEquals(expected, result);
    }
}