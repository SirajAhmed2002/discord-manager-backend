package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core.AbstractDelegationHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core.SlashCommandDelegationHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Specialized delegation handler for Discord slash command interactions.
 * Implements enhanced slash command functionality with bulk registration and command management.
 */
@Component
public class SlashCommandDelegationHandlerImpl extends AbstractDelegationHandler<SlashCommandInteractionEvent>
        implements SlashCommandDelegationHandler {

    /**
     * Constructs a new SlashCommandDelegationHandlerImpl with error handling capabilities.
     *
     * @param errorHandler the error handler for managing interaction failures
     */
    public SlashCommandDelegationHandlerImpl(InteractionErrorHandler errorHandler) {
        super(errorHandler);
    }

    /**
     * Registers multiple slash commands with their handlers in a thread-safe manner.
     *
     * @param commands the list of slash commands to register
     */
    @Override
    public synchronized void registerCommands(List<JdaSlashCommand> commands) {
        int newCommands = 0;
        int updatedCommands = 0;

        for (JdaSlashCommand command : commands) {
            String commandName = command.getName();

            if (handlers.containsKey(commandName)) {
                log.debug("Updating existing handler for command: {}", commandName);
                updatedCommands++;
            } else {
                log.debug("Adding new handler for command: {}", commandName);
                newCommands++;
            }

            handlers.put(commandName, command.getHandler());
        }

        log.info("Registered slash commands: {} new, {} updated, {} total",
                newCommands, updatedCommands, handlers.size());
    }

    /**
     * Removes a single slash command by name in a thread-safe manner.
     *
     * @param commandName the name of the command to remove
     * @return true if the command was successfully removed, false otherwise
     */
    @Override
    public synchronized boolean removeCommand(String commandName) {
        return removeHandler(commandName);
    }

    /**
     * Removes multiple slash commands in a thread-safe manner.
     *
     * @param commands the list of commands to remove
     */
    @Override
    public synchronized void removeCommands(List<JdaSlashCommand> commands) {
        int removed = 0;
        for (JdaSlashCommand command : commands) {
            if (handlers.remove(command.getName()) != null) {
                removed++;
            }
        }
        log.info("Removed {} slash commands, {} remaining", removed, handlers.size());
    }

    /**
     * Gets the names of all currently registered slash commands.
     *
     * @return a new list containing all registered command names
     */
    @Override
    public List<String> getRegisteredCommandNames() {
        return new ArrayList<>(handlers.keySet());
    }

    /**
     * Checks if a specific slash command is currently registered.
     *
     * @param commandName the name of the command to check
     * @return true if the command is registered, false otherwise
     */
    @Override
    public boolean isCommandRegistered(String commandName) {
        return handlers.containsKey(commandName);
    }

    /**
     * Extracts the unique identifier from a slash command interaction event.
     *
     * @param event the slash command interaction event
     * @return the command name as the identifier
     */
    @Override
    protected String extractIdentifier(SlashCommandInteractionEvent event) {
        return event.getName();
    }

    /**
     * Provides the handler type name for logging and debugging purposes.
     *
     * @return the string "slash command" identifying this handler type
     */
    @Override
    protected String getHandlerTypeName() {
        return "slash command";
    }

    /**
     * Handles errors that occur during slash command interaction processing.
     *
     * @param event the slash command interaction event that caused the error
     * @param identifier the command name where the error occurred
     * @param e the exception that was thrown
     */
    @Override
    protected void handleError(SlashCommandInteractionEvent event, String identifier, Exception e) {
        errorHandler.handleInteractionError(event, identifier, e);
    }

    /**
     * Handles cases where no handler is registered for a slash command.
     * Responds to the user with an error message about the unknown command.
     *
     * @param event the slash command interaction event with no registered handler
     * @param identifier the command name that has no handler
     */
    @Override
    protected void handleNoHandlerFound(SlashCommandInteractionEvent event, String identifier) {
        log.warn("No handler found for slash command: {}", identifier);
        if (!event.isAcknowledged()) {
            event.reply("Unknown command: " + identifier)
                    .setEphemeral(true)
                    .queue();
        }
    }
}