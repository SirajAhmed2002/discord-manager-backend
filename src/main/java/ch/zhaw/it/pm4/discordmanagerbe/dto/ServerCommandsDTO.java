package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO representing all bot commands available on a Discord server.
 * Contains a list of bot command configurations.
 */
public class ServerCommandsDTO {
    /** List of bot commands available on the server */
    private List<BotCommandsDTO> botCommands;

    /**
     * Default constructor.
     */
    public ServerCommandsDTO() {
        // Default constructor
    }

    /**
     * Gets the list of bot commands.
     * @return List of bot command configurations
     */
    public List<BotCommandsDTO> getBotCommands() {
        return botCommands;
    }

    /**
     * Sets the list of bot commands.
     * @param botCommands List of bot command configurations to set
     */
    public void setBotCommands(List<BotCommandsDTO> botCommands) {
        this.botCommands = botCommands;
    }
}