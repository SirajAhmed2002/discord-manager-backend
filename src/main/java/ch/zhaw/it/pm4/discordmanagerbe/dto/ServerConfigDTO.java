package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * Data Transfer Object representing a Discord server configuration.
 * Contains all information needed to define a server's structure including channels and categories.
 */
public class ServerConfigDTO {

    /** The unique ID of the Discord server */
    private String id;
    /** The name of the Discord server */
    private String name;
    /** List of category channels in the server */
    private List<CategoryDTO> categories;
    /** List of text channels in the server */
    private List<TextChannelDTO> textChannels;
    /** List of voice channels in the server */
    private List<VoiceChannelDTO> voiceChannels;

    /**
     * Default constructor.
     */
    public ServerConfigDTO() {
        // Default constructor
    }

    /**
     * Gets the server ID.
     * @return The Discord server ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the server ID.
     * @param id The Discord server ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the server name.
     * @return The server name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the server name.
     * @param name The server name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of categories in this server.
     * @return List of category channels
     */
    public List<CategoryDTO> getCategories() {
        return categories;
    }

    /**
     * Sets the list of categories for this server.
     * @param categories List of category channels to set
     */
    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }

    /**
     * Gets the list of text channels in this server.
     * @return List of text channels
     */
    public List<TextChannelDTO> getTextChannels() {
        return textChannels;
    }

    /**
     * Sets the list of text channels for this server.
     * @param textChannels List of text channels to set
     */
    public void setTextChannels(List<TextChannelDTO> textChannels) {
        this.textChannels = textChannels;
    }

    /**
     * Gets the list of voice channels in this server.
     * @return List of voice channels
     */
    public List<VoiceChannelDTO> getVoiceChannels() {
        return voiceChannels;
    }

    /**
     * Sets the list of voice channels for this server.
     * @param voiceChannels List of voice channels to set
     */
    public void setVoiceChannels(List<VoiceChannelDTO> voiceChannels) {
        this.voiceChannels = voiceChannels;
    }
}