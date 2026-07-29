package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO representing permission overrides for a specific role in a channel.
 * Contains allowed and denied permissions for the role.
 */
public class ChannelRolePermissionDTO {
    /** The ID of the channel */
    private String channelId;
    /** The ID of the role */
    private String roleId;
    /** List of permissions that are explicitly allowed */
    private List<String> allowedPermissions;
    /** List of permissions that are explicitly denied */
    private List<String> deniedPermissions;

    /**
     * Default constructor.
     */
    public ChannelRolePermissionDTO() {
        // Default constructor
    }

    /**
     * Gets the channel ID.
     * @return The channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the channel ID.
     * @param channelId The channel ID to set
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Gets the role ID.
     * @return The role ID
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Sets the role ID.
     * @param roleId The role ID to set
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Gets the list of allowed permissions.
     * @return List of allowed permission names
     */
    public List<String> getAllowedPermissions() {
        return allowedPermissions;
    }

    /**
     * Sets the list of allowed permissions.
     * @param allowedPermissions List of allowed permission names to set
     */
    public void setAllowedPermissions(List<String> allowedPermissions) {
        this.allowedPermissions = allowedPermissions;
    }

    /**
     * Gets the list of denied permissions.
     * @return List of denied permission names
     */
    public List<String> getDeniedPermissions() {
        return deniedPermissions;
    }

    /**
     * Sets the list of denied permissions.
     * @param deniedPermissions List of denied permission names to set
     */
    public void setDeniedPermissions(List<String> deniedPermissions) {
        this.deniedPermissions = deniedPermissions;
    }
}