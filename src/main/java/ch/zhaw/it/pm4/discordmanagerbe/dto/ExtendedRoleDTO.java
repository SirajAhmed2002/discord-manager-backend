package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * Extended Role DTO with all Discord role properties.
 * Extends RoleDTO with additional role information like name, color, and permissions.
 */
public class ExtendedRoleDTO extends RoleDTO {
    /** The name of the role */
    private String name;
    /** The color of the role in hex format */
    private String color;
    /** List of permissions assigned to this role */
    private List<String> permissions;

    /**
     * Gets the role name.
     * @return The role name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the role name.
     * @param name The role name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the role color.
     * @return The role color in hex format
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the role color.
     * @param color The role color in hex format to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Gets the list of permissions for this role.
     * @return List of permission names
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Sets the list of permissions for this role.
     * @param permissions List of permission names to set
     */
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}