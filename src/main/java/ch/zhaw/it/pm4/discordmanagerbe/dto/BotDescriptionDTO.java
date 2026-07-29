package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO for representing bot description information.
 * Contains bot name and its description.
 */
public class BotDescriptionDTO {
    /** The name of the bot */
    private String botName;
    /** The description of the bot */
    private String botDescription;

    /**
     * Constructor with bot name and description.
     * @param botName The name of the bot
     * @param botDescription The description of the bot
     */
    public BotDescriptionDTO(String botName, String botDescription) {
        this.botName = botName;
        this.botDescription = botDescription;
    }

    /**
     * Gets the bot name.
     * @return The bot name
     */
    public String getBotName() {
        return botName;
    }

    /**
     * Sets the bot name.
     * @param botName The bot name to set
     */
    public void setBotName(String botName) {
        this.botName = botName;
    }

    /**
     * Gets the bot description.
     * @return The bot description
     */
    public String getBotDescription() {
        return botDescription;
    }

    /**
     * Sets the bot description.
     * @param botDescription The bot description to set
     */
    public void setBotDescription(String botDescription) {
        this.botDescription = botDescription;
    }
}