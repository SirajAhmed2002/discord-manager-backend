package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Base interface for delegation handlers that manage multiple interaction handlers.
 * Extends InteractionHandler with registration and removal capabilities for dynamic handler management.
 *
 * @param <T> the type of Discord interaction event this handler processes
 */
public interface DelegationHandler<T> extends InteractionHandler<T> {

    /**
     * Registers a single handler for a specific identifier.
     *
     * @param identifier the unique identifier for the handler
     * @param handler the consumer function to handle the interaction
     */
    void registerHandler(String identifier, Consumer<T> handler);

    /**
     * Registers multiple handlers at once for bulk operations.
     *
     * @param handlers map of identifiers to their respective handler functions
     */
    void registerHandlers(Map<String, Consumer<T>> handlers);

    /**
     * Removes a handler by its identifier.
     *
     * @param identifier the identifier of the handler to remove
     * @return true if the handler was successfully removed, false if not found
     */
    boolean removeHandler(String identifier);
}
