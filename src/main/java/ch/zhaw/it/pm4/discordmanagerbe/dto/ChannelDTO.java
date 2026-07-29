package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Base Data Transfer Object for Discord channels.
 * Contains common properties for all channel types.
 */
public class ChannelDTO {
    /** The unique ID of the channel */
    private String id;
    /** The name of the channel */
    private String name;
    /** The type of the channel (TEXT, VOICE, CATEGORY) */
    private ChannelType channelType;
    /** The position of the channel in the channel list */
    private int position;

    /**
     * Default constructor.
     */
    public ChannelDTO() {
        // Default constructor
    }

    /**
     * Gets the channel ID.
     * @return The channel ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the channel ID.
     * @param id The channel ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the channel name.
     * @return The channel name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the channel name.
     * @param name The channel name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the channel type.
     * @return The channel type
     */
    public ChannelType getChannelType() {
        return channelType;
    }

    /**
     * Sets the channel type.
     * @param channelType The channel type to set
     */
    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * Gets the channel position.
     * @return The channel position in the list
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the channel position.
     * @param position The channel position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }
}