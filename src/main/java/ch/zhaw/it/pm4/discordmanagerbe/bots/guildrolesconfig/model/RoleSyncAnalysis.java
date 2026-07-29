package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model;

import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Analysis result containing the changes needed for role sync.
 */
public class RoleSyncAnalysis {
    /**
     * List of roles to be created.
     */
    private final List<ExtendedRoleDTO> rolesToCreate = new ArrayList<>();

    /**
     * List of roles to be updated, containing information about current and desired state.
     */
    private final List<RoleUpdateInfo> rolesToUpdate = new ArrayList<>();

    /**
     * List of roles to be deleted, containing the current state of the roles.
     */
    private final List<Role> rolesToDelete = new ArrayList<>();


    /**
     * Adds a role to the list of roles to be created.
     * @param role the role to be created
     */
    public void addRoleToCreate(ExtendedRoleDTO role) {
        rolesToCreate.add(role);
    }

    /**
     * Adds a role update information to the list of roles to be updated.
     * @param updateInfo the information about the role update
     */
    public void addRoleToUpdate(RoleUpdateInfo updateInfo) {
        rolesToUpdate.add(updateInfo);
    }

    /**
     * Adds a list of roles to the list of roles to be deleted.
     * @param roles the list of roles to be deleted
     */
    public void addRolesToDelete(List<Role> roles) {
        rolesToDelete.addAll(roles);
    }

    /**
     * Gets the list of roles to be created. Read-only access.
     * @return unmodifiable list of roles to be created
     */
    public List<ExtendedRoleDTO> getRolesToCreate() {
        return Collections.unmodifiableList(rolesToCreate);
    }

    /**
     * Gets the list of roles to be updated. Read-only access.
     * @return unmodifiable list of role update information
     */
    public List<RoleUpdateInfo> getRolesToUpdate() {
        return Collections.unmodifiableList(rolesToUpdate);
    }

    /**
     * Gets the list of roles to be deleted. Read-only access.
     * @return unmodifiable list of roles to be deleted
     */
    public List<Role> getRolesToDelete() {
        return Collections.unmodifiableList(rolesToDelete);
    }

    /**
     * Checks if there are any roles to be created.
     * @return true if there are roles to be created, false otherwise
     */
    public boolean hasChanges() {
        return !rolesToCreate.isEmpty() || !rolesToUpdate.isEmpty() || !rolesToDelete.isEmpty();
    }

    /**
     * Checks if there are any roles to be created.
     * @return true if there are roles to be created, false otherwise
     */
    public String getSummary() {
        return String.format("Analysis: %d to create, %d to update, %d to delete",
                rolesToCreate.size(), rolesToUpdate.size(), rolesToDelete.size());
    }
}