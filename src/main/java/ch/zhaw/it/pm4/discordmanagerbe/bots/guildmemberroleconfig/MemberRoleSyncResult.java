package ch.zhaw.it.pm4.discordmanagerbe.bots.guildmemberroleconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains the results of a member role synchronization operation.
 * Tracks successful role assignments, removals, and any errors encountered.
 */
public class MemberRoleSyncResult {

    /** Whether the sync operation was successful. */
    private boolean success;

    /** Message describing the operation result. */
    private String message;

    /** List of successful role assignments. */
    private final List<MemberRoleChangeInfo> roleAssignments = new ArrayList<>();

    /** List of successful role removals. */
    private final List<MemberRoleChangeInfo> roleRemovals = new ArrayList<>();

    /** List of errors encountered during sync. */
    private final List<String> errors = new ArrayList<>();

    /**
     * Creates a new sync result.
     *
     * @param success whether the operation was successful
     * @param message descriptive message about the result
     */
    public MemberRoleSyncResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Adds a successful role assignment to the result.
     *
     * @param memberName name of the member
     * @param roleName name of the assigned role
     */
    public void addRoleAssignment(String memberName, String roleName) {
        roleAssignments.add(new MemberRoleChangeInfo(memberName, roleName));
    }

    /**
     * Adds a successful role removal to the result.
     *
     * @param memberName name of the member
     * @param roleName name of the removed role
     */
    public void addRoleRemoval(String memberName, String roleName) {
        roleRemovals.add(new MemberRoleChangeInfo(memberName, roleName));
    }

    /**
     * Adds an error message to the result.
     *
     * @param error the error message
     */
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * Gets whether the sync was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() { return success; }

    /**
     * Sets the success status.
     *
     * @param success the success status
     */
    public void setSuccess(boolean success) { this.success = success; }

    /**
     * Gets the result message.
     *
     * @return the message
     */
    public String getMessage() { return message; }

    /**
     * Sets the result message.
     *
     * @param message the message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Gets all successful role assignments.
     *
     * @return unmodifiable list of role assignments
     */
    public List<MemberRoleChangeInfo> getRoleAssignments() { return Collections.unmodifiableList(roleAssignments); }

    /**
     * Gets all successful role removals.
     *
     * @return unmodifiable list of role removals
     */
    public List<MemberRoleChangeInfo> getRoleRemovals() { return Collections.unmodifiableList(roleRemovals); }

    /**
     * Gets all errors encountered.
     *
     * @return unmodifiable list of error messages
     */
    public List<String> getErrors() { return Collections.unmodifiableList(errors); }

    /**
     * Gets a summary of all changes made.
     */
    public String getSummary() {
        return String.format("Member Role Sync Summary: Roles Added: %d, Roles Removed: %d, Errors: %d",
                roleAssignments.size(), roleRemovals.size(), errors.size());
    }

    /**
     * Information about a single member role change.
     */
    public static class MemberRoleChangeInfo {

        /** Name of the member whose role changed. */
        private final String memberName, roleName;

        /**
         * Creates a new role change info.
         *
         * @param memberName name of the member
         * @param roleName name of the role
         */
        public MemberRoleChangeInfo(String memberName, String roleName) {
            this.memberName = memberName;
            this.roleName = roleName;
        }

        /**
         * Gets the member name.
         *
         * @return the member name
         */
        public String getMemberName() { return memberName; }

        /**
         * Gets the role name.
         *
         * @return the role name
         */
        public String getRoleName() { return roleName; }

        @Override
        public String toString() { return memberName + " ↔ " + roleName; }
    }
}