package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.handler.UniversalDiscordHandler;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.handler.UniversalDiscordHandler.EntityType;
import ch.zhaw.it.pm4.discordmanagerbe.dto.CategoryDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.TextChannelDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.VoiceChannelDTO;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for synchronizing Discord server configurations with rate limiting and ID mapping.
 * Handles CRUD operations for categories, text channels, and voice channels in proper order.
 */
@Service
public class SyncService {

    /** Logger instance for this class. */
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    /** Maximum number of Discord API requests per second. */
    private static final int MAX_REQUESTS_PER_SECOND = 10;

    /** Universal Discord handler for entity operations. */
    private final UniversalDiscordHandler handler;

    /** Mapping from DTO IDs to Discord entity IDs. */
    private final Map<String, String> idMapping = new ConcurrentHashMap<>();

    /** Counter for rate limiting requests. */
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    /** Timestamp of the last rate limit reset. */
    private volatile long lastResetTime = System.currentTimeMillis();

    public SyncService(UniversalDiscordHandler handler) {
        this.handler = handler;
    }

    /**
     * Main synchronization method processing all changes in correct order.
     * @param guild the Discord guild to sync
     * @param diff the differences to apply
     * @param config the target server configuration
     */
    public void processSync(Guild guild, Map<String, Object> diff, ServerConfigDTO config) {
        idMapping.clear();
        log.info("Starting sync for guild: {}", guild.getName());

        // Process in correct order
        processEntities(guild, diff, "categories", UniversalDiscordHandler.CATEGORY, "create");
        sleep(100); // Consistency wait for categories
        processEntities(guild, diff, "categories", UniversalDiscordHandler.CATEGORY, "update");

        processEntities(guild, diff, "textChannels", UniversalDiscordHandler.TEXT_CHANNEL, "create");
        processEntities(guild, diff, "voiceChannels", UniversalDiscordHandler.VOICE_CHANNEL, "create");

        processEntities(guild, diff, "textChannels", UniversalDiscordHandler.TEXT_CHANNEL, "update");
        processEntities(guild, diff, "voiceChannels", UniversalDiscordHandler.VOICE_CHANNEL, "update");

        updateAllPositions(guild, config);

        processEntities(guild, diff, "textChannels", UniversalDiscordHandler.TEXT_CHANNEL, "delete");
        processEntities(guild, diff, "voiceChannels", UniversalDiscordHandler.VOICE_CHANNEL, "delete");
        processEntities(guild, diff, "categories", UniversalDiscordHandler.CATEGORY, "delete");

        log.info("Sync completed for guild: {}", guild.getName());
    }

    /**
     * Processes entities of a specific type and action from the diff.
     * @param guild the Discord guild
     * @param diff the differences map
     * @param entityKey the key for the entity type in the diff
     * @param entityType the entity type configuration
     * @param action the action to perform (create, update, delete)
     */
    private <T> void processEntities(Guild guild, Map<String, Object> diff, String entityKey,
                                     EntityType<T> entityType, String action) {
        getDiffList(diff, entityKey).stream()
                .filter(diffEntry -> action.equals(diffEntry.get("action")))
                .forEach(diffEntry -> executeWithRateLimit(() ->
                        processEntity(guild, diffEntry, entityType, action)));
    }

    /**
     * Processes a single entity based on the action type.
     * @param guild the Discord guild
     * @param diffEntry the diff entry for this entity
     * @param entityType the entity type configuration
     * @param action the action to perform
     */
    private <T> void processEntity(Guild guild, Map<String, Object> diffEntry,
                                   EntityType<T> entityType, String action) {
        try {
            switch (action) {
                case "create" -> {
                    Object data = diffEntry.get("data");
                    T created = handler.create(guild, data, entityType, this::resolveId);
                    storeMapping(data, created);
                }
                case "update" -> {
                    String id = (String) diffEntry.get("id");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> changes = (Map<String, Object>) diffEntry.get("changes");
                    handler.update(guild, resolveId(id), changes, entityType, this::resolveId);
                }
                case "delete" -> {
                    String id = (String) diffEntry.get("id");
                    handler.delete(guild, id, entityType);
                }
            }
        } catch (Exception e) {
            log.error("Failed to {} {}: {}", action, entityType.name(), e.getMessage(), e);
        }
    }

    /**
     * Updates positions for all entities in the configuration.
     * @param guild the Discord guild
     * @param config the server configuration containing position data
     */
    private void updateAllPositions(Guild guild, ServerConfigDTO config) {
        // Categories
        Optional.ofNullable(config.getCategories())
                .ifPresent(categories -> updatePositions(guild, categories,
                        CategoryDTO::getPosition, CategoryDTO::getId, UniversalDiscordHandler.CATEGORY));

        // Text channels grouped by parent
        Optional.ofNullable(config.getTextChannels())
                .ifPresent(channels -> updateChannelPositions(guild, channels,
                        TextChannelDTO::getPosition, TextChannelDTO::getId,
                        TextChannelDTO::getParentCategoryId, UniversalDiscordHandler.TEXT_CHANNEL));

        // Voice channels grouped by parent
        Optional.ofNullable(config.getVoiceChannels())
                .ifPresent(channels -> updateChannelPositions(guild, channels,
                        VoiceChannelDTO::getPosition, VoiceChannelDTO::getId,
                        VoiceChannelDTO::getParentCategoryId, UniversalDiscordHandler.VOICE_CHANNEL));
    }

    /**
     * Generic method to update positions for a list of entities.
     * @param guild the Discord guild
     * @param entities list of entities to position
     * @param positionExtractor function to extract position from entity
     * @param idExtractor function to extract ID from entity
     * @param entityType the entity type configuration
     */
    private <T, E> void updatePositions(Guild guild, List<E> entities,
                                        Function<E, Integer> positionExtractor,
                                        Function<E, String> idExtractor,
                                        EntityType<T> entityType) {
        entities.stream()
                .sorted(Comparator.comparing(positionExtractor))
                .forEach(entity -> {
                    int position = entities.indexOf(entity);
                    String id = idExtractor.apply(entity);
                    executeWithRateLimit(() ->
                            handler.updatePosition(guild, id, position, entityType, this::resolveId));
                });
    }

    /**
     * Updates positions for channels grouped by their parent category.
     * @param guild the Discord guild
     * @param channels list of channels to position
     * @param positionExtractor function to extract position from channel
     * @param idExtractor function to extract ID from channel
     * @param parentExtractor function to extract parent ID from channel
     * @param entityType the entity type configuration
     */
    private <T, E> void updateChannelPositions(Guild guild, List<E> channels,
                                               Function<E, Integer> positionExtractor,
                                               Function<E, String> idExtractor,
                                               Function<E, String> parentExtractor,
                                               EntityType<T> entityType) {
        channels.stream()
                .collect(Collectors.groupingBy(c ->
                        String.valueOf(parentExtractor.apply(c))))
                .values()
                .forEach(group -> updatePositions(guild, group, positionExtractor, idExtractor, entityType));
    }

    /**
     * Executes an operation with rate limiting and automatic retry on rate limit errors.
     * @param operation the operation to execute
     */
    private void executeWithRateLimit(Runnable operation) {
        // Simple rate limiting
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > 1000) {
            requestCounter.set(0);
            lastResetTime = currentTime;
        }

        if (requestCounter.incrementAndGet() > MAX_REQUESTS_PER_SECOND) {
            long waitTime = 1000 - (currentTime - lastResetTime);
            if (waitTime > 0) {
                log.info("Local rate limit reached, waiting {} ms", waitTime);
                sleep(waitTime);
            }
            requestCounter.set(1);
            lastResetTime = System.currentTimeMillis();
        }

        try {
            operation.run();
        } catch (Exception e) {
            // Enhanced rate limit error handling
            if (e.getMessage() != null && e.getMessage().contains("rate limit")) {
                log.warn("Discord rate limit encountered: {}", e.getMessage());

                // Try to extract retry-after time from exception message
                String retryAfterMs = extractRetryAfterTime(e.getMessage());
                if (retryAfterMs != null) {
                    long retryAfter = Long.parseLong(retryAfterMs);
                    log.warn("Discord requests us to wait {} ms ({} seconds) before retrying",
                            retryAfter, retryAfter / 1000.0);
                    sleep(retryAfter + 100); // Add small buffer
                } else {
                    log.warn("Could not extract retry-after time, using default 1000ms wait");
                    sleep(1000);
                }

                log.info("Retrying operation after rate limit wait...");
                executeWithRateLimit(operation); // Retry
            } else {
                throw e;
            }
        }
    }

    /**
     * Stores ID mapping from DTO to created Discord entity.
     * @param dto the data transfer object
     * @param entity the created Discord entity
     */
    private void storeMapping(Object dto, Object entity) {
        try {
            String dtoId = (String) dto.getClass().getMethod("getId").invoke(dto);
            String entityId = (String) entity.getClass().getMethod("getId").invoke(entity);
            if (dtoId != null && entityId != null) {
                idMapping.put(dtoId, entityId);
                log.debug("Stored mapping: {} -> {}", dtoId, entityId);
            }
        } catch (Exception e) {
            log.debug("Could not store ID mapping: {}", e.getMessage());
        }
    }

    /**
     * Resolves a DTO ID to a Discord entity ID using stored mappings.
     * @param id the DTO ID to resolve
     * @return the Discord entity ID or original ID if no mapping exists
     */
    private String resolveId(String id) {
        return idMapping.getOrDefault(id, id);
    }

    /**
     * Thread-safe sleep method with interrupt handling.
     * @param millis milliseconds to sleep
     */
    private void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    /**
     * Safely extracts a list from the diff map for a given component.
     * @param diff the differences map
     * @param component the component key
     * @return list of diff entries or empty list if not found
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getDiffList(Map<String, Object> diff, String component) {
        return Optional.ofNullable(diff.get(component))
                .filter(List.class::isInstance)
                .map(list -> (List<Map<String, Object>>) list)
                .orElse(Collections.emptyList());
    }

    /**
     * Extracts retry-after time from Discord rate limit error messages.
     * @param errorMessage the error message from Discord
     * @return the retry-after time in milliseconds or null if not found
     */
    private String extractRetryAfterTime(String errorMessage) {
        try {
            // Look for pattern "Retry-After: <number> ms"
            Pattern pattern = Pattern.compile("Retry-After:\\s*(\\d+)\\s*ms");
            Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.debug("Failed to extract retry-after time: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Clears all stored ID mappings for a fresh synchronization.
     */
    public void clearMappings() {
        idMapping.clear();
    }

    /**
     * Returns service statistics for monitoring.
     * @return map containing current service statistics
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "mappingCount", idMapping.size(),
                "currentRequests", requestCounter.get(),
                "lastReset", java.time.Instant.ofEpochMilli(lastResetTime)
        );
    }
}