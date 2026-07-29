package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core.AbstractDelegationHandler;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.springframework.stereotype.Component;

/**
 * Specialized delegation handler for Discord string select menu interactions.
 * Extends the abstract delegation handler to provide specific behavior for string select components.
 */
@Component
public class StringSelectDelegationHandlerImpl extends AbstractDelegationHandler<StringSelectInteractionEvent> {

    /**
     * Constructs a new StringSelectDelegationHandlerImpl with error handling capabilities.
     *
     * @param errorHandler the error handler for managing interaction failures
     */
    public StringSelectDelegationHandlerImpl(InteractionErrorHandler errorHandler) {
        super(errorHandler);
    }

    /**
     * Extracts the unique identifier from a string select interaction event.
     *
     * @param event the string select interaction event
     * @return the component ID as the identifier
     */
    @Override
    protected String extractIdentifier(StringSelectInteractionEvent event) {
        return event.getComponentId();
    }

    /**
     * Provides the handler type name for logging and debugging purposes.
     *
     * @return the string "string select" identifying this handler type
     */
    @Override
    protected String getHandlerTypeName() {
        return "string select";
    }

    /**
     * Handles errors that occur during string select interaction processing.
     *
     * @param event the string select interaction event that caused the error
     * @param identifier the component identifier where the error occurred
     * @param e the exception that was thrown
     */
    @Override
    protected void handleError(StringSelectInteractionEvent event, String identifier, Exception e) {
        errorHandler.handleInteractionError(event, identifier, e);
    }

    /**
     * Handles cases where no handler is registered for a string select interaction.
     * String select menus typically don't require a response when no handler is found.
     *
     * @param event the string select interaction event with no registered handler
     * @param identifier the component identifier that has no handler
     */
    @Override
    protected void handleNoHandlerFound(StringSelectInteractionEvent event, String identifier) {
        log.debug("No handler found for string select interaction: {}", identifier);
        // String selects typically don't need a response when no handler is found
    }
}