package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service;

import ch.zhaw.it.pm4.discordmanagerbe.dto.CategoryDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.TextChannelDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.VoiceChannelDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for comparing server configurations with actual Discord server state.
 * Identifies differences between configured and existing channels and categories.
 */
@Component
public class ServerConfigComparisonService {

    /**
     * JDA instance for Discord API interactions.
     */
    private final JDA jdaBean;

    /**
     * Constructs a new ServerConfigComparisonService.
     *
     * @param jdaBean the JDA instance for Discord API access
     */
    @Autowired
    public ServerConfigComparisonService(JDA jdaBean) {
        this.jdaBean = jdaBean;
    }

    /**
     * Compares a server configuration with the actual Discord server state.
     *
     * @param serverId the Discord server ID
     * @param config the server configuration to compare
     * @return a map containing differences between configuration and actual state
     * @throws IllegalArgumentException if the guild is not found
     */
    public Map<String, Object> compareWithDiscordServer(String serverId, ServerConfigDTO config) {
        Guild guild = Optional.ofNullable(jdaBean.getGuildById(serverId))
                .orElseThrow(() -> new IllegalArgumentException("Guild not found: " + serverId));

        Map<String, Object> diff = new HashMap<>();
        addDiffIfNotEmpty(diff, "categories", compareEntities(
                guild.getCategories(), config.getCategories(), Category::getId,
                this::createCategoryDiff, this::updateCategoryDiff));
        addDiffIfNotEmpty(diff, "textChannels", compareEntities(
                guild.getTextChannels(), config.getTextChannels(), TextChannel::getId,
                this::createTextChannelDiff, this::updateTextChannelDiff));
        addDiffIfNotEmpty(diff, "voiceChannels", compareEntities(
                guild.getVoiceChannels(), config.getVoiceChannels(), VoiceChannel::getId,
                this::createVoiceChannelDiff, this::updateVoiceChannelDiff));

        return diff;
    }

    /**
     * Compares lists of existing and configured entities to identify differences.
     *
     * @param <T> the type of existing entities
     * @param <D> the type of configuration DTOs
     * @param existing list of existing entities from Discord
     * @param config list of configured entities
     * @param idExtractor function to extract ID from existing entities
     * @param createDiffFunc function to create diff for new entities
     * @param updateDiffFunc function to create diff for updated entities
     * @return list of differences between existing and configured entities
     */
    private <T, D> List<Map<String, Object>> compareEntities(List<T> existing, List<D> config,
                                                             Function<T, String> idExtractor, Function<D, Map<String, Object>> createDiffFunc,
                                                             Function2<T, D, Map<String, Object>> updateDiffFunc) {

        Map<String, T> existingById = existing.stream().collect(Collectors.toMap(idExtractor, Function.identity()));
        Set<String> existingIds = new HashSet<>(existingById.keySet());

        List<Map<String, Object>> configDiffs = Optional.ofNullable(config).orElse(Collections.emptyList())
                .stream()
                .map(dto -> processConfigEntity(dto, existingById, createDiffFunc, updateDiffFunc))
                .filter(Objects::nonNull)
                .toList();

        Set<String> configIds = Optional.ofNullable(config).orElse(Collections.emptyList())
                .stream()
                .map(this::getId)
                .filter(id -> id.matches("\\d+"))
                .collect(Collectors.toSet());

        existingIds.removeAll(configIds);
        List<Map<String, Object>> deleteDiffs = existingIds.stream()
                .map(id -> Map.<String, Object>of("action", "delete", "id", id))
                .toList();

        return Stream.concat(configDiffs.stream(), deleteDiffs.stream())
                .collect(Collectors.toList());
    }

    /**
     * Processes a single configuration entity to determine if it needs creation or update.
     *
     * @param <T> the type of existing entity
     * @param <D> the type of configuration DTO
     * @param dto the configuration DTO to process
     * @param existingById map of existing entities by ID
     * @param createDiffFunc function to create diff for new entities
     * @param updateDiffFunc function to create diff for updated entities
     * @return diff map for the entity, or null if no changes needed
     */
    private <T, D> Map<String, Object> processConfigEntity(D dto, Map<String, T> existingById,
                                                           Function<D, Map<String, Object>> createDiffFunc,
                                                           Function2<T, D, Map<String, Object>> updateDiffFunc) {

        String id = getId(dto);
        if (!id.matches("\\d+")) {
            return createDiffFunc.apply(dto);
        }

        T existingEntity = existingById.get(id);
        if (existingEntity == null) {
            return createDiffFunc.apply(dto);
        }

        return updateDiffFunc.apply(existingEntity, dto);
    }

    /**
     * Creates a diff for a new category.
     *
     * @param dto the category DTO
     * @return diff map for category creation
     */
    private Map<String, Object> createCategoryDiff(CategoryDTO dto) {
        return Map.of("action", "create", "data", dto);
    }

    /**
     * Creates a diff for a new text channel.
     *
     * @param dto the text channel DTO
     * @return diff map for text channel creation
     */
    private Map<String, Object> createTextChannelDiff(TextChannelDTO dto) {
        return Map.of("action", "create", "data", dto);
    }

    /**
     * Creates a diff for a new voice channel.
     *
     * @param dto the voice channel DTO
     * @return diff map for voice channel creation
     */
    private Map<String, Object> createVoiceChannelDiff(VoiceChannelDTO dto) {
        return Map.of("action", "create", "data", dto);
    }

    /**
     * Creates a diff for an updated category.
     *
     * @param existing the existing Discord category
     * @param dto the desired category configuration
     * @return diff map for category update, or null if no changes needed
     */
    private Map<String, Object> updateCategoryDiff(Category existing, CategoryDTO dto) {
        Map<String, Object> changes = new HashMap<>();
        addChangeIfDifferent(changes, "name", existing.getName(), dto.getName());
        addChangeIfDifferent(changes, "position", existing.getPosition(), dto.getPosition());
        return changes.isEmpty() ? null : Map.of("action", "update", "id", dto.getId(), "changes", changes);
    }

    /**
     * Creates a diff for an updated text channel.
     *
     * @param existing the existing Discord text channel
     * @param dto the desired text channel configuration
     * @return diff map for text channel update, or null if no changes needed
     */
    private Map<String, Object> updateTextChannelDiff(TextChannel existing, TextChannelDTO dto) {
        Map<String, Object> changes = new HashMap<>();
        addChangeIfDifferent(changes, "name", existing.getName(), dto.getName());
        addChangeIfDifferent(changes, "parentCategoryId",
                existing.getParentCategory() != null ? existing.getParentCategory().getId() : null,
                dto.getParentCategoryId());
        addChangeIfDifferent(changes, "topic", existing.getTopic(), dto.getTopic());
        addChangeIfDifferent(changes, "nsfw", existing.isNSFW(), dto.isNsfw());
        addChangeIfDifferent(changes, "position", existing.getPosition(), dto.getPosition());
        return changes.isEmpty() ? null : Map.of("action", "update", "id", dto.getId(), "changes", changes);
    }

    /**
     * Creates a diff for an updated voice channel.
     *
     * @param existing the existing Discord voice channel
     * @param dto the desired voice channel configuration
     * @return diff map for voice channel update, or null if no changes needed
     */
    private Map<String, Object> updateVoiceChannelDiff(VoiceChannel existing, VoiceChannelDTO dto) {
        Map<String, Object> changes = new HashMap<>();
        addChangeIfDifferent(changes, "name", existing.getName(), dto.getName());
        addChangeIfDifferent(changes, "parentCategoryId",
                existing.getParentCategory() != null ? existing.getParentCategory().getId() : null,
                dto.getParentCategoryId());
        addChangeIfDifferent(changes, "userLimit", existing.getUserLimit(), dto.getUserLimit());
        addChangeIfDifferent(changes, "bitrate", existing.getBitrate() / 1000, dto.getBitrate());
        addChangeIfDifferent(changes, "position", existing.getPosition(), dto.getPosition());
        return changes.isEmpty() ? null : Map.of("action", "update", "id", dto.getId(), "changes", changes);
    }

    /**
     * Adds a change to the changes map if current and desired values differ.
     *
     * @param <T> the type of values being compared
     * @param changes the map to add changes to
     * @param key the property key
     * @param current the current value
     * @param desired the desired value
     */
    private <T> void addChangeIfDifferent(Map<String, Object> changes, String key, T current, T desired) {
        if (!Objects.equals(current, desired)) {
            Map<String, Object> changeMap = new HashMap<>();
            changeMap.put("current", current);
            changeMap.put("desired", desired);
            changes.put(key, changeMap);
        }
    }

    /**
     * Adds a diff list to the main diff map if the list is not empty.
     *
     * @param diff the main diff map
     * @param key the key for the diff type
     * @param diffs the list of diffs to add
     */
    private void addDiffIfNotEmpty(Map<String, Object> diff, String key, List<Map<String, Object>> diffs) {
        if (!diffs.isEmpty()) diff.put(key, diffs);
    }

    /**
     * Extracts the ID from a DTO using reflection.
     *
     * @param dto the DTO object
     * @return the ID as a string, or empty string if extraction fails
     */
    private String getId(Object dto) {
        try { return (String) dto.getClass().getMethod("getId").invoke(dto); }
        catch (Exception e) { return ""; }
    }

    /**
     * Functional interface for functions that take two parameters and return a result.
     *
     * @param <T> the type of the first parameter
     * @param <U> the type of the second parameter
     * @param <R> the type of the result
     */
    @FunctionalInterface
    private interface Function2<T, U, R> {
        R apply(T t, U u);
    }
}