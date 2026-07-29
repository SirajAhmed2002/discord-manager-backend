package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

/**
 * Specialized interface for slash command delegation handlers.
 * Extends DelegationHandler with slash command-specific operations for enhanced command management.
 */
public interface SlashCommandDelegationHandler extends DelegationHandler<SlashCommandInteractionEvent> {

    /**
     * Registers multiple slash commands with their handlers in a single operation.
     *
     * @param commands the list of slash commands to register
     */
    void registerCommands(List<JdaSlashCommand> commands);

    /**
     * Removes multiple slash commands from the handler.
     *
     * @param commands the list of commands to remove
     */
    void removeCommands(List<JdaSlashCommand> commands);

    /**
     * Removes a specific slash command by name.
     *
     * @param commandName the name of the command to remove
     * @return true if the command was successfully removed, false if not found
     */
    boolean removeCommand(String commandName);

    /**
     * Gets the names of all currently registered slash commands.
     *
     * @return a list containing all registered command names
     */
    List<String> getRegisteredCommandNames();

    /**
     * Checks if a specific slash command is currently registered.
     *
     * @param commandName the name of the command to check
     * @return true if the command is registered, false otherwise
     */
    boolean isCommandRegistered(String commandName);
}
