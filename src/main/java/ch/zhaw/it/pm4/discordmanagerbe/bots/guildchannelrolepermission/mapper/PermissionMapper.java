package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.mapper;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.config.PermissionMappingConfig;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ChannelRolePermissionDTO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PermissionOverride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    /**
     * Logger for the PermissionMapper class.
     */
    private static final Logger log = LoggerFactory.getLogger(PermissionMapper.class);

    /**
     * Configuration for custom permission mappings.
     */
    private final PermissionMappingConfig mappingConfig;

    /**
     * Constructor for PermissionMapper.
     * @param mappingConfig The configuration containing custom permission mappings.
     */
    @Autowired
    public PermissionMapper(PermissionMappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    /**
     * Parses a list of permission strings into a set of Permission enums.
     * @param permissionStrings List of permission strings to parse.
     * @return Set of Permission enums corresponding to the input strings.
     */
    public Set<Permission> parsePermissions(List<String> permissionStrings) {
        if (permissionStrings == null) {
            return Collections.emptySet();
        }

        return permissionStrings.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parsePermission)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Converts a set of Permission enums to a sorted list of their string representations.
     * @param permissions Set of Permission enums to convert.
     * @return Sorted list of permission names as strings.
     */
    public List<String> permissionsToStringList(Set<Permission> permissions) {
        return permissions.stream()
                .map(Permission::name)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Maps a PermissionOverride object to a ChannelRolePermissionDTO.
     * @param override The PermissionOverride object to map.
     * @return ChannelRolePermissionDTO containing the role ID, channel ID,
     */
    public ChannelRolePermissionDTO mapToDTO(PermissionOverride override) {
        ChannelRolePermissionDTO dto = new ChannelRolePermissionDTO();
        dto.setRoleId(Objects.requireNonNull(override.getRole()).getId());
        dto.setChannelId(override.getChannel().getId());
        dto.setAllowedPermissions(permissionsToStringList(override.getAllowed()));
        dto.setDeniedPermissions(permissionsToStringList(override.getDenied()));
        return dto;
    }

    /**
     * Parses a single permission string into a Permission enum.
     * @param permissionStr The permission string to parse, e.g., "MANAGE_CHANNELS".
     * @return Permission enum corresponding to the input string, or null if unknown.
     */
    private Permission parsePermission(String permissionStr) {
        String upperCase = permissionStr.toUpperCase();

        // Check custom mapping first
        Permission mapped = mappingConfig.getPermissionMapping().get(upperCase);
        if (mapped != null) {
            return mapped;
        }

        // Fall back to direct enum parsing
        try {
            return Permission.valueOf(upperCase);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown permission: {}", permissionStr);
            return null;
        }
    }
}