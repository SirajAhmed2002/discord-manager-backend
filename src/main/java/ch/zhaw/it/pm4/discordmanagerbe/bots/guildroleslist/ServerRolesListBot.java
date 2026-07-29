package ch.zhaw.it.pm4.discordmanagerbe.bots.guildroleslist;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Discord bot for retrieving comprehensive role information from Discord guilds.
 * This bot provides functionality to fetch all roles from a specified guild
 * with complete Discord role properties including permissions and colors.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_ROLES_LIST)
@Component
public class ServerRolesListBot extends AbstractJdaBot {

    /**
     * Logger instance for this class to handle logging operations.
     */
    private static final Logger logger = LoggerFactory.getLogger(ServerRolesListBot.class);

    /**
     * JSON mapper for converting objects to JSON format with pretty printing enabled.
     */
    private final ObjectMapper jsonMapper;

    /**
     * Constructs a new ServerRolesListBot with the provided JDA instance.
     * Initializes the JSON mapper with indented output for better readability.
     *
     * @param jdaBean the JDA instance used for Discord API interactions
     */
    public ServerRolesListBot(JDA jdaBean) {
        super(jdaBean);
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Fetches all roles from the specified Discord guild with complete role information.
     * This method validates the guild ID, retrieves the guild, and processes all roles
     * to return comprehensive role data.
     *
     * @param guildId the Discord guild ID as a string
     * @return ServerRoleListDTO containing all roles with their complete properties
     * @throws IllegalArgumentException if guildId is null, empty, or guild not found
     */
    public ServerRoleListDTO fetchGuildRoles(String guildId) {
        validateGuildId(guildId);
        Guild guild = retrieveGuild(guildId);
        List<ExtendedRoleDTO> rolesList = processGuildRoles(guild);
        ServerRoleListDTO response = new ServerRoleListDTO(rolesList);
        logFetchResult(guild, rolesList, response);
        return response;
    }

    /**
     * Validates the provided guild ID to ensure it is not null or empty.
     *
     * @param guildId the guild ID to validate
     * @throws IllegalArgumentException if guildId is null or empty
     */
    private void validateGuildId(String guildId) {
        if (guildId == null || guildId.isEmpty()) {
            logger.error("Guild ID is not set.");
            throw new IllegalArgumentException("Guild ID must be set.");
        }
    }

    /**
     * Retrieves the Discord guild by its ID.
     *
     * @param guildId the Discord guild ID
     * @return the Guild object
     * @throws IllegalArgumentException if guild is not found
     */
    private Guild retrieveGuild(String guildId) {
        Guild guild = jdaBean.getGuildById(guildId);
        if (guild == null) {
            logger.error("Guild not found: {}", guildId);
            throw new IllegalArgumentException("Guild not found: " + guildId);
        }
        return guild;
    }

    /**
     * Processes all roles from the given guild and converts them to ExtendedRoleDTO objects.
     * Roles are processed in their natural Discord order (highest position first).
     *
     * @param guild the Discord guild containing the roles
     * @return list of ExtendedRoleDTO objects with complete role information
     */
    private List<ExtendedRoleDTO> processGuildRoles(Guild guild) {
        List<ExtendedRoleDTO> rolesList = new ArrayList<>();
        List<Role> roles = guild.getRoles();

        for (Role role : roles) {
            ExtendedRoleDTO roleDTO = convertRoleToDTO(role);
            rolesList.add(roleDTO);
        }

        return rolesList;
    }

    /**
     * Converts a Discord Role object to an ExtendedRoleDTO with all relevant properties.
     * This includes basic role information, color, and permissions.
     *
     * @param role the Discord Role object to convert
     * @return ExtendedRoleDTO containing all role properties
     */
    private ExtendedRoleDTO convertRoleToDTO(Role role) {
        ExtendedRoleDTO roleDTO = new ExtendedRoleDTO();

        setBasicRoleProperties(roleDTO, role);
        setRoleColor(roleDTO, role);
        setRolePermissions(roleDTO, role);

        return roleDTO;
    }

    /**
     * Sets the basic properties of a role DTO (ID and name).
     *
     * @param roleDTO the DTO to populate
     * @param role the Discord role containing the data
     */
    private void setBasicRoleProperties(ExtendedRoleDTO roleDTO, Role role) {
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getName());
    }

    /**
     * Sets the color property of a role DTO.
     * Converts the Discord role color to a hexadecimal string format.
     *
     * @param roleDTO the DTO to populate
     * @param role the Discord role containing the color data
     */
    private void setRoleColor(ExtendedRoleDTO roleDTO, Role role) {
        String colorHex = convertColorToHex(role.getColor());
        roleDTO.setColor(colorHex);
    }

    /**
     * Converts a Color object to its hexadecimal string representation.
     *
     * @param color the Color object to convert, may be null
     * @return hexadecimal color string (e.g., "#FF5733") or null if color is null
     */
    private String convertColorToHex(Color color) {
        if (color == null) {
            return null;
        }
        return "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
    }

    /**
     * Sets the permissions list for a role DTO.
     * Converts Discord Permission enums to their string representations.
     *
     * @param roleDTO the DTO to populate
     * @param role the Discord role containing the permissions
     */
    private void setRolePermissions(ExtendedRoleDTO roleDTO, Role role) {
        List<String> permissions = role.getPermissions().stream()
                .map(Permission::toString)
                .collect(Collectors.toList());
        roleDTO.setPermissions(permissions);
    }

    /**
     * Logs the result of the role fetching operation.
     * Includes summary information and detailed JSON output in debug mode.
     *
     * @param guild the Discord guild that was processed
     * @param rolesList the list of roles that were fetched
     * @param response the complete response object
     */
    private void logFetchResult(Guild guild, List<ExtendedRoleDTO> rolesList, ServerRoleListDTO response) {
        logger.info("Fetched {} roles from guild {} ({})",
                rolesList.size(), guild.getName(), guild.getId());

        logDetailedResponse(response);
    }

    /**
     * Logs the detailed JSON response in debug mode.
     * If JSON serialization fails, logs an error instead.
     *
     * @param response the response object to serialize and log
     */
    private void logDetailedResponse(ServerRoleListDTO response) {
        try {
            String jsonResponse = jsonMapper.writeValueAsString(response);
            logger.debug("Role fetch response: {}", jsonResponse);
        } catch (Exception ex) {
            logger.error("Error generating JSON for logging", ex);
        }
    }
}