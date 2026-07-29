package ch.zhaw.it.pm4.discordmanagerbe.service;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Coordinator service that manages the interactions between DiscordServerService and JdaSlashCommandService
 * to avoid circular dependencies.
 */
@Service
public class JdaBotStatusCoordinatorService {

    /** Logger instance for logging events and debugging information. */
    private static final Logger log = LoggerFactory.getLogger(JdaBotStatusCoordinatorService.class);

    /** Service for managing Discord server data and bot statuses. */
    private final DiscordServerService serverService;

    /** Service for managing JDA slash commands. */
    private final JdaSlashCommandService jdaSlashCommandService;

    /**
     * Constructor for JdaBotStatusCoordinatorService.
     *
     * @param serverService The service for managing Discord server data
     * @param jdaSlashCommandService The service for managing JDA slash commands
     */
    @Autowired
    public JdaBotStatusCoordinatorService(DiscordServerService serverService,
                                          JdaSlashCommandService jdaSlashCommandService) {
        this.serverService = serverService;
        this.jdaSlashCommandService = jdaSlashCommandService;
    }

    /**
     * Enables a bot for a specific server and registers its commands.
     * 
     * @param serverId The Discord server ID
     * @param botType The type of bot to enable
     * @return true if the bot was enabled, false if the server wasn't found
     */
    public boolean enableBot(String serverId, SlashCommandBotType botType) {
        boolean success = serverService.enableBotInternal(serverId, botType);
        if (success) {
            jdaSlashCommandService.registerCommandsForServer(serverId, botType.name());
            log.info("JDA Bot {} enabled and commands registered for server {}", botType, serverId);
        }
        return success;
    }

    /**
     * Disables a bot for a specific server and unregisters its commands.
     * 
     * @param serverId The Discord server ID
     * @param botType The type of bot to disable
     * @return true if the bot was disabled, false if the server wasn't found
     */
    public boolean disableBot(String serverId, SlashCommandBotType botType) {
        boolean success = serverService.disableBotInternal(serverId, botType);
        if (success) {
            jdaSlashCommandService.unregisterCommandsForServer(serverId, botType.name());
            log.info("JDA Bot {} disabled and commands unregistered for server {}", botType, serverId);
        }
        return success;
    }
    
    /**
     * Gets a server by its ID.
     * 
     * @param serverId The Discord server ID
     * @return Optional containing the server if found
     */
    public Optional<DiscordServer> getServerById(String serverId) {
        return serverService.getServerById(serverId);
    }
    
    /**
     * Gets all slash commands for a server, grouped by bot type.
     * 
     * @param serverId The Discord server ID
     * @return Map of bot types to their commands
     */
    public Map<String, List<JdaSlashCommand>> getServerCommands(String serverId) {
        return jdaSlashCommandService.getCommandsForServer(serverId);
    }
    
    /**
     * Initializes slash commands for all bots on all enabled servers.
     * Should be called after application startup.
     */
    public void initializeAllCommands() {
        log.info("Initializing JDA slash commands for all servers");
        // Get all servers
        List<DiscordServer> allServers = serverService.getAllServers();
        
        // For each server, register commands for enabled bots
        for (DiscordServer server : allServers) {
            Set<SlashCommandBotType> enabledBots = server.getEnabledBots();
            for (SlashCommandBotType botType : enabledBots) {
                jdaSlashCommandService.registerCommandsForServer(server.getServerId(), botType.name());
            }
        }
    }
}