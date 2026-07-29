package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.exception;

/**
 * Exception thrown when an entity (like a guild, channel, or role) is not found.
 * This is typically used in service layers to indicate that a requested entity
 * does not exist in the Discord server.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Constructs a new EntityNotFoundException with the specified detail message.
     * @param message the detail message, which is saved for later retrieval by the
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}

