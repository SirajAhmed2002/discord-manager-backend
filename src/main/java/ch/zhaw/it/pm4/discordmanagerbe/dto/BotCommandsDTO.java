package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO for representing bot commands information.
 * Contains bot name and list of available commands.
 */
public class BotCommandsDTO {
    /** The name of the bot */
    private String botName;
    /** List of available commands for the bot */
    private List<CommandDTO> commands;

    /**
     * Default constructor.
     */
    public BotCommandsDTO() {
        // Default constructor
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
     * Gets the list of commands.
     * @return List of available commands
     */
    public List<CommandDTO> getCommands() {
        return commands;
    }

    /**
     * Sets the list of commands.
     * @param commands List of commands to set
     */
    public void setCommands(List<CommandDTO> commands) {
        this.commands = commands;
    }
}