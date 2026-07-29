package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.messagesender.MessageSender;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.timelinereader.TimelineReader;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionformatter.TranscriptionFormatter;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionservice.TranscriptionService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptionProcessorTest {

    @Mock
    private TimelineReader timelineReader;

    @Mock
    private TranscriptionService transcriptionService;

    @Mock
    private TranscriptionFormatter formatter;

    @Mock
    private MessageSender messageSender;

    @Mock
    private File timelineFile;

    @Mock
    private MessageChannel textChannel;

    @InjectMocks
    private TranscriptionProcessor transcriptionProcessor;

    private Map<Integer, File> sequenceFiles;
    private List<SequenceTimelineEntry> timeline;
    private Map<Integer, String> transcriptions;

    @BeforeEach
    void setUp() {
        // Vorbereitung der Test-Daten
        sequenceFiles = new HashMap<>();
        sequenceFiles.put(1, mock(File.class));
        sequenceFiles.put(2, mock(File.class));

        timeline = new ArrayList<>();
        timeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                1000L, 2000L, 1024, "audio1.wav"));
        timeline.add(new SequenceTimelineEntry(2, "user2Id", "User2",
                2000L, 3000L, 2048, "audio2.wav"));

        transcriptions = new HashMap<>();
        transcriptions.put(1, "Hallo, wie geht's?");
        transcriptions.put(2, "Mir geht es gut, danke!");
    }

    @Test
    void testTranscribeTemporaryFilesSuccess() throws IOException {
        // Arrange
        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(timeline);
        when(transcriptionService.transcribeSequenceFiles(sequenceFiles)).thenReturn(transcriptions);
        when(formatter.formatTranscription(any(), any())).thenReturn("Formatierte Transkription");

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);

        // Assert
        assertEquals("Formatierte Transkription", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService).transcribeSequenceFiles(sequenceFiles);
        verify(formatter).formatTranscription(argThat(list -> list.size() == 2), eq(transcriptions));
        verify(messageSender).sendTranscriptionToChannel(textChannel, "Formatierte Transkription");
    }

    @Test
    void testTranscribeTemporaryFilesWithNullTextChannel() throws IOException {
        // Arrange
        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(timeline);
        when(transcriptionService.transcribeSequenceFiles(sequenceFiles)).thenReturn(transcriptions);
        when(formatter.formatTranscription(any(), any())).thenReturn("Formatierte Transkription");

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, null);

        // Assert
        assertEquals("Formatierte Transkription", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService).transcribeSequenceFiles(sequenceFiles);
        verify(formatter).formatTranscription(argThat(list -> list.size() == 2), eq(transcriptions));
        // Verifiziere, dass messageSender nicht aufgerufen wird, wenn textChannel null ist
        verify(messageSender, never()).sendTranscriptionToChannel(any(), any());
    }

    @Test
    void testTranscribeTemporaryFilesWithTimelineReaderException() throws IOException {
        // Arrange
        when(timelineReader.readTimelineFile(timelineFile)).thenThrow(new RuntimeException("Timeline-Lesefehler"));

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);

        // Assert
        assertEquals("Error creating transcription: Timeline-Lesefehler", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService, never()).transcribeSequenceFiles(any());
        verify(formatter, never()).formatTranscription(any(), any());
        verify(messageSender, never()).sendTranscriptionToChannel(any(), any());
    }

    @Test
    void testTranscribeTemporaryFilesWithTranscriptionServiceException() throws IOException {
        // Arrange
        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(timeline);
        when(transcriptionService.transcribeSequenceFiles(sequenceFiles))
                .thenThrow(new RuntimeException("Transkriptionsfehler"));

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);

        // Assert
        assertEquals("Error creating transcription: Transkriptionsfehler", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService).transcribeSequenceFiles(sequenceFiles);
        verify(formatter, never()).formatTranscription(any(), any());
        verify(messageSender, never()).sendTranscriptionToChannel(any(), any());
    }

    @Test
    void testTranscribeTemporaryFilesWithFormatterException() throws IOException {
        // Arrange
        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(timeline);
        when(transcriptionService.transcribeSequenceFiles(sequenceFiles)).thenReturn(transcriptions);
        when(formatter.formatTranscription(any(), any()))
                .thenThrow(new RuntimeException("Formatierungsfehler"));

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);

        // Assert
        assertEquals("Error creating transcription: Formatierungsfehler", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService).transcribeSequenceFiles(sequenceFiles);
        verify(formatter).formatTranscription(argThat(list -> list.size() == 2), eq(transcriptions));
        verify(messageSender, never()).sendTranscriptionToChannel(any(), any());
    }

    @Test
    void testTranscribeTemporaryFilesWithMessageSenderException() throws IOException {
        // Arrange
        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(timeline);
        when(transcriptionService.transcribeSequenceFiles(sequenceFiles)).thenReturn(transcriptions);
        when(formatter.formatTranscription(any(), any())).thenReturn("Formatierte Transkription");
        doThrow(new RuntimeException("Sendefehler"))
                .when(messageSender).sendTranscriptionToChannel(any(), any());

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);

        // Assert
        assertEquals("Error creating transcription: Sendefehler", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService).transcribeSequenceFiles(sequenceFiles);
        verify(formatter).formatTranscription(argThat(list -> list.size() == 2), eq(transcriptions));
        verify(messageSender).sendTranscriptionToChannel(textChannel, "Formatierte Transkription");
    }

    @Test
    void testTimelineSorting() throws IOException {
        // Arrange - Timeline mit unsortierter Reihenfolge
        List<SequenceTimelineEntry> unsortedTimeline = new ArrayList<>();
        unsortedTimeline.add(new SequenceTimelineEntry(2, "user2Id", "User2",
                2000L, 3000L, 2048, "audio2.wav"));
        unsortedTimeline.add(new SequenceTimelineEntry(1, "user1Id", "User1",
                1000L, 2000L, 1024, "audio1.wav"));

        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(unsortedTimeline);
        when(transcriptionService.transcribeSequenceFiles(sequenceFiles)).thenReturn(transcriptions);
        when(formatter.formatTranscription(any(), any())).thenReturn("Formatierte Transkription");

        // Act
        transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);

        // Assert - Überprüfe, dass die sortierte Timeline an den Formatter übergeben wird
        verify(formatter).formatTranscription(argThat(list ->
                        list.size() == 2 &&
                                list.get(0).getSequenceId() == 1 &&
                                list.get(1).getSequenceId() == 2),
                eq(transcriptions));
    }

    @Test
    void testTimelineWithEmptySequenceFiles() throws IOException {
        // Arrange
        Map<Integer, File> emptySequenceFiles = new HashMap<>();
        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(timeline);
        when(transcriptionService.transcribeSequenceFiles(emptySequenceFiles)).thenReturn(new HashMap<>());
        when(formatter.formatTranscription(any(), any())).thenReturn("Leere Transkription");

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, emptySequenceFiles, textChannel);

        // Assert
        assertEquals("Leere Transkription", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService).transcribeSequenceFiles(emptySequenceFiles);
        verify(formatter).formatTranscription(eq(timeline), eq(new HashMap<>()));
        verify(messageSender).sendTranscriptionToChannel(textChannel, "Leere Transkription");
    }

    @Test
    void testTimelineWithEmptyTimeline() throws IOException {
        // Arrange
        List<SequenceTimelineEntry> emptyTimeline = new ArrayList<>();
        when(timelineReader.readTimelineFile(timelineFile)).thenReturn(emptyTimeline);
        when(transcriptionService.transcribeSequenceFiles(sequenceFiles)).thenReturn(transcriptions);
        when(formatter.formatTranscription(any(), any())).thenReturn("Transkription ohne Timeline");

        // Act
        String result = transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);

        // Assert
        assertEquals("Transkription ohne Timeline", result);
        verify(timelineReader).readTimelineFile(timelineFile);
        verify(transcriptionService).transcribeSequenceFiles(sequenceFiles);
        verify(formatter).formatTranscription(eq(emptyTimeline), eq(transcriptions));
        verify(messageSender).sendTranscriptionToChannel(textChannel, "Transkription ohne Timeline");
    }
}