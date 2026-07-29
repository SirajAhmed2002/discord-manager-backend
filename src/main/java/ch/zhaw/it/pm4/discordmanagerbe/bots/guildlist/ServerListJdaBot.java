package ch.zhaw.it.pm4.discordmanagerbe.bots.guildlist;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.DiscordServerRepository;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerListDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Discord bot for managing and retrieving guild/server lists.
 * Handles server synchronization with database and admin permission checks.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_LIST)
@Component
public class ServerListJdaBot extends AbstractJdaBot {

    /** Logger for this class */
    private static final Logger log = LoggerFactory.getLogger(ServerListJdaBot.class);

    /** Permissions that grant effective admin privileges */
    private static final EnumSet<Permission> ADMIN_LIKE_PERMISSIONS = EnumSet.of(
            Permission.MANAGE_SERVER,
            Permission.MANAGE_ROLES,
            Permission.MANAGE_CHANNEL,
            Permission.BAN_MEMBERS,
            Permission.KICK_MEMBERS
    );

    /** JSON object mapper for serialization */
    private final ObjectMapper objectMapper;

    /** Repository for Discord server database operations */
    private final DiscordServerRepository discordServerRepository;

    /**
     * Constructs the bot with JDA and repository dependencies.
     *
     * @param jdaBean JDA instance for Discord API interactions
     * @param discordServerRepository repository for server data persistence
     */
    @Autowired
    public ServerListJdaBot(JDA jdaBean, DiscordServerRepository discordServerRepository) {
        super(jdaBean);
        this.discordServerRepository = discordServerRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Saves or updates a guild in the database.
     *
     * @param guild the Discord guild to save
     */
    private void saveGuildToDatabase(Guild guild) {
        try {
            log.info("Explicitly saving guild to database: {} (ID: {})", guild.getName(), guild.getId());

            DiscordServer server = discordServerRepository.findByServerId(guild.getId())
                    .map(existing -> updateExistingServer(existing, guild))
                    .orElseGet(() -> createNewServer(guild));

            discordServerRepository.save(server);
            log.info("Successfully saved guild to database: {}", guild.getId());

        } catch (Exception e) {
            log.error("ERROR saving guild to database: {} - {}", guild.getId(), e.getMessage(), e);
        }
    }

    /**
     * Creates a new DiscordServer entity from a guild.
     *
     * @param guild the Discord guild
     * @return new DiscordServer entity
     */
    private DiscordServer createNewServer(Guild guild) {
        DiscordServer server = new DiscordServer(guild.getId(), guild.getName(), guild.getOwnerId());
        log.info("Created new DiscordServer: {}", server);
        return server;
    }

    /**
     * Updates an existing DiscordServer entity with guild data.
     *
     * @param server existing DiscordServer entity
     * @param guild the Discord guild with updated data
     * @return updated DiscordServer entity
     */
    private DiscordServer updateExistingServer(DiscordServer server, Guild guild) {
        server.setServerName(guild.getName());
        server.setOwnerId(guild.getOwnerId());
        log.info("Updated existing DiscordServer: {}", server);
        return server;
    }

    /**
     * Synchronizes guild list with database.
     *
     * @param guilds list of guilds to synchronize
     */
    private void syncGuildsToDatabase(List<Guild> guilds) {
        guilds.forEach(this::saveGuildToDatabase);
    }

    /**
     * Retrieves a member from a guild by user ID.
     *
     * @param guild the Discord guild
     * @param userId the user ID to search for
     * @return Optional containing the member if found
     */
    private Optional<Member> getMemberFromGuild(Guild guild, String userId) {
        try {
            return Optional.ofNullable(guild.retrieveMemberById(userId).complete());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if a member has admin privileges in a guild.
     *
     * @param member the guild member
     * @param guild the Discord guild
     * @return true if member has admin privileges
     */
    private boolean isUserAdmin(Member member, Guild guild) {
        return member.hasPermission(Permission.ADMINISTRATOR) || isEffectiveAdmin(member, guild);
    }

    /**
     * Checks if a member has effective admin privileges.
     *
     * @param member the guild member
     * @param guild the Discord guild
     * @return true if member has effective admin privileges
     */
    private boolean isEffectiveAdmin(Member member, Guild guild) {
        return guild.getOwnerIdLong() == member.getIdLong() ||
                member.hasPermission(ADMIN_LIKE_PERMISSIONS);
    }

    /**
     * Gets all guilds where the specified user has admin permissions.
     *
     * @param userId the Discord user ID to check
     * @return list of guilds where user is admin
     */
    public List<Guild> getGuildsWhereUserIsAdmin(String userId) {
        return jdaBean.getGuilds().stream()
                .filter(guild -> getMemberFromGuild(guild, userId)
                        .map(member -> isUserAdmin(member, guild))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    /**
     * Gets all guilds the bot is a member of.
     *
     * @return list of all guilds
     */
    public List<Guild> getAllGuilds() {
        return jdaBean.getGuilds();
    }

    /**
     * Converts a Guild object to a ServerDTO.
     *
     * @param guild the guild to convert
     * @return ServerDTO with guild information
     */
    private ServerDTO convertGuildToServerDTO(Guild guild) {
        ServerDTO serverDTO = new ServerDTO();
        serverDTO.setId(guild.getId());
        serverDTO.setName(guild.getName());
        serverDTO.setOwnerId(guild.getOwnerId());
        serverDTO.setMemberCount(guild.getMemberCount());
        serverDTO.setCreationDate(guild.getTimeCreated());
        return serverDTO;
    }

    /**
     * Converts guild list to ServerListDTO and syncs to database.
     *
     * @param guilds list of guilds to convert
     * @return ServerListDTO containing server information
     */
    private ServerListDTO convertGuildsToServerListDTO(List<Guild> guilds) {
        syncGuildsToDatabase(guilds);

        List<ServerDTO> serverDTOs = guilds.stream()
                .map(this::convertGuildToServerDTO)
                .collect(Collectors.toList());

        ServerListDTO serverListDTO = new ServerListDTO();
        serverListDTO.setServers(serverDTOs);
        return serverListDTO;
    }

    /**
     * Gets all servers the bot is a member of in DTO format.
     *
     * @return ServerListDTO containing all servers
     */
    public ServerListDTO getAllServersDTO() {
        return convertGuildsToServerListDTO(getAllGuilds());
    }

    /**
     * Gets servers where the specified user has admin permissions in DTO format.
     *
     * @param userId the Discord user ID to check
     * @return ServerListDTO containing admin servers
     */
    public ServerListDTO getAdminServersDTO(String userId) {
        return convertGuildsToServerListDTO(getGuildsWhereUserIsAdmin(userId));
    }

    /**
     * Gets admin servers as JSON string.
     *
     * @param userId the Discord user ID to check
     * @return JSON string representation of admin servers
     * @throws JsonProcessingException if JSON processing fails
     */
    public String getAdminServersJson(String userId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(getAdminServersDTO(userId));
    }

    /**
     * Gets all servers as JSON string.
     *
     * @return JSON string representation of all servers
     * @throws JsonProcessingException if JSON processing fails
     */
    public String getAllServersJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(getAllServersDTO());
    }

    /**
     * Gets all servers from database with current sync.
     *
     * @return list of DiscordServer entities from database
     */
    public List<DiscordServer> getAllServersFromDatabase() {
        syncGuildsToDatabase(getAllGuilds());
        return discordServerRepository.findAll();
    }

    /**
     * Gets servers owned by a specific user from database.
     *
     * @param ownerId the Discord user ID of the owner
     * @return list of DiscordServer entities owned by user
     */
    public List<DiscordServer> getServersByOwner(String ownerId) {
        syncGuildsToDatabase(getAllGuilds());
        return discordServerRepository.findByOwnerId(ownerId);
    }

    /**
     * Synchronizes all current guilds to database.
     */
    public void syncGuildsToDatabase() {
        List<Guild> currentGuilds = getAllGuilds();
        log.info("Syncing {} guilds to database", currentGuilds.size());
        syncGuildsToDatabase(currentGuilds);
    }
}