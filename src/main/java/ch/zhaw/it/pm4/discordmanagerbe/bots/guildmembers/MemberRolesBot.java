package ch.zhaw.it.pm4.discordmanagerbe.bots.guildmembers;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberListDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.RoleDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Discord bot that retrieves all members of a specified guild and their roles.
 * Filters out bot users and returns only human members with their associated role information.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_MEMBER_LIST)
@Component
public class MemberRolesBot extends AbstractJdaBot {

    /** Logger for this class. */
    private static final Logger logger = LoggerFactory.getLogger(MemberRolesBot.class);

    /** Timeout in seconds for member loading operations. */
    private static final int TIMEOUT_SECONDS = 30;

    /**
     * Creates a new MemberRolesBot instance.
     *
     * @param jdaBean the JDA instance to use for Discord operations
     */
    public MemberRolesBot(JDA jdaBean) {
        super(jdaBean);
    }

    /**
     * This method retrieves all members of the specified guild and their roles.
     *
     * @param guildId The Discord guild ID to fetch members from
     * @return MemberListDTO containing the guild members and their roles
     * @throws IllegalArgumentException if guildId is invalid or guild not found
     */
    public MemberListDTO fetchGuildMembers(String guildId) {
        validateGuildId(guildId);
        Guild guild = getGuild(guildId);

        CompletableFuture<MemberListDTO> resultFuture = new CompletableFuture<>();

        guild.loadMembers()
                .onSuccess(members -> handleMembersLoaded(members, guildId, resultFuture))
                .onError(error -> handleLoadError(error, guildId, resultFuture));

        return awaitResult(resultFuture);
    }

    /**
     * Validates that the guild ID is not null or empty.
     *
     * @param guildId the guild ID to validate
     * @throws IllegalArgumentException if guild ID is null or empty
     */
    private void validateGuildId(String guildId) {
        if (guildId == null || guildId.isEmpty()) {
            logger.error("Guild ID is not set.");
            throw new IllegalArgumentException("Guild ID must be set.");
        }
    }

    /**
     * Retrieves the guild by ID.
     *
     * @param guildId the guild ID
     * @return the guild instance
     * @throws IllegalArgumentException if guild is not found
     */
    private Guild getGuild(String guildId) {
        Guild guild = jdaBean.getGuildById(guildId);
        if (guild == null) {
            logger.error("Guild not found: {}", guildId);
            throw new IllegalArgumentException("Guild not found: " + guildId);
        }
        return guild;
    }

    /**
     * Handles successfully loaded members by converting them to DTOs.
     *
     * @param members the loaded members
     * @param guildId the guild ID
     * @param resultFuture the future to complete with the result
     */
    private void handleMembersLoaded(List<Member> members, String guildId, CompletableFuture<MemberListDTO> resultFuture) {
        List<MemberDTO> membersList = convertMembersToDTO(members);
        MemberListDTO response = new MemberListDTO(guildId, membersList);

        logResult(response, guildId);
        resultFuture.complete(response);
    }

    /**
     * Handles errors during member loading.
     *
     * @param error the error that occurred
     * @param guildId the guild ID
     * @param resultFuture the future to complete exceptionally
     */
    private void handleLoadError(Throwable error, String guildId, CompletableFuture<MemberListDTO> resultFuture) {
        logger.error("Error loading members for guild {}: {}", guildId, error.getMessage());
        resultFuture.completeExceptionally(new RuntimeException("Error loading members: " + error.getMessage(), error));
    }

    /**
     * Converts a list of members to DTOs, filtering out bots.
     *
     * @param members the members to convert
     * @return list of member DTOs
     */
    private List<MemberDTO> convertMembersToDTO(List<Member> members) {
        return members.stream()
                .filter(member -> !member.getUser().isBot())
                .map(this::convertMemberToDTO)
                .toList();
    }

    /**
     * Converts a single member to DTO.
     *
     * @param member the member to convert
     * @return the member DTO
     */
    private MemberDTO convertMemberToDTO(Member member) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setName(createDisplayName(member.getUser()));
        memberDTO.setId(member.getId());
        memberDTO.setRoles(convertRolesToDTO(member.getRoles()));
        return memberDTO;
    }

    /**
     * Creates a display name for a user, including discriminator if not "0".
     *
     * @param user the user
     * @return the formatted display name
     */
    private String createDisplayName(User user) {
        String discriminator = user.getDiscriminator();
        return user.getName() + (!Objects.equals(discriminator, "0") ? "#" + discriminator : "");
    }

    /**
     * Converts a list of roles to DTOs.
     *
     * @param roles the roles to convert
     * @return list of role DTOs
     */
    private List<RoleDTO> convertRolesToDTO(List<Role> roles) {
        return roles.stream()
                .map(role -> {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId(role.getId());
                    return roleDTO;
                })
                .toList();
    }

    /**
     * Logs the result of the member fetch operation.
     *
     * @param response the response to log
     * @param guildId the guild ID
     */
    private void logResult(MemberListDTO response, String guildId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            logger.info("Fetched {} human members from guild {}", response.getMembers().size(), guildId);
            logger.debug(mapper.writeValueAsString(response));
        } catch (Exception ex) {
            logger.error("Error generating JSON", ex);
        }
    }

    /**
     * Waits for the result future to complete within the timeout period.
     *
     * @param resultFuture the future to wait for
     * @return the completed result
     * @throws RuntimeException if timeout occurs or execution fails
     */
    private MemberListDTO awaitResult(CompletableFuture<MemberListDTO> resultFuture) {
        try {
            return resultFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to fetch guild members: " + e.getMessage(), e);
        }
    }
}