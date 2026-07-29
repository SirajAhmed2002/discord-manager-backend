package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Data Transfer Object for Discord voice channels.
 * Extends ChannelCategorizableDTO with voice channel specific properties.
 */
public class VoiceChannelDTO extends ChannelCategorizableDTO {
    /** The audio bitrate of the voice channel in kbps */
    private int bitrate;
    /** The maximum number of users allowed in the channel (0 = unlimited) */
    private int userLimit;

    /**
     * Default constructor.
     * Sets the channel type to VOICE.
     */
    public VoiceChannelDTO() {
        this.setChannelType(ChannelType.VOICE);
    }

    /**
     * Gets the voice channel bitrate.
     * @return The audio bitrate in kbps
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * Sets the voice channel bitrate.
     * @param bitrate The audio bitrate in kbps to set
     */
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    /**
     * Gets the user limit.
     * @return The maximum number of users allowed (0 = unlimited)
     */
    public int getUserLimit() {
        return userLimit;
    }

    /**
     * Sets the user limit.
     * @param userLimit The maximum number of users allowed to set (0 = unlimited)
     */
    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }
}