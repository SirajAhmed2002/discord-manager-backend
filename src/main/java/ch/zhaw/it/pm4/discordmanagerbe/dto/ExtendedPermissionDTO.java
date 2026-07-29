package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Extended permission DTO with type classification.
 * Extends PermissionDTO to include channel and guild type flags.
 */
public class ExtendedPermissionDTO extends PermissionDTO {
    /** Flag indicating if this permission applies to channels */
    private boolean isChannel;

    /** Flag indicating if this permission applies to guilds */
    private boolean isGuild;

    /**
     * Constructor with permission key and type flags.
     * @param key The permission key
     * @param isChannel Whether this permission applies to channels
     * @param isGuild Whether this permission applies to guilds
     */
    public ExtendedPermissionDTO(String key, boolean isChannel, boolean isGuild) {
        super(key);
        this.isChannel = isChannel;
        this.isGuild = isGuild;
    }

    /**
     * Checks if this permission applies to channels.
     * @return true if this is a channel permission
     */
    public boolean isChannel() {
        return isChannel;
    }

    /**
     * Sets the channel permission flag.
     * @param isChannel Whether this permission applies to channels
     */
    public void setChannel(boolean isChannel) {
        this.isChannel = isChannel;
    }

    /**
     * Checks if this permission applies to guilds.
     * @return true if this is a guild permission
     */
    public boolean isGuild() {
        return isGuild;
    }

    /**
     * Sets the guild permission flag.
     * @param isGuild Whether this permission applies to guilds
     */
    public void setGuild(boolean isGuild) {
        this.isGuild = isGuild;
    }
}