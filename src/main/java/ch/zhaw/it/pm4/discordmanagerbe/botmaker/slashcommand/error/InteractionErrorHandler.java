package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized error handler for Discord interaction events.
 * Provides consistent error handling and response messaging across all interaction types.
 */
@Component
public class InteractionErrorHandler {

    /**
     * Logger instance for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(InteractionErrorHandler.class);

    /**
     * Generic method to handle interaction errors for any interaction type with consistent logging and response.
     *
     * @param <T> the interaction event type
     * @param event the interaction event that encountered an error
     * @param identifier the identifier (command name, custom id, etc.) for context
     * @param e the exception that occurred during processing
     */
    public <T> void handleInteractionError(T event, String identifier, Exception e) {
        String interactionType = getInteractionType(event);
        String errorMessagePrefix = getErrorMessagePrefix(event);
        String errorMessage = getErrorMessage(e);

        log.error("Error handling {} {}: {}", interactionType, identifier, errorMessage, e);

        // Handle the special case of already acknowledged interactions (mainly for slash commands)
        if (e instanceof IllegalStateException) {
            String message = e.getMessage();
            if (message != null && message.contains("already been acknowledged")) {
                log.warn("{} {} was already acknowledged. Using follow-up message instead.",
                        capitalize(interactionType), identifier);
                return;
            }
        }

        sendErrorResponse(event, errorMessagePrefix + errorMessage, identifier);
    }

    /**
     * Determines the interaction type as a string for logging purposes.
     *
     * @param <T> the interaction event type
     * @param event the interaction event
     * @return a descriptive string identifying the interaction type
     */
    private <T> String getInteractionType(T event) {
        return switch (event) {
            case SlashCommandInteractionEvent ignored -> "slash command";
            case ButtonInteractionEvent ignored -> "button interaction";
            case StringSelectInteractionEvent ignored -> "string select interaction";
            case ModalInteractionEvent ignored -> "modal interaction";
            default -> "unknown interaction";
        };
    }

    /**
     * Gets the appropriate error message prefix for each interaction type.
     *
     * @param <T> the interaction event type
     * @param event the interaction event
     * @return a user-friendly error message prefix
     */
    private <T> String getErrorMessagePrefix(T event) {
        return switch (event) {
            case SlashCommandInteractionEvent ignored -> "An error occurred while processing the command: ";
            case ButtonInteractionEvent ignored -> "An error occurred while processing the button interaction: ";
            case StringSelectInteractionEvent ignored -> "An error occurred while processing the select menu interaction: ";
            case ModalInteractionEvent ignored -> "An error occurred while processing the modal interaction: ";
            default -> "An error occurred while processing the interaction: ";
        };
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str the string to capitalize
     * @return the string with first letter capitalized, or original string if null/empty
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Functional interface for abstracting interaction response handling.
     */
    @FunctionalInterface
    private interface InteractionHandler {
        void handle(boolean isAcknowledged, InteractionHook hook, ReplyCallbackAction replyAction);
    }

    /**
     * Sends an error response to the user for any interaction type.
     * Handles both acknowledged and unacknowledged interactions appropriately.
     *
     * @param <T> the interaction event type
     * @param event the interaction event requiring error response
     * @param message the error message to send to the user
     * @param identifier the identifier for logging purposes
     */
    private <T> void sendErrorResponse(T event, String message, String identifier) {
        try {
            InteractionHandler handler = (isAcknowledged, hook, replyAction) -> {
                if (isAcknowledged) {
                    hook.sendMessage(message)
                            .setEphemeral(true)
                            .queue();
                } else {
                    replyAction.setEphemeral(true)
                            .queue();
                }
            };

            // Type-specific handling for different interaction events
            switch (event) {
                case SlashCommandInteractionEvent slashEvent ->
                        handler.handle(slashEvent.isAcknowledged(), slashEvent.getHook(), slashEvent.reply(message));
                case ButtonInteractionEvent buttonEvent ->
                        handler.handle(buttonEvent.isAcknowledged(), buttonEvent.getHook(), buttonEvent.reply(message));
                case StringSelectInteractionEvent selectEvent ->
                        handler.handle(selectEvent.isAcknowledged(), selectEvent.getHook(), selectEvent.reply(message));
                case ModalInteractionEvent modalEvent ->
                        handler.handle(modalEvent.isAcknowledged(), modalEvent.getHook(), modalEvent.reply(message));
                default -> {
                    log.error("Unsupported interaction event type: {}", event.getClass().getSimpleName());
                }
            }
        } catch (Exception replyError) {
            log.error("Failed to send error message for interaction {}: {}", identifier, replyError.getMessage());
        }
    }

    /**
     * Safely extracts a meaningful error message from an exception.
     *
     * @param e the exception to extract message from
     * @return a descriptive error message, never null
     */
    private String getErrorMessage(Exception e) {
        if (e == null) {
            return "Unknown error occurred";
        }

        String message = e.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return e.getClass().getSimpleName();
        }

        return message;
    }
}