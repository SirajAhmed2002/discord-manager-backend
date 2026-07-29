package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core.AbstractDelegationHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Specialized delegation handler for Discord button interactions.
 * Extends the abstract delegation handler with button-specific behavior and prefix matching support.
 */
@Component
public class ButtonDelegationHandlerImpl extends AbstractDelegationHandler<ButtonInteractionEvent> {

    /**
     * Constructs a new ButtonDelegationHandlerImpl with error handling capabilities.
     *
     * @param errorHandler the error handler for managing interaction failures
     */
    public ButtonDelegationHandlerImpl(InteractionErrorHandler errorHandler) {
        super(errorHandler);
    }

    /**
     * Extracts the unique identifier from a button interaction event.
     *
     * @param event the button interaction event
     * @return the component ID as the identifier
     */
    @Override
    protected String extractIdentifier(ButtonInteractionEvent event) {
        return event.getComponentId();
    }

    /**
     * Provides the handler type name for logging and debugging purposes.
     *
     * @return the string "button" identifying this handler type
     */
    @Override
    protected String getHandlerTypeName() {
        return "button";
    }

    /**
     * Handles errors that occur during button interaction processing.
     *
     * @param event the button interaction event that caused the error
     * @param identifier the component identifier where the error occurred
     * @param e the exception that was thrown
     */
    @Override
    protected void handleError(ButtonInteractionEvent event, String identifier, Exception e) {
        errorHandler.handleInteractionError(event, identifier, e);
    }

    /**
     * Handles cases where no handler is registered for a button interaction.
     * Buttons typically don't require a response when no handler is found.
     *
     * @param event the button interaction event with no registered handler
     * @param identifier the component identifier that has no handler
     */
    @Override
    protected void handleNoHandlerFound(ButtonInteractionEvent event, String identifier) {
        log.debug("No handler found for button interaction: {}", identifier);
        // Buttons typically don't need a response when no handler is found
    }

    /**
     * Finds a handler for the given custom ID with support for prefix matching.
     * First attempts exact matching, then falls back to prefix matching for dynamic button IDs.
     *
     * @param customId the custom ID to find a handler for
     * @return the matching handler, or null if no handler is found
     */
    @Override
    protected Consumer<ButtonInteractionEvent> findHandler(String customId) {
        Consumer<ButtonInteractionEvent> handler = handlers.get(customId);

        if (handler == null) {
            for (String key : handlers.keySet()) {
                if (customId.startsWith(key)) {
                    return handlers.get(key);
                }
            }
        }

        return handler;
    }
}