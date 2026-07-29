package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Basic DTO representing a Discord role.
 * Contains the fundamental role identifier.
 */
public class RoleDTO {
    /** The unique ID of the role */
    private String id;

    /**
     * Default constructor.
     */
    public RoleDTO() {
        // Default constructor
    }

    /**
     * Constructor with role ID.
     * @param id The unique role ID
     */
    public RoleDTO(String id) {
        this.id = id;
    }

    /**
     * Gets the role ID.
     * @return The unique role ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the role ID.
     * @param id The unique role ID to set
     */
    public void setId(String id) {
        this.id = id;
    }
}