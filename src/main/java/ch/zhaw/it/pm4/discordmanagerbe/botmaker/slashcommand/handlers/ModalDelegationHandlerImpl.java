package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core.AbstractDelegationHandler;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.springframework.stereotype.Component;

/**
 * Specialized delegation handler for Discord modal interactions.
 * Extends the abstract delegation handler to provide specific behavior for modal components.
 */
@Component
public class ModalDelegationHandlerImpl extends AbstractDelegationHandler<ModalInteractionEvent> {

    /**
     * Constructs a new ModalDelegationHandlerImpl with error handling capabilities.
     *
     * @param errorHandler the error handler for managing interaction failures
     */
    public ModalDelegationHandlerImpl(InteractionErrorHandler errorHandler) {
        super(errorHandler);
    }

    /**
     * Extracts the unique identifier from a modal interaction event.
     *
     * @param event the modal interaction event
     * @return the modal ID as the identifier
     */
    @Override
    protected String extractIdentifier(ModalInteractionEvent event) {
        return event.getModalId();
    }

    /**
     * Provides the handler type name for logging and debugging purposes.
     *
     * @return the string "modal" identifying this handler type
     */
    @Override
    protected String getHandlerTypeName() {
        return "modal";
    }

    /**
     * Handles errors that occur during modal interaction processing.
     *
     * @param event the modal interaction event that caused the error
     * @param identifier the modal identifier where the error occurred
     * @param e the exception that was thrown
     */
    @Override
    protected void handleError(ModalInteractionEvent event, String identifier, Exception e) {
        errorHandler.handleInteractionError(event, identifier, e);
    }

    /**
     * Handles cases where no handler is registered for a modal interaction.
     * Modals typically don't require a response when no handler is found.
     *
     * @param event the modal interaction event with no registered handler
     * @param identifier the modal identifier that has no handler
     */
    @Override
    protected void handleNoHandlerFound(ModalInteractionEvent event, String identifier) {
        log.debug("No handler found for modal interaction: {}", identifier);
        // Modals typically don't need a response when no handler is found
    }
}