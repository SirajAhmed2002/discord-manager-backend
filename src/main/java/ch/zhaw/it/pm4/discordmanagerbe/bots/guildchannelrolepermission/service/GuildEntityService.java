package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing guild-related entities such as Guilds, Channels, and Roles.
 * Provides methods to retrieve these entities by their IDs.
 */
@Service
public class GuildEntityService {

    /**
     * The JDA instance used to interact with Discord's API.
     */
    private final JDA jda;

    /**
     * Constructor for GuildEntityService.
     * @param jda The JDA instance to use for API interactions.
     */
    @Autowired
    public GuildEntityService(JDA jda) {
        this.jda = jda;
    }

    /**
     * Retrieves a Guild by its ID.
     * @param serverId The ID of the server (guild) to retrieve.
     * @return An Optional containing the Guild if found, or empty if not found.
     */
    public Optional<Guild> getGuild(String serverId) {
        return Optional.ofNullable(jda.getGuildById(serverId));
    }

    /**
     * Retrieves a GuildChannel by its ID within a specific Guild.
     * @param guild The Guild to search within.
     * @param channelId The ID of the channel to retrieve.
     * @return An Optional containing the GuildChannel if found, or empty if not found.
     */
    public Optional<GuildChannel> getChannel(Guild guild, String channelId) {
        return Optional.ofNullable(guild.getGuildChannelById(channelId));
    }

    /**
     * Retrieves a Role by its ID within a specific Guild.
     * @param guild The Guild to search within.
     * @param roleId The ID of the role to retrieve.
     * @return An Optional containing the Role if found, or empty if not found.
     */
    public Optional<Role> getRole(Guild guild, String roleId) {
        return Optional.ofNullable(guild.getRoleById(roleId));
    }
}