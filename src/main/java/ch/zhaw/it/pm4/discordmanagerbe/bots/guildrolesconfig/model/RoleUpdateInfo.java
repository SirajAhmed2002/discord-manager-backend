package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model;

import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about the current and desired state of a role during the sync process.
 * Contains flags to indicate which properties have changed.
 */
public class RoleUpdateInfo {
    /**
     * The current state of the role in the Discord server.
     */
    private Role currentRole;

    /**
     * The desired state of the role as defined in the configuration.
     */
    private ExtendedRoleDTO desiredRole;

    /**
     * Flag indicating if the name of the role has changed.
     */
    private boolean nameChanged;

    /**
     * Flag indicating if the color of the role has changed.
     */
    private boolean colorChanged;

    /**
     * Flag indicating if the mentionable status of the role has changed.
     */
    private boolean mentionableChanged;

    /**
     * Flag indicating if the hoisted status of the role has changed.
     */
    private boolean hoistedChanged;

    /**
     * Flag indicating if the permissions of the role have changed.
     */
    private boolean permissionsChanged;

    /**
     * List of changes made to the role.
     */
    private final List<String> changes = new ArrayList<>();

    /**
     * Constructor for creating a RoleUpdateInfo instance.
     * @return a new instance of RoleUpdateInfo
     */
    public boolean hasChanges() {
        return nameChanged || colorChanged || mentionableChanged || hoistedChanged || permissionsChanged;
    }

    /**
     * Adds a change description to the list of changes.
     * @param change the description of the change made to the role
     */
    public void addChange(String change) {
        changes.add(change);
    }

    /**
     * Gets the current state of the role in the Discord server.
     * @return the current Role object representing the role
     */
    public Role getCurrentRole() {
        return currentRole;
    }

    /**
     * Sets the current state of the role in the Discord server.
     * @param currentRole the Role object representing the current state of the role
     */
    public void setCurrentRole(Role currentRole) {
        this.currentRole = currentRole;
    }

    /**
     * Gets the desired state of the role as defined in the configuration.
     * @return the ExtendedRoleDTO object representing the desired role state
     */
    public ExtendedRoleDTO getDesiredRole() {
        return desiredRole;
    }

    /**
     * Sets the desired state of the role as defined in the configuration.
     * @param desiredRole the ExtendedRoleDTO object representing the desired role state
     */
    public void setDesiredRole(ExtendedRoleDTO desiredRole) {
        this.desiredRole = desiredRole;
    }

    /**
     * Checks if the name of the role has changed.
     * @return true if the name has changed, false otherwise
     */
    public boolean isNameChanged() {
        return nameChanged;
    }

    /**
     * Sets the flag indicating if the name of the role has changed.
     * @param nameChanged true if the name has changed, false otherwise
     */
    public void setNameChanged(boolean nameChanged) {
        this.nameChanged = nameChanged;
    }

    /**
     * Checks if the color of the role has changed.
     * @return true if the color has changed, false otherwise
     */
    public boolean isColorChanged() {
        return colorChanged;
    }

    /**
     * Sets the flag indicating if the color of the role has changed.
     * @param colorChanged true if the color has changed, false otherwise
     */
    public void setColorChanged(boolean colorChanged) {
        this.colorChanged = colorChanged;
    }

    /**
     * Checks if the mentionable status of the role has changed.
     * @return true if the mentionable status has changed, false otherwise
     */
    public boolean isPermissionsChanged() {
        return permissionsChanged;
    }

    /**
     * Sets the flag indicating if the permissions of the role have changed.
     * @param permissionsChanged true if the permissions have changed, false otherwise
     */
    public void setPermissionsChanged(boolean permissionsChanged) {
        this.permissionsChanged = permissionsChanged;
    }

    /**
     * Gets the list of changes made to the role.
     * @return the list of changes made to the role
     */
    public List<String> getChanges() {
        return changes;
    }
}
