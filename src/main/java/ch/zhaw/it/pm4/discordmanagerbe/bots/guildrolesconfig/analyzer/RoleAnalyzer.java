package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.analyzer;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncAnalysis;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleUpdateInfo;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes differences between current Discord roles and desired configuration.
 */
@Component
public class RoleAnalyzer {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(RoleAnalyzer.class);

    /**
     * Analyzes the current roles in a guild against the desired roles defined in ServerRoleListDTO.
     * @param guild the Discord guild to analyze
     * @param roleListDTO the desired roles configuration
     * @return RoleSyncAnalysis containing the analysis results
     */
    public RoleSyncAnalysis analyzeRoleChanges(Guild guild, ServerRoleListDTO roleListDTO) {
        logger.debug("Analyzing role changes for guild: {}", guild.getName());
        
        RoleSyncAnalysis analysis = new RoleSyncAnalysis();
        RoleMapping roleMapping = RoleMapping.fromGuild(guild);
        HashSet<String> processedRoleIds = new HashSet<>();
        
        analyzeDesiredRoles(roleListDTO, analysis, roleMapping, processedRoleIds);
        findRolesToDelete(guild, analysis, processedRoleIds);
        
        logAnalysisResults(analysis);
        return analysis;
    }

    /**
     * Analyzes the desired roles against the current roles in the guild.
     * @param roleListDTO the desired roles configuration
     * @param analysis the RoleSyncAnalysis to populate with results
     * @param roleMapping the mapping of current roles for efficient lookups
     * @param processedRoleIds a set to track which roles have been processed
     */
    private void analyzeDesiredRoles(ServerRoleListDTO roleListDTO, 
                                   RoleSyncAnalysis analysis, 
                                   RoleMapping roleMapping, 
                                   Set<String> processedRoleIds) {
        for (ExtendedRoleDTO dtoRole : roleListDTO.getRoles()) {
            Role existingRole = roleMapping.findRole(dtoRole);
            
            if (existingRole == null) {
                analysis.addRoleToCreate(dtoRole);
                logger.debug("Role to create: {}", dtoRole.getName());
            } else {
                processedRoleIds.add(existingRole.getId());
                RoleUpdateInfo updateInfo = RoleUpdateAnalyzer.analyze(existingRole, dtoRole);
                
                if (updateInfo.hasChanges()) {
                    analysis.addRoleToUpdate(updateInfo);
                    logger.debug("Role to update: {} with changes: {}", 
                               existingRole.getName(), updateInfo.getChanges());
                }
            }
        }
    }

    /**
     * Finds roles in the guild that are not present in the processedRoleIds set,
     * @param guild the Discord guild to analyze
     * @param analysis the RoleSyncAnalysis to populate with roles to delete
     * @param processedRoleIds a set of role IDs that have already been processed
     */
    private void findRolesToDelete(Guild guild, RoleSyncAnalysis analysis, Set<String> processedRoleIds) {
        List<Role> rolesToDelete = guild.getRoles().stream()
            .filter(role -> !processedRoleIds.contains(role.getId()))
            .filter(this::isNotSystemRole)
            .collect(Collectors.toList());
            
        analysis.addRolesToDelete(rolesToDelete);
    }

    /**
     * Checks if a role is not a system role (public or managed).
     * @param role the role to check
     * @return true if the role is not a system role, false otherwise
     */
    private boolean isNotSystemRole(Role role) {
        return !role.isPublicRole() && !role.isManaged();
    }

    /**
     * Logs the results of the role analysis.
     * @param analysis the RoleSyncAnalysis containing the results
     */
    private void logAnalysisResults(RoleSyncAnalysis analysis) {
        logger.info("Analysis complete - Create: {}, Update: {}, Delete: {}",
                   analysis.getRolesToCreate().size(),
                   analysis.getRolesToUpdate().size(), 
                   analysis.getRolesToDelete().size());
    }

    /**
     * Handles role mapping for efficient lookups.
     */
    private static class RoleMapping {
        /**
         * Maps roles by ID and name for quick access.
         */
        private final Map<String, Role> byId;

        /**
         * Maps roles by name (case-insensitive) for quick access.
         */
        private final Map<String, Role> byName;

        /**
         * Constructor for RoleMapping.
         * @param byId a map of roles by ID
         * @param byName a map of roles by name (case-insensitive)
         */
        private RoleMapping(Map<String, Role> byId, Map<String, Role> byName) {
            this.byId = byId;
            this.byName = byName;
        }

        /**
         * Creates a RoleMapping from a Discord guild.
         * @param guild the Discord guild to extract roles from
         * @return a RoleMapping instance containing roles by ID and name
         */
        static RoleMapping fromGuild(Guild guild) {
            HashMap<String, Role> byId = new HashMap<>();
            HashMap<String, Role> byName = new HashMap<String, Role>();
            
            for (Role role : guild.getRoles()) {
                byId.put(role.getId(), role);
                byName.put(role.getName().toLowerCase(), role);
            }
            
            return new RoleMapping(byId, byName);
        }

        /**
         * Finds a role based on the provided ExtendedRoleDTO.
         * @param dtoRole the ExtendedRoleDTO containing role information
         * @return the Role if found, or null if not found
         */
        Role findRole(ExtendedRoleDTO dtoRole) {
            // Prefer ID lookup (most reliable)
            if (dtoRole.getId() != null && !dtoRole.getId().isEmpty()) {
                Role roleById = byId.get(dtoRole.getId());
                if (roleById != null) {
                    return roleById;
                }
            }
            
            // Fallback to name lookup
            if (dtoRole.getName() != null) {
                return byName.get(dtoRole.getName().toLowerCase());
            }
            
            return null;
        }
    }

    /**
     * Analyzes role updates in detail.
     */
    private static class RoleUpdateAnalyzer {

        /**
         * Analyzes the differences between the current role and the desired role configuration.
         * @param currentRole the current role in the Discord server
         * @param desiredRole the desired role configuration from ExtendedRoleDTO
         * @return RoleUpdateInfo containing the analysis results
         */
        static RoleUpdateInfo analyze(Role currentRole, ExtendedRoleDTO desiredRole) {
            RoleUpdateInfo updateInfo = new RoleUpdateInfo();
            updateInfo.setCurrentRole(currentRole);
            updateInfo.setDesiredRole(desiredRole);
            
            analyzeNameChange(currentRole, desiredRole, updateInfo);
            analyzeColorChange(currentRole, desiredRole, updateInfo);
            analyzePermissionChange(currentRole, desiredRole, updateInfo);
            
            return updateInfo;
        }

        /**
         * Analyzes if the role name has changed.
         * @param currentRole the current role in the Discord server
         * @param desiredRole the desired role configuration from ExtendedRoleDTO
         * @param updateInfo the RoleUpdateInfo to populate with changes
         */
        private static void analyzeNameChange(Role currentRole, ExtendedRoleDTO desiredRole, RoleUpdateInfo updateInfo) {
            if (desiredRole.getName() != null && !currentRole.getName().equals(desiredRole.getName())) {
                updateInfo.setNameChanged(true);
                updateInfo.addChange(String.format("Name: '%s' → '%s'", 
                                   currentRole.getName(), desiredRole.getName()));
            }
        }

        /**
         * Analyzes if the role color has changed.
         * @param currentRole the current role in the Discord server
         * @param desiredRole the desired role configuration from ExtendedRoleDTO
         * @param updateInfo the RoleUpdateInfo to populate with changes
         */
        private static void analyzeColorChange(Role currentRole, ExtendedRoleDTO desiredRole, RoleUpdateInfo updateInfo) {
            if (desiredRole.getColor() == null) {
                return;
            }
            
            String currentColorHex = formatColor(currentRole.getColor());
            String desiredColorHex = normalizeColor(desiredRole.getColor());
            
            if (!Objects.equals(currentColorHex, desiredColorHex)) {
                updateInfo.setColorChanged(true);
                updateInfo.addChange(String.format("Color: %s → %s", currentColorHex, desiredRole.getColor()));
            }
        }

        /**
         * Analyzes if the role permissions have changed.
         * @param currentRole the current role in the Discord server
         * @param desiredRole the desired role configuration from ExtendedRoleDTO
         * @param updateInfo the RoleUpdateInfo to populate with changes
         */
        private static void analyzePermissionChange(Role currentRole, ExtendedRoleDTO desiredRole, RoleUpdateInfo updateInfo) {
            if (desiredRole.getPermissions() == null) {
                return;
            }
            
            EnumSet<Permission> currentPerms = currentRole.getPermissions();
            Set<Permission> desiredPerms = parsePermissions(desiredRole.getPermissions());
            
            if (!currentPerms.equals(desiredPerms)) {
                updateInfo.setPermissionsChanged(true);
                updateInfo.addChange("Permissions updated");
            }
        }

        /**
         * Formats a Color object to a hexadecimal string.
         * @param color the Color object to format, may be null
         * @return the hexadecimal color string (e.g., "#FF5733") or null if color is null
         */
        private static String formatColor(Color color) {
            return color != null ? String.format("#%06X", color.getRGB() & 0xFFFFFF) : null;
        }

        /**
         * Normalizes a color string to a consistent format.
         * @param color the color string to normalize, may be null
         * @return the normalized color string in hexadecimal format (e.g., "#FF5733") or null if input is null
         */
        private static String normalizeColor(String color) {
            if (color == null) return null;
            String hex = color.startsWith("#") ? color.substring(1) : color;
            return "#" + hex.toUpperCase();
        }

        /**
         * Parses a list of permission names into a set of Permission enums.
         * @param permissionNames the list of permission names to parse, may be null
         * @return a set of Permission enums corresponding to the provided names
         */
        private static Set<Permission> parsePermissions(List<String> permissionNames) {
            if (permissionNames == null) {
                return Collections.emptySet();
            }
            
            return permissionNames.stream()
                .map(RoleUpdateAnalyzer::parsePermission)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }

        /**
         * Parses a single permission name into a Permission enum.
         * @param permissionName the name of the permission to parse
         * @return the corresponding Permission enum, or null if the name is invalid
         */
        private static Permission parsePermission(String permissionName) {
            try {
                return Permission.valueOf(permissionName.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                LoggerFactory.getLogger(RoleAnalyzer.class).warn("Unknown permission: {}", permissionName);
                return null;
            }
        }
    }
}