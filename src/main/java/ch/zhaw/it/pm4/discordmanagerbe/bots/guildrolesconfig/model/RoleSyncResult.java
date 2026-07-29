package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of the role sync operation.
 */
public class RoleSyncResult {

    /**
     * Indicates whether the sync operation was successful.
     */
    private boolean success;

    /**
     * Message providing additional information about the sync result.
     */
    private String message;

    /**
     * List of roles that were created during the sync operation.
     */
    private final List<RoleChangeInfo> createdRoles = new ArrayList<>();

    /**
     * List of roles that were updated during the sync operation,
     */
    private final List<RoleChangeInfo> updatedRoles = new ArrayList<>();

    /**
     * List of roles that were deleted during the sync operation.
     */
    private final List<RoleChangeInfo> deletedRoles = new ArrayList<>();

    /**
     * List of errors encountered during the sync operation.
     */
    private final List<String> errors = new ArrayList<>();

    /**
     * Private constructor to create a RoleSyncResult instance.
     * @param success indicates whether the sync operation was successful
     * @param message provides additional information about the sync result
     */
    private RoleSyncResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a successful RoleSyncResult with a message. Factory method for convenience.
     * @param message the message to include in the result
     * @return a new RoleSyncResult instance indicating success
     */
    public static RoleSyncResult success(String message) {
        return new RoleSyncResult(true, message);
    }

    /**
     * Creates a failed RoleSyncResult with an error message. Factory method for convenience.
     * @param message the error message to include in the result
     * @return a new RoleSyncResult instance indicating failure
     */
    public static RoleSyncResult failure(String message) {
        return new RoleSyncResult(false, message);
    }

    /**
     * Adds a newly created role to the result. Mutator method for convenience.
     * @param name the name of the role
     * @param id the unique identifier of the role
     */
    public void addCreatedRole(String name, String id) {
        createdRoles.add(new RoleChangeInfo(name, id, null));
    }

    /**
     * Adds an updated role to the result, including the changes made. Mutator method for convenience.
     * @param name the name of the role
     * @param id the unique identifier of the role
     * @param changes the list of changes made to the role
     */
    public void addUpdatedRole(String name, String id, List<String> changes) {
        updatedRoles.add(new RoleChangeInfo(name, id, changes));
    }

    /**
     * Adds a deleted role to the result. Mutator method for convenience.
     * @param name the name of the role
     * @param id the unique identifier of the role
     */
    public void addDeletedRole(String name, String id) {
        deletedRoles.add(new RoleChangeInfo(name, id, null));
    }

    /**
     * Adds an error message to the result. Mutator method for convenience.
     * @param error the error message to add
     */
    public void addError(String error) {
        errors.add(error);
        updateStatusBasedOnErrors();
    }

    /**
     * Gets whether the sync operation was successful.
     * @return true if the sync was successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the message providing additional information about the sync result.
     * @return the message associated with the sync result
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the list of errors encountered during the sync operation.
     * @return unmodifiable list of created roles
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Gets a summary of the sync operation, including counts of created, updated, deleted roles and errors.
     * @return a formatted summary string
     */
    public String getSummary() {
        return String.format("Role Sync Summary: Created: %d, Updated: %d, Deleted: %d, Errors: %d",
                           createdRoles.size(), updatedRoles.size(), deletedRoles.size(), errors.size());
    }

    /**
     * Sets the success status of the sync operation.
     * @param success true if the sync was successful, false otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets the message providing additional information about the sync result.
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Updates the success status based on the presence of errors.
     */
    private void updateStatusBasedOnErrors() {
        if (!errors.isEmpty()) {
            this.success = false;
            this.message = String.format("Role sync completed with %d error(s). Check error list for details.", 
                                       errors.size());
        }
    }

    /**
     * Gets the list of created roles. Read-only access.
     * @return unmodifiable list of created roles
     */
    public List<RoleChangeInfo> getCreatedRoles() {
        return Collections.unmodifiableList(createdRoles);
    }

    /**
     * Gets the list of updated roles. Read-only access.
     * @return unmodifiable list of updated roles
     */
    public List<RoleChangeInfo> getUpdatedRoles() {
        return Collections.unmodifiableList(updatedRoles);
    }

    /**
     * Gets the list of deleted roles. Read-only access.
     * @return unmodifiable list of deleted roles
     */
    public List<RoleChangeInfo> getDeletedRoles() {
        return Collections.unmodifiableList(deletedRoles);
    }
}