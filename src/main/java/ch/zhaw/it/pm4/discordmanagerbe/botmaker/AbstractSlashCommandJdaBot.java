package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Abstract base class for Discord bots that handle slash commands.
 * Provides command registration, interaction handling, and lifecycle management.
 */
@Component
public abstract class AbstractSlashCommandJdaBot extends AbstractJdaBot {

    /**
     * Logger instance for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(AbstractSlashCommandJdaBot.class);

    /**
     * The type of Discord bot this instance represents.
     */
    private SlashCommandBotType botType;

    /**
     * List of slash commands registered by this bot.
     */
    private final List<JdaSlashCommand> commands = new ArrayList<>();

    /**
     * Map of button interaction handlers indexed by component ID.
     */
    private final Map<String, Consumer<ButtonInteractionEvent>> buttonHandlers = new HashMap<>();

    /**
     * Map of string select interaction handlers indexed by component ID.
     */
    private final Map<String, java.util.function.Consumer<StringSelectInteractionEvent>> selectHandlers = new HashMap<>();

    /**
     * Map of modal interaction handlers indexed by modal ID.
     */
    private final Map<String, Consumer<ModalInteractionEvent>> modalHandlers = new HashMap<>();

    /**
     * Service for managing slash command registration and execution.
     */
    private final JdaSlashCommandService slashCommandService;

    /**
     * Service for handling JDA event listeners and interaction routing.
     */
    private final JdaEventListenerService slashCommandListener;

    private String description;

    /**
     * Constructs an AbstractSlashCommandJdaBot with required dependencies.
     *
     * @param jdaBean the JDA instance for Discord API communication
     * @param slashCommandService service for managing slash commands
     * @param slashCommandListener service for handling event listeners
     */
    @Autowired
    public AbstractSlashCommandJdaBot(JDA jdaBean, JdaSlashCommandService slashCommandService, JdaEventListenerService slashCommandListener) {
        super(jdaBean);
        this.slashCommandService = slashCommandService;
        this.slashCommandListener = slashCommandListener;
    }

    /**
     * Initializes the bot by setting up commands and registering interaction handlers.
     * Called automatically after dependency injection.
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing JDA slash command bot {}", getClass().getSimpleName());
        
        // Setup commands
        setupCommands();

        registerButtonInteractionHandlers();
        registerStringInteractionHandlers();
        registerModalInteractionHandlers();
        slashCommandListener.registerButtonHandlers(buttonHandlers);
        slashCommandListener.registerStringSelectHandlers(selectHandlers);
        slashCommandListener.registerModalHandlers(modalHandlers);
        
        // Register commands with the service
        if (botType != null && !commands.isEmpty()) {
            slashCommandService.registerCommandsForBot(botType, commands);
            log.info("Registered {} slash commands for bot type {}", commands.size(), botType);
        } else {
            log.warn("Bot type or commands not set, slash commands will not be registered");
        }
    }

    /**
     * Sets up the slash commands for this bot.
     * Must be implemented by subclasses to define bot-specific commands.
     */
    protected abstract void setupCommands();

    /**
     * Registers button interaction handlers for this bot.
     * Must be implemented by subclasses to handle button interactions.
     */
    protected abstract void registerButtonInteractionHandlers();

    /**
     * Registers string select interaction handlers for this bot.
     * Must be implemented by subclasses to handle select menu interactions.
     */
    protected abstract void registerStringInteractionHandlers();

    /**
     * Registers modal interaction handlers for this bot.
     * Must be implemented by subclasses to handle modal form submissions.
     */
    protected abstract void registerModalInteractionHandlers();

    /**
     * Sets the bot type for this instance.
     *
     * @param botType the Discord bot type
     */
    protected void setBotType(SlashCommandBotType botType) {
        this.botType = botType;
    }

    /**
     * Returns the bot type of this instance.
     *
     * @return the Discord bot type
     */
    public SlashCommandBotType getBotType() {
        return botType;
    }

    /**
     * Registers a new slash command with the bot.
     *
     * @param name the command name
     * @param description the command description
     * @param commandData the JDA command data structure
     * @param handler the event handler for command execution
     */
    protected void registerCommand(String name, String description, 
                                  SlashCommandData commandData, 
                                  Consumer<SlashCommandInteractionEvent> handler) {
        if (botType == null) {
            log.warn("Bot type not set, command {} will not be registered", name);
            return;
        }
        
        JdaSlashCommand command = new JdaSlashCommand(name, description, commandData, handler, botType.name());
        commands.add(command);
        log.debug("Command {} registered for bot {}", name, botType);
    }

    /**
     * Creates a basic slash command data structure.
     *
     * @param name the command name
     * @param description the command description
     * @return SlashCommandData for further customization
     */
    protected SlashCommandData createCommand(String name, String description) {
        return Commands.slash(name, description);
    }

    /**
     * Returns all slash commands registered by this bot.
     *
     * @return a copy of the command list
     */
    public List<JdaSlashCommand> getCommands() {
        return new ArrayList<>(commands);
    }

    /**
     * Unregisters all commands and interaction handlers for this bot.
     */
    public void unregisterCommands() {
        if (botType != null) {
            slashCommandService.unregisterCommandsForBot(botType);
            removeButtonInteractionHandlers();
            removeStringInteractionHandlers();
            removeModalInteractionHandlers();
            log.info("Unregistered all commands for bot type {}", botType);
        }
    }

    /**
     * Removes all button interaction handlers from the listener service.
     */
    private void removeButtonInteractionHandlers(){
        for (String id : buttonHandlers.keySet()) {
            slashCommandListener.removeButtonHandler(id);
        }
        buttonHandlers.clear();
    }

    /**
     * Removes all string select interaction handlers from the listener service.
     */
    private void removeStringInteractionHandlers(){
        for (String id : selectHandlers.keySet()) {
            slashCommandListener.removeStringSelectHandler(id);
        }
        selectHandlers.clear();
    }

    /**
     * Removes all modal interaction handlers from the listener service.
     */
    private void removeModalInteractionHandlers(){
        for (String id : modalHandlers.keySet()) {
            slashCommandListener.removeModalHandler(id);
        }
        modalHandlers.clear();
    }

    /**
     * Registers a button interaction handler for the specified component ID.
     *
     * @param id the button component ID
     * @param handler the event handler for button interactions
     */
    public void registerButtonInteractionHandler(String id, Consumer<ButtonInteractionEvent> handler) {
        buttonHandlers.put(id, handler);
    }

    /**
     * Registers a string select interaction handler for the specified component ID.
     *
     * @param id the select menu component ID
     * @param handler the event handler for select interactions
     */
    public void registerStringInteractionHandler(String id, Consumer<StringSelectInteractionEvent> handler) {
        selectHandlers.put(id, handler);
    }

    /**
     * Registers a modal interaction handler for the specified modal ID.
     *
     * @param id the modal component ID
     * @param handler the event handler for modal submissions
     */
    public void registerModalInteractionHandler(String id, Consumer<ModalInteractionEvent> handler) {
        modalHandlers.put(id, handler);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}