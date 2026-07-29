package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * Data Transfer Object for channel permission overview response.
 * Contains all permission overrides for a channel.
 */
public class ChannelRolePermissionsDTO {
    /** List of permission overrides for roles in the channel */
    private List<ChannelRolePermissionDTO> overrides;

    /**
     * Default constructor.
     */
    public ChannelRolePermissionsDTO() {
        // Default constructor
    }

    /**
     * Gets the list of permission overrides.
     * @return List of channel role permission overrides
     */
    public List<ChannelRolePermissionDTO> getOverrides() {
        return overrides;
    }

    /**
     * Sets the list of permission overrides.
     * @param overrides List of permission overrides to set
     */
    public void setOverrides(List<ChannelRolePermissionDTO> overrides) {
        this.overrides = overrides;
    }
}