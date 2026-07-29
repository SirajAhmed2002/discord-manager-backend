package ch.zhaw.it.pm4.discordmanagerbe.service;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.DiscordServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing Discord servers.
 * Provides methods to retrieve, enable, and disable bots for servers.
 */
@Service
public class DiscordServerService {

    /** Logger instance for logging events and debugging information. */
    private static final Logger log = LoggerFactory.getLogger(DiscordServerService.class);

    /** Repository for accessing and managing Discord server data. */
    private final DiscordServerRepository serverRepository;

    /**
     * Constructor for DiscordServerService.
     *
     * @param serverRepository The repository for managing Discord server data
     */
    @Autowired
    public DiscordServerService(DiscordServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }
    
    /**
     * Gets a server by its ID.
     * 
     * @param serverId The Discord server ID
     * @return Optional containing the server if found
     */
    @Transactional(readOnly = true)
    public Optional<DiscordServer> getServerById(String serverId) {
        return serverRepository.findById(serverId);
    }
    
    /**
     * Internal method to enable a bot for a server without notifying the slash command service.
     * Used by the coordinator service to avoid circular dependencies.
     * 
     * @param serverId The Discord server ID
     * @param botType The type of bot to enable
     * @return true if the bot was enabled, false if the server wasn't found
     */
    @Transactional
    public boolean enableBotInternal(String serverId, SlashCommandBotType botType) {
        Optional<DiscordServer> server = serverRepository.findById(serverId);
        if (server.isPresent()) {
            boolean changed = server.get().enableBot(botType);
            if (changed) {
                serverRepository.save(server.get());
                log.info("Enabled {} for server ID: {}", botType, serverId);
            }
            return true;
        }
        log.warn("Cannot enable bot: Server with ID {} not found", serverId);
        return false;
    }
    
    /**
     * Internal method to disable a bot for a server without notifying the slash command service.
     * Used by the coordinator service to avoid circular dependencies.
     * 
     * @param serverId The Discord server ID
     * @param botType The type of bot to disable
     * @return true if the bot was disabled, false if the server wasn't found
     */
    @Transactional
    public boolean disableBotInternal(String serverId, SlashCommandBotType botType) {
        Optional<DiscordServer> server = serverRepository.findById(serverId);
        if (server.isPresent()) {
            boolean changed = server.get().disableBot(botType);
            if (changed) {
                serverRepository.save(server.get());
                log.info("Disabled {} for server ID: {}", botType, serverId);
            }
            return true;
        }
        log.warn("Cannot disable bot: Server with ID {} not found", serverId);
        return false;
    }
    
    /**
     * Gets all servers.
     * 
     * @return List of all servers
     */
    @Transactional(readOnly = true)
    public List<DiscordServer> getAllServers() {
        return serverRepository.findAll();
    }
}