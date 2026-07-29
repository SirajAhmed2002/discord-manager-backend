package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO representing a collection of all Discord permissions.
 * Provides categorized access to channel and guild permissions.
 */
public class AllPermissionsDTO {

    /** List of all extended permissions */
    private final List<ExtendedPermissionDTO> permissions;

    /** Total count of all permissions */
    private int totalCount;

    /** Count of channel-specific permissions */
    private int channelPermissionsCount;

    /** Count of guild-specific permissions */
    private int guildPermissionsCount;

    /**
     * Default constructor initializing empty permissions list.
     */
    public AllPermissionsDTO() {
        this.permissions = new ArrayList<>();
    }

    /**
     * Constructor with permissions list.
     * @param permissions List of extended permissions, null-safe
     */
    public AllPermissionsDTO(List<ExtendedPermissionDTO> permissions) {
        this.permissions = permissions != null ? permissions : new ArrayList<>();
    }

    /**
     * Gets the total count of all permissions.
     * @return Total permission count
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Gets the count of channel-specific permissions.
     * @return Channel permissions count
     */
    public int getChannelPermissionsCount() {
        return channelPermissionsCount;
    }

    /**
     * Gets the count of guild-specific permissions.
     * @return Guild permissions count
     */
    public int getGuildPermissionsCount() {
        return guildPermissionsCount;
    }

    /**
     * Gets permissions filtered by channel type.
     * @return List of channel-specific permissions
     */
    public List<PermissionDTO> getChannelPermissions() {
        return permissions
                .stream()
                .filter(ExtendedPermissionDTO::isChannel)
                .map(permission -> new PermissionDTO(permission.getKey()))
                .collect(Collectors.toList());
    }

    /**
     * Gets permissions filtered by guild type.
     * @return List of guild-specific permissions
     */
    public List<PermissionDTO> getGuildPermissions() {
        return permissions
                .stream()
                .filter(ExtendedPermissionDTO::isGuild)
                .map(permission -> new PermissionDTO(permission.getKey()))
                .collect(Collectors.toList());
    }
}