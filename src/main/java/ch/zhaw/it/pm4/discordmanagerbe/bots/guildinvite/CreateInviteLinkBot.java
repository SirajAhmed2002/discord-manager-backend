package ch.zhaw.it.pm4.discordmanagerbe.bots.guildinvite;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This class is responsible for creating an invite link for a specified guild (server).
 * It allows setting parameters for the maximum number of users and the maximum age of the invite link.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_INVITE_CREATE)
@Component
public class CreateInviteLinkBot extends AbstractJdaBot {

    /** The prefix for Discord invite links. */
    private static final String INVITE_LINK_PREFIX = "https://discord.gg/";

    /** Number of seconds in one hour, used for converting hours to seconds. */
    private static final int SECONDS_PER_HOUR = 3600;

    /** Logger instance for this class. */
    private static final Logger logger = LoggerFactory.getLogger(CreateInviteLinkBot.class);

    /** The most recently generated invite link, cached for retrieval. */
    private String inviteLink;

    /**
     * Constructs a new CreateInviteLinkBot with the provided JDA instance.
     *
     * @param jdaBean The JDA instance used for Discord API interactions
     */
    public CreateInviteLinkBot(JDA jdaBean) {
        super(jdaBean);
    }

    /**
     * Creates an invite link for the specified guild.
     * With settable parameters for max users and max age.
     *
     * @param guildId The ID of the guild to create an invite for
     * @param maxUsers The maximum number of uses for the invite (0 for unlimited)
     * @param maxAgeInHours The maximum age of the invite in hours (0 for unlimited)
     * @return The generated invite link
     * @throws IllegalArgumentException if the guild ID is not set or the guild is not found
     * @throws RuntimeException if there is an error creating the invite
     */
    public String createInviteLink(String guildId, int maxUsers, int maxAgeInHours) {
        validateGuildId(guildId);
        Guild guild = getGuildOrThrow(guildId);
        TextChannel channel = getAvailableTextChannel(guild);

        return createInvite(guild, channel, maxUsers, maxAgeInHours);
    }

    /**
     * Gets the most recently generated invite link.
     *
     * @return The invite link, or null if no invite has been generated yet
     */
    public String getInviteLink() {
        return inviteLink;
    }

    /**
     * Validates that the provided guild ID is not null or empty.
     *
     * @param guildId The guild ID to validate
     * @throws IllegalArgumentException if the guild ID is null, empty, or contains only whitespace
     */
    private void validateGuildId(String guildId) {
        if (guildId == null || guildId.trim().isEmpty()) {
            logger.error("Guild ID is not set or empty.");
            throw new IllegalArgumentException("Guild ID must be set and not empty.");
        }
    }

    /**
     * Retrieves a Guild by its ID, throwing an exception if not found.
     *
     * @param guildId The ID of the guild to retrieve
     * @return The Guild object corresponding to the provided ID
     * @throws IllegalArgumentException if no guild is found with the given ID
     */
    private Guild getGuildOrThrow(String guildId) {
        return Optional.ofNullable(jdaBean.getGuildById(guildId))
                .orElseThrow(() -> {
                    logger.error("Guild not found: {}", guildId);
                    return new IllegalArgumentException("Guild not found: " + guildId);
                });
    }

    /**
     * Finds an available text channel in the guild for invite creation.
     * Prefers the system channel, falls back to the first available text channel.
     *
     * @param guild The guild to search for text channels
     * @return A TextChannel that can be used for invite creation
     * @throws IllegalStateException if no text channels are found in the guild
     */
    private TextChannel getAvailableTextChannel(Guild guild) {
        return Optional.ofNullable(guild.getSystemChannel())
                .or(() -> {
                    logger.info("System channel not set for guild {}. Using first text channel instead.",
                            guild.getId());
                    return guild.getTextChannels().stream().findFirst();
                })
                .orElseThrow(() -> {
                    logger.error("No text channels found in guild {}", guild.getId());
                    return new IllegalStateException("No text channels found in guild");
                });
    }

    /**
     * Creates an invite link.
     *
     * @param guild The guild for which to create the invite
     * @param channel The text channel to create the invite for
     * @param maxUsers The maximum number of uses for the invite (0 for unlimited)
     * @param maxAgeInHours The maximum age of the invite in hours (0 for unlimited)
     * @return The generated invite link
     * @throws RuntimeException if invite creation fails
     */
    private String createInvite(Guild guild, TextChannel channel, int maxUsers, int maxAgeInHours) {
        try {
            Invite invite = channel.createInvite()
                    .setMaxUses(maxUsers)
                    .setMaxAge(maxAgeInHours * SECONDS_PER_HOUR)
                    .complete();

            String link = INVITE_LINK_PREFIX + invite.getCode();
            this.inviteLink = link;

            logger.info("Created invite link for guild {}: {}", guild.getId(), link);
            return link;

        } catch (Exception e) {
            String errorMessage = "Error creating invite for guild " + guild.getId() + ": " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}