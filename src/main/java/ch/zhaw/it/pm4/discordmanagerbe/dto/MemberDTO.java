package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO representing a Discord server member.
 * Contains member information including name, ID, and assigned roles.
 */
public class MemberDTO {
    /** The display name of the member */
    private String name;
    /** The unique ID of the member */
    private String id;
    /** List of roles assigned to this member */
    private List<RoleDTO> roles;

    /**
     * Default constructor.
     */
    public MemberDTO() {
        // Default constructor
    }

    /**
     * Gets the member name.
     * @return The member display name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the member name.
     * @param name The member display name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the member ID.
     * @return The unique member ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the member ID.
     * @param id The unique member ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the list of roles assigned to this member.
     * @return List of roles
     */
    public List<RoleDTO> getRoles() {
        return roles;
    }

    /**
     * Sets the list of roles for this member.
     * @param roles List of roles to set
     */
    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }
}