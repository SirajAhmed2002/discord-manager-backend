package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core.DelegationHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core.SlashCommandDelegationHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers.ButtonDelegationHandlerImpl;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers.ModalDelegationHandlerImpl;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers.SlashCommandDelegationHandlerImpl;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers.StringSelectDelegationHandlerImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Main JDA event listener service that acts as a facade for Discord interactions.
 * Delegates different types of Discord interactions to specialized handlers and manages event registration.
 */
@Service
public class JdaEventListenerService extends ListenerAdapter {

    /**
     * Logger instance for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(JdaEventListenerService.class);

    /**
     * Handler for slash command interactions with enhanced type safety.
     */
    private final SlashCommandDelegationHandler slashCommandHandler;

    /**
     * Handler for button interaction events.
     */
    private final DelegationHandler<ButtonInteractionEvent> buttonHandler;

    /**
     * Handler for string select menu interaction events.
     */
    private final DelegationHandler<StringSelectInteractionEvent> stringSelectHandler;

    /**
     * Handler for modal interaction events.
     */
    private final DelegationHandler<ModalInteractionEvent> modalHandler;

    /**
     * JDA instance for Discord API interactions.
     */
    private final JDA jda;

    /**
     * Unique identifier for this service instance for debugging purposes.
     */
    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);

    /**
     * Flag to track whether this listener has been registered with JDA.
     */
    private boolean registered = false;

    /**
     * Constructs a new JdaEventListenerService with all required delegation handlers.
     *
     * @param jda the JDA instance for Discord interactions
     * @param slashCommandHandler handler for slash command interactions
     * @param buttonHandler handler for button interactions
     * @param stringSelectHandler handler for string select interactions
     * @param modalHandler handler for modal interactions
     */
    @Autowired
    public JdaEventListenerService(JDA jda,
                                   SlashCommandDelegationHandlerImpl slashCommandHandler,
                                   ButtonDelegationHandlerImpl buttonHandler,
                                   StringSelectDelegationHandlerImpl stringSelectHandler,
                                   ModalDelegationHandlerImpl modalHandler) {

        this.jda = jda;
        this.slashCommandHandler = slashCommandHandler;
        this.buttonHandler = buttonHandler;
        this.stringSelectHandler = stringSelectHandler;
        this.modalHandler = modalHandler;
        initialize();
        log.info("JdaEventListenerService created with instance ID: {}", instanceId);
    }

    /**
     * Initializes the service by registering this listener with JDA if not already registered.
     */
    public void initialize() {
        // Only register this listener once with JDA
        if (!registered) {
            jda.addEventListener(this);
            registered = true;
            log.info("JdaEventListenerService (ID: {}) registered with JDA", instanceId);
        }
    }

    /**
     * Registers multiple slash commands with the command handler.
     *
     * @param commands the list of slash commands to register
     */
    public void appendCommands(List<JdaSlashCommand> commands) {
        slashCommandHandler.registerCommands(commands);
    }

    /**
     * Removes a specific slash command by name.
     *
     * @param commandName the name of the command to remove
     * @return true if the command was successfully removed, false otherwise
     */
    public boolean removeCommand(String commandName) {
        return slashCommandHandler.removeCommand(commandName);
    }

    /**
     * Removes multiple slash commands from the handler.
     *
     * @param commands the list of commands to remove
     */
    public void removeCommands(List<JdaSlashCommand> commands) {
        slashCommandHandler.removeCommands(commands);
    }

    /**
     * Gets the names of all registered slash commands.
     *
     * @return list of registered command names
     */
    public List<String> getRegisteredCommandNames() {
        return slashCommandHandler.getRegisteredCommandNames();
    }

    /**
     * Checks if a specific command is registered.
     *
     * @param commandName the name of the command to check
     * @return true if the command is registered, false otherwise
     */
    public boolean isCommandRegistered(String commandName) {
        return slashCommandHandler.isCommandRegistered(commandName);
    }

    /**
     * Gets the total number of registered slash command handlers.
     *
     * @return the number of registered command handlers
     */
    public int getHandlerCount() {
        return slashCommandHandler.getHandlerCount();
    }

    /**
     * Registers a handler for a specific button interaction.
     *
     * @param customId the custom ID of the button
     * @param handler the handler function for the button interaction
     */
    public void registerButtonHandler(String customId, Consumer<ButtonInteractionEvent> handler) {
        buttonHandler.registerHandler(customId, handler);
    }

    /**
     * Registers multiple button handlers at once.
     *
     * @param handlers map of custom IDs to their respective handlers
     */
    public void registerButtonHandlers(Map<String, Consumer<ButtonInteractionEvent>> handlers) {
        buttonHandler.registerHandlers(handlers);
    }

    /**
     * Removes a button handler by custom ID.
     *
     * @param customId the custom ID of the button handler to remove
     * @return true if the handler was successfully removed, false otherwise
     */
    public boolean removeButtonHandler(String customId) {
        return buttonHandler.removeHandler(customId);
    }

    /**
     * Gets the total number of registered button handlers.
     *
     * @return the number of registered button handlers
     */
    public int getButtonHandlerCount() {
        return buttonHandler.getHandlerCount();
    }

    /**
     * Registers a handler for a specific string select menu interaction.
     *
     * @param customId the custom ID of the string select menu
     * @param handler the handler function for the interaction
     */
    public void registerStringSelectHandler(String customId, Consumer<StringSelectInteractionEvent> handler) {
        stringSelectHandler.registerHandler(customId, handler);
    }

    /**
     * Registers multiple string select handlers at once.
     *
     * @param handlers map of custom IDs to their respective handlers
     */
    public void registerStringSelectHandlers(Map<String, Consumer<StringSelectInteractionEvent>> handlers) {
        stringSelectHandler.registerHandlers(handlers);
    }

    /**
     * Removes a string select handler by custom ID.
     *
     * @param customId the custom ID of the handler to remove
     * @return true if the handler was successfully removed, false otherwise
     */
    public boolean removeStringSelectHandler(String customId) {
        return stringSelectHandler.removeHandler(customId);
    }

    /**
     * Gets the total number of registered string select handlers.
     *
     * @return the number of registered string select handlers
     */
    public int getStringSelectHandlerCount() {
        return stringSelectHandler.getHandlerCount();
    }

    /**
     * Registers a handler for a specific modal interaction.
     *
     * @param customId the custom ID of the modal
     * @param handler the handler function for the modal interaction
     */
    public void registerModalHandler(String customId, Consumer<ModalInteractionEvent> handler) {
        modalHandler.registerHandler(customId, handler);
    }

    /**
     * Registers multiple modal handlers at once.
     *
     * @param handlers map of custom IDs to their respective handlers
     */
    public void registerModalHandlers(Map<String, Consumer<ModalInteractionEvent>> handlers) {
        modalHandler.registerHandlers(handlers);
    }

    /**
     * Removes a modal handler by custom ID.
     *
     * @param customId the custom ID of the handler to remove
     * @return true if the handler was successfully removed, false otherwise
     */
    public boolean removeModalHandler(String customId) {
        return modalHandler.removeHandler(customId);
    }

    /**
     * Gets the total number of registered modal handlers.
     *
     * @return the number of registered modal handlers
     */
    public int getModalHandlerCount() {
        return modalHandler.getHandlerCount();
    }

    /**
     * Handles incoming slash command interactions by delegating to the slash command handler.
     *
     * @param event the slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        slashCommandHandler.handleInteraction(event);
    }

    /**
     * Handles incoming button interactions by delegating to the button handler.
     *
     * @param event the button interaction event
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        buttonHandler.handleInteraction(event);
    }

    /**
     * Handles incoming string select menu interactions by delegating to the string select handler.
     *
     * @param event the string select interaction event
     */
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        stringSelectHandler.handleInteraction(event);
    }

    /**
     * Handles incoming modal interactions by delegating to the modal handler.
     *
     * @param event the modal interaction event
     */
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        modalHandler.handleInteraction(event);
    }
}