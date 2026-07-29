package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.config;

import net.dv8tion.jda.api.Permission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for mapping custom permission names to JDA Permission enums.
 * This allows for flexibility in defining permissions without hardcoding them.
 */
@Configuration
public class PermissionMappingConfig {

    /**
     * Creates a mapping of custom permission names to JDA Permission enums.
     * @return A map where keys are custom permission names and values are JDA Permission enums.
     */
    @Bean
    public Map<String, Permission> getPermissionMapping() {
        Map<String, Permission> map = new HashMap<>();
        map.put("SEND_MESSAGES", Permission.MESSAGE_SEND);
        map.put("READ_MESSAGES", Permission.VIEW_CHANNEL);
        map.put("MANAGE_MESSAGES", Permission.MESSAGE_MANAGE);
        map.put("EMBED_LINKS", Permission.MESSAGE_EMBED_LINKS);
        map.put("ATTACH_FILES", Permission.MESSAGE_ATTACH_FILES);
        map.put("READ_MESSAGE_HISTORY", Permission.MESSAGE_HISTORY);
        map.put("MENTION_EVERYONE", Permission.MESSAGE_MENTION_EVERYONE);
        map.put("USE_EXTERNAL_EMOJIS", Permission.MESSAGE_EXT_EMOJI);
        map.put("ADD_REACTIONS", Permission.MESSAGE_ADD_REACTION);
        map.put("CONNECT", Permission.VOICE_CONNECT);
        map.put("SPEAK", Permission.VOICE_SPEAK);
        map.put("MUTE_MEMBERS", Permission.VOICE_MUTE_OTHERS);
        map.put("DEAFEN_MEMBERS", Permission.VOICE_DEAF_OTHERS);
        map.put("MOVE_MEMBERS", Permission.VOICE_MOVE_OTHERS);
        map.put("USE_VAD", Permission.VOICE_USE_VAD);
        map.put("CREATE_INSTANT_INVITE", Permission.CREATE_INSTANT_INVITE);
        map.put("KICK_MEMBERS", Permission.KICK_MEMBERS);
        map.put("BAN_MEMBERS", Permission.BAN_MEMBERS);
        map.put("ADMINISTRATOR", Permission.ADMINISTRATOR);
        map.put("MANAGE_CHANNELS", Permission.MANAGE_CHANNEL);
        map.put("MANAGE_SERVER", Permission.MANAGE_SERVER);
        map.put("MANAGE_ROLES", Permission.MANAGE_ROLES);
        map.put("MANAGE_WEBHOOKS", Permission.MANAGE_WEBHOOKS);
        return Collections.unmodifiableMap(map);
    }
}