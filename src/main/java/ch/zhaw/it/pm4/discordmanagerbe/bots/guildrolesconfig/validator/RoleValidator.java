package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.validator;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.ValidationResult;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates role sync requests and individual role configurations.
 */
@Component
public class RoleValidator {

    /**
     * Maximum length for role names in Discord.
     */
    private static final int MAX_ROLE_NAME_LENGTH = 100;

    /**
     * Pattern to validate hex color codes.
     */
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^[0-9A-Fa-f]{6}$");

    /**
     * Validates a role sync request for a guild.
     * @param guildId the ID of the guild to validate against
     * @param roleListDTO the role list DTO containing roles to validate
     * @param jda the JDA instance to interact with Discord
     * @return ValidationResult indicating success or failure
     */
    public ValidationResult validateSyncRequest(String guildId, ServerRoleListDTO roleListDTO, JDA jda) {
        ValidationResult basicValidation = validateBasicInputs(guildId, roleListDTO);
        if (!basicValidation.isValid()) {
            return basicValidation;
        }
        
        Guild guild = jda.getGuildById(guildId);
        if (guild == null) {
            return ValidationResult.failure("Guild not found: " + guildId);
        }
        
        ValidationResult permissionValidation = validateBotPermissions(guild);
        if (!permissionValidation.isValid()) {
            return permissionValidation;
        }
        
        return validateRoles(roleListDTO, guild);
    }

    /**
     * Validates basic inputs for a role sync request.
     * @param guildId the ID of the guild to validate against
     * @param roleListDTO the role list DTO containing roles to validate
     * @return ValidationResult indicating success or failure
     */
    private ValidationResult validateBasicInputs(String guildId, ServerRoleListDTO roleListDTO) {
        if (roleListDTO == null) {
            return ValidationResult.failure("Role list DTO cannot be null");
        }
        
        if (isBlankString(guildId)) {
            return ValidationResult.failure("Guild ID cannot be null or empty");
        }
        
        if (roleListDTO.getRoles() == null || roleListDTO.getRoles().isEmpty()) {
            return ValidationResult.failure("Role list cannot be empty");
        }
        
        return ValidationResult.success(null);
    }

    /**
     * Validates that the bot has the necessary permissions in the guild.
     * @param guild the Discord guild to check permissions in
     * @return ValidationResult indicating success or failure
     */
    private ValidationResult validateBotPermissions(Guild guild) {
        Member botMember = guild.getSelfMember();
        if (!botMember.hasPermission(Permission.MANAGE_ROLES)) {
            return ValidationResult.failure("Bot lacks MANAGE_ROLES permission");
        }
        return ValidationResult.success(guild);
    }

    /**
     * Validates the roles in the provided ServerRoleListDTO against the guild.
     * @param roleListDTO the ServerRoleListDTO containing roles to validate
     * @param guild the Discord guild to validate against
     * @return ValidationResult indicating success or failure
     */
    private ValidationResult validateRoles(ServerRoleListDTO roleListDTO, Guild guild) {
        for (ExtendedRoleDTO role : roleListDTO.getRoles()) {
            String error = validateSingleRole(role);
            if (error != null) {
                return ValidationResult.failure("Invalid role: " + error);
            }
        }
        return ValidationResult.success(guild);
    }

    /**
     * Validates a single role configuration.
     * @param role the ExtendedRoleDTO representing the role to validate
     * @return null if valid, or an error message if invalid
     */
    private String validateSingleRole(ExtendedRoleDTO role) {
        if (role == null) {
            return "Role cannot be null";
        }
        
        if (isBlankString(role.getName())) {
            return "Role name cannot be null or empty";
        }
        
        if (role.getName().length() > MAX_ROLE_NAME_LENGTH) {
            return String.format("Role name too long (max %d characters): %s", 
                               MAX_ROLE_NAME_LENGTH, role.getName());
        }
        
        if (!isValidHexColor(role.getColor())) {
            return "Invalid color format: " + role.getColor();
        }
        
        return null;
    }

    /**
     * Checks if a given string is a valid hex color code.
     * @param color the color string to validate, can be null
     * @return true if valid hex color, false otherwise
     */
    private boolean isValidHexColor(String color) {
        if (color == null) {
            return true;
        }
        
        String hex = color.startsWith("#") ? color.substring(1) : color;
        return HEX_COLOR_PATTERN.matcher(hex).matches();
    }

    /**
     * Checks if a string is blank (null or empty after trimming).
     * @param str the string to check
     * @return true if the string is blank, false otherwise
     */
    private boolean isBlankString(String str) {
        return str == null || str.trim().isEmpty();
    }
}