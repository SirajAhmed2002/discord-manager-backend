package ch.zhaw.it.pm4.discordmanagerbe.bots.guildmemberroleconfig;

import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberListDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.RoleDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberRolesConfigBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private Guild guild;

    @Mock
    private Member selfMember;

    @Mock
    private Member targetMember;

    @Mock
    private User targetUser;

    @Mock
    private Role role1;

    @Mock
    private Role role2;

    @Mock
    private Role managedRole;

    @Mock
    private AuditableRestAction<Void> addRoleAction;

    @Mock
    private AuditableRestAction<Void> removeRoleAction;

    @Mock
    private Task<List<Member>> loadMembersTask;

    private MemberRolesConfigBot bot;
    private static final String GUILD_ID = "123456789";
    private static final String MEMBER_ID = "987654321";
    private static final String ROLE1_ID = "111111111";
    private static final String ROLE2_ID = "222222222";
    private static final String MANAGED_ROLE_ID = "333333333";

    @BeforeEach
    void setUp() {
        bot = new MemberRolesConfigBot(jdaBean);
        setupBasicMocks();
    }

    private void setupBasicMocks() {
        // Basic guild setup
        lenient().when(jdaBean.getGuildById(GUILD_ID)).thenReturn(guild);
        lenient().when(guild.getSelfMember()).thenReturn(selfMember);
        lenient().when(selfMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(true);

        // Member setup
        lenient().when(targetMember.getId()).thenReturn(MEMBER_ID);
        lenient().when(targetMember.getUser()).thenReturn(targetUser);
        lenient().when(targetUser.isBot()).thenReturn(false);
        lenient().when(targetMember.getEffectiveName()).thenReturn("TestUser");
        lenient().when(targetMember.getRoles()).thenReturn(new ArrayList<>());

        // Role setup
        lenient().when(role1.getId()).thenReturn(ROLE1_ID);
        lenient().when(role1.getName()).thenReturn("Role1");
        lenient().when(role1.getGuild()).thenReturn(guild);
        lenient().when(role1.isManaged()).thenReturn(false);
        lenient().when(selfMember.canInteract(role1)).thenReturn(true);

        lenient().when(role2.getId()).thenReturn(ROLE2_ID);
        lenient().when(role2.getName()).thenReturn("Role2");
        lenient().when(role2.getGuild()).thenReturn(guild);
        lenient().when(role2.isManaged()).thenReturn(false);
        lenient().when(selfMember.canInteract(role2)).thenReturn(true);

        lenient().when(managedRole.getId()).thenReturn(MANAGED_ROLE_ID);
        lenient().when(managedRole.getName()).thenReturn("ManagedRole");
        lenient().when(managedRole.getGuild()).thenReturn(guild);
        lenient().when(managedRole.isManaged()).thenReturn(true);

        lenient().when(guild.getRoles()).thenReturn(Arrays.asList(role1, role2, managedRole));

        // Mock member loading - KORRIGIERT für Method Chaining
        lenient().when(guild.loadMembers()).thenReturn(loadMembersTask);
        lenient().when(loadMembersTask.onSuccess(any(Consumer.class))).thenReturn(loadMembersTask);
        lenient().when(loadMembersTask.onError(any(Consumer.class))).thenReturn(loadMembersTask);

        // Mock role operations
        lenient().when(guild.addRoleToMember(any(Member.class), any(Role.class))).thenReturn(addRoleAction);
        lenient().when(guild.removeRoleFromMember(any(Member.class), any(Role.class))).thenReturn(removeRoleAction);

        lenient().doAnswer(invocation -> {
            Consumer<Void> successCallback = invocation.getArgument(0);
            successCallback.accept(null);
            return null;
        }).when(addRoleAction).queue(any(Consumer.class), any(Consumer.class));

        lenient().doAnswer(invocation -> {
            Consumer<Void> successCallback = invocation.getArgument(0);
            successCallback.accept(null);
            return null;
        }).when(removeRoleAction).queue(any(Consumer.class), any(Consumer.class));
    }

    private void mockMemberLoading(List<Member> members) {
        // Mock das Verhalten so, dass onSuccess den Consumer mit den Members aufruft
        lenient().when(loadMembersTask.onSuccess(any(Consumer.class))).thenAnswer(invocation -> {
            Consumer<List<Member>> successCallback = invocation.getArgument(0);
            successCallback.accept(members);
            return loadMembersTask; // Wichtig: Task-Objekt zurückgeben für Method Chaining
        });

        // onError sollte das Task-Objekt zurückgeben ohne den Consumer aufzurufen
        lenient().when(loadMembersTask.onError(any(Consumer.class))).thenReturn(loadMembersTask);
    }

    @Test
    void testSyncMemberRoles_ValidInput_Success() {
        // Arrange
        mockMemberLoading(List.of(targetMember));

        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Member role sync completed.", result.getMessage());
    }

    @Test
    void testSyncMemberRoles_NullGuildId_ReturnsError() {
        // Arrange
        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(null, memberListDTO, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid member list DTO or guild ID.", result.getMessage());
    }

    @Test
    void testSyncMemberRoles_EmptyGuildId_ReturnsError() {
        // Arrange
        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles("", memberListDTO, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid member list DTO or guild ID.", result.getMessage());
    }

    @Test
    void testSyncMemberRoles_NullMemberListDTO_ReturnsError() {
        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, null, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid member list DTO or guild ID.", result.getMessage());
    }

    @Test
    void testSyncMemberRoles_GuildNotFound_ReturnsError() {
        // Arrange
        when(jdaBean.getGuildById(GUILD_ID)).thenReturn(null);
        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Guild not found: " + GUILD_ID, result.getMessage());
    }

    @Test
    void testSyncMemberRoles_BotLacksPermission_ReturnsError() {
        // Arrange
        when(selfMember.hasPermission(Permission.MANAGE_ROLES)).thenReturn(false);
        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Bot lacks MANAGE_ROLES permission.", result.getMessage());
    }

    @Test
    void testSyncMemberRoles_MemberNotFound_AddsError() throws InterruptedException {
        // Arrange
        mockMemberLoading(List.of()); // Empty member list

        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess()); // Should be false due to errors
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().getFirst().contains("Member not found in guild"));
    }

    @Test
    void testSyncMemberRoles_AddRolesToMember_Success() throws InterruptedException {
        // Arrange
        mockMemberLoading(List.of(targetMember));

        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        verify(guild, times(2)).addRoleToMember(eq(targetMember), any(Role.class));
        verify(addRoleAction, times(2)).queue(any(Consumer.class), any(Consumer.class));
    }

    @Test
    void testSyncMemberRoles_RemoveUnlistedRoles_Success() throws InterruptedException {
        // Arrange
        when(targetMember.getRoles()).thenReturn(Arrays.asList(role1, role2));
        mockMemberLoading(List.of(targetMember));

        // Create DTO with only one role, so the other should be removed
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(MEMBER_ID);
        memberDTO.setName("TestUser");

        RoleDTO roleDTO = new RoleDTO(ROLE1_ID);
        memberDTO.setRoles(List.of(roleDTO));

        MemberListDTO memberListDTO = new MemberListDTO("guild123", List.of(memberDTO));

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, true);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        verify(guild, never()).addRoleToMember(any(), any()); // No roles to add
        verify(guild, times(1)).removeRoleFromMember(eq(targetMember), eq(role2));
    }

    @Test
    void testSyncMemberRoles_ManagedRole_SkipsOperation() throws InterruptedException {
        // Arrange
        mockMemberLoading(List.of(targetMember));

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(MEMBER_ID);
        memberDTO.setName("TestUser");

        RoleDTO roleDTO = new RoleDTO(MANAGED_ROLE_ID);
        memberDTO.setRoles(List.of(roleDTO));

        MemberListDTO memberListDTO = new MemberListDTO("guild123", List.of(memberDTO));

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess()); // Should have errors due to managed role
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Cannot manage role 'ManagedRole'")));
    }

    @Test
    void testSyncMemberRoles_BotMember_SkipsMember() throws InterruptedException {
        // Arrange
        Member botMember = mock(Member.class);
        User botUser = mock(User.class);
        when(botMember.getUser()).thenReturn(botUser);
        when(botUser.isBot()).thenReturn(true);

        mockMemberLoading(Arrays.asList(targetMember, botMember));

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId("bot123"); // Try to assign role to bot
        memberDTO.setName("BotUser");

        RoleDTO roleDTO = new RoleDTO(ROLE1_ID);
        memberDTO.setRoles(List.of(roleDTO));

        MemberListDTO memberListDTO = new MemberListDTO("guild123", List.of(memberDTO));

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess()); // Should have error because bot member not found in lookup
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Member not found in guild")));
    }

    @Test
    void testSyncMemberRoles_RoleOperationFailure_HandlesError() throws InterruptedException {
        // Arrange
        mockMemberLoading(List.of(targetMember));

        // Mock failure for add role operation
        doAnswer(invocation -> {
            Consumer<Throwable> errorCallback = invocation.getArgument(1);
            errorCallback.accept(new RuntimeException("Discord API Error"));
            return null;
        }).when(addRoleAction).queue(any(Consumer.class), any(Consumer.class));

        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Failed to add role")));
    }

    @Test
    void testSyncMemberRoles_CannotInteractWithRole_SkipsRole() throws InterruptedException {
        // Arrange
        when(selfMember.canInteract(role1)).thenReturn(false);
        mockMemberLoading(List.of(targetMember));

        MemberListDTO memberListDTO = createMemberListDTO();

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Cannot manage role 'Role1'")));
    }

    @Test
    void testSyncMemberRoles_EmptyMemberList_Success() {
        // Arrange
        mockMemberLoading(List.of(targetMember));

        MemberListDTO memberListDTO = new MemberListDTO("guild123", List.of());

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Member role sync completed.", result.getMessage());
    }

    @Test
    void testSyncMemberRoles_RoleNotFoundInGuild_SkipsRole() throws InterruptedException {
        // Arrange
        mockMemberLoading(List.of(targetMember));

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(MEMBER_ID);
        memberDTO.setName("TestUser");

        RoleDTO roleDTO = new RoleDTO("nonexistent123");
        memberDTO.setRoles(List.of(roleDTO));

        MemberListDTO memberListDTO = new MemberListDTO("guild123", List.of(memberDTO));

        // Act
        MemberRoleSyncResult result = bot.syncMemberRoles(GUILD_ID, memberListDTO, false);

        // Give some time for async operations
        Thread.sleep(100);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess()); // Should succeed because role is simply skipped
        verify(guild, never()).addRoleToMember(any(), any());
    }

    private MemberListDTO createMemberListDTO() {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(MEMBER_ID);
        memberDTO.setName("TestUser");

        RoleDTO role1DTO = new RoleDTO(ROLE1_ID);
        RoleDTO role2DTO = new RoleDTO(ROLE2_ID);

        memberDTO.setRoles(Arrays.asList(role1DTO, role2DTO));

        return new MemberListDTO("guild123", List.of(memberDTO));
    }
}