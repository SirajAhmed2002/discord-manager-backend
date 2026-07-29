package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * Data Transfer Object for Discord text channels.
 * Extends ChannelCategorizableDTO with text channel specific properties.
 */
public class TextChannelDTO extends ChannelCategorizableDTO {

    /** The topic description of the text channel */
    private String topic;
    /** Whether the channel is marked as NSFW (Not Safe For Work) */
    private boolean nsfw;

    /**
     * Default constructor.
     * Sets the channel type to TEXT.
     */
    public TextChannelDTO() {
        this.setChannelType(ChannelType.TEXT);
    }

    /**
     * Gets the channel topic.
     * @return The topic description of the text channel
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the channel topic.
     * @param topic The topic description to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Checks if the channel is NSFW.
     * @return true if the channel is marked as NSFW
     */
    public boolean isNsfw() {
        return nsfw;
    }

    /**
     * Sets the NSFW flag.
     * @param nsfw Whether the channel should be marked as NSFW
     */
    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }
}