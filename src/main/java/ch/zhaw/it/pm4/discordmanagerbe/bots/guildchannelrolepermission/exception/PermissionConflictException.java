package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.exception;

/**
 * Exception thrown when there are conflicting permissions
 * in a permission override operation.
 */
public class PermissionConflictException extends RuntimeException {
    /**
     * Constructor for PermissionConflictException.
     * @param message the detail message, which is saved for later retrieval by the
     */
    public PermissionConflictException(String message) {
        super(message);
    }
}
