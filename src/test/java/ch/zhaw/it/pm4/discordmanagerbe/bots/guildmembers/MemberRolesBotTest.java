package ch.zhaw.it.pm4.discordmanagerbe.bots.guildmembers;

import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.MemberListDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.RoleDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberRolesBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private Guild guild;

    @Mock
    private Member humanMember1;

    @Mock
    private Member humanMember2;

    @Mock
    private Member botMember;

    @Mock
    private User humanUser1;

    @Mock
    private User humanUser2;

    @Mock
    private User botUser;

    @Mock
    private Role role1;

    @Mock
    private Role role2;

    @Mock
    private Task<List<Member>> loadMembersTask;

    private MemberRolesBot memberRolesBot;

    private static final String VALID_GUILD_ID = "123456789";
    private static final String MEMBER1_ID = "member1";
    private static final String MEMBER2_ID = "member2";
    private static final String BOT_MEMBER_ID = "botmember";
    private static final String ROLE1_ID = "role1";
    private static final String ROLE2_ID = "role2";

    @BeforeEach
    void setUp() {
        memberRolesBot = new MemberRolesBot(jdaBean);
        setupMockBehavior();
    }

    private void setupMockBehavior() {
        // Setup JDA mock
        lenient().when(jdaBean.getGuildById(VALID_GUILD_ID)).thenReturn(guild);
        lenient().when(jdaBean.getGuildById("invalid")).thenReturn(null);

        // Setup Guild mock
        lenient().when(guild.loadMembers()).thenReturn(loadMembersTask);

        // Setup human users
        lenient().when(humanUser1.getName()).thenReturn("TestUser1");
        lenient().when(humanUser1.getDiscriminator()).thenReturn("1234");
        lenient().when(humanUser1.isBot()).thenReturn(false);

        lenient().when(humanUser2.getName()).thenReturn("TestUser2");
        lenient().when(humanUser2.getDiscriminator()).thenReturn("0");
        lenient().when(humanUser2.isBot()).thenReturn(false);

        // Setup bot user
        lenient().when(botUser.getName()).thenReturn("BotUser");
        lenient().when(botUser.getDiscriminator()).thenReturn("0000");
        lenient().when(botUser.isBot()).thenReturn(true);

        // Setup human members
        lenient().when(humanMember1.getUser()).thenReturn(humanUser1);
        lenient().when(humanMember1.getId()).thenReturn(MEMBER1_ID);
        lenient().when(humanMember1.getRoles()).thenReturn(Arrays.asList(role1, role2));

        lenient().when(humanMember2.getUser()).thenReturn(humanUser2);
        lenient().when(humanMember2.getId()).thenReturn(MEMBER2_ID);
        lenient().when(humanMember2.getRoles()).thenReturn(Collections.singletonList(role1));

        // Setup bot member
        lenient().when(botMember.getUser()).thenReturn(botUser);
        lenient().when(botMember.getId()).thenReturn(BOT_MEMBER_ID);
        lenient().when(botMember.getRoles()).thenReturn(Collections.emptyList());

        // Setup roles
        lenient().when(role1.getId()).thenReturn(ROLE1_ID);
        lenient().when(role2.getId()).thenReturn(ROLE2_ID);
    }

    @Test
    void fetchGuildMembers_ValidGuildId_ReturnsCorrectMemberList() {
        // Arrange
        List<Member> allMembers = Arrays.asList(humanMember1, humanMember2, botMember);

        when(loadMembersTask.onSuccess(any())).thenAnswer(invocation -> {
            Consumer<List<Member>> successCallback = invocation.getArgument(0);
            successCallback.accept(allMembers);
            return loadMembersTask;
        });

        // Act
        MemberListDTO result = memberRolesBot.fetchGuildMembers(VALID_GUILD_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(VALID_GUILD_ID);
        assertThat(result.getMembers()).hasSize(2); // Only human members, bot excluded

        // Verify first member
        MemberDTO member1 = result.getMembers().get(0);
        assertThat(member1.getId()).isEqualTo(MEMBER1_ID);
        assertThat(member1.getName()).isEqualTo("TestUser1#1234");
        assertThat(member1.getRoles()).hasSize(2);
        assertThat(member1.getRoles().get(0).getId()).isEqualTo(ROLE1_ID);
        assertThat(member1.getRoles().get(1).getId()).isEqualTo(ROLE2_ID);

        // Verify second member
        MemberDTO member2 = result.getMembers().get(1);
        assertThat(member2.getId()).isEqualTo(MEMBER2_ID);
        assertThat(member2.getName()).isEqualTo("TestUser2"); // No discriminator for "0"
        assertThat(member2.getRoles()).hasSize(1);
        assertThat(member2.getRoles().get(0).getId()).isEqualTo(ROLE1_ID);
    }

    @Test
    void fetchGuildMembers_EmptyMemberList_ReturnsEmptyResult() {
        // Arrange
        when(loadMembersTask.onSuccess(any())).thenAnswer(invocation -> {
            Consumer<List<Member>> successCallback = invocation.getArgument(0);
            successCallback.accept(Collections.emptyList());
            return loadMembersTask;
        });

        // Act
        MemberListDTO result = memberRolesBot.fetchGuildMembers(VALID_GUILD_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(VALID_GUILD_ID);
        assertThat(result.getMembers()).isEmpty();
    }

    @Test
    void fetchGuildMembers_OnlyBotMembers_ReturnsEmptyResult() {
        // Arrange
        List<Member> botOnlyMembers = Collections.singletonList(botMember);

        when(loadMembersTask.onSuccess(any())).thenAnswer(invocation -> {
            Consumer<List<Member>> successCallback = invocation.getArgument(0);
            successCallback.accept(botOnlyMembers);
            return loadMembersTask;
        });

        // Act
        MemberListDTO result = memberRolesBot.fetchGuildMembers(VALID_GUILD_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(VALID_GUILD_ID);
        assertThat(result.getMembers()).isEmpty();
    }

    @Test
    void fetchGuildMembers_NullGuildId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> memberRolesBot.fetchGuildMembers(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Guild ID must be set.");
    }

    @Test
    void fetchGuildMembers_EmptyGuildId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> memberRolesBot.fetchGuildMembers(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Guild ID must be set.");
    }

    @Test
    void fetchGuildMembers_InvalidGuildId_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> memberRolesBot.fetchGuildMembers("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Guild not found: invalid");
    }

    @Test
    void fetchGuildMembers_LoadMembersError_ThrowsRuntimeException() {
        // Arrange
        RuntimeException loadError = new RuntimeException("Failed to load members");

        // Simulate error by making onSuccess throw when called
        when(loadMembersTask.onSuccess(any())).thenThrow(loadError);

        // Act & Assert
        assertThatThrownBy(() -> memberRolesBot.fetchGuildMembers(VALID_GUILD_ID))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void fetchGuildMembers_MemberWithNoRoles_HandledCorrectly() {
        // Arrange
        Member memberWithoutRoles = mock(Member.class);
        User userWithoutRoles = mock(User.class);

        when(userWithoutRoles.getName()).thenReturn("NoRoleUser");
        when(userWithoutRoles.getDiscriminator()).thenReturn("5678");
        when(userWithoutRoles.isBot()).thenReturn(false);
        when(memberWithoutRoles.getUser()).thenReturn(userWithoutRoles);
        when(memberWithoutRoles.getId()).thenReturn("noroleuser");
        when(memberWithoutRoles.getRoles()).thenReturn(Collections.emptyList());

        List<Member> members = Collections.singletonList(memberWithoutRoles);

        when(loadMembersTask.onSuccess(any())).thenAnswer(invocation -> {
            Consumer<List<Member>> successCallback = invocation.getArgument(0);
            successCallback.accept(members);
            return loadMembersTask;
        });

        // Act
        MemberListDTO result = memberRolesBot.fetchGuildMembers(VALID_GUILD_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getMembers()).hasSize(1);

        MemberDTO member = result.getMembers().get(0);
        assertThat(member.getId()).isEqualTo("noroleuser");
        assertThat(member.getName()).isEqualTo("NoRoleUser#5678");
        assertThat(member.getRoles()).isEmpty();
    }

    @Test
    void fetchGuildMembers_DiscriminatorHandling_WorksCorrectly() {
        // Arrange
        Member memberWithZeroDiscriminator = mock(Member.class);
        Member memberWithNormalDiscriminator = mock(Member.class);

        User userWithZeroDiscriminator = mock(User.class);
        User userWithNormalDiscriminator = mock(User.class);

        // User with discriminator "0" (new Discord username system)
        when(userWithZeroDiscriminator.getName()).thenReturn("NewUser");
        when(userWithZeroDiscriminator.getDiscriminator()).thenReturn("0");
        when(userWithZeroDiscriminator.isBot()).thenReturn(false);
        when(memberWithZeroDiscriminator.getUser()).thenReturn(userWithZeroDiscriminator);
        when(memberWithZeroDiscriminator.getId()).thenReturn("newuser");
        when(memberWithZeroDiscriminator.getRoles()).thenReturn(Collections.emptyList());

        // User with normal discriminator (legacy system)
        when(userWithNormalDiscriminator.getName()).thenReturn("OldUser");
        when(userWithNormalDiscriminator.getDiscriminator()).thenReturn("1234");
        when(userWithNormalDiscriminator.isBot()).thenReturn(false);
        when(memberWithNormalDiscriminator.getUser()).thenReturn(userWithNormalDiscriminator);
        when(memberWithNormalDiscriminator.getId()).thenReturn("olduser");
        when(memberWithNormalDiscriminator.getRoles()).thenReturn(Collections.emptyList());

        List<Member> members = Arrays.asList(memberWithZeroDiscriminator, memberWithNormalDiscriminator);

        when(loadMembersTask.onSuccess(any())).thenAnswer(invocation -> {
            Consumer<List<Member>> successCallback = invocation.getArgument(0);
            successCallback.accept(members);
            return loadMembersTask;
        });

        // Act
        MemberListDTO result = memberRolesBot.fetchGuildMembers(VALID_GUILD_ID);

        // Assert
        assertThat(result.getMembers()).hasSize(2);

        // New Discord username (no # shown)
        MemberDTO newUser = result.getMembers().stream()
                .filter(m -> m.getId().equals("newuser"))
                .findFirst()
                .orElseThrow();
        assertThat(newUser.getName()).isEqualTo("NewUser");

        // Legacy Discord username (with # discriminator)
        MemberDTO oldUser = result.getMembers().stream()
                .filter(m -> m.getId().equals("olduser"))
                .findFirst()
                .orElseThrow();
        assertThat(oldUser.getName()).isEqualTo("OldUser#1234");
    }

    @Test
    void fetchGuildMembers_VerifyMethodCalls() {
        // Arrange
        List<Member> members = Arrays.asList(humanMember1, humanMember2);

        when(loadMembersTask.onSuccess(any())).thenAnswer(invocation -> {
            Consumer<List<Member>> successCallback = invocation.getArgument(0);
            successCallback.accept(members);
            return loadMembersTask;
        });

        // Act
        memberRolesBot.fetchGuildMembers(VALID_GUILD_ID);

        // Assert
        verify(jdaBean).getGuildById(VALID_GUILD_ID);
        verify(guild).loadMembers();
        verify(loadMembersTask).onSuccess(any());

        // Verify bot filtering
        verify(humanUser1).isBot();
        verify(humanUser2).isBot();
        verify(botUser, never()).isBot(); // Bot member should be filtered out before checking
    }
}