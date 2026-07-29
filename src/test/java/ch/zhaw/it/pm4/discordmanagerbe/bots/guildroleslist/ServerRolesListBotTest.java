package ch.zhaw.it.pm4.discordmanagerbe.bots.guildroleslist;

import ch.zhaw.it.pm4.discordmanagerbe.dto.ExtendedRoleDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerRoleListDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerRolesListBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private Guild guild;

    @Mock
    private Role role1;

    @Mock
    private Role role2;

    @Mock
    private Role role3;

    @Mock
    private Role.RoleTags roleTag1;

    @Mock
    private Role.RoleTags roleTag2;

    @Mock
    private Role.RoleTags roleTag3;

    private ServerRolesListBot serverRolesListBot;

    private static final String VALID_GUILD_ID = "123456789012345678";
    private static final String INVALID_GUILD_ID = "999999999999999999";
    private static final String GUILD_NAME = "Test Guild";

    @BeforeEach
    void setUp() {
        serverRolesListBot = new ServerRolesListBot(jdaBean);
    }

    @Test
    void fetchGuildRoles_WithValidGuildId_ShouldReturnRoleList() {
        // Arrange
        setupValidGuild();
        setupRoles();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertEquals(3, result.getRoles().size());

        ExtendedRoleDTO firstRole = result.getRoles().getFirst();
        assertEquals("role1", firstRole.getId());
        assertEquals("Admin", firstRole.getName());
        assertEquals("#FF0000", firstRole.getColor());
        assertTrue(firstRole.getPermissions().contains("ADMINISTRATOR"));
    }

    @Test
    void fetchGuildRoles_WithNullGuildId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serverRolesListBot.fetchGuildRoles(null)
        );
        assertEquals("Guild ID must be set.", exception.getMessage());
    }

    @Test
    void fetchGuildRoles_WithEmptyGuildId_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serverRolesListBot.fetchGuildRoles("")
        );
        assertEquals("Guild ID must be set.", exception.getMessage());
    }

    @Test
    void fetchGuildRoles_WithNonExistentGuild_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(jdaBean.getGuildById(INVALID_GUILD_ID)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serverRolesListBot.fetchGuildRoles(INVALID_GUILD_ID)
        );
        assertEquals("Guild not found: " + INVALID_GUILD_ID, exception.getMessage());
    }

    @Test
    void fetchGuildRoles_WithEmptyRoleList_ShouldReturnEmptyList() {
        // Arrange
        when(jdaBean.getGuildById(VALID_GUILD_ID)).thenReturn(guild);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(guild.getId()).thenReturn(VALID_GUILD_ID);
        when(guild.getRoles()).thenReturn(Collections.emptyList());

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }

    @Test
    void fetchGuildRoles_WithRoleHavingNullColor_ShouldHandleGracefully() {
        // Arrange
        setupValidGuild();
        setupRoleWithNullColor();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        ExtendedRoleDTO roleDTO = result.getRoles().getFirst();
        assertNull(roleDTO.getColor());
    }

    @Test
    void fetchGuildRoles_WithBoostRole_ShouldProcessNormally() {
        // Arrange
        setupValidGuild();
        setupBoostRole();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        ExtendedRoleDTO roleDTO = result.getRoles().getFirst();
        assertEquals("boost_role", roleDTO.getId());
        assertEquals("Server Booster", roleDTO.getName());
    }

    @Test
    void fetchGuildRoles_WithBotRole_ShouldProcessNormally() {
        // Arrange
        setupValidGuild();
        setupBotRole();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        ExtendedRoleDTO roleDTO = result.getRoles().getFirst();
        assertEquals("bot_role", roleDTO.getId());
        assertEquals("Bot Role", roleDTO.getName());
        assertEquals("#808080", roleDTO.getColor());
    }

    @Test
    void fetchGuildRoles_WithIntegrationRole_ShouldProcessNormally() {
        // Arrange
        setupValidGuild();
        setupIntegrationRole();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        ExtendedRoleDTO roleDTO = result.getRoles().getFirst();
        assertEquals("integration_role", roleDTO.getId());
        assertEquals("Integration Role", roleDTO.getName());
    }

    @Test
    void fetchGuildRoles_WithPurchasableRole_ShouldProcessNormally() {
        // Arrange
        setupValidGuild();
        setupPurchasableRole();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        ExtendedRoleDTO roleDTO = result.getRoles().getFirst();
        assertEquals("purchasable_role", roleDTO.getId());
        assertEquals("Premium Role", roleDTO.getName());
        assertEquals("#FFFF00", roleDTO.getColor());
    }

    @Test
    void fetchGuildRoles_WithRoleHavingNoPermissions_ShouldReturnEmptyPermissionsList() {
        // Arrange
        setupValidGuild();
        setupRoleWithNoPermissions();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        ExtendedRoleDTO roleDTO = result.getRoles().getFirst();
        assertNotNull(roleDTO.getPermissions());
        assertTrue(roleDTO.getPermissions().isEmpty());
    }

    @Test
    void fetchGuildRoles_WithMultiplePermissions_ShouldReturnAllPermissions() {
        // Arrange
        setupValidGuild();
        setupRoleWithMultiplePermissions();

        // Act
        ServerRoleListDTO result = serverRolesListBot.fetchGuildRoles(VALID_GUILD_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        ExtendedRoleDTO roleDTO = result.getRoles().getFirst();
        List<String> permissions = roleDTO.getPermissions();
        assertTrue(permissions.contains("ADMINISTRATOR"));
        assertTrue(permissions.contains("MANAGE_SERVER"));
        assertTrue(permissions.contains("KICK_MEMBERS"));
    }

    // Helper methods for test setup

    private void setupValidGuild() {
        when(jdaBean.getGuildById(VALID_GUILD_ID)).thenReturn(guild);
        when(guild.getName()).thenReturn(GUILD_NAME);
        when(guild.getId()).thenReturn(VALID_GUILD_ID);
    }

    private void setupRoles() {
        // Setup role1 (Admin role with red color)
        when(role1.getId()).thenReturn("role1");
        when(role1.getName()).thenReturn("Admin");
        when(role1.getColor()).thenReturn(Color.RED);
        when(role1.getPermissions()).thenReturn(EnumSet.of(Permission.ADMINISTRATOR));

        // Setup role2 (Moderator role with blue color)
        when(role2.getId()).thenReturn("role2");
        when(role2.getName()).thenReturn("Moderator");
        when(role2.getColor()).thenReturn(Color.BLUE);
        when(role2.getPermissions()).thenReturn(EnumSet.of(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS));

        // Setup role3 (Member role with no color)
        when(role3.getId()).thenReturn("role3");
        when(role3.getName()).thenReturn("Member");
        when(role3.getColor()).thenReturn(null);
        when(role3.getPermissions()).thenReturn(EnumSet.of(Permission.MESSAGE_SEND));

        when(guild.getRoles()).thenReturn(Arrays.asList(role1, role2, role3));
    }

    private void setupRoleWithNullColor() {
        when(role1.getId()).thenReturn("role1");
        when(role1.getName()).thenReturn("No Color Role");
        when(role1.getColor()).thenReturn(null);
        when(role1.getPermissions()).thenReturn(EnumSet.noneOf(Permission.class));

        when(guild.getRoles()).thenReturn(Collections.singletonList(role1));
    }

    private void setupBoostRole() {
        when(role1.getId()).thenReturn("boost_role");
        when(role1.getName()).thenReturn("Server Booster");
        when(role1.getColor()).thenReturn(Color.PINK);
        when(role1.getPermissions()).thenReturn(EnumSet.noneOf(Permission.class));

        when(guild.getRoles()).thenReturn(Collections.singletonList(role1));
    }

    private void setupBotRole() {
        when(role1.getId()).thenReturn("bot_role");
        when(role1.getName()).thenReturn("Bot Role");
        when(role1.getColor()).thenReturn(Color.GRAY);
        when(role1.getPermissions()).thenReturn(EnumSet.noneOf(Permission.class));

        when(guild.getRoles()).thenReturn(Collections.singletonList(role1));
    }

    private void setupIntegrationRole() {
        when(role1.getId()).thenReturn("integration_role");
        when(role1.getName()).thenReturn("Integration Role");
        when(role1.getColor()).thenReturn(Color.GREEN);
        when(role1.getPermissions()).thenReturn(EnumSet.noneOf(Permission.class));

        when(guild.getRoles()).thenReturn(Collections.singletonList(role1));
    }

    private void setupPurchasableRole() {
        when(role1.getId()).thenReturn("purchasable_role");
        when(role1.getName()).thenReturn("Premium Role");
        when(role1.getColor()).thenReturn(Color.YELLOW);
        when(role1.getPermissions()).thenReturn(EnumSet.noneOf(Permission.class));

        when(guild.getRoles()).thenReturn(Collections.singletonList(role1));
    }

    private void setupRoleWithNoPermissions() {
        when(role1.getId()).thenReturn("no_perms_role");
        when(role1.getName()).thenReturn("No Permissions Role");
        when(role1.getColor()).thenReturn(Color.WHITE);
        when(role1.getPermissions()).thenReturn(EnumSet.noneOf(Permission.class));

        when(guild.getRoles()).thenReturn(Collections.singletonList(role1));
    }

    private void setupRoleWithMultiplePermissions() {
        when(role1.getId()).thenReturn("multi_perms_role");
        when(role1.getName()).thenReturn("Multi Permissions Role");
        when(role1.getColor()).thenReturn(Color.ORANGE);
        when(role1.getPermissions()).thenReturn(EnumSet.of(
                Permission.ADMINISTRATOR,
                Permission.MANAGE_SERVER,
                Permission.KICK_MEMBERS
        ));

        when(guild.getRoles()).thenReturn(Collections.singletonList(role1));
    }
}