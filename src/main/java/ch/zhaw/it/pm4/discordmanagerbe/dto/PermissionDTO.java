package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO representing a single Discord permission.
 * Contains the permission key identifier.
 */
public class PermissionDTO {

    /** The unique key identifier for the permission */
    private String key;

    /**
     * Default constructor.
     */
    public PermissionDTO() {
        // Default constructor
    }

    /**
     * Constructor with permission key.
     * @param key The permission key identifier
     */
    public PermissionDTO(String key) {
        this.key = key;
    }

    /**
     * Gets the permission key.
     * @return The permission key identifier
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the permission key.
     * @param key The permission key identifier to set
     */
    public void setKey(String key) {
        this.key = key;
    }
}