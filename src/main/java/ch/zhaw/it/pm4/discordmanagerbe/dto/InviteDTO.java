package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO for Discord invite settings.
 * Contains invite expiration and usage limits.
 */
public class InviteDTO {
    /** Maximum age of the invite in seconds */
    private int maxAge;
    /** Maximum number of users that can use the invite */
    private int maxUsers;

    /**
     * Default constructor.
     */
    public InviteDTO() {
        // Default constructor
    }

    /**
     * Gets the maximum age of the invite.
     * @return Maximum age in seconds
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the maximum age of the invite.
     * @param maxAge Maximum age in seconds to set
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Gets the maximum number of users.
     * @return Maximum number of users
     */
    public int getMaxUsers() {
        return maxUsers;
    }

    /**
     * Sets the maximum number of users.
     * @param maxUsers Maximum number of users to set
     */
    public void setMaxUses(int maxUsers) {
        this.maxUsers = maxUsers;
    }
}