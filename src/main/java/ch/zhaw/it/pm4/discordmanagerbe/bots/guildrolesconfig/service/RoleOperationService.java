package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncAnalysis;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleUpdateInfo;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.managers.RoleManager;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.*;

import java.util.stream.Collectors;

/**
 * Handles Discord role operations (create, update, delete).
 */
@Component
public class RoleOperationService {
    /**
     * Logger for this service.
     */
    private static final Logger logger = LoggerFactory.getLogger(RoleOperationService.class);

    /**
     * Applies role changes to the specified guild based on the analysis.
     * @param guild the Discord guild to apply changes to
     * @param analysis the analysis containing roles to create, update, and delete
     * @param deleteUnlistedRoles whether to delete roles not listed in the analysis
     * @return RoleSyncResult containing the results of the operation
     */
    public RoleSyncResult applyRoleChanges(Guild guild, RoleSyncAnalysis analysis, boolean deleteUnlistedRoles) {
        logger.info("Applying role changes to guild: {}", guild.getName());
        
        RoleSyncResult result = RoleSyncResult.success("Role sync completed successfully.");
        
        try {
            executeRoleOperations(guild, analysis, deleteUnlistedRoles, result);
        } catch (Exception e) {
            logger.error("Critical error during role operations", e);
            result.setSuccess(false);
            result.setMessage("Critical error during role sync: " + e.getMessage());
        }
        
        logger.info("Role sync completed. {}", result.getSummary());
        return result;
    }

    /**
     * Executes the role operations based on the analysis.
     * @param guild the Discord guild to apply changes to
     * @param analysis the analysis containing roles to create, update, and delete
     * @param deleteUnlistedRoles whether to delete roles not listed in the analysis
     * @param result the result object to store operation results
     */
    private void executeRoleOperations(Guild guild, RoleSyncAnalysis analysis, 
                                     boolean deleteUnlistedRoles, RoleSyncResult result) {
        RoleCreator roleCreator = new RoleCreator(guild, result);
        RoleUpdater roleUpdater = new RoleUpdater(guild, result);
        RoleDeleter roleDeleter = new RoleDeleter(guild, result);

        roleCreator.createRoles(analysis.getRolesToCreate());
        roleUpdater.updateRoles(analysis.getRolesToUpdate());
        
        if (deleteUnlistedRoles) {
            roleDeleter.deleteRoles(analysis.getRolesToDelete());
        }
    }

    /**
     * Handles role creation operations.
     */
    private static class RoleCreator {

        /**
         * The guild where roles will be created.
         */
        private final Guild guild;

        /**
         * The result object to store creation results.
         */
        private final RoleSyncResult result;

        /**
         * Constructor for RoleCreator.
         * @param guild the Discord guild where roles will be created
         * @param result the result object to store creation results
         */
        RoleCreator(Guild guild, RoleSyncResult result) {
            this.guild = guild;
            this.result = result;
        }

        /**
         * Creates roles based on the provided list of ExtendedRoleDTO objects.
         * @param rolesToCreate the list of roles to create
         */
        void createRoles(List<ExtendedRoleDTO> rolesToCreate) {
            logger.debug("Creating {} roles", rolesToCreate.size());
            
            for (ExtendedRoleDTO roleDto : rolesToCreate) {
                try {
                    createSingleRole(roleDto);
                } catch (Exception e) {
                    handleCreationError(roleDto, e);
                }
            }
        }

        /**
         * Creates a single role based on the provided ExtendedRoleDTO.
         * @param roleDto the role data transfer object containing role details
         */
        private void createSingleRole(ExtendedRoleDTO roleDto) {
            RoleAction roleAction = guild.createRole();
            RoleActionConfigurer.configure(roleAction, roleDto);
            
            try {
                Role role = roleAction.complete();
                logger.info("Successfully created role: {} (ID: {})", role.getName(), role.getId());
                result.addCreatedRole(role.getName(), role.getId());
            } catch (Exception e) {
                logger.error("Failed to create role: {}", roleDto.getName(), e);
                throw e;
            }
        }

        /**
         * Handles errors that occur during role creation.
         * @param roleDto the role data transfer object that caused the error
         * @param e the exception that was thrown
         */
        private void handleCreationError(ExtendedRoleDTO roleDto, Exception e) {
            String errorMsg = String.format("Failed to create role '%s': %s", roleDto.getName(), e.getMessage());
            logger.error(errorMsg, e);
            result.addError(errorMsg);
        }
    }

    /**
     * Handles role update operations.
     */
    private static class RoleUpdater {

        /**
         * The guild where roles will be updated.
         */
        private final RoleSyncResult result;

        /**
         * The member representing the bot in the guild, used for permission checks.
         */
        private final Member botMember;

        /**
         * Constructor for RoleUpdater.
         * @param guild the Discord guild where roles will be updated
         * @param result the result object to store update results
         */
        RoleUpdater(Guild guild, RoleSyncResult result) {
            this.result = result;
            this.botMember = guild.getSelfMember();
        }

        /**
         * Updates roles based on the provided list of RoleUpdateInfo objects.
         * @param rolesToUpdate the list of roles to update, each containing current and desired state
         */
        void updateRoles(List<RoleUpdateInfo> rolesToUpdate) {
            logger.debug("Updating {} roles", rolesToUpdate.size());
            
            for (RoleUpdateInfo updateInfo : rolesToUpdate) {
                try {
                    updateSingleRole(updateInfo);
                } catch (Exception e) {
                    handleUpdateError(updateInfo, e);
                }
            }
        }

        /**
         * Updates a single role based on the provided RoleUpdateInfo.
         * @param updateInfo the information about the role update, including current and desired state
         */
        private void updateSingleRole(RoleUpdateInfo updateInfo) {
            Role role = updateInfo.getCurrentRole();
            ExtendedRoleDTO desiredRole = updateInfo.getDesiredRole();
            
            if (!RolePermissionChecker.canModifyRole(botMember, role)) {
                String errorMsg = String.format("Cannot update role '%s': Insufficient permissions or managed role", role.getName());
                result.addError(errorMsg);
                return;
            }
            
            RoleManager roleManager = role.getManager();
            applyUpdates(roleManager, updateInfo, desiredRole);
            
            try {
                roleManager.complete();
                logger.info("Successfully updated role: {} with changes: {}", 
                           role.getName(), updateInfo.getChanges());
                result.addUpdatedRole(role.getName(), role.getId(), updateInfo.getChanges());
            } catch (Exception e) {
                logger.error("Failed to update role: {}", role.getName(), e);
                throw e;
            }
        }

        /**
         * Applies the updates to the role manager based on the update information and desired role.
         * @param roleManager the role manager to apply updates to
         * @param updateInfo the information about the role update, including current and desired state
         * @param desiredRole the desired state of the role as defined in the configuration
         */
        private void applyUpdates(net.dv8tion.jda.api.managers.RoleManager roleManager, 
                                RoleUpdateInfo updateInfo, ExtendedRoleDTO desiredRole) {
            if (updateInfo.isNameChanged()) {
                roleManager.setName(desiredRole.getName());
            }
            
            if (updateInfo.isColorChanged()) {
                roleManager.setColor(ColorParser.parseColor(desiredRole.getColor()));
            }
            
            if (updateInfo.isPermissionsChanged()) {
                roleManager.setPermissions(PermissionParser.parsePermissions(desiredRole.getPermissions()));
            }
        }

        /**
         * Handles errors that occur during role updates.
         * @param updateInfo the information about the role update that caused the error
         * @param e the exception that was thrown
         */
        private void handleUpdateError(RoleUpdateInfo updateInfo, Exception e) {
            String roleName = updateInfo.getCurrentRole().getName();
            String errorMsg = String.format("Failed to update role '%s': %s", roleName, e.getMessage());
            logger.error(errorMsg, e);
            result.addError(errorMsg);
        }
    }

    /**
     * Handles role deletion operations.
     */
    private static class RoleDeleter {
        /**
         * The guild where roles will be deleted.
         */
        private final RoleSyncResult result;

        /**
         * The member representing the bot in the guild, used for permission checks.
         */
        private final Member botMember;

        /**
         * Constructor for RoleDeleter.
         * @param guild the Discord guild where roles will be deleted
         * @param result the result object to store deletion results
         */
        RoleDeleter(Guild guild, RoleSyncResult result) {
            this.result = result;
            this.botMember = guild.getSelfMember();
        }

        /**
         * Deletes roles based on the provided list of Role objects.
         * @param rolesToDelete the list of roles to delete
         */
        void deleteRoles(List<Role> rolesToDelete) {
            logger.debug("Deleting {} roles", rolesToDelete.size());
            
            for (Role role : rolesToDelete) {
                try {
                    deleteSingleRole(role);
                } catch (Exception e) {
                    handleDeletionError(role, e);
                }
            }
        }

        /**
         * Deletes a single role.
         * @param role the role to delete
         */
        private void deleteSingleRole(Role role) {
            if (!RolePermissionChecker.canModifyRole(botMember, role)) {
                String errorMsg = String.format("Cannot delete role '%s': Insufficient permissions or managed role", role.getName());
                result.addError(errorMsg);
                return;
            }
            
            try {
                role.delete().complete();
                logger.info("Successfully deleted role: {} (ID: {})", role.getName(), role.getId());
                result.addDeletedRole(role.getName(), role.getId());
            } catch (Exception e) {
                logger.error("Failed to delete role: {}", role.getName(), e);
                throw e;
            }
        }

        /**
         * Handles errors that occur during role deletion.
         * @param role the role that caused the error
         * @param e the exception that was thrown
         */
        private void handleDeletionError(Role role, Exception e) {
            String errorMsg = String.format("Failed to delete role '%s': %s", role.getName(), e.getMessage());
            logger.error(errorMsg, e);
            result.addError(errorMsg);
        }
    }

    /**
     * Utility for configuring role actions.
     */
    private static class RoleActionConfigurer {

        /**
         * Configures a RoleAction based on the provided ExtendedRoleDTO.
         * @param roleAction the RoleAction to configure
         * @param roleDto the ExtendedRoleDTO containing role details
         */
        static void configure(RoleAction roleAction, ExtendedRoleDTO roleDto) {
            if (roleDto.getName() != null) {
                roleAction.setName(roleDto.getName());
            }
            
            if (roleDto.getColor() != null) {
                Color color = ColorParser.parseColor(roleDto.getColor());
                if (color != null) {
                    roleAction.setColor(color);
                }
            }
            
            if (roleDto.getPermissions() != null) {
                Set<Permission> permissions = PermissionParser.parsePermissions(roleDto.getPermissions());
                roleAction.setPermissions(permissions);
            }
        }
    }

    /**
     * Utility for checking role permissions.
     */
    private static class RolePermissionChecker {

        /**
         * Checks if the bot member can modify the specified role.
         * @param botMember the member representing the bot in the guild
         * @param role the role to check permissions for
         * @return true if the bot can modify the role, false otherwise
         */
        static boolean canModifyRole(Member botMember, Role role) {
            return botMember.canInteract(role) && !role.isManaged() && !role.isPublicRole();
        }
    }

    /**
     * Utility for parsing colors.
     */
    private static class ColorParser {

        /**
         * Parses a hexadecimal color string into a Color object.
         * @param hexColor the hexadecimal color string (e.g., "#FF5733" or "FF5733")
         * @return the Color object, or null if the string is invalid
         */
        static Color parseColor(String hexColor) {
            if (hexColor == null || hexColor.isEmpty()) {
                return null;
            }
            
            try {
                String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
                int rgb = Integer.parseInt(hex, 16);
                return new Color(rgb);
            } catch (NumberFormatException e) {
                logger.warn("Invalid color format: {}", hexColor);
                return null;
            }
        }
    }

    /**
     * Utility for parsing permissions.
     */
    private static class PermissionParser {

        /**
         * Parses a list of permission names into a set of Permission enums.
         * @param permissionNames the list of permission names to parse
         * @return a set of Permission enums, or an empty set if the input is null
         */
        static Set<Permission> parsePermissions(List<String> permissionNames) {
            if (permissionNames == null) {
                return Collections.emptySet();
            }
            
            return permissionNames.stream()
                .map(PermissionParser::parsePermission)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }

        private static Permission parsePermission(String permissionName) {
            try {
                return Permission.valueOf(permissionName.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown permission: {}", permissionName);
                return null;
            }
        }
    }
}