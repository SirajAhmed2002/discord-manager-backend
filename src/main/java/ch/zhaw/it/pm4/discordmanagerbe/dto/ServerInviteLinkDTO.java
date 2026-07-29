package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO representing a Discord server invite link.
 * Contains the generated invite URL for joining the server.
 */
public class ServerInviteLinkDTO {
    /** The Discord invite link URL */
    private String inviteLink;

    /**
     * Constructor with invite link.
     * @param inviteLink The Discord invite link URL
     */
    public ServerInviteLinkDTO(String inviteLink) {
        this.inviteLink = inviteLink;
    }

    /**
     * Gets the invite link.
     * @return The Discord invite link URL
     */
    public String getInviteLink() {
        return inviteLink;
    }

    /**
     * Sets the invite link.
     * @param inviteLink The Discord invite link URL to set
     */
    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }
}