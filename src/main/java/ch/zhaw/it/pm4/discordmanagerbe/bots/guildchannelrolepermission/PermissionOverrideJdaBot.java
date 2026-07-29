package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.service.PermissionOverrideService;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This bot manages permission overrides for roles in specific channels of a Discord server.
 * It allows setting, retrieving, and removing role permission overrides.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_CHANNEL_ROLE_PERMISSION)
@Component
public class PermissionOverrideJdaBot extends AbstractJdaBot {

    /**
     * Service for managing permission overrides in Discord channels.
     */
    private final PermissionOverrideService permissionService;

    /**
     * Constructor for PermissionOverrideJdaBot.
     * @param jdaBean the JDA instance used for Discord API interactions
     * @param permissionService the service for managing permission overrides
     */
    @Autowired
    public PermissionOverrideJdaBot(JDA jdaBean, PermissionOverrideService permissionService) {
        super(jdaBean);
        this.permissionService = permissionService;
    }

    /**
     * Sets a permission override for a specific role in a channel of a Discord server.
     * @param serverId the ID of the Discord server
     * @param channelId the ID of the channel where the override is applied
     * @param dto the ChannelRolePermissionsDTO containing the role and its permissions to override
     * @return true if the override was successfully applied, false otherwise
     */
    public boolean setRolePermissionOverride(String serverId, String channelId, ChannelRolePermissionsDTO dto) {
        return permissionService.applyPermissionOverrides(serverId, channelId, dto)
                .isSuccess();
    }

    /**
     * Retrieves all permission overrides for a specific channel in a Discord server.
     * @param serverId the ID of the Discord server
     * @param channelId the ID of the channel for which to retrieve permission overrides
     * @return ChannelRolePermissionsDTO containing all permission overrides for the channel
     */
    public ChannelRolePermissionsDTO getAllChannelPermissionOverrides(String serverId, String channelId) {
        return permissionService.getAllChannelPermissionOverrides(serverId, channelId)
                .getDataOrThrow();
    }

    /**
     * Removes a role permission override for a specific channel in a Discord server.
     * @param serverId the ID of the Discord server
     * @param channelId the ID of the channel from which to remove the override
     * @param roleId the ID of the role for which to remove the permission override
     * @return true if the override was successfully removed, false otherwise
     */
    public boolean removeRolePermissionOverride(String serverId, String channelId, String roleId) {
        return permissionService.removeRolePermissionOverride(serverId, channelId, roleId)
                .isSuccess();
    }
}