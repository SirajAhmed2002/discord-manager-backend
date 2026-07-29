package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core;

/**
 * Generic interface for handling Discord interactions.
 * Provides the fundamental contract for processing interaction events and handler management.
 *
 * @param <T> the type of Discord interaction event this handler processes
 */
public interface InteractionHandler<T> {

    /**
     * Processes an incoming Discord interaction event.
     *
     * @param event the interaction event to handle
     */
    void handleInteraction(T event);

    /**
     * Checks if this handler can process interactions with the given identifier.
     *
     * @param identifier the identifier to check (e.g., command name, custom ID)
     * @return true if this handler can process the identifier, false otherwise
     */
    boolean canHandle(String identifier);

    /**
     * Gets the total number of registered handlers.
     *
     * @return the count of currently registered handlers
     */
    int getHandlerCount();
}
