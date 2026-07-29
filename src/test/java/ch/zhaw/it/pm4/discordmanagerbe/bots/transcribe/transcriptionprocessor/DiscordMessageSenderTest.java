package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.messagesender.DiscordMessageSender;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordMessageSenderTest {

    @Mock
    private MessageChannel messageChannel;

    @Mock
    private MessageCreateAction messageCreateAction;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private DiscordMessageSender discordMessageSender;

    @BeforeEach
    void setUp() {
        discordMessageSender = new DiscordMessageSender();
    }

    @Test
    void sendTranscriptionToChannel_nullChannel_shouldLogWarning() {
        // Arrange
        MessageChannel nullChannel = null;
        String transcription = "Test Transcription";

        // Act
        discordMessageSender.sendTranscriptionToChannel(nullChannel, transcription);

        // Assert
        verifyNoInteractions(messageChannel);
    }

    @Test
    void sendTranscriptionToChannel_shortMessage_shouldSendSingleMessage() {
        // Arrange
        String transcription = "Short test transcription";
        when(messageChannel.sendMessage(transcription)).thenReturn(messageCreateAction);
        doAnswer(invocation -> {
            Consumer<Object> successCallback = invocation.getArgument(0);
            Consumer<Throwable> errorCallback = invocation.getArgument(1);
            successCallback.accept(null); // Erfolgreich ausgeführt
            return null;
        }).when(messageCreateAction).queue(any(Consumer.class), any(Consumer.class));

        // Act
        discordMessageSender.sendTranscriptionToChannel(messageChannel, transcription);

        // Assert
        verify(messageChannel, times(1)).sendMessage(transcription);
        verify(messageCreateAction, times(1)).queue(any(Consumer.class), any(Consumer.class));
    }

    @Test
    void sendTranscriptionToChannel_longMessage_shouldSplitAndSendMultipleMessages() {
        // Arrange
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1100; i++) {
            sb.append("Test sentence ").append(i).append(".\n\n");
        }
        String longTranscription = sb.toString();
        when(messageChannel.sendMessage(any(String.class))).thenReturn(messageCreateAction);
        doNothing().when(messageCreateAction).queue();

        // Act
        discordMessageSender.sendTranscriptionToChannel(messageChannel, longTranscription);

        // Assert
        verify(messageChannel, atLeast(2)).sendMessage(any(String.class));
    }

    @Test
    void sendTranscriptionToChannel_messageAtExactLimit_shouldSendSingleMessage() {
        // Arrange
        StringBuilder exactLengthTranscription = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            exactLengthTranscription.append("A");
        }
        String message = exactLengthTranscription.toString();
        when(messageChannel.sendMessage(message)).thenReturn(messageCreateAction);
        doAnswer(invocation -> {
            Consumer<Object> successCallback = invocation.getArgument(0);
            Consumer<Throwable> errorCallback = invocation.getArgument(1);
            successCallback.accept(null); // Erfolgreich ausgeführt
            return null;
        }).when(messageCreateAction).queue(any(Consumer.class), any(Consumer.class));

        // Act
        discordMessageSender.sendTranscriptionToChannel(messageChannel, message);

        // Assert
        verify(messageChannel, times(1)).sendMessage(message);
        verify(messageCreateAction, times(1)).queue(any(Consumer.class), any(Consumer.class));
    }

    @Test
    void sendTranscriptionToChannel_errorOccurs_shouldLogError() {
        // Arrange
        String transcription = "Test transcription";
        Exception testException = new RuntimeException("Test error");

        when(messageChannel.sendMessage(transcription)).thenReturn(messageCreateAction);
        doAnswer(invocation -> {
            Consumer<Object> successCallback = invocation.getArgument(0);
            Consumer<Throwable> errorCallback = invocation.getArgument(1);
            errorCallback.accept(testException); // Fehler simulieren
            return null;
        }).when(messageCreateAction).queue(any(Consumer.class), any(Consumer.class));

        // Act
        discordMessageSender.sendTranscriptionToChannel(messageChannel, transcription);

        // Assert
        verify(messageChannel, times(1)).sendMessage(transcription);
        verify(messageCreateAction, times(1)).queue(any(Consumer.class), any(Consumer.class));
    }
}