package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Abstract base class providing common functionality for all Discord interaction delegation handlers.
 * Implements thread-safe handler registration, removal, and execution with centralized error handling.
 *
 * @param <T> the type of Discord interaction event this handler processes
 */
public abstract class AbstractDelegationHandler<T> implements DelegationHandler<T> {

    /**
     * Logger instance for this handler class.
     */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Thread-safe map storing registered handlers by their identifiers.
     */
    protected final Map<String, Consumer<T>> handlers = new ConcurrentHashMap<>();

    /**
     * Centralized error handler for managing interaction failures.
     */
    protected final InteractionErrorHandler errorHandler;

    /**
     * Constructs a new AbstractDelegationHandler with error handling capabilities.
     *
     * @param errorHandler the error handler for managing interaction failures
     */
    protected AbstractDelegationHandler(InteractionErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Registers a single handler for a specific identifier in a thread-safe manner.
     *
     * @param identifier the unique identifier for the handler
     * @param handler the consumer function to handle the interaction
     */
    @Override
    public synchronized void registerHandler(String identifier, Consumer<T> handler) {
        handlers.put(identifier, handler);
        log.debug("Registered {} handler for identifier: {}", getHandlerTypeName(), identifier);
    }

    /**
     * Registers multiple handlers at once in a thread-safe manner.
     *
     * @param newHandlers map of identifiers to their respective handlers
     */
    @Override
    public synchronized void registerHandlers(Map<String, Consumer<T>> newHandlers) {
        handlers.putAll(newHandlers);
        log.info("Registered {} {} handlers", newHandlers.size(), getHandlerTypeName());
    }

    /**
     * Removes a handler by identifier in a thread-safe manner.
     *
     * @param identifier the identifier of the handler to remove
     * @return true if the handler was successfully removed, false otherwise
     */
    @Override
    public synchronized boolean removeHandler(String identifier) {
        boolean removed = handlers.remove(identifier) != null;
        if (removed) {
            log.info("Removed {} handler for identifier: {}", getHandlerTypeName(), identifier);
        }
        return removed;
    }

    /**
     * Gets the total number of registered handlers.
     *
     * @return the count of registered handlers
     */
    @Override
    public int getHandlerCount() {
        return handlers.size();
    }

    /**
     * Handles an incoming interaction by finding and executing the appropriate handler.
     * Includes comprehensive error handling and logging.
     *
     * @param event the interaction event to handle
     */
    @Override
    public void handleInteraction(T event) {
        String identifier = extractIdentifier(event);
        Consumer<T> handler = findHandler(identifier);

        log.debug("Received {} interaction: {} (Handler count: {})",
                getHandlerTypeName(), identifier, handlers.size());

        if (handler != null) {
            try {
                log.info("Handling {} interaction: {}", getHandlerTypeName(), identifier);
                handler.accept(event);
            } catch (Exception e) {
                handleError(event, identifier, e);
            }
        } else {
            handleNoHandlerFound(event, identifier);
        }
    }

    /**
     * Checks if this handler can process the given identifier.
     *
     * @param identifier the identifier to check
     * @return true if a handler exists for the identifier, false otherwise
     */
    @Override
    public boolean canHandle(String identifier) {
        return findHandler(identifier) != null;
    }

    /**
     * Extracts the unique identifier from the interaction event.
     * Implementation varies by interaction type (e.g., command name, custom ID).
     *
     * @param event the interaction event
     * @return the extracted identifier
     */
    protected abstract String extractIdentifier(T event);

    /**
     * Provides the handler type name for logging and debugging purposes.
     *
     * @return a descriptive name for this handler type
     */
    protected abstract String getHandlerTypeName();

    /**
     * Handles errors that occur during interaction event processing.
     *
     * @param event the interaction event that caused the error
     * @param identifier the identifier where the error occurred
     * @param e the exception that was thrown
     */
    protected abstract void handleError(T event, String identifier, Exception e);

    /**
     * Handles cases where no handler is found for an interaction.
     *
     * @param event the interaction event with no registered handler
     * @param identifier the identifier that has no handler
     */
    protected abstract void handleNoHandlerFound(T event, String identifier);

    /**
     * Finds a handler for the given identifier.
     * Can be overridden for custom logic such as prefix matching.
     *
     * @param identifier the identifier to find a handler for
     * @return the matching handler, or null if no handler is found
     */
    protected Consumer<T> findHandler(String identifier) {
        return handlers.get(identifier);
    }
}