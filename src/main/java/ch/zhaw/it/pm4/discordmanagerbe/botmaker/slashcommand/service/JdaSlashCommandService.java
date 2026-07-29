package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.DiscordServerRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing Discord slash commands registration and coordination across servers.
 * Handles command registration, unregistration, and synchronization between bot types and Discord guilds.
 */
@Service
public class JdaSlashCommandService {

    /**
     * Logger instance for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(JdaSlashCommandService.class);

    /**
     * JDA instance for Discord API interactions.
     */
    private final JDA jda;

    /**
     * Repository for Discord server data access.
     */
    private final DiscordServerRepository discordServerRepository;

    /**
     * Service for handling Discord event listeners and command execution.
     */
    private final JdaEventListenerService commandListener;

    /**
     * Thread-safe map storing commands organized by bot type.
     */
    private final Map<String, List<JdaSlashCommand>> commandsByBotType = new ConcurrentHashMap<>();

    /**
     * Constructs a new JdaSlashCommandService with required dependencies.
     *
     * @param jda the JDA instance for Discord interactions
     * @param discordServerRepository repository for server data access
     * @param commandListener service for handling command events
     */
    @Autowired
    public JdaSlashCommandService(JDA jda, 
                                 DiscordServerRepository discordServerRepository,
                                 JdaEventListenerService commandListener) {
        this.jda = jda;
        this.discordServerRepository = discordServerRepository;
        this.commandListener = commandListener;
        initialize();
    }

    /**
     * Initializes the slash command service.
     */
    public void initialize() {
        log.info("Initializing JDA slash command service");
    }

    /**
     * Registers commands for a specific bot type and updates all servers.
     *
     * @param botType the bot type to register commands for
     * @param commands the list of commands to register
     */
    public void registerCommandsForBot(SlashCommandBotType botType, List<JdaSlashCommand> commands) {
        log.info("Registering {} commands for bot type {}", commands.size(), botType);
        commandsByBotType.put(botType.name(), new ArrayList<>(commands));
        commandListener.appendCommands(commands);
        updateCommandsForAllServers();
    }

    /**
     * Unregisters all commands for a specific bot type and updates all servers.
     *
     * @param botType the bot type to unregister commands for
     */
    public void unregisterCommandsForBot(SlashCommandBotType botType) {
        List<JdaSlashCommand> commands = commandsByBotType.remove(botType.name());
        
        if (commands != null && !commands.isEmpty()) {
            commandListener.removeCommands(commands);
            updateCommandsForAllServers();
            log.info("Unregistered {} commands for bot type {}", commands.size(), botType);
        }
    }

    /**
     * Updates slash commands for all Discord servers based on their enabled bot configurations.
     */
    public void updateCommandsForAllServers() {
        List<DiscordServer> servers = discordServerRepository.findAll();
        
        for (Guild guild : jda.getGuilds()) {
            String guildId = guild.getId();

            Optional<DiscordServer> serverOpt = servers.stream()
                    .filter(s -> s.getServerId().equals(guildId))
                    .findFirst();
            
            if (serverOpt.isPresent()) {
                DiscordServer server = serverOpt.get();
                updateCommandsForServer(guild, server.getEnabledBots());
            } else {
                updateCommandsForServer(guild, new HashSet<>());
            }
        }
    }

    /**
     * Updates slash commands for a specific Discord server based on enabled bots.
     *
     * @param guild the Discord guild to update commands for
     * @param enabledBots the set of enabled bot types for this server
     */
    private void updateCommandsForServer(Guild guild, Set<SlashCommandBotType> enabledBots) {
        List<JdaSlashCommand> commands = new ArrayList<>();
        
        for (SlashCommandBotType botType : enabledBots) {
            List<JdaSlashCommand> botCommands = commandsByBotType.getOrDefault(botType.name(), Collections.emptyList());
            commands.addAll(botCommands);
        }

        if (commands.isEmpty()) {
            guild.updateCommands().queue(
                cmds -> log.info("Cleared all commands for server {}", guild.getName()),
                error -> log.error("Error clearing commands for server {}: {}", guild.getName(), error.getMessage())
            );
        } else {
            guild.updateCommands()
                .addCommands(commands.stream()
                    .map(JdaSlashCommand::getCommandData)
                    .collect(Collectors.toList()))
                .queue(
                    cmds -> log.info("Updated {} commands for server {}", cmds.size(), guild.getName()),
                    error -> log.error("Error updating commands for server {}: {}", guild.getName(), error.getMessage())
                );
        }
    }

    /**
     * Registers commands for a specific bot type on a specific server.
     *
     * @param serverId the Discord server ID
     * @param botTypeName the name of the bot type
     */
    public void registerCommandsForServer(String serverId, String botTypeName) {
        Guild guild = jda.getGuildById(serverId);
        if (guild == null) {
            log.warn("Guild with ID {} not found", serverId);
            return;
        }

        DiscordServer server = discordServerRepository.findById(serverId).orElse(null);
        if (server == null) {
            log.warn("Server with ID {} not found in database", serverId);
            return;
        }

        Set<SlashCommandBotType> enabledBots = server.getEnabledBots();
        updateCommandsForServer(guild, enabledBots);
        
        log.info("Registered commands for bot {} on server {}", botTypeName, serverId);
    }

    /**
     * Unregisters commands for a specific bot type on a specific server.
     *
     * @param serverId the Discord server ID
     * @param botTypeName the name of the bot type
     */
    public void unregisterCommandsForServer(String serverId, String botTypeName) {
        Guild guild = jda.getGuildById(serverId);
        if (guild == null) {
            log.warn("Guild with ID {} not found", serverId);
            return;
        }

        DiscordServer server = discordServerRepository.findById(serverId).orElse(null);
        if (server == null) {
            log.warn("Server with ID {} not found in database", serverId);
            return;
        }

        Set<SlashCommandBotType> enabledBots = server.getEnabledBots();
        updateCommandsForServer(guild, enabledBots);
        
        log.info("Unregistered commands for bot {} on server {}", botTypeName, serverId);
    }

    /**
     * Retrieves all registered commands for a specific bot type.
     *
     * @param botTypeName the name of the bot type
     * @return a new list containing all commands for the specified bot type
     */
    public List<JdaSlashCommand> getCommandsForBotType(String botTypeName) {
        return new ArrayList<>(commandsByBotType.getOrDefault(botTypeName, Collections.emptyList()));
    }

    /**
     * Retrieves all commands available for a specific server, organized by bot type.
     *
     * @param serverId the Discord server ID
     * @return a map of bot type names to their respective command lists
     */
    public Map<String, List<JdaSlashCommand>> getCommandsForServer(String serverId) {
        Map<String, List<JdaSlashCommand>> result = new HashMap<>();
        
        DiscordServer server = discordServerRepository.findById(serverId).orElse(null);
        if (server == null) {
            return result;
        }
        
        Set<SlashCommandBotType> enabledBots = server.getEnabledBots();
        for (SlashCommandBotType botType : enabledBots) {
            List<JdaSlashCommand> commands = commandsByBotType.getOrDefault(botType.name(), Collections.emptyList());
            if (!commands.isEmpty()) {
                result.put(botType.name(), new ArrayList<>(commands));
            }
        }
        
        return result;
    }

    /**
     * Gets the total number of registered commands across all bot types.
     *
     * @return the total command count
     */
    public int getTotalCommandCount() {
        return commandListener.getHandlerCount();
    }

    /**
     * Gets the total number of bot types that have registered commands.
     *
     * @return the number of bot types with commands
     */
    public int getBotTypeCount() {
        return commandsByBotType.size();
    }
}