package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.analyzer.RoleAnalyzer;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncAnalysis;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.RoleSyncResult;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.service.RoleOperationService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.validator.RoleValidator;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model.ValidationResult;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Bot for managing Discord guild role configurations.
 * Syncs roles based on ServerRoleListDTO definitions.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_ROLES_CONFIG)
@Component
public class ServerRolesConfigBot extends AbstractJdaBot {
    private static final Logger logger = LoggerFactory.getLogger(ServerRolesConfigBot.class);
    
    private final RoleAnalyzer roleAnalyzer;
    private final RoleOperationService roleOperationService;
    private final RoleValidator roleValidator;

    public ServerRolesConfigBot(JDA jdaBean, 
                               RoleAnalyzer roleAnalyzer,
                               RoleOperationService roleOperationService,
                               RoleValidator roleValidator) {
        super(jdaBean);
        this.roleAnalyzer = roleAnalyzer;
        this.roleOperationService = roleOperationService;
        this.roleValidator = roleValidator;
    }

    /**
     * Syncs the guild's roles with the provided configuration.
     */
    public RoleSyncResult syncGuildRoles(String guildId,
                                         ServerRoleListDTO roleListDTO,
                                         boolean deleteUnlistedRoles) {
        return performRoleSync(guildId, roleListDTO, deleteUnlistedRoles);
    }

    /**
     * Performs the role synchronization process.
     * @param guildId the ID of the guild to sync roles for
     * @param roleListDTO the role list DTO containing the roles to sync
     * @param deleteUnlistedRoles whether to delete roles not listed in the DTO
     * @return RoleSyncResult containing the result of the sync operation
     */
    private RoleSyncResult performRoleSync(String guildId, ServerRoleListDTO roleListDTO, boolean deleteUnlistedRoles) {
        try {
            ValidationResult validation = roleValidator.validateSyncRequest(guildId, roleListDTO, jdaBean);
            if (!validation.isValid()) {
                return RoleSyncResult.failure(validation.getErrorMessage());
            }

            RoleSyncAnalysis analysis = roleAnalyzer.analyzeRoleChanges(validation.getGuild(), roleListDTO);
            return roleOperationService.applyRoleChanges(validation.getGuild(), analysis, deleteUnlistedRoles);
            
        } catch (Exception e) {
            logger.error("Unexpected error during role sync for guild: {}", guildId, e);
            return RoleSyncResult.failure("Unexpected error: " + e.getMessage());
        }
    }
}