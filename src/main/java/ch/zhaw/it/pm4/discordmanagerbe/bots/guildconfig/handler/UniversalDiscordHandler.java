package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.handler;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Universal handler for Discord entities providing unified CRUD operations.
 * Supports Categories, TextChannels, and VoiceChannels through a functional approach.
 */
@Component
public class UniversalDiscordHandler {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(UniversalDiscordHandler.class);

    /** Entity type configuration for Discord categories. */
    public static final EntityType<Category> CATEGORY = new EntityType<>(
            "Category",
            (guild, dto, idResolver) -> guild.createCategory(Objects.requireNonNull(getProperty(dto, "getName"))).complete(),
            Guild::getCategoryById,
            (category, changes, idResolver) -> updateCategory(category, changes),
            category -> category.delete().complete(),
            (category, pos) -> category.getManager().setPosition(pos).complete()
    );

    /** Entity type configuration for Discord text channels. */
    public static final EntityType<TextChannel> TEXT_CHANNEL = new EntityType<>(
            "TextChannel",
            UniversalDiscordHandler::createTextChannel,
            Guild::getTextChannelById,
            UniversalDiscordHandler::updateTextChannel,
            channel -> channel.delete().complete(),
            (channel, pos) -> channel.getManager().setPosition(pos).complete()
    );

    /** Entity type configuration for Discord voice channels. */
    public static final EntityType<VoiceChannel> VOICE_CHANNEL = new EntityType<>(
            "VoiceChannel",
            UniversalDiscordHandler::createVoiceChannel,
            Guild::getVoiceChannelById,
            UniversalDiscordHandler::updateVoiceChannel,
            channel -> channel.delete().complete(),
            (channel, pos) -> channel.getManager().setPosition(pos).complete()
    );

    /**
     * Creates a Discord entity of the specified type.
     *
     * @param guild the Discord guild
     * @param dto the data transfer object containing entity properties
     * @param type the entity type configuration
     * @param idResolver function to resolve entity IDs
     * @param <T> the entity type
     * @return the created entity
     */
    public <T> T create(Guild guild, Object dto, EntityType<T> type, Function<String, String> idResolver) {
        return executeWithLogging("create " + type.name(), () ->
                type.creator().apply(guild, dto, idResolver));
    }

    /**
     * Updates a Discord entity with the provided changes.
     *
     * @param guild the Discord guild
     * @param entityId the entity ID to update
     * @param changes map of property changes
     * @param type the entity type configuration
     * @param idResolver function to resolve entity IDs
     * @param <T> the entity type
     */
    public <T> void update(Guild guild, String entityId, Map<String, Object> changes,
                           EntityType<T> type, Function<String, String> idResolver) {
        executeWithLogging("update " + type.name(), () -> {
            T entity = type.finder().apply(guild, entityId);
            if (entity != null) {
                type.updater().accept(entity, changes, idResolver);
            } else {
                log.warn("{} not found: {}", type.name(), entityId);
            }
        });
    }

    /**
     * Deletes a Discord entity.
     *
     * @param guild the Discord guild
     * @param entityId the entity ID to delete
     * @param type the entity type configuration
     * @param <T> the entity type
     */
    public <T> void delete(Guild guild, String entityId, EntityType<T> type) {
        executeWithLogging("delete " + type.name(), () -> {
            T entity = type.finder().apply(guild, entityId);
            if (entity != null) {
                type.deleter().accept(entity);
            } else {
                log.warn("{} not found: {}", type.name(), entityId);
            }
        });
    }

    /**
     * Updates the position of a Discord entity.
     *
     * @param guild the Discord guild
     * @param entityId the entity ID
     * @param position the new position
     * @param type the entity type configuration
     * @param idResolver function to resolve entity IDs
     * @param <T> the entity type
     */
    public <T> void updatePosition(Guild guild, String entityId, int position,
                                   EntityType<T> type, Function<String, String> idResolver) {
        executeWithLogging("update position for " + type.name(), () -> {
            String resolvedId = idResolver.apply(entityId);
            T entity = type.finder().apply(guild, resolvedId);
            if (entity != null) {
                type.positionUpdater().accept(entity, position);
            } else {
                log.warn("{} not found: {}", type.name(), entityId);
            }
        });
    }

    /**
     * Updates a Discord category with the provided changes.
     *
     * @param category the category to update
     * @param changes map of property changes
     */
    private static void updateCategory(Category category, Map<String, Object> changes) {
        CategoryManager manager = category.getManager();

        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            try {
                Object value = extractDesired(entry.getValue());
                if ("name".equals(entry.getKey())) {
                    manager = manager.setName((String) value);
                }
            } catch (Exception e) {
                log.error("Failed to apply change {}: {}", entry.getKey(), e.getMessage());
            }
        }

        manager.complete();
    }

    /**
     * Creates a new text channel in Discord.
     *
     * @param guild the Discord guild
     * @param dto the data transfer object containing channel properties
     * @param idResolver function to resolve entity IDs
     * @return the created text channel
     */
    private static TextChannel createTextChannel(Guild guild, Object dto, Function<String, String> idResolver) {
        String name = Objects.requireNonNull(getProperty(dto, "getName"));
        String parentId = getProperty(dto, "getParentCategoryId");
        Category parent = parentId != null ? guild.getCategoryById(idResolver.apply(parentId)) : null;

        TextChannel channel = parent != null ?
                parent.createTextChannel(name).complete() :
                guild.createTextChannel(name).complete();

        // Apply additional properties using method chaining
        TextChannelManager manager = channel.getManager();

        String topic = getProperty(dto, "getTopic");
        if (topic != null) {
            manager = manager.setTopic(topic);
        }

        Boolean nsfw = getProperty(dto, "isNsfw");
        if (nsfw != null && channel.isNSFW() != nsfw) {
            manager = manager.setNSFW(nsfw);
        }

        manager.complete();
        return channel;
    }

    /**
     * Updates a Discord text channel with the provided changes.
     *
     * @param channel the text channel to update
     * @param changes map of property changes
     * @param idResolver function to resolve entity IDs
     */
    private static void updateTextChannel(TextChannel channel, Map<String, Object> changes, Function<String, String> idResolver) {
        TextChannelManager manager = channel.getManager();

        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            try {
                Object value = extractDesired(entry.getValue());
                manager = switch (entry.getKey()) {
                    case "name" -> manager.setName((String) value);
                    case "topic" -> manager.setTopic((String) value);
                    case "nsfw" -> manager.setNSFW((Boolean) value);
                    case "parentCategoryId" -> {
                        String parentId = (String) value;
                        Category parent = parentId != null && !parentId.isEmpty() ?
                                channel.getGuild().getCategoryById(idResolver.apply(parentId)) : null;
                        yield manager.setParent(parent);
                    }
                    default -> manager;
                };
            } catch (Exception e) {
                log.error("Failed to apply change {}: {}", entry.getKey(), e.getMessage());
            }
        }

        manager.complete();
    }

    /**
     * Creates a new voice channel in Discord.
     *
     * @param guild the Discord guild
     * @param dto the data transfer object containing channel properties
     * @param idResolver function to resolve entity IDs
     * @return the created voice channel
     */
    private static VoiceChannel createVoiceChannel(Guild guild, Object dto, Function<String, String> idResolver) {
        String name = Objects.requireNonNull(getProperty(dto, "getName"));
        String parentId = getProperty(dto, "getParentCategoryId");
        Category parent = parentId != null ? guild.getCategoryById(idResolver.apply(parentId)) : null;

        VoiceChannel channel = parent != null ?
                parent.createVoiceChannel(name).complete() :
                guild.createVoiceChannel(name).complete();

        // Apply additional properties using method chaining
        VoiceChannelManager manager = channel.getManager();

        Integer userLimit = getProperty(dto, "getUserLimit");
        if (userLimit != null && userLimit > 0 && channel.getUserLimit() != userLimit) {
            manager = manager.setUserLimit(userLimit);
        }

        Integer bitrate = getProperty(dto, "getBitrate");
        if (bitrate != null && bitrate > 0 && channel.getBitrate() != bitrate) {
            manager = manager.setBitrate(bitrate);
        }

        manager.complete();
        return channel;
    }

    /**
     * Updates a Discord voice channel with the provided changes.
     *
     * @param channel the voice channel to update
     * @param changes map of property changes
     * @param idResolver function to resolve entity IDs
     */
    private static void updateVoiceChannel(VoiceChannel channel, Map<String, Object> changes, Function<String, String> idResolver) {
        VoiceChannelManager manager = channel.getManager();

        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            try {
                Object value = extractDesired(entry.getValue());
                manager = switch (entry.getKey()) {
                    case "name" -> manager.setName((String) value);
                    case "userLimit" -> manager.setUserLimit(((Number) value).intValue());
                    case "bitrate" -> manager.setBitrate(((Number) value).intValue());
                    case "parentCategoryId" -> {
                        String parentId = (String) value;
                        Category parent = parentId != null && !parentId.isEmpty() ?
                                channel.getGuild().getCategoryById(idResolver.apply(parentId)) : null;
                        yield manager.setParent(parent);
                    }
                    default -> manager;
                };
            } catch (Exception e) {
                log.error("Failed to apply change {}: {}", entry.getKey(), e.getMessage());
            }
        }

        manager.complete();
    }

    /**
     * Executes an action with logging and error handling.
     *
     * @param operation the operation description for logging
     * @param action the action to execute
     * @param <T> the return type
     * @return the result of the action
     */
    private <T> T executeWithLogging(String operation, java.util.concurrent.Callable<T> action) {
        try {
            log.debug("Executing: {}", operation);
            T result = action.call();
            log.debug("Completed: {}", operation);
            return result;
        } catch (Exception e) {
            log.error("Failed to {}: {}", operation, e.getMessage(), e);
            throw new RuntimeException("Failed to " + operation, e);
        }
    }

    /**
     * Executes a runnable action with logging and error handling.
     *
     * @param operation the operation description for logging
     * @param action the action to execute
     */
    private void executeWithLogging(String operation, Runnable action) {
        executeWithLogging(operation, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Extracts the desired value from a change object.
     *
     * @param changeValue the change value, possibly a map with "desired" key
     * @return the extracted desired value or the original value
     */
    @SuppressWarnings("unchecked")
    private static Object extractDesired(Object changeValue) {
        return Optional.ofNullable(changeValue)
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(map -> map.get("desired"))
                .orElse(changeValue);
    }

    /**
     * Generic property extraction using reflection.
     *
     * @param dto the data transfer object
     * @param methodName the method name to invoke
     * @param <T> the return type
     * @return the property value or null if extraction fails
     */
    @SuppressWarnings("unchecked")
    private static <T> T getProperty(Object dto, String methodName) {
        try {
            Method method = dto.getClass().getMethod(methodName);
            return (T) method.invoke(dto);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Functional interface for operations requiring three parameters.
     *
     * @param <T> first parameter type
     * @param <U> second parameter type
     * @param <V> third parameter type
     * @param <R> return type
     */
    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {

        /**
         * Applies this function to the given arguments.
         *
         * @param t the first argument
         * @param u the second argument
         * @param v the third argument
         * @return the function result
         */
        R apply(T t, U u, V v);
    }

    /**
     * Functional interface for consumers requiring three parameters.
     *
     * @param <T> first parameter type
     * @param <U> second parameter type
     * @param <V> third parameter type
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {

        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first argument
         * @param u the second argument
         * @param v the third argument
         */
        void accept(T t, U u, V v);
    }

    /**
     * Configuration record for Discord entity types containing all necessary operations.
     *
     * @param name human-readable name of the entity type
     * @param creator function to create new entities
     * @param finder function to find entities by ID
     * @param updater function to update entities
     * @param deleter function to delete entities
     * @param positionUpdater function to update entity positions
     * @param <T> the Discord entity type
     */
    public record EntityType<T>(
            String name,
            TriFunction<Guild, Object, Function<String, String>, T> creator,
            BiFunction<Guild, String, T> finder,
            TriConsumer<T, Map<String, Object>, Function<String, String>> updater,
            Consumer<T> deleter,
            BiConsumer<T, Integer> positionUpdater
    ) {}
}