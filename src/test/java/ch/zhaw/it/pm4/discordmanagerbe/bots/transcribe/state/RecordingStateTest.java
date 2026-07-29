package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordingStateTest {

    @Mock
    private TranscribeBot mockBot;

    @Mock
    private SlashCommandInteractionEvent mockEvent;

    @Mock
    private Member mockMember;

    @Mock
    private GuildVoiceState mockVoiceState;

    @Mock
    private AudioChannelUnion mockAudioChannelUnion;

    @Mock
    private VoiceChannel mockVoiceChannel;

    @Mock
    private MessageChannelUnion mockMessageChannelUnion;

    @Mock
    private ReplyCallbackAction mockReplyAction;

    @Mock
    private TranscribeBot.RecordingSession mockRecordingSession;

    private RecordingState recordingState;
    private Map<String, TranscribeBot.RecordingSession> mockActiveSessions;
    private Map<String, Set<String>> mockPendingAcceptances;
    private Map<String, Set<String>> mockAcceptedUsers;

    private static final String CHANNEL_ID = "123456789";
    private static final String SUCCESS_MESSAGE = "Recording stopped successfully";
    private static final String NO_RECORDING_MESSAGE = "No active recording";

    @BeforeEach
    void setUp() {
        recordingState = new RecordingState(mockBot);

        // Initialize mock collections
        mockActiveSessions = new ConcurrentHashMap<>();
        mockPendingAcceptances = new ConcurrentHashMap<>();
        mockAcceptedUsers = new ConcurrentHashMap<>();

        InteractionHook mockHook = mock(InteractionHook.class);
        lenient().when(mockEvent.getHook()).thenReturn(mockHook);

        WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
        lenient().when(mockHook.sendMessage(anyString())).thenReturn(mockAction);
        lenient().when(mockAction.setEphemeral(true)).thenReturn(mockAction);

        // Setup bot mocks
        lenient().when(mockBot.getActiveSessions()).thenReturn(mockActiveSessions);
        lenient().when(mockBot.getPendingAcceptances()).thenReturn(mockPendingAcceptances);
        lenient().when(mockBot.getAcceptedUsers()).thenReturn(mockAcceptedUsers);
        lenient().when(mockBot.getConnectedState()).thenReturn(mock(ConnectedState.class));

        // Setup event mocks
        lenient().when(mockEvent.getMember()).thenReturn(mockMember);
        lenient().when(mockEvent.getChannel()).thenReturn(mockMessageChannelUnion);
        lenient().when(mockEvent.reply(anyString())).thenReturn(mockReplyAction);
        lenient().when(mockReplyAction.setEphemeral(anyBoolean())).thenReturn(mockReplyAction);

        // Setup member and voice channel mocks
        lenient().when(mockMember.getVoiceState()).thenReturn(mockVoiceState);
        lenient().when(mockVoiceState.inAudioChannel()).thenReturn(true);
        lenient().when(mockVoiceState.getChannel()).thenReturn(mockAudioChannelUnion);
        lenient().when(mockAudioChannelUnion.asVoiceChannel()).thenReturn(mockVoiceChannel);
        lenient().when(mockVoiceChannel.getId()).thenReturn(CHANNEL_ID);
    }

    @Test
    void stopRecording_SuccessfulStopRecording_TransitionsToConnectedState() {
        // Arrange
        mockActiveSessions.put(CHANNEL_ID, mockRecordingSession);
        mockPendingAcceptances.put(CHANNEL_ID, new HashSet<>());
        mockAcceptedUsers.put(CHANNEL_ID, new HashSet<>());

        // Act
        recordingState.stopRecording(mockEvent);

        // Assert
        verify(mockRecordingSession).stopRecording(mockMessageChannelUnion);
        verify(mockBot).setState(mockBot.getPendingApprovalState());

        // Verify cleanup
        assert mockActiveSessions.isEmpty();
        assert mockPendingAcceptances.isEmpty();
        assert mockAcceptedUsers.isEmpty();
    }

    @Test
    void stopRecording_MemberNotInVoiceChannel_ReturnsEarly() {
        // Arrange
        when(mockVoiceState.inAudioChannel()).thenReturn(false);

        // Act
        recordingState.stopRecording(mockEvent);

        // Assert
        // Verify no recording operations were performed
        verify(mockRecordingSession, never()).stopRecording(any());
        verify(mockBot, never()).setState(any());
    }

    @Test
    void stopRecording_NoActiveRecording_SendsErrorMessage() {
        // Act
        recordingState.stopRecording(mockEvent);

        // Assert
        // Verify no recording operations were performed
        verify(mockRecordingSession, never()).stopRecording(any());
        verify(mockBot, never()).setState(any());
    }

    @Test
    void stopRecording_ExceptionDuringStopRecording_SendsErrorMessage() {
        // Arrange
        mockActiveSessions.put(CHANNEL_ID, mockRecordingSession);
        RuntimeException testException = new RuntimeException("Test exception");
        doThrow(testException).when(mockRecordingSession).stopRecording(mockMessageChannelUnion);

        // Act
        recordingState.stopRecording(mockEvent);

        // Assert
        // Verify state was not changed
        verify(mockBot, never()).setState(any());
    }

    @Test
    void stopRecording_MultipleChannelDataCleanup_CleansAllData() {
        // Arrange
        mockActiveSessions.put(CHANNEL_ID, mockRecordingSession);

        Set<String> pendingUsers = new HashSet<>();
        pendingUsers.add("user1");
        pendingUsers.add("user2");
        mockPendingAcceptances.put(CHANNEL_ID, pendingUsers);

        Set<String> acceptedUsers = new HashSet<>();
        acceptedUsers.add("user3");
        acceptedUsers.add("user4");
        mockAcceptedUsers.put(CHANNEL_ID, acceptedUsers);

        // Act
        recordingState.stopRecording(mockEvent);

        // Assert - All data should be cleaned up
        assert !mockActiveSessions.containsKey(CHANNEL_ID);
        assert !mockPendingAcceptances.containsKey(CHANNEL_ID);
        assert !mockAcceptedUsers.containsKey(CHANNEL_ID);
    }

    @Test
    void stopRecording_ValidatesRecordingSessionExists_BeforeProcessing() {
        // Arrange
        mockActiveSessions.put("different_channel", mockRecordingSession); // Different channel

        // Act
        recordingState.stopRecording(mockEvent);

        // Assert
        // Verify the recording session for different channel was not affected
        verify(mockRecordingSession, never()).stopRecording(any());
        assert mockActiveSessions.containsKey("different_channel");
    }

    @Test
    void stopRecording_PartialCleanupOnException_DoesNotChangeState() {
        // Arrange
        mockActiveSessions.put(CHANNEL_ID, mockRecordingSession);
        mockPendingAcceptances.put(CHANNEL_ID, new HashSet<>());
        mockAcceptedUsers.put(CHANNEL_ID, new HashSet<>());

        RuntimeException testException = new RuntimeException("Cleanup failed");
        doThrow(testException).when(mockRecordingSession).stopRecording(mockMessageChannelUnion);

        // Act
        recordingState.stopRecording(mockEvent);

        // Assert
        // Verify state transition did not occur
        verify(mockBot, never()).setState(any());

        // Original data should still be present since cleanup didn't complete
        assert mockActiveSessions.containsKey(CHANNEL_ID);
        assert mockPendingAcceptances.containsKey(CHANNEL_ID);
        assert mockAcceptedUsers.containsKey(CHANNEL_ID);
    }
}