package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.entities.Guild;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisconnectedStateTest {

    @Mock
    private TranscribeBot mockBot;

    @Mock
    private SlashCommandInteractionEvent mockEvent;

    @Mock
    private Member mockMember;

    @Mock
    private VoiceChannel mockVoiceChannel;

    @Mock
    private Guild mockGuild;

    @Mock
    private AudioManager mockAudioManager;

    @Mock
    private ReplyCallbackAction mockReplyAction;

    @Mock
    private ConnectedState mockConnectedState;

    private DisconnectedState disconnectedState;

    @BeforeEach
    void setUp() {
        disconnectedState = spy(new DisconnectedState(mockBot));
    }

    @Test
    void joinVoiceChannel_shouldConnectSuccessfully_whenMemberInValidChannel() {
        // Arrange
        String channelName = "General Voice";
        String successMessage = "Successfully joined voice channel!";

        when(mockEvent.getMember()).thenReturn(mockMember);
        doReturn(mockVoiceChannel).when(disconnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);
        when(mockVoiceChannel.getName()).thenReturn(channelName);
        when(mockVoiceChannel.getGuild()).thenReturn(mockGuild);
        when(mockGuild.getAudioManager()).thenReturn(mockAudioManager);
        when(mockBot.getConnectedState()).thenReturn(mockConnectedState);

        InteractionHook mockHook = mock(InteractionHook.class);
        when(mockEvent.getHook()).thenReturn(mockHook);

        WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
        when(mockHook.sendMessage(anyString())).thenReturn(mockAction);

        // Act
        disconnectedState.joinVoiceChannel(mockEvent);

        // Assert
        verify(mockAudioManager).openAudioConnection(mockVoiceChannel);
        verify(mockBot).setActiveAudioManager(mockAudioManager);
        verify(mockBot).setState(mockConnectedState);
    }

    @Test
    void joinVoiceChannel_shouldReturnEarly_whenChannelValidationFails() {
        // Arrange
        when(mockEvent.getMember()).thenReturn(mockMember);
        doReturn(null).when(disconnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);

        // Act
        disconnectedState.joinVoiceChannel(mockEvent);

        // Assert
        verify(mockBot, never()).setActiveAudioManager(any());
        verify(mockBot, never()).setState(any());
        verify(mockEvent, never()).reply(anyString());
    }

    @Test
    void joinVoiceChannel_shouldHandleException_whenConnectionFails() {
        // Arrange
        String channelName = "General Voice";
        String errorMessage = "Connection error occurred!";
        RuntimeException testException = new RuntimeException("Connection failed");

        lenient().when(mockEvent.getMember()).thenReturn(mockMember);
        lenient().doReturn(mockVoiceChannel).when(disconnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);
        lenient().when(mockVoiceChannel.getName()).thenReturn(channelName);
        lenient().when(mockVoiceChannel.getGuild()).thenReturn(mockGuild);
        lenient().when(mockGuild.getAudioManager()).thenReturn(mockAudioManager);
        lenient().doThrow(testException).when(mockAudioManager).openAudioConnection(mockVoiceChannel);

        InteractionHook mockHook = mock(InteractionHook.class);
        when(mockEvent.getHook()).thenReturn(mockHook);

        WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
        when(mockHook.sendMessage(anyString())).thenReturn(mockAction);
        when(mockAction.setEphemeral(true)).thenReturn(mockAction);

        // Act
        disconnectedState.joinVoiceChannel(mockEvent);

        // Assert
        verify(mockBot, never()).setActiveAudioManager(any());
        verify(mockBot, never()).setState(any());
    }

    @Test
    void joinVoiceChannel_shouldHandleException_whenStateUpdateFails() {
        // Arrange
        String channelName = "General Voice";
        String errorMessage = "Connection error occurred!";
        RuntimeException testException = new RuntimeException("State update failed");

        lenient().when(mockEvent.getMember()).thenReturn(mockMember);
        lenient().doReturn(mockVoiceChannel).when(disconnectedState).getAndValidateMemberVoiceChannel(mockMember, mockEvent);
        lenient().when(mockVoiceChannel.getName()).thenReturn(channelName);
        lenient().when(mockVoiceChannel.getGuild()).thenReturn(mockGuild);
        lenient().when(mockGuild.getAudioManager()).thenReturn(mockAudioManager);
        lenient().when(mockBot.getConnectedState()).thenReturn(mockConnectedState);
        lenient().doThrow(testException).when(mockBot).setState(mockConnectedState);

        InteractionHook mockHook = mock(InteractionHook.class);
        when(mockEvent.getHook()).thenReturn(mockHook);

        WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
        when(mockHook.sendMessage(anyString())).thenReturn(mockAction);
        when(mockAction.setEphemeral(true)).thenReturn(mockAction);

        // Act
        disconnectedState.joinVoiceChannel(mockEvent);

        // Assert
        verify(mockAudioManager).openAudioConnection(mockVoiceChannel);
        verify(mockBot).setActiveAudioManager(mockAudioManager);
    }
}