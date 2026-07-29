package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO representing a bot command.
 * Contains command name and description.
 */
public class CommandDTO {
    /** The name of the command */
    private String name;
    /** The description of what the command does */
    private String description;

    /**
     * Default constructor.
     */
    public CommandDTO() {
        // Default constructor
    }

    /**
     * Gets the command name.
     * @return The command name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the command name.
     * @param name The command name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the command description.
     * @return The command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the command description.
     * @param description The command description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}