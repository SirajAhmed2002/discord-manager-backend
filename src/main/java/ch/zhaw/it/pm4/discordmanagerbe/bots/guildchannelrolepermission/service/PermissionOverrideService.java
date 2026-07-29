package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.exception.EntityNotFoundException;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.model.PermissionOverrideResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.validator.PermissionValidator;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.mapper.PermissionMapper;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing permission overrides for roles in specific channels of a Discord guild.
 * Provides methods to apply, retrieve, and remove permission overrides.
 */
@Service
public class PermissionOverrideService {

    /**
     * Logger for the PermissionOverrideService class.
     */
    private static final Logger log = LoggerFactory.getLogger(PermissionOverrideService.class);

    /**
     * Service for retrieving guild-related entities such as Guilds, Channels, and Roles.
     */
    private final GuildEntityService guildEntityService;

    /**
     * Validator for checking permission-related operations such as ID validation and conflict detection.
     */
    private final PermissionValidator validator;

    /**
     * Mapper for converting between PermissionOverride entities and DTOs.
     */
    private final PermissionMapper permissionMapper;

    /**
     * Constructor for PermissionOverrideService.
     * @param guildEntityService Service for managing guild-related entities.
     * @param validator Validator for permission operations.
     * @param permissionMapper Mapper for permission overrides.
     */
    @Autowired
    public PermissionOverrideService(
            GuildEntityService guildEntityService,
            PermissionValidator validator,
            PermissionMapper permissionMapper) {
        this.guildEntityService = guildEntityService;
        this.validator = validator;
        this.permissionMapper = permissionMapper;
    }

    /**
     * Applies permission overrides for a specific channel in a guild.
     * @param serverId the ID of the Discord server (guild) where the channel exists
     * @param channelId the ID of the channel where the permission overrides will be applied
     * @param dto the DTO containing the permission overrides to apply
     * @return PermissionOverrideResult indicating success or failure of the operation
     */
    public PermissionOverrideResult<Void> applyPermissionOverrides(
            String serverId, String channelId, ChannelRolePermissionsDTO dto) {
        
        try {
            validator.validateIds(serverId, channelId);
            
            Guild guild = guildEntityService.getGuild(serverId)
                    .orElseThrow(() -> new EntityNotFoundException("Guild not found: " + serverId));

            boolean allSuccessful = dto.getOverrides().stream()
                    .allMatch(override -> applySingleOverride(guild, channelId, override));

            return allSuccessful 
                    ? PermissionOverrideResult.success()
                    : PermissionOverrideResult.failure("Some permission overrides failed to apply");

        } catch (Exception e) {
            log.error("Failed to apply permission overrides for server {}, channel {}: {}", 
                    serverId, channelId, e.getMessage());
            return PermissionOverrideResult.failure("Failed to apply permission overrides", e);
        }
    }

    /**
     * Retrieves all permission overrides for a specific channel in a guild.
     * @param serverId the ID of the Discord server (guild) where the channel exists
     * @param channelId the ID of the channel for which to retrieve permission overrides
     * @return PermissionOverrideResult containing a DTO with the list of permission overrides
     */
    public PermissionOverrideResult<ChannelRolePermissionsDTO> getAllChannelPermissionOverrides(
            String serverId, String channelId) {
        
        try {
            validator.validateIds(serverId, channelId);
            
            Guild guild = guildEntityService.getGuild(serverId)
                    .orElseThrow(() -> new EntityNotFoundException("Guild not found: " + serverId));
                    
            GuildChannel channel = guildEntityService.getChannel(guild, channelId)
                    .orElseThrow(() -> new EntityNotFoundException("Channel not found: " + channelId));

            List<ChannelRolePermissionDTO> overrides = channel.getPermissionContainer()
                    .getPermissionOverrides().stream()
                    .filter(PermissionOverride::isRoleOverride)
                    .map(permissionMapper::mapToDTO)
                    .collect(Collectors.toList());

            ChannelRolePermissionsDTO response = new ChannelRolePermissionsDTO();
            response.setOverrides(overrides);
            
            return PermissionOverrideResult.success(response);

        } catch (Exception e) {
            log.error("Failed to retrieve permission overrides for server {}, channel {}: {}", 
                    serverId, channelId, e.getMessage());
            return PermissionOverrideResult.failure("Failed to retrieve permission overrides", e);
        }
    }

    /**
     * Removes a permission override for a specific role in a channel.
     * @param serverId the ID of the Discord server (guild) where the channel exists
     * @param channelId the ID of the channel from which to remove the permission override
     * @param roleId the ID of the role for which to remove the permission override
     * @return PermissionOverrideResult indicating success or failure of the operation
     */
    public PermissionOverrideResult<Void> removeRolePermissionOverride(
            String serverId, String channelId, String roleId) {
        
        try {
            validator.validateIds(serverId, channelId, roleId);
            
            Guild guild = guildEntityService.getGuild(serverId)
                    .orElseThrow(() -> new EntityNotFoundException("Guild not found: " + serverId));
                    
            GuildChannel channel = guildEntityService.getChannel(guild, channelId)
                    .orElseThrow(() -> new EntityNotFoundException("Channel not found: " + channelId));
                    
            Role role = guildEntityService.getRole(guild, roleId)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleId));

            return removePermissionOverride(channel, role);

        } catch (Exception e) {
            log.error("Failed to remove permission override for role {} in channel {}: {}", 
                    roleId, channelId, e.getMessage());
            return PermissionOverrideResult.failure("Failed to remove permission override", e);
        }
    }

    /**
     * Applies a single permission override for a role in a specific channel.
     * @param guild the Discord guild where the channel exists
     * @param channelId the ID of the channel where the permission override will be applied
     * @param dto the DTO containing the permission override details
     * @return true if the override was successfully applied, false otherwise
     */
    private boolean applySingleOverride(Guild guild, String channelId, ChannelRolePermissionDTO dto) {
        try {
            GuildChannel channel = guildEntityService.getChannel(guild, channelId)
                    .orElseThrow(() -> new EntityNotFoundException("Channel not found: " + channelId));
                    
            Role role = guildEntityService.getRole(guild, dto.getRoleId())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + dto.getRoleId()));

            Set<Permission> allowedPerms = permissionMapper.parsePermissions(dto.getAllowedPermissions());
            Set<Permission> deniedPerms = permissionMapper.parsePermissions(dto.getDeniedPermissions());

            validator.validateNoConflicts(allowedPerms, deniedPerms);

            channel.getPermissionContainer()
                    .upsertPermissionOverride(role)
                    .setAllowed(allowedPerms)
                    .setDenied(deniedPerms)
                    .complete();

            log.info("Successfully applied permission override for role {} in channel {}", 
                    dto.getRoleId(), channelId);
            return true;

        } catch (Exception e) {
            log.error("Failed to apply permission override for role {} in channel {}: {}", 
                    dto.getRoleId(), channelId, e.getMessage());
            return false;
        }
    }

    /**
     * Removes a permission override for a specific role in a channel.
     * @param channel the channel from which to remove the permission override
     * @param role the role for which to remove the permission override
     * @return PermissionOverrideResult indicating success or failure of the operation
     */
    private PermissionOverrideResult<Void> removePermissionOverride(GuildChannel channel, Role role) {
        Optional<PermissionOverride> overrideOpt = 
                Optional.ofNullable(channel.getPermissionContainer().getPermissionOverride(role));

        if (overrideOpt.isEmpty()) {
            String message = String.format("No permission override found for role %s in channel %s", 
                    role.getId(), channel.getId());
            log.warn(message);
            return PermissionOverrideResult.failure(message);
        }

        try {
            overrideOpt.get().delete().complete();
            log.info("Successfully removed permission override for role {} in channel {}", 
                    role.getId(), channel.getId());
            return PermissionOverrideResult.success();
        } catch (Exception e) {
            log.error("Failed to delete permission override: {}", e.getMessage());
            return PermissionOverrideResult.failure("Failed to delete permission override", e);
        }
    }
}