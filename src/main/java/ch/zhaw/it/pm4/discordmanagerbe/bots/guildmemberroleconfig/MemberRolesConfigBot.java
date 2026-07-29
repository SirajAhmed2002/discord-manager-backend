package ch.zhaw.it.pm4.discordmanagerbe.bots.guildmemberroleconfig;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberListDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.RoleDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Discord bot for managing member role assignments within a guild.
 * Synchronizes member roles based on provided configuration data.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_MEMBER_ROLES_CONFIG)
@Component
public class MemberRolesConfigBot extends AbstractJdaBot {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(MemberRolesConfigBot.class);

    /**
     * Constructs a new MemberRolesConfigBot.
     *
     * @param jdaBean the JDA instance to use
     */
    public MemberRolesConfigBot(JDA jdaBean) {
        super(jdaBean);
    }

    /**
     * Syncs the guild's member role assignments with the provided MemberListDTO.
     * This will:
     * - Add members to roles they should have but don't
     * - Remove members from roles they shouldn't have (optional)
     * - Skip bot members and managed roles
     *
     * @param guildId The Discord guild ID
     * @param memberListDTO The desired member-role configuration
     * @param removeUnlistedRoles If true, removes members from roles not specified in the DTO
     * @return MemberRoleSyncResult containing detailed results of the sync operation
     */
    public MemberRoleSyncResult syncMemberRoles(String guildId, MemberListDTO memberListDTO, boolean removeUnlistedRoles) {
        MemberRoleSyncResult validationResult = validateInputParameters(guildId, memberListDTO);
        if (validationResult != null) {
            return validationResult;
        }

        Guild guild = jdaBean.getGuildById(guildId);
        if (guild == null) {
            return new MemberRoleSyncResult(false, "Guild not found: " + guildId);
        }

        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            return new MemberRoleSyncResult(false, "Bot lacks MANAGE_ROLES permission.");
        }

        try {
            List<Member> allMembers = loadGuildMembers(guild);
            return performRoleSync(guild, memberListDTO, allMembers, removeUnlistedRoles);
        } catch (Exception e) {
            logger.error("Error during member role sync", e);
            return new MemberRoleSyncResult(false, "Failed to sync member roles: " + e.getMessage());
        }
    }

    /**
     * Validates input parameters for role sync operation.
     *
     * @param guildId the guild ID to validate
     * @param memberListDTO the member list to validate
     * @return validation error result or null if valid
     */
    private MemberRoleSyncResult validateInputParameters(String guildId, MemberListDTO memberListDTO) {
        if (memberListDTO == null || guildId == null || guildId.isEmpty()) {
            return new MemberRoleSyncResult(false, "Invalid member list DTO or guild ID.");
        }
        return null;
    }

    /**
     * Loads all members from the specified guild.
     *
     * @param guild the guild to load members from
     * @return list of all guild members
     * @throws Exception if loading fails or times out
     */
    private List<Member> loadGuildMembers(Guild guild) throws Exception {
        CompletableFuture<List<Member>> membersFuture = new CompletableFuture<>();
        guild.loadMembers()
                .onSuccess(membersFuture::complete)
                .onError(membersFuture::completeExceptionally);
        return membersFuture.get(30, TimeUnit.SECONDS);
    }

    /**
     * Executes the role synchronization process.
     *
     * @param guild the target guild
     * @param memberListDTO the desired member configuration
     * @param allMembers all guild members
     * @param removeUnlistedRoles whether to remove unlisted roles
     * @return the sync operation result
     * @throws Exception if sync operations fail
     */
    private MemberRoleSyncResult performRoleSync(Guild guild, MemberListDTO memberListDTO,
                                                 List<Member> allMembers, boolean removeUnlistedRoles) throws Exception {

        MemberRoleSyncResult result = new MemberRoleSyncResult(true, "Member role sync completed.");

        List<CompletableFuture<Void>> allOperations = prepareRoleSyncOperations(
                guild, memberListDTO, allMembers, removeUnlistedRoles, result
        );

        // Warte auf alle Operationen, aber lasse Fehler in einzelnen Operations zu
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                allOperations.toArray(new CompletableFuture[0])
        );

        try {
            allOf.get(60, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            logger.debug("Some role operations failed, but this is handled gracefully", e);
        }

        return finalizeResult(result);
    }

    /**
     * Prepares all role sync operations for execution.
     *
     * @param guild the target guild
     * @param memberListDTO the member configuration
     * @param allMembers all guild members
     * @param removeUnlistedRoles whether to remove unlisted roles
     * @return list of async operations to execute
     */
    private List<CompletableFuture<Void>> prepareRoleSyncOperations(Guild guild, MemberListDTO memberListDTO,
                                                                    List<Member> allMembers, boolean removeUnlistedRoles,
                                                                    MemberRoleSyncResult result) {

        Map<String, Member> membersById = createMemberLookupMap(allMembers);
        Map<String, Role> rolesById = createRoleLookupMap(guild);

        List<CompletableFuture<Void>> allOperations = new ArrayList<>();

        for (MemberDTO memberDTO : memberListDTO.getMembers()) {
            List<CompletableFuture<Void>> memberOperations = processMemberRoleChanges(
                    guild, memberDTO, membersById, rolesById, removeUnlistedRoles, result
            );
            allOperations.addAll(memberOperations);
        }
        return allOperations;
    }

    /**
     * Creates a member lookup map by ID, excluding bots.
     *
     * @param allMembers the members to map
     * @return map of member ID to member
     */
    private Map<String, Member> createMemberLookupMap(List<Member> allMembers) {
        return allMembers.stream()
                .filter(member -> !member.getUser().isBot())
                .collect(Collectors.toMap(Member::getId, member -> member));
    }

    /**
     * Creates a role lookup map by ID.
     *
     * @param guild the guild containing the roles
     * @return map of role ID to role
     */
    private Map<String, Role> createRoleLookupMap(Guild guild) {
        return guild.getRoles().stream()
                .collect(Collectors.toMap(Role::getId, role -> role));
    }

    /**
     * Processes role changes for a single member.
     *
     * @param guild the target guild
     * @param memberDTO the member configuration
     * @param membersById member lookup map
     * @param rolesById role lookup map
     * @param removeUnlistedRoles whether to remove unlisted roles
     * @param result the result object to update
     * @return list of operations for this member
     */
    private List<CompletableFuture<Void>> processMemberRoleChanges(Guild guild, MemberDTO memberDTO,
                                                                   Map<String, Member> membersById,
                                                                   Map<String, Role> rolesById,
                                                                   boolean removeUnlistedRoles,
                                                                   MemberRoleSyncResult result) {

        Member discordMember = membersById.get(memberDTO.getId());
        if (discordMember == null) {
            result.addError("Member not found in guild: " + memberDTO.getName() + " (ID: " + memberDTO.getId() + ")");
            return Collections.emptyList();
        }

        RoleDifference roleDiff = calculateRoleDifferences(discordMember, memberDTO);

        List<CompletableFuture<Void>> operations = new ArrayList<>(createRoleAddOperations(guild, discordMember, roleDiff.getRolesToAdd(),
                rolesById, memberDTO.getName(), result));

        if (removeUnlistedRoles) {
            operations.addAll(createRoleRemoveOperations(guild, discordMember, roleDiff.getRolesToRemove(),
                    rolesById, memberDTO.getName(), result));
        }
        return operations;
    }

    /**
     * Calculates role differences between current and desired state.
     *
     * @param member the Discord member
     * @param memberDTO the desired member configuration
     * @return the role differences
     */
    private RoleDifference calculateRoleDifferences(Member member, MemberDTO memberDTO) {
        Set<String> currentRoleIds = member.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<String> desiredRoleIds = memberDTO.getRoles().stream()
                .map(RoleDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> rolesToAdd = new HashSet<>(desiredRoleIds);
        rolesToAdd.removeAll(currentRoleIds);

        Set<String> rolesToRemove = new HashSet<>(currentRoleIds);
        rolesToRemove.removeAll(desiredRoleIds);

        return new RoleDifference(rolesToAdd, rolesToRemove);
    }

    /**
     * Creates operations to add roles to a member.
     *
     * @param guild the target guild
     * @param member the member to modify
     * @param roleIds the role IDs to add
     * @param rolesById role lookup map
     * @param memberName the member's name for logging
     * @param result the result object to update
     * @return list of add operations
     */
    private List<CompletableFuture<Void>> createRoleAddOperations(Guild guild, Member member,
                                                                  Set<String> roleIds, Map<String, Role> rolesById,
                                                                  String memberName, MemberRoleSyncResult result) {
        return createRoleOperations(guild::addRoleToMember, member, roleIds, rolesById, memberName, "add", result);
    }

    /**
     * Creates operations to remove roles from a member.
     *
     * @param guild the target guild
     * @param member the member to modify
     * @param roleIds the role IDs to remove
     * @param rolesById role lookup map
     * @param memberName the member's name for logging
     * @param result the result object to update
     * @return list of remove operations
     */
    private List<CompletableFuture<Void>> createRoleRemoveOperations(Guild guild, Member member,
                                                                     Set<String> roleIds, Map<String, Role> rolesById,
                                                                     String memberName, MemberRoleSyncResult result) {
        return createRoleOperations(guild::removeRoleFromMember, member, roleIds, rolesById, memberName, "remove", result);
    }

    /**
     * Creates role operations for a set of role IDs.
     *
     * @param operation the operation function to apply
     * @param member the target member
     * @param roleIds the role IDs to process
     * @param rolesById role lookup map
     * @param memberName the member's name for logging
     * @param actionType the action type ("add" or "remove")
     * @param result the result object to update
     * @return list of role operations
     */
    private List<CompletableFuture<Void>> createRoleOperations(BiFunction<Member, Role, RestAction<Void>> operation,
                                                               Member member, Set<String> roleIds,
                                                               Map<String, Role> rolesById, String memberName,
                                                               String actionType, MemberRoleSyncResult result) {

        return roleIds.stream()
                .map(rolesById::get)
                .filter(role -> validateRoleForOperation(role, memberName, result))
                .map(role -> executeRoleOperation(operation, member, role, actionType, result))
                .collect(Collectors.toList());
    }

    /**
     * Validates that a role can be managed by the bot.
     *
     * @param role the role to validate
     * @param memberName the member's name for error reporting
     * @param result the result object to update with errors
     * @return true if role can be managed, false otherwise
     */
    private boolean validateRoleForOperation(Role role, String memberName, MemberRoleSyncResult result) {
        if (role == null) {
            return false;
        }

        if (!role.getGuild().getSelfMember().canInteract(role) || role.isManaged()) {
            result.addError("Cannot manage role '" + role.getName() + "' for member " + memberName);
            return false;
        }
        return true;
    }

    /**
     * Executes a single role operation asynchronously.
     *
     * @param operation the operation to execute
     * @param member the target member
     * @param role the target role
     * @param actionType the action type ("add" or "remove")
     * @param result the result object to update
     * @return future representing the operation
     */
    private CompletableFuture<Void> executeRoleOperation(BiFunction<Member, Role, RestAction<Void>> operation,
                                                         Member member, Role role, String actionType,
                                                         MemberRoleSyncResult result) {

        CompletableFuture<Void> future = new CompletableFuture<>();
        boolean isAddOperation = "add".equals(actionType);

        operation.apply(member, role).queue(
                success -> {
                    String memberName = member.getEffectiveName();
                    String roleName = role.getName();
                    String logAction = isAddOperation ? "Added" : "Removed";
                    String preposition = isAddOperation ? "to" : "from";

                    logger.info("{} role '{}' {} member '{}'", logAction, roleName, preposition, memberName);

                    if (isAddOperation) {
                        result.addRoleAssignment(memberName, roleName);
                    } else {
                        result.addRoleRemoval(memberName, roleName);
                    }

                    future.complete(null);
                },
                error -> {
                    String preposition = isAddOperation ? "to" : "from";
                    String errorMessage = "Failed to " + actionType + " role '" + role.getName() +
                            "' " + preposition + " member '" + member.getEffectiveName() + "': " + error.getMessage();

                    logger.error(errorMessage);
                    result.addError(errorMessage);

                    // WICHTIG: Complete mit null statt Exception, damit allOf() nicht abbricht
                    future.complete(null);
                }
        );

        return future;
    }

    /**
     * Finalizes the sync result based on any errors encountered.
     *
     * @param result the result to finalize
     * @return the finalized result
     */
    private MemberRoleSyncResult finalizeResult(MemberRoleSyncResult result) {
        if (!result.getErrors().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("Member role sync completed with errors. Check error list for details.");
        }
        return result;
    }

    /**
     * Represents the difference between current and desired roles for a member.
     */
    private static class RoleDifference {

        /** Role IDs to be added. */
        private final Set<String> rolesToAdd;

        /** Role IDs to be removed. */
        private final Set<String> rolesToRemove;

        /**
         * Creates a new RoleDifference.
         *
         * @param rolesToAdd roles to add
         * @param rolesToRemove roles to remove
         */
        public RoleDifference(Set<String> rolesToAdd, Set<String> rolesToRemove) {
            this.rolesToAdd = rolesToAdd;
            this.rolesToRemove = rolesToRemove;
        }

        /**
         * Gets roles to be added.
         *
         * @return set of role IDs to add
         */
        public Set<String> getRolesToAdd() {
            return rolesToAdd;
        }

        /**
         * Gets roles to be removed.
         *
         * @return set of role IDs to remove
         */
        public Set<String> getRolesToRemove() {
            return rolesToRemove;
        }
    }
}