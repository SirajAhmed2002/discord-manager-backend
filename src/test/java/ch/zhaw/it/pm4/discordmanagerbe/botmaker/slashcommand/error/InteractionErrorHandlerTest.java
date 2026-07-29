package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionErrorHandlerTest {

    private InteractionErrorHandler interactionErrorHandler;

    @Mock
    private SlashCommandInteractionEvent slashCommandEvent;

    @Mock
    private ButtonInteractionEvent buttonEvent;

    @Mock
    private StringSelectInteractionEvent stringSelectEvent;

    @Mock
    private ModalInteractionEvent modalEvent;

    @Mock
    private InteractionHook interactionHook;

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    @Mock
    private WebhookMessageCreateAction webhookMessageCreateAction;

    @BeforeEach
    void setUp() {
        interactionErrorHandler = new InteractionErrorHandler();
    }

    @Test
    void handleInteractionError_SlashCommand_NotAcknowledged_ShouldReplyWithError() {
        // Arrange
        String commandName = "testCommand";
        Exception testException = new RuntimeException("Test error");

        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);

        // Act
        interactionErrorHandler.handleInteractionError(slashCommandEvent, commandName, testException);

        // Assert
        verify(slashCommandEvent).reply("An error occurred while processing the command: Test error");
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
        verify(slashCommandEvent).getHook();
    }

    @Test
    void handleInteractionError_SlashCommand_AlreadyAcknowledged_ShouldUseHook() {
        // Arrange
        String commandName = "testCommand";
        Exception testException = new RuntimeException("Test error");

        when(slashCommandEvent.isAcknowledged()).thenReturn(true);
        when(slashCommandEvent.getHook()).thenReturn(interactionHook);
        when(slashCommandEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(interactionHook.sendMessage(anyString())).thenReturn(webhookMessageCreateAction);
        when(webhookMessageCreateAction.setEphemeral(true)).thenReturn(webhookMessageCreateAction);

        // Act
        interactionErrorHandler.handleInteractionError(slashCommandEvent, commandName, testException);

        // Assert
        verify(interactionHook).sendMessage("An error occurred while processing the command: Test error");
        verify(webhookMessageCreateAction).setEphemeral(true);
        verify(webhookMessageCreateAction).queue();
    }

    @Test
    void handleInteractionError_ButtonInteraction_NotAcknowledged_ShouldReplyWithError() {
        // Arrange
        String buttonId = "testButton";
        Exception testException = new RuntimeException("Button error");

        when(buttonEvent.isAcknowledged()).thenReturn(false);
        when(buttonEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);

        // Act
        interactionErrorHandler.handleInteractionError(buttonEvent, buttonId, testException);

        // Assert
        verify(buttonEvent).reply("An error occurred while processing the button interaction: Button error");
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    void handleInteractionError_StringSelectInteraction_NotAcknowledged_ShouldReplyWithError() {
        // Arrange
        String selectId = "testSelect";
        Exception testException = new RuntimeException("Select error");

        when(stringSelectEvent.isAcknowledged()).thenReturn(false);
        when(stringSelectEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);

        // Act
        interactionErrorHandler.handleInteractionError(stringSelectEvent, selectId, testException);

        // Assert
        verify(stringSelectEvent).reply("An error occurred while processing the select menu interaction: Select error");
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    void handleInteractionError_ModalInteraction_NotAcknowledged_ShouldReplyWithError() {
        // Arrange
        String modalId = "testModal";
        Exception testException = new RuntimeException("Modal error");

        when(modalEvent.isAcknowledged()).thenReturn(false);
        when(modalEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);

        // Act
        interactionErrorHandler.handleInteractionError(modalEvent, modalId, testException);

        // Assert
        verify(modalEvent).reply("An error occurred while processing the modal interaction: Modal error");
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    void handleInteractionError_AlreadyAcknowledgedException_ShouldReturnEarly() {
        // Arrange
        String commandName = "testCommand";
        IllegalStateException alreadyAcknowledgedException =
                new IllegalStateException("Interaction has already been acknowledged");

        // Act
        interactionErrorHandler.handleInteractionError(slashCommandEvent, commandName, alreadyAcknowledgedException);

        // Assert
        verify(slashCommandEvent, never()).reply(anyString());
        verify(slashCommandEvent, never()).getHook();
        // Verify that the method returns early without attempting to send a response
    }

    @Test
    void handleInteractionError_ReplyThrowsException_ShouldNotThrow() {
        // Arrange
        String commandName = "testCommand";
        Exception testException = new RuntimeException("Test error");

        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.reply(anyString())).thenThrow(new RuntimeException("Reply failed"));

        // Act & Assert - Should not throw an exception
        interactionErrorHandler.handleInteractionError(slashCommandEvent, commandName, testException);

        // Verify that the method handles the exception gracefully
        verify(slashCommandEvent).reply(anyString());
    }

    @Test
    void handleInteractionError_HookSendMessageThrowsException_ShouldNotThrow() {
        // Arrange
        String commandName = "testCommand";
        Exception testException = new RuntimeException("Test error");

        when(slashCommandEvent.isAcknowledged()).thenReturn(true);
        when(slashCommandEvent.getHook()).thenReturn(interactionHook);
        when(slashCommandEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(interactionHook.sendMessage(anyString())).thenThrow(new RuntimeException("Hook send failed"));

        // Act & Assert - Should not throw an exception
        interactionErrorHandler.handleInteractionError(slashCommandEvent, commandName, testException);

        // Verify that the method handles the exception gracefully
        verify(interactionHook).sendMessage(anyString());
    }

    @Test
    void handleInteractionError_UnknownInteractionType_ShouldHandleGracefully() {
        // Arrange
        String identifier = "unknown";
        Exception testException = new RuntimeException("Test error");
        Object unknownEvent = new Object(); // Unknown interaction type

        // Act & Assert - Should not throw an exception
        interactionErrorHandler.handleInteractionError(unknownEvent, identifier, testException);

        // The method should handle unknown types gracefully without throwing
    }

    @Test
    void handleInteractionError_NullException_ShouldHandleGracefully() {
        // Arrange
        String commandName = "testCommand";

        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);

        // Act
        interactionErrorHandler.handleInteractionError(slashCommandEvent, commandName, null);

        // Assert - Should handle null exception gracefully
        verify(slashCommandEvent).reply(contains("An error occurred while processing the command"));
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    void handleInteractionError_EmptyIdentifier_ShouldStillWork() {
        // Arrange
        String emptyIdentifier = "";
        Exception testException = new RuntimeException("Test error");

        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);

        // Act
        interactionErrorHandler.handleInteractionError(slashCommandEvent, emptyIdentifier, testException);

        // Assert
        verify(slashCommandEvent).reply("An error occurred while processing the command: Test error");
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    void handleInteractionError_NullIdentifier_ShouldStillWork() {
        // Arrange
        Exception testException = new RuntimeException("Test error");

        when(slashCommandEvent.isAcknowledged()).thenReturn(false);
        when(slashCommandEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);

        // Act
        interactionErrorHandler.handleInteractionError(slashCommandEvent, null, testException);

        // Assert
        verify(slashCommandEvent).reply("An error occurred while processing the command: Test error");
        verify(replyCallbackAction).setEphemeral(true);
        verify(replyCallbackAction).queue();
    }

    @Test
    void handleInteractionError_ButtonInteraction_AlreadyAcknowledged_ShouldUseHook() {
        // Arrange
        String buttonId = "testButton";
        Exception testException = new RuntimeException("Button error");

        when(buttonEvent.isAcknowledged()).thenReturn(true);
        when(buttonEvent.getHook()).thenReturn(interactionHook);
        when(buttonEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(interactionHook.sendMessage(anyString())).thenReturn(webhookMessageCreateAction);
        when(webhookMessageCreateAction.setEphemeral(true)).thenReturn(webhookMessageCreateAction);

        // Act
        interactionErrorHandler.handleInteractionError(buttonEvent, buttonId, testException);

        // Assert
        verify(interactionHook).sendMessage("An error occurred while processing the button interaction: Button error");
        verify(webhookMessageCreateAction).setEphemeral(true);
        verify(webhookMessageCreateAction).queue();
    }

    @Test
    void handleInteractionError_StringSelectInteraction_AlreadyAcknowledged_ShouldUseHook() {
        // Arrange
        String selectId = "testSelect";
        Exception testException = new RuntimeException("Select error");

        when(stringSelectEvent.isAcknowledged()).thenReturn(true);
        when(stringSelectEvent.getHook()).thenReturn(interactionHook);
        when(stringSelectEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(interactionHook.sendMessage(anyString())).thenReturn(webhookMessageCreateAction);
        when(webhookMessageCreateAction.setEphemeral(true)).thenReturn(webhookMessageCreateAction);

        // Act
        interactionErrorHandler.handleInteractionError(stringSelectEvent, selectId, testException);

        // Assert
        verify(interactionHook).sendMessage("An error occurred while processing the select menu interaction: Select error");
        verify(webhookMessageCreateAction).setEphemeral(true);
        verify(webhookMessageCreateAction).queue();
    }

    @Test
    void handleInteractionError_ModalInteraction_AlreadyAcknowledged_ShouldUseHook() {
        // Arrange
        String modalId = "testModal";
        Exception testException = new RuntimeException("Modal error");

        when(modalEvent.isAcknowledged()).thenReturn(true);
        when(modalEvent.getHook()).thenReturn(interactionHook);
        when(modalEvent.reply(anyString())).thenReturn(replyCallbackAction);
        when(interactionHook.sendMessage(anyString())).thenReturn(webhookMessageCreateAction);
        when(webhookMessageCreateAction.setEphemeral(true)).thenReturn(webhookMessageCreateAction);

        // Act
        interactionErrorHandler.handleInteractionError(modalEvent, modalId, testException);

        // Assert
        verify(interactionHook).sendMessage("An error occurred while processing the modal interaction: Modal error");
        verify(webhookMessageCreateAction).setEphemeral(true);
        verify(webhookMessageCreateAction).queue();
    }
}