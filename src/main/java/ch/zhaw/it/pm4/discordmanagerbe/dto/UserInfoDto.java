package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Data Transfer Object for user information.
 * Contains basic user details including Discord and application identifiers.
 */
public class UserInfoDto {
    /** The username in the application */
    private String username;
    /** The Discord user ID */
    private String discordId;
    /** The email address of the user */
    private String email;

    /**
     * Default constructor.
     */
    public UserInfoDto() {
        // Default constructor
    }

    /**
     * Constructor with all user information.
     * @param username The username in the application
     * @param discordId The Discord user ID
     * @param email The email address of the user
     */
    public UserInfoDto(String username, String discordId, String email) {
        this.username = username;
        this.discordId = discordId;
        this.email = email;
    }

    /**
     * Gets the username.
     * @return The username in the application
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the Discord ID.
     * @return The Discord user ID
     */
    public String getDiscordId() {
        return discordId;
    }

    /**
     * Sets the Discord ID.
     * @param discordId The Discord user ID to set
     */
    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    /**
     * Gets the email address.
     * @return The email address of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
}