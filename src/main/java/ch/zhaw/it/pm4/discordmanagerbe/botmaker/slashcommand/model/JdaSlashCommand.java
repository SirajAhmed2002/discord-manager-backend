package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.function.Consumer;

/**
 * Represents a Discord slash command with its metadata, execution handler, and bot type association.
 * Encapsulates all necessary information for command registration and execution within the JDA framework.
 */
public class JdaSlashCommand {

    /**
     * The unique name identifier of the slash command.
     */
    private final String name;

    /**
     * The descriptive text explaining what the command does.
     */
    private final String description;

    /**
     * The JDA command data structure containing command definition and parameters.
     */
    private final SlashCommandData commandData;

    /**
     * The functional handler that executes when the command is invoked.
     */
    private final Consumer<SlashCommandInteractionEvent> handler;

    /**
     * The bot type this command belongs to for organizational purposes.
     */
    private final String botType;

    /**
     * Creates a new JdaSlashCommand with all required components.
     *
     * @param name the unique command name
     * @param description the command description shown to users
     * @param commandData the JDA command data structure
     * @param handler the function to execute when command is invoked
     * @param botType the bot type this command belongs to
     */
    public JdaSlashCommand(String name,
                           String description,
                           SlashCommandData commandData,
                           Consumer<SlashCommandInteractionEvent> handler,
                           String botType) {
        this.name = name;
        this.description = description;
        this.commandData = commandData;
        this.handler = handler;
        this.botType = botType;
    }

    /**
     * Gets the unique name of the slash command.
     *
     * @return the command name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the slash command.
     *
     * @return the command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the JDA command data structure for Discord registration.
     *
     * @return the command data
     */
    public SlashCommandData getCommandData() {
        return commandData;
    }

    /**
     * Gets the execution handler for this command.
     *
     * @return the command handler function
     */
    public Consumer<SlashCommandInteractionEvent> getHandler() {
        return handler;
    }

    /**
     * Gets the bot type this command is associated with.
     *
     * @return the bot type name
     */
    public String getBotType() {
        return botType;
    }
}