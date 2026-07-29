package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service.ServerConfigComparisonService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service.SyncService;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Main bot class for synchronizing Discord server configurations.
 * Coordinates comparison and synchronization services to apply configuration changes.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_CONFIG)
@Component
public class ServerConfigJdaBot {

    /** Logger instance for this class. */
    private static final Logger log = LoggerFactory.getLogger(ServerConfigJdaBot.class);

    /** JDA instance for Discord API access. */
    private final JDA jdaBean;

    /** Service for comparing configurations with Discord state. */
    private final ServerConfigComparisonService comparisonService;

    /** Service for executing synchronization operations. */
    private final SyncService syncService;

    @Autowired
    public ServerConfigJdaBot(JDA jdaBean,
                              ServerConfigComparisonService comparisonService,
                              SyncService syncService) {
        this.jdaBean = jdaBean;
        this.comparisonService = comparisonService;
        this.syncService = syncService;
    }

    /**
     * Main synchronization method that applies configuration changes to Discord.
     * @param serverId the Discord server ID
     * @param config the target server configuration
     * @return result map containing sync status and statistics
     */
    public Map<String, Object> syncWithDiscordServer(String serverId, ServerConfigDTO config) {
        log.info("Starting sync for server: {}", serverId);
        syncService.clearMappings();
        Map<String, Object> diff = comparisonService.compareWithDiscordServer(serverId, config);

        if (diff.isEmpty()) {
            log.info("No changes needed for server: {}", serverId);
            return Map.of("status", "No changes needed");
        }

        Guild guild = Optional.ofNullable(jdaBean.getGuildById(serverId))
                .orElseThrow(() -> new IllegalArgumentException("Guild not found: " + serverId));
        log.info("Processing {} change groups for server: {}", diff.size(), serverId);
        syncService.processSync(guild, diff, config);
        Map<String, Object> result = comparisonService.compareWithDiscordServer(serverId, config);
        log.info("Sync completed for server: {}", serverId);

        return enrichResult(result);
    }

    /**
     * Enriches the sync result with metadata and statistics.
     * @param result the basic sync result
     * @return enriched result with additional metadata
     */
    private Map<String, Object> enrichResult(Map<String, Object> result) {
        return Map.of(
                "sync", result,
                "stats", syncService.getStats(),
                "timestamp", java.time.Instant.now().toString()
        );
    }

    /**
     * Returns current bot status for monitoring purposes.
     * @return status map containing JDA state and service statistics
     */
    public Map<String, Object> getStatus() {
        return Map.of(
                "jda", jdaBean.getStatus().toString(),
                "guilds", jdaBean.getGuilds().size(),
                "sync", syncService.getStats()
        );
    }
}