package ch.zhaw.it.pm4.discordmanagerbe.api;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractSlashCommandJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.JdaBotEntry;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.JdaBotService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.PermissionOverrideJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildmemberroleconfig.MemberRoleSyncResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildmemberroleconfig.MemberRolesConfigBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildmembers.MemberRolesBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.ServerConfigJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildinfo.ServerInfoJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildinvite.CreateInviteLinkBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildlist.ServerListJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildpermissionlist.ServerPermissionListBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.ServerRolesConfigBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildroleslist.ServerRolesListBot;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import ch.zhaw.it.pm4.discordmanagerbe.dto.*;
import ch.zhaw.it.pm4.discordmanagerbe.service.JdaBotStatusCoordinatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller managing Discord server operations including configuration,
 * bot management, member roles, permissions, and invite link generation.
 */
@RestController
@RequestMapping("/servers")
public class ServerManagementApiGateway {

    /** Server invite link from configuration. */
    @Value("${server.invite.link}")
    private String serverInviteLink;

    /** Service for managing JDA bot instances. */
    private final JdaBotService jdaBotService;

    /** Service coordinating bot status across servers. */
    private final JdaBotStatusCoordinatorService jdaBotStatusCoordinator;

    /** Default maximum users for invite links (0 = unlimited). */
    private static final int DEFAULT_INVITE_LINK_MAX_USERS = 0;

    /** Default maximum age for invite links in hours. */
    private static final int DEFAULT_INVITE_LINK_MAX_AGE = 24;

    /**
     * Constructs ServerManagementApiGateway with required services.
     *
     * @param jdaBotService service for bot management
     * @param jdaBotStatusCoordinator service for bot status coordination
     */
    @Autowired
    public ServerManagementApiGateway(JdaBotService jdaBotService,
                                      JdaBotStatusCoordinatorService jdaBotStatusCoordinator) {
        this.jdaBotStatusCoordinator = jdaBotStatusCoordinator;
        this.jdaBotService = jdaBotService;
    }

    /**
     * Retrieves all available Discord permissions.
     *
     * @return response containing all permission types
     */
    @GetMapping("/permissions")
    public ResponseEntity<AllPermissionsDTO> getChannelPermissions() {
        ServerPermissionListBot bot = (ServerPermissionListBot) getBot(ServerBotType.GUILD_PERMISSION_LIST).getBotInstance();
        return ResponseEntity.ok(bot.getAllPermissions());
    }

    /**
     * This endpoint returns a list of all servers with their IDs and names.
     * @param authentication Automatically injected by Spring Security
     * @return A JSON response containing server IDs and names.
     */
    @GetMapping("/")
    public ResponseEntity<ServerListDTO> getServers(Authentication authentication) {
        String discordId = getDiscordIdFromAuth(authentication);
        ServerListJdaBot bot = (ServerListJdaBot) getBot(ServerBotType.GUILD_LIST).getBotInstance();
        return ResponseEntity.ok(bot.getAdminServersDTO(discordId));
    }

    /**
     * This endpoint adds a new server.
     * @return A string indicating the result of the addition.
     */
    @GetMapping("/add-server-link")
    public ResponseEntity<ServerInviteLinkDTO> addServer(){
        ServerInviteLinkDTO serverInviteLinkDTO = new ServerInviteLinkDTO(serverInviteLink);
        return ResponseEntity.ok(serverInviteLinkDTO);
    }

    /**
     * This endpoint gets the details of a specific server.
     * @param serverId The ID of the server to retrieve.
     * @return A string containing the server details.
     */
    @GetMapping("/{serverId}")
    public ResponseEntity<ServerConfigDTO> getServerById(@PathVariable String serverId){
        try {
            ServerInfoJdaBot bot = (ServerInfoJdaBot) getBot(ServerBotType.GUILD_INFO).getBotInstance();
            ServerConfigDTO serverInfo = bot.getServerInfo(serverId);
            return ResponseEntity.ok(serverInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * This endpoint updates the configuration of a specific server.
     * @param serverId The ID of the server to update.
     * @param config The configuration JSON to update.
     * @return A string indicating the result of the update.
     */
    @PutMapping("/{serverId}/config")
    public ResponseEntity<Boolean> updateServerConfig(@PathVariable String serverId, @RequestBody ServerConfigDTO config) {
        ServerConfigJdaBot bot = (ServerConfigJdaBot) getBot(ServerBotType.GUILD_CONFIG).getBotInstance();
        bot.syncWithDiscordServer(serverId, config);
        return ResponseEntity.ok(true);
    }

    /**
     * This endpoint gets the status of all available bots for a server.
     * @param serverId The ID of the server
     * @return A JSON response containing bot status
     */
    @GetMapping("/{serverId}/bots")
    public ResponseEntity<Object> getServerBots(@PathVariable String serverId) {
        Optional<DiscordServer> server = jdaBotStatusCoordinator.getServerById(serverId);

        if (server.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"Server not found\"}");
        }

        // Create a map of all bots with their enabled/disabled status
        Map<String, Boolean> botStatusMap = new HashMap<>();
        for (SlashCommandBotType botType : SlashCommandBotType.values()) {
            if(botType != SlashCommandBotType.NONE){
                botStatusMap.put(botType.name(), server.get().isBotEnabled(botType));
            }
        }

        return ResponseEntity.ok(Map.of("bots", botStatusMap));
    }

    /**
     * Retrieves description for a specific bot type.
     *
     * @param botType bot type identifier
     * @return bot description information
     */
    @GetMapping("/bots/{botType}/description")
    public ResponseEntity<BotDescriptionDTO> getBotDescription(@PathVariable String botType) {
        SlashCommandBotType bot = SlashCommandBotType.valueOf(botType.toUpperCase());
        Optional<JdaBotEntry> botEntry = jdaBotService.getBot(bot);
        if (botEntry.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new BotDescriptionDTO("",""));
        }
        JdaBotEntry jdaBotEntry = botEntry.get();
        AbstractSlashCommandJdaBot slashCommandBot = (AbstractSlashCommandJdaBot) jdaBotEntry.getBotInstance();
        BotDescriptionDTO botDescription = new BotDescriptionDTO(botType, slashCommandBot.getDescription());

        return ResponseEntity.ok(botDescription);
    }

    /**
     * This endpoint enables a specific bot for a server.
     * @param serverId The ID of the server
     * @param botType The type of bot to enable
     * @return A JSON response indicating success or failure
     */
    @PostMapping("/{serverId}/bots/{botType}/enable")
    public ResponseEntity<Object> enableBot(@PathVariable String serverId, @PathVariable String botType) {
        try {
            SlashCommandBotType bot = SlashCommandBotType.valueOf(botType.toUpperCase());
            boolean success = jdaBotStatusCoordinator.enableBot(serverId, bot);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", bot + " has been enabled for server " + serverId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", "error",
                                "message", "Server not found"
                        ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", "error",
                            "message", "Invalid bot type: " + botType
                    ));
        }
    }

    /**
     * This endpoint disables a specific bot for a server.
     * @param serverId The ID of the server
     * @param botType The type of bot to disable
     * @return A JSON response indicating success or failure
     */
    @PostMapping("/{serverId}/bots/{botType}/disable")
    public ResponseEntity<Object> disableBot(@PathVariable String serverId, @PathVariable String botType) {
        try {
            SlashCommandBotType bot = SlashCommandBotType.valueOf(botType.toUpperCase());
            boolean success = jdaBotStatusCoordinator.disableBot(serverId, bot);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", bot + " has been disabled for server " + serverId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "status", "error",
                                "message", "Server not found"
                        ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", "error",
                            "message", "Invalid bot type: " + botType
                    ));
        }
    }

    /**
     * This endpoint gets all slash commands registered for a server, grouped by bot.
     * @param serverId The ID of the server
     * @return A JSON response containing the commands
     */
    @GetMapping("/{serverId}/commands")
    public ResponseEntity<ServerCommandsDTO> getServerCommands(@PathVariable String serverId) {
        // Get all commands for the server from JDA
        Map<String, List<JdaSlashCommand>> jdaCommands = jdaBotStatusCoordinator.getServerCommands(serverId);

        // Convert to a simpler structure for JSON
        Map<String, List<Map<String, String>>> result = new HashMap<>();
        ServerCommandsDTO serverCommandsDTO = new ServerCommandsDTO();
        List<BotCommandsDTO> botCommandsList = new ArrayList<>();

        // Add JDA commands
        jdaCommands.forEach((botType, commands) -> {
            BotCommandsDTO botCommandsDTO = new BotCommandsDTO();
            botCommandsDTO.setBotName(botType);
            List<CommandDTO> commandInfoList = commands.stream()
                    .map(cmd -> {
                        CommandDTO commandDTO = new CommandDTO();
                        commandDTO.setName(cmd.getName());
                        commandDTO.setDescription(cmd.getDescription());
                        return commandDTO;
                    })
                    .collect(Collectors.toList());
            botCommandsDTO.setCommands(commandInfoList);
            botCommandsList.add(botCommandsDTO);
        });
        serverCommandsDTO.setBotCommands(botCommandsList);

        return ResponseEntity.ok(serverCommandsDTO);
    }

    /**
     * This endpoint retrieves all members of a specified guild (server) and their roles.
     * @param guildId the ID of the target guild
     * @return a JSON response containing the member data
     */
    @GetMapping("/{guildId}/members")
    public ResponseEntity<MemberListDTO> getMemberRoles(@PathVariable String guildId) {
        MemberRolesBot bot = (MemberRolesBot) getBot(ServerBotType.GUILD_MEMBER_LIST).getBotInstance();

        try {
            return ResponseEntity.ok(bot.fetchGuildMembers(guildId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MemberListDTO("", new ArrayList<>()));
        }
    }

    /**
     * Synchronizes member roles for a server.
     *
     * @param serverId Discord server ID
     * @param members member list with role assignments
     * @return synchronization result
     */
    @PostMapping("/{serverId}/members")
    public ResponseEntity<MemberRoleSyncResult> addMemberRoles(@PathVariable String serverId, @RequestBody MemberListDTO members) {
        MemberRolesConfigBot bot = (MemberRolesConfigBot) getBot(ServerBotType.GUILD_MEMBER_ROLES_CONFIG).getBotInstance();
        return ResponseEntity.ok(bot.syncMemberRoles(serverId, members, true));
    }

    /**
     * Generates invite link for a server with optional parameters.
     *
     * @param serverId Discord server ID
     * @param invite invite configuration (optional)
     * @return generated invite link
     */
    @PostMapping("/{serverId}/invite")
    public ResponseEntity<String> getInviteLink(@PathVariable String serverId, @RequestBody(required = false) InviteDTO invite) {
        int maxAge = DEFAULT_INVITE_LINK_MAX_AGE;
        int maxUsers = DEFAULT_INVITE_LINK_MAX_USERS;
        if (invite != null) {
            maxAge = invite.getMaxAge();
            maxUsers = invite.getMaxUsers();
        }

        CreateInviteLinkBot bot = (CreateInviteLinkBot) getBot(ServerBotType.GUILD_INVITE_CREATE).getBotInstance();

        if(maxUsers > 100 || maxUsers < 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"maxUsers must be between 0 and 100\"}");
        }

        if(maxAge > 168 || maxAge < 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"maxAge must be between 0 and 168\"}");
        }

        try{
            String inviteLink = bot.createInviteLink(serverId, maxUsers, maxAge);
            if (inviteLink == null || inviteLink.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\": \"Failed to create invite link\"}");
            }

            return ResponseEntity.ok(inviteLink);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Retrieves all roles available in a server.
     *
     * @param serverId Discord server ID
     * @return server role list
     */
    @GetMapping("/{serverId}/roles")
    public ResponseEntity<ServerRoleListDTO> getRoles(@PathVariable String serverId){
        ServerRolesListBot bot = (ServerRolesListBot) getBot(ServerBotType.GUILD_ROLES_LIST).getBotInstance();
        return ResponseEntity.ok(bot.fetchGuildRoles(serverId));
    }

    /**
     * Synchronizes roles for a server.
     *
     * @param serverId Discord server ID
     * @param roles role configuration to sync
     * @return synchronization result
     */
    @PostMapping("/{serverId}/roles")
    public ResponseEntity<RoleSyncResult> addRole(@PathVariable String serverId, @RequestBody ServerRoleListDTO roles) {
        ServerRolesConfigBot bot = (ServerRolesConfigBot) getBot(ServerBotType.GUILD_ROLES_CONFIG).getBotInstance();
        return ResponseEntity.ok(bot.syncGuildRoles(serverId, roles, true));
    }

    /**
     * Retrieves role permissions for a specific channel.
     *
     * @param serverId Discord server ID
     * @param channelId Discord channel ID
     * @return channel role permissions
     */
    @GetMapping("/{serverId}/{channelId}/role-permissions")
    public ResponseEntity<ChannelRolePermissionsDTO> getChannelRolePermissions(@PathVariable String serverId, @PathVariable String channelId) {
        PermissionOverrideJdaBot bot = (PermissionOverrideJdaBot) getBot(ServerBotType.GUILD_CHANNEL_ROLE_PERMISSION).getBotInstance();

        ChannelRolePermissionsDTO overview = bot.getAllChannelPermissionOverrides(serverId, channelId);
        return ResponseEntity.ok(overview);
    }

    /**
     * Updates role permissions for a specific channel.
     *
     * @param serverId Discord server ID
     * @param channelId Discord channel ID
     * @param permissions new permission configuration
     * @return operation success status
     */
    @PostMapping("/{serverId}/{channelId}/role-permissions")
    public ResponseEntity<Boolean> setChannelRolePermissions(@PathVariable String serverId, @PathVariable String channelId,
                                                             @RequestBody ChannelRolePermissionsDTO permissions) {

        PermissionOverrideJdaBot bot = (PermissionOverrideJdaBot) getBot(ServerBotType.GUILD_CHANNEL_ROLE_PERMISSION).getBotInstance();

        boolean success = bot.setRolePermissionOverride(serverId, channelId, permissions);
        return ResponseEntity.ok(success);
    }

    /**
     * Extracts Discord ID from Spring Security authentication object.
     *
     * @param auth authentication object
     * @return Discord ID or empty string if not found
     */
    private String getDiscordIdFromAuth(Authentication auth) {
        if (auth == null) {
            return "";
        }

        Object details = auth.getDetails();
        if (details instanceof Map) {
            Object discordId = ((Map<?, ?>) details).get("discordId");
            return discordId != null ? discordId.toString() : "";
        }
        return "";
    }

    /**
     * Retrieves bot instance by server bot type.
     *
     * @param botType server bot type
     * @return JDA bot entry
     * @throws IllegalArgumentException if bot type not found
     */
    private JdaBotEntry getBot(ServerBotType botType) {
        Optional<JdaBotEntry> botOpt = jdaBotService.getBot(botType);
        if (botOpt.isPresent()) {
            return botOpt.get();
        } else {
            throw new IllegalArgumentException("Bot not found: " + botType);
        }
    }
}