package ch.zhaw.it.pm4.discordmanagerbe.bots.guildpermissionlist;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.dto.AllPermissionsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedPermissionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Bot for retrieving and providing detailed information about Discord permissions.
 * Identified as a server bot of type GUILD_PERMISSION_LIST.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_PERMISSION_LIST)
@Component
public class ServerPermissionListBot extends AbstractJdaBot {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(ServerPermissionListBot.class);

    /**
     * Constructs an instance of ServerPermissionListBot with the required JDA bean.
     *
     * @param jdaBean The JDA instance used for interacting with the Discord API
     */
    public ServerPermissionListBot(JDA jdaBean) {
        super(jdaBean);
    }

    /**
     * Returns all available Discord permissions with their details using DTOs.
     *
     * @return AllPermissionsDTO containing all Discord permissions with detailed information
     */
    public AllPermissionsDTO getAllPermissions() {
        List<ExtendedPermissionDTO> permissionsList = new ArrayList<>();

        for (Permission permission : Permission.values()) {
            ExtendedPermissionDTO permissionDTO = new ExtendedPermissionDTO(permission.toString(), permission.isChannel(), permission.isGuild());

            permissionsList.add(permissionDTO);
        }

        AllPermissionsDTO response = new AllPermissionsDTO(permissionsList);

        // Log the result
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            logger.info("Retrieved {} Discord permissions ({} channel, {} guild)",
                    response.getTotalCount(),
                    response.getChannelPermissionsCount(),
                    response.getGuildPermissionsCount());
            logger.debug(mapper.writeValueAsString(response));
        } catch (Exception ex) {
            logger.error("Error generating permissions JSON", ex);
        }

        return response;
    }

    /**
     * Returns a human-readable description for each permission.
     *
     * @param permission The permission to describe
     * @return String description of the permission
     */
    private String getPermissionDescription(Permission permission) {
        return switch (permission) {
            case CREATE_INSTANT_INVITE -> "Allows creating instant invites";
            case KICK_MEMBERS -> "Allows kicking members from the server";
            case BAN_MEMBERS -> "Allows banning members from the server";
            case ADMINISTRATOR -> "Allows all permissions and bypasses channel permission overwrites";
            case MANAGE_CHANNEL -> "Allows management and editing of channels";
            case MANAGE_SERVER -> "Allows management and editing of the server";
            case MESSAGE_ADD_REACTION -> "Allows adding reactions to messages";
            case VIEW_AUDIT_LOGS -> "Allows viewing of audit logs";
            case PRIORITY_SPEAKER -> "Allows for higher volume in voice channels";
            case VOICE_STREAM -> "Allows streaming in voice channels";
            case VIEW_CHANNEL -> "Allows viewing channels";
            case MESSAGE_SEND -> "Allows sending messages in channels";
            case MESSAGE_TTS -> "Allows sending text-to-speech messages";
            case MESSAGE_MANAGE -> "Allows management and deletion of messages";
            case MESSAGE_EMBED_LINKS -> "Allows embedding links in messages";
            case MESSAGE_ATTACH_FILES -> "Allows attaching files to messages";
            case MESSAGE_HISTORY -> "Allows reading message history";
            case MESSAGE_MENTION_EVERYONE -> "Allows mentioning @everyone and @here";
            case MESSAGE_EXT_EMOJI -> "Allows using external emojis";
            case VIEW_GUILD_INSIGHTS -> "Allows viewing server insights";
            case VOICE_CONNECT -> "Allows connecting to voice channels";
            case VOICE_SPEAK -> "Allows speaking in voice channels";
            case VOICE_MUTE_OTHERS -> "Allows muting members in voice channels";
            case VOICE_DEAF_OTHERS -> "Allows deafening members in voice channels";
            case VOICE_MOVE_OTHERS -> "Allows moving members between voice channels";
            case VOICE_USE_VAD -> "Allows using voice activity detection";
            case NICKNAME_CHANGE -> "Allows changing own nickname";
            case NICKNAME_MANAGE -> "Allows managing other members' nicknames";
            case MANAGE_ROLES -> "Allows management and editing of roles";
            case MANAGE_PERMISSIONS -> "Allows management of channel permissions";
            case MANAGE_WEBHOOKS -> "Allows management of webhooks";
            case MANAGE_EVENTS -> "Allows creating, editing, and deleting scheduled events";
            case MANAGE_THREADS -> "Allows managing threads";
            case MESSAGE_SEND_IN_THREADS -> "Allows sending messages in threads";
            case MESSAGE_EXT_STICKER -> "Allows using external stickers";
            case USE_EMBEDDED_ACTIVITIES -> "Allows launching activities in voice channels";
            case MODERATE_MEMBERS -> "Allows timing out members";
            default -> "Discord permission: " + permission.getName();
        };
    }
}
