package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectedStateTest {

    @Mock
    private TranscribeBot mockBot;

    @Mock
    private SlashCommandInteractionEvent mockEvent;

    @Mock
    private Member mockMember;

    @Mock
    private VoiceChannel mockVoiceChannel;

    @Mock
    private AudioManager mockAudioManager;

    @Mock
    private ReplyCallbackAction mockReplyAction;

    @Mock
    private PendingApprovalState mockPendingApprovalState;

    @Mock
    private DisconnectedState mockDisconnectedState;

    private ConnectedState connectedState;

    @BeforeEach
    void setUp() {
        connectedState = new ConnectedState(mockBot);
    }

    @Test
    void leaveVoiceChannel_shouldLeaveSuccessfully_whenConnectedToChannel() {
        // Arrange
        when(mockBot.getActiveAudioManager()).thenReturn(mockAudioManager);
        when(mockAudioManager.isConnected()).thenReturn(true);
        when(mockBot.getDisconnectedState()).thenReturn(mockDisconnectedState);

        // Mock InteractionHook
        InteractionHook mockHook = mock(InteractionHook.class);
        when(mockEvent.getHook()).thenReturn(mockHook);

        WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
        when(mockHook.sendMessage(anyString())).thenReturn(mockAction);

        // Act
        connectedState.leaveVoiceChannel(mockEvent);

        // Assert
        verify(mockAudioManager).closeAudioConnection();
        verify(mockBot).setActiveAudioManager(null);
        verify(mockBot).setState(mockDisconnectedState);
        verify(mockAction).queue();
    }

    @Test
    void leaveVoiceChannel_shouldDoNothing_whenAudioManagerIsNull() {
        // Arrange
        when(mockBot.getActiveAudioManager()).thenReturn(null);

        // Act
        connectedState.leaveVoiceChannel(mockEvent);

        // Assert
        verify(mockBot, never()).setActiveAudioManager(any());
        verify(mockBot, never()).setState(any());
        verify(mockEvent, never()).reply(anyString());
    }

    @Test
    void leaveVoiceChannel_shouldDoNothing_whenNotConnected() {
        // Arrange
        when(mockBot.getActiveAudioManager()).thenReturn(mockAudioManager);
        when(mockAudioManager.isConnected()).thenReturn(false);

        // Act
        connectedState.leaveVoiceChannel(mockEvent);

        // Assert
        verify(mockAudioManager, never()).closeAudioConnection();
        verify(mockBot, never()).setActiveAudioManager(any());
        verify(mockBot, never()).setState(any());
        verify(mockEvent, never()).reply(anyString());
    }

    @Test
    void leaveVoiceChannel_shouldDoNothing_whenAudioManagerIsNullAndNotConnected() {
        // Arrange
        when(mockBot.getActiveAudioManager()).thenReturn(mockAudioManager);
        when(mockAudioManager.isConnected()).thenReturn(false);

        // Act
        connectedState.leaveVoiceChannel(mockEvent);

        // Assert
        verify(mockAudioManager, never()).closeAudioConnection();
        verify(mockBot, never()).setActiveAudioManager(any());
        verify(mockBot, never()).setState(any());
        verify(mockEvent, never()).reply(anyString());
    }

    @Test
    void lockVoiceChannel_Success_ShouldLockChannelRequestPermissionAndTransitionState() {
        // Arrange
        String expectedMessage = "Channel locked, approval needed";

        when(mockEvent.getMember()).thenReturn(mockMember);
        when(mockBot.getPendingApprovalState()).thenReturn(mockPendingApprovalState);

        ConnectedState spyConnectedState = spy(connectedState);

        // Mock the getAndValidateMemberVoiceChannel method to return a valid channel
        doReturn(mockVoiceChannel).when(spyConnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);

        // Mock the helper methods
        doNothing().when(spyConnectedState).lockChannel(mockVoiceChannel);
        doNothing().when(spyConnectedState).requestRecordingPermission(mockEvent, mockVoiceChannel);

        try (MockedStatic<BotMessages> mockedBotMessages = mockStatic(BotMessages.class)) {
            mockedBotMessages.when(() -> BotMessages.get(MessageKey.CHANNEL_LOCKED_APPROVAL_NEEDED))
                    .thenReturn(expectedMessage);

            // Act
            spyConnectedState.lockVoiceChannel(mockEvent);

            // Assert
            verify(spyConnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);
            verify(spyConnectedState).lockChannel(mockVoiceChannel);
            verify(spyConnectedState).requestRecordingPermission(mockEvent, mockVoiceChannel);
            verify(mockBot).setState(mockPendingApprovalState);
        }
    }

    @Test
    void lockVoiceChannel_InvalidVoiceChannel_ShouldNotProceedWithLocking() {
        // Arrange
        when(mockEvent.getMember()).thenReturn(mockMember);

        ConnectedState spyConnectedState = spy(connectedState);
        // Mock getAndValidateMemberVoiceChannel to return null (invalid channel)
        doReturn(null).when(spyConnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);

        // Act
        spyConnectedState.lockVoiceChannel(mockEvent);

        // Assert
        verify(spyConnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);

        // Verify that no further actions are taken
        verify(spyConnectedState, never()).lockChannel(any());
        verify(spyConnectedState, never()).requestRecordingPermission(any(), any());
        verify(mockBot, never()).setState(any());
        verify(mockEvent, never()).reply(anyString());
    }

    @Test
    void lockVoiceChannel_NullMember_ShouldHandleGracefully() {
        // Arrange
        when(mockEvent.getMember()).thenReturn(null);

        ConnectedState spyConnectedState = spy(connectedState);
        doReturn(null).when(spyConnectedState).getAndValidateMemberVoiceChannel(null, mockEvent);

        // Act
        spyConnectedState.lockVoiceChannel(mockEvent);

        // Assert
        verify(spyConnectedState).getAndValidateMemberVoiceChannel(null, mockEvent);
        verify(spyConnectedState, never()).lockChannel(any());
        verify(spyConnectedState, never()).requestRecordingPermission(any(), any());
        verify(mockBot, never()).setState(any());
    }

    @Test
    void lockVoiceChannel_LockChannelThrowsException_ShouldNotTransitionState() {
        // Arrange
        when(mockEvent.getMember()).thenReturn(mockMember);

        ConnectedState spyConnectedState = spy(connectedState);
        doReturn(mockVoiceChannel).when(spyConnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);
        doThrow(new RuntimeException("Lock failed")).when(spyConnectedState).lockChannel(mockVoiceChannel);

        // Act & Assert
        try {
            spyConnectedState.lockVoiceChannel(mockEvent);
        } catch (RuntimeException e) {
            // Expected exception
        }

        verify(spyConnectedState).lockChannel(mockVoiceChannel);
        // Verify that state transition and permission request don't happen after exception
        verify(spyConnectedState, never()).requestRecordingPermission(any(), any());
        verify(mockBot, never()).setState(any());
        verify(mockEvent, never()).reply(anyString());
    }

    @Test
    void lockVoiceChannel_MultipleCallsWithSameParameters_ShouldBehaveConsistently() {
        // Arrange
        String expectedMessage = "Channel locked, approval needed";

        when(mockEvent.getMember()).thenReturn(mockMember);
        when(mockBot.getPendingApprovalState()).thenReturn(mockPendingApprovalState);

        ConnectedState spyConnectedState = spy(connectedState);
        doReturn(mockVoiceChannel).when(spyConnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);
        doNothing().when(spyConnectedState).lockChannel(mockVoiceChannel);
        doNothing().when(spyConnectedState).requestRecordingPermission(mockEvent, mockVoiceChannel);

        try (MockedStatic<BotMessages> mockedBotMessages = mockStatic(BotMessages.class)) {
            mockedBotMessages.when(() -> BotMessages.get(MessageKey.CHANNEL_LOCKED_APPROVAL_NEEDED))
                    .thenReturn(expectedMessage);

            // Act
            spyConnectedState.lockVoiceChannel(mockEvent);
            spyConnectedState.lockVoiceChannel(mockEvent);

            // Assert
            verify(spyConnectedState, times(2)).getAndValidateMemberVoiceChannel(mockMember, mockEvent);
            verify(spyConnectedState, times(2)).lockChannel(mockVoiceChannel);
            verify(spyConnectedState, times(2)).requestRecordingPermission(mockEvent, mockVoiceChannel);
            verify(mockBot, times(2)).setState(mockPendingApprovalState);
        }
    }
}