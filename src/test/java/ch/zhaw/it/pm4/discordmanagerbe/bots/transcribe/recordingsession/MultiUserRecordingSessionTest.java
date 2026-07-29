package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.TranscriptionProcessor;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SpeechSequenceEntry;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MultiUserRecordingSessionTest {

    @Mock
    private TranscriptionProcessor transcriptionProcessor;

    @Mock
    private SequenceManager sequenceManager;

    @Mock
    private AudioFileManager audioFileManager;

    @Mock
    private TemporaryResourceManager tempResourceManager;

    @Mock
    private AudioManager audioManager;

    @Mock
    private VoiceChannel voiceChannel;

    private MultiUserRecordingSession recordingSession;

    @BeforeEach
    public void setUp() {
        recordingSession = new MultiUserRecordingSession(
                transcriptionProcessor,
                sequenceManager,
                audioFileManager,
                tempResourceManager
        );
        recordingSession.setAudioManager(audioManager);
        recordingSession.setChannel(voiceChannel);
    }

    @Nested
    class StopRecordingTests {
        @Mock
        private MessageChannel textChannel;

        @Test
        public void testStopRecording_Success() throws Exception {
            // Arrange
            List<SpeechSequenceEntry> entries = new ArrayList<>();
            File tempDir = new File("/tmp/test");
            Map<Integer, File> sequenceFiles = new HashMap<>();
            File timelineFile = new File("/tmp/test/timeline.txt");

            when(sequenceManager.getSequenceEntries()).thenReturn(entries);
            when(tempResourceManager.createTemporaryDirectory()).thenReturn(tempDir);
            when(audioFileManager.saveTemporaryAudioFiles(entries, tempDir)).thenReturn(sequenceFiles);
            when(audioFileManager.createTemporaryTimelineFile(entries, tempDir)).thenReturn(timelineFile);

            // Act
            recordingSession.stopRecording(textChannel);

            // Assert
            verify(audioManager).setReceivingHandler(null);
            verify(sequenceManager).finalizeAllSequences();
            verify(sequenceManager).getSequenceEntries();
            verify(tempResourceManager).createTemporaryDirectory();
            verify(audioFileManager).saveTemporaryAudioFiles(entries, tempDir);
            verify(audioFileManager).createTemporaryTimelineFile(entries, tempDir);
            verify(sequenceManager).reset();
        }

        @Test
        public void testStopRecording_ErrorDuringProcessing() {
            // Arrange
            String errorMessage = "Failed to create directory";
            when(sequenceManager.getSequenceEntries()).thenThrow(new RuntimeException(errorMessage));

            // Act
            recordingSession.stopRecording(textChannel);

            // Assert
            verify(audioManager).setReceivingHandler(null);
            verify(sequenceManager).finalizeAllSequences();
        }

        @Test
        public void testStopRecording_ErrorInFinalizeSequences() {
            // Arrange
            String errorMessage = "Failed to finalize sequences";
            doThrow(new RuntimeException(errorMessage)).when(sequenceManager).finalizeAllSequences();

            // Act
            recordingSession.stopRecording(textChannel);

            // Assert
            verify(audioManager).setReceivingHandler(null);
        }

        @Test
        public void testStopRecording_ErrorInSavingAudioFiles() throws Exception {
            // Arrange
            List<SpeechSequenceEntry> entries = new ArrayList<>();
            File tempDir = new File("/tmp/test");
            String errorMessage = "Failed to save audio files";

            when(sequenceManager.getSequenceEntries()).thenReturn(entries);
            when(tempResourceManager.createTemporaryDirectory()).thenReturn(tempDir);
            when(audioFileManager.saveTemporaryAudioFiles(eq(entries), eq(tempDir)))
                    .thenThrow(new RuntimeException(errorMessage));

            // Act
            recordingSession.stopRecording(textChannel);

            // Assert
            verify(audioManager).setReceivingHandler(null);
            verify(sequenceManager).finalizeAllSequences();
            verify(sequenceManager).getSequenceEntries();
            verify(tempResourceManager).createTemporaryDirectory();
        }

        @Test
        public void testStopRecording_ErrorInCreatingTimelineFile() throws Exception {
            // Arrange
            List<SpeechSequenceEntry> entries = new ArrayList<>();
            File tempDir = new File("/tmp/test");
            Map<Integer, File> sequenceFiles = new HashMap<>();
            String errorMessage = "Failed to create timeline file";

            when(sequenceManager.getSequenceEntries()).thenReturn(entries);
            when(tempResourceManager.createTemporaryDirectory()).thenReturn(tempDir);
            when(audioFileManager.saveTemporaryAudioFiles(entries, tempDir)).thenReturn(sequenceFiles);
            when(audioFileManager.createTemporaryTimelineFile(eq(entries), eq(tempDir)))
                    .thenThrow(new RuntimeException(errorMessage));

            // Act
            recordingSession.stopRecording(textChannel);

            // Assert
            verify(audioManager).setReceivingHandler(null);
            verify(sequenceManager).finalizeAllSequences();
            verify(sequenceManager).getSequenceEntries();
            verify(tempResourceManager).createTemporaryDirectory();
            verify(audioFileManager).saveTemporaryAudioFiles(entries, tempDir);
        }
    }

    @Nested
    class HandleUserAudioTests {
        @Mock
        private UserAudio userAudio;

        @Mock
        private User user;

        @Mock
        private Member member;

        private static final String USER_ID = "123456789";
        private static final String USER_NAME = "TestUser";
        private static final long SILENCE_THRESHOLD_MS = 1500; // Same as in the class

        @BeforeEach
        public void setUpAudioTests() {
            lenient().when(userAudio.getUser()).thenReturn(user);
            lenient().when(user.getId()).thenReturn(USER_ID);
            lenient().when(member.getId()).thenReturn(USER_ID);
            lenient().when(member.getEffectiveName()).thenReturn(USER_NAME);
        }

        @Test
        public void testHandleUserAudio_ValidAudioFromUser() {
            // Arrange
            byte[] audioData = new byte[] {1, 2, 3, 4, 5};
            when(user.isBot()).thenReturn(false);
            when(userAudio.getAudioData(anyDouble())).thenReturn(audioData);
            when(voiceChannel.getMembers()).thenReturn(Collections.singletonList(member));

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Long> timestampCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<byte[]> audioDataCaptor = ArgumentCaptor.forClass(byte[].class);
            ArgumentCaptor<Long> thresholdCaptor = ArgumentCaptor.forClass(Long.class);

            verify(sequenceManager).processAudioData(
                    userIdCaptor.capture(),
                    usernameCaptor.capture(),
                    timestampCaptor.capture(),
                    audioDataCaptor.capture(),
                    thresholdCaptor.capture()
            );

            assertEquals(USER_ID, userIdCaptor.getValue());
            assertEquals(USER_NAME, usernameCaptor.getValue());
            assertTrue(timestampCaptor.getValue() >= 0);
            assertArrayEquals(audioData, audioDataCaptor.getValue());
            assertEquals(SILENCE_THRESHOLD_MS, thresholdCaptor.getValue());
        }

        @Test
        public void testHandleUserAudio_BotUser() {
            // Arrange
            when(user.isBot()).thenReturn(true);

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            verify(userAudio, never()).getAudioData(anyDouble());
            verify(sequenceManager, never()).processAudioData(
                    any(String.class), any(String.class), anyLong(), any(byte[].class), anyLong()
            );
        }

        @Test
        public void testHandleUserAudio_RecordingStopped() {
            // Arrange
            recordingSession.stopRecording(null);

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            verify(userAudio, never()).getAudioData(anyDouble());
            verify(sequenceManager, never()).processAudioData(
                    any(String.class), any(String.class), anyLong(), any(byte[].class), anyLong()
            );
        }

        @Test
        public void testHandleUserAudio_EmptyAudioData() {
            // Arrange
            byte[] audioData = new byte[0];
            when(user.isBot()).thenReturn(false);
            when(userAudio.getAudioData(anyDouble())).thenReturn(audioData);

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            verify(sequenceManager, never()).processAudioData(
                    any(String.class), any(String.class), anyLong(), any(byte[].class), anyLong()
            );
        }

        @Test
        public void testHandleUserAudio_NullAudioData() {
            // Arrange
            when(user.isBot()).thenReturn(false);
            when(userAudio.getAudioData(anyDouble())).thenReturn(null);

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            verify(sequenceManager, never()).processAudioData(
                    any(String.class), any(String.class), anyLong(), any(byte[].class), anyLong()
            );
        }

        @Test
        public void testHandleUserAudio_UserNotFoundInChannel() {
            // Arrange
            byte[] audioData = new byte[] {1, 2, 3, 4, 5};
            when(user.isBot()).thenReturn(false);
            when(userAudio.getAudioData(anyDouble())).thenReturn(audioData);
            when(voiceChannel.getMembers()).thenReturn(Collections.emptyList());

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
            verify(sequenceManager).processAudioData(
                    eq(USER_ID),
                    usernameCaptor.capture(),
                    anyLong(),
                    eq(audioData),
                    eq(SILENCE_THRESHOLD_MS)
            );

            assertEquals("Unknown", usernameCaptor.getValue());
        }

        @Test
        public void testHandleUserAudio_MultipleUsersInChannel() {
            // Arrange
            byte[] audioData = new byte[] {1, 2, 3, 4, 5};
            Member otherMember = mock(Member.class);
            when(otherMember.getId()).thenReturn("987654321");

            when(user.isBot()).thenReturn(false);
            when(userAudio.getAudioData(anyDouble())).thenReturn(audioData);
            when(voiceChannel.getMembers()).thenReturn(Arrays.asList(otherMember, member));

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            verify(sequenceManager).processAudioData(
                    eq(USER_ID),
                    eq(USER_NAME),
                    anyLong(),
                    eq(audioData),
                    eq(SILENCE_THRESHOLD_MS)
            );
        }

        @Test
        public void testHandleUserAudio_TimestampCalculation() {
            // Arrange
            byte[] audioData = new byte[] {1, 2, 3, 4, 5};
            when(user.isBot()).thenReturn(false);
            when(userAudio.getAudioData(anyDouble())).thenReturn(audioData);
            when(voiceChannel.getMembers()).thenReturn(Collections.singletonList(member));

            // Act
            recordingSession.handleUserAudio(userAudio);

            // Assert
            ArgumentCaptor<Long> timestampCaptor = ArgumentCaptor.forClass(Long.class);
            verify(sequenceManager).processAudioData(
                    any(String.class),
                    any(String.class),
                    timestampCaptor.capture(),
                    any(byte[].class),
                    anyLong()
            );

            assertTrue(timestampCaptor.getValue() >= 0);
        }
    }
}