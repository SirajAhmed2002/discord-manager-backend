package ch.zhaw.it.pm4.discordmanagerbe.bots.guildlist;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.DiscordServerRepository;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerListDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerListJdaBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private DiscordServerRepository discordServerRepository;

    @Mock
    private Guild guild1;

    @Mock
    private Guild guild2;

    @Mock
    private Member member;

    @Mock
    private CacheRestAction<Member> memberRestAction;

    private ServerListJdaBot serverListJdaBot;

    private static final String USER_ID = "123456789";
    private static final String GUILD_ID_1 = "guild1";
    private static final String GUILD_ID_2 = "guild2";
    private static final String GUILD_NAME_1 = "Test Guild 1";
    private static final String GUILD_NAME_2 = "Test Guild 2";
    private static final String OWNER_ID = "123";

    @BeforeEach
    void setUp() {
        serverListJdaBot = new ServerListJdaBot(jdaBean, discordServerRepository);
    }

    @Test
    void testGetAllGuilds() {
        // Arrange
        List<Guild> expectedGuilds = Arrays.asList(guild1, guild2);
        when(jdaBean.getGuilds()).thenReturn(expectedGuilds);

        // Act
        List<Guild> actualGuilds = serverListJdaBot.getAllGuilds();

        // Assert
        assertEquals(expectedGuilds, actualGuilds);
        verify(jdaBean).getGuilds();
    }

    @Test
    void testGetGuildsWhereUserIsAdmin_UserIsOwner() {
        // Arrange
        when(jdaBean.getGuilds()).thenReturn(Arrays.asList(guild1, guild2));
        when(guild1.retrieveMemberById(USER_ID)).thenReturn(memberRestAction);
        when(memberRestAction.complete()).thenReturn(member);
        when(member.getIdLong()).thenReturn(Long.parseLong(USER_ID));
        when(guild1.getOwnerIdLong()).thenReturn(Long.parseLong(USER_ID));

        CacheRestAction<Member> memberRestAction2 = mock(CacheRestAction.class);
        when(guild2.retrieveMemberById(USER_ID)).thenReturn(memberRestAction2);
        when(memberRestAction2.complete()).thenReturn(null);

        // Act
        List<Guild> adminGuilds = serverListJdaBot.getGuildsWhereUserIsAdmin(USER_ID);

        // Assert
        assertEquals(1, adminGuilds.size());
        assertTrue(adminGuilds.contains(guild1));
    }

    @Test
    void testGetGuildsWhereUserIsAdmin_UserHasAdministratorPermission() {
        // Arrange
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        when(guild1.retrieveMemberById(USER_ID)).thenReturn(memberRestAction);
        when(memberRestAction.complete()).thenReturn(member);
        when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(true);

        // Act
        List<Guild> adminGuilds = serverListJdaBot.getGuildsWhereUserIsAdmin(USER_ID);

        // Assert
        assertEquals(1, adminGuilds.size());
        assertTrue(adminGuilds.contains(guild1));
    }

    @Test
    void testGetGuildsWhereUserIsAdmin_UserHasEffectiveAdminPermissions() {
        // Arrange
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        when(guild1.retrieveMemberById(USER_ID)).thenReturn(memberRestAction);
        when(memberRestAction.complete()).thenReturn(member);
        when(member.getIdLong()).thenReturn(Long.parseLong(USER_ID));
        when(guild1.getOwnerIdLong()).thenReturn(999L);
        when(member.hasPermission(Permission.ADMINISTRATOR)).thenReturn(false);
        when(member.hasPermission(any(EnumSet.class))).thenReturn(true);

        // Act
        List<Guild> adminGuilds = serverListJdaBot.getGuildsWhereUserIsAdmin(USER_ID);

        // Assert
        assertEquals(1, adminGuilds.size());
        assertTrue(adminGuilds.contains(guild1));
    }

    @Test
    void testGetGuildsWhereUserIsAdmin_UserNotInGuild() {
        // Arrange
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        when(guild1.retrieveMemberById(USER_ID)).thenReturn(memberRestAction);
        when(memberRestAction.complete()).thenReturn(null);

        // Act
        List<Guild> adminGuilds = serverListJdaBot.getGuildsWhereUserIsAdmin(USER_ID);

        // Assert
        assertTrue(adminGuilds.isEmpty());
    }

    @Test
    void testGetGuildsWhereUserIsAdmin_ExceptionThrown() {
        // Arrange
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        when(guild1.retrieveMemberById(USER_ID)).thenReturn(memberRestAction);
        when(memberRestAction.complete()).thenThrow(new RuntimeException("API Error"));

        // Act
        List<Guild> adminGuilds = serverListJdaBot.getGuildsWhereUserIsAdmin(USER_ID);

        // Assert
        assertTrue(adminGuilds.isEmpty());
    }

    @Test
    void testGetAllServersDTO() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        setupGuildForDTO(guild2, GUILD_ID_2, GUILD_NAME_2, OWNER_ID, 50);
        when(jdaBean.getGuilds()).thenReturn(Arrays.asList(guild1, guild2));

        // Act
        ServerListDTO result = serverListJdaBot.getAllServersDTO();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getServers().size());

        ServerDTO server1DTO = result.getServers().get(0);
        assertEquals(GUILD_ID_1, server1DTO.getId());
        assertEquals(GUILD_NAME_1, server1DTO.getName());
        assertEquals(OWNER_ID, server1DTO.getOwnerId());
        assertEquals(100, server1DTO.getMemberCount());

        verify(discordServerRepository, times(2)).save(any(DiscordServer.class));
    }

    @Test
    void testGetAdminServersDTO() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, USER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(Arrays.asList(guild1, guild2));

        // User is owner of guild1
        when(guild1.retrieveMemberById(USER_ID)).thenReturn(memberRestAction);
        when(memberRestAction.complete()).thenReturn(member);
        when(member.getIdLong()).thenReturn(Long.parseLong(USER_ID));
        when(guild1.getOwnerIdLong()).thenReturn(Long.parseLong(USER_ID));

        // User is not in guild2
        CacheRestAction<Member> memberRestAction2 = mock(CacheRestAction.class);
        when(guild2.retrieveMemberById(USER_ID)).thenReturn(memberRestAction2);
        when(memberRestAction2.complete()).thenReturn(null);

        // Act
        ServerListDTO result = serverListJdaBot.getAdminServersDTO(USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getServers().size());
        assertEquals(GUILD_ID_1, result.getServers().get(0).getId());
    }

    @Test
    void testGetAllServersJson() throws JsonProcessingException {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));

        // Act
        String json = serverListJdaBot.getAllServersJson();

        // Assert
        assertNotNull(json);
        assertTrue(json.contains(GUILD_ID_1));
        assertTrue(json.contains(GUILD_NAME_1));
        assertTrue(json.contains("servers"));
    }

    @Test
    void testGetAdminServersJson() throws JsonProcessingException {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        when(guild1.retrieveMemberById(USER_ID)).thenReturn(memberRestAction);
        when(memberRestAction.complete()).thenReturn(member);
        when(member.getIdLong()).thenReturn(Long.parseLong(USER_ID));
        when(guild1.getOwnerIdLong()).thenReturn(Long.parseLong(USER_ID));

        // Act
        String json = serverListJdaBot.getAdminServersJson(USER_ID);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains(GUILD_ID_1));
        assertTrue(json.contains("servers"));
    }

    @Test
    void testGetAllServersFromDatabase() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        List<DiscordServer> expectedServers = List.of(
                new DiscordServer(GUILD_ID_1, GUILD_NAME_1, OWNER_ID)
        );
        when(discordServerRepository.findAll()).thenReturn(expectedServers);

        // Act
        List<DiscordServer> result = serverListJdaBot.getAllServersFromDatabase();

        // Assert
        assertEquals(expectedServers, result);
        verify(discordServerRepository).save(any(DiscordServer.class));
        verify(discordServerRepository).findAll();
    }

    @Test
    void testGetServersByOwner() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        List<DiscordServer> expectedServers = List.of(
                new DiscordServer(GUILD_ID_1, GUILD_NAME_1, OWNER_ID)
        );
        when(discordServerRepository.findByOwnerId(OWNER_ID)).thenReturn(expectedServers);

        // Act
        List<DiscordServer> result = serverListJdaBot.getServersByOwner(OWNER_ID);

        // Assert
        assertEquals(expectedServers, result);
        verify(discordServerRepository).findByOwnerId(OWNER_ID);
    }

    @Test
    void testSyncGuildsToDatabase() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        setupGuildForDTO(guild2, GUILD_ID_2, GUILD_NAME_2, OWNER_ID, 50);
        when(jdaBean.getGuilds()).thenReturn(Arrays.asList(guild1, guild2));

        // Act
        serverListJdaBot.syncGuildsToDatabase();

        // Assert
        verify(discordServerRepository, times(2)).save(any(DiscordServer.class));
    }

    @Test
    void testSaveGuildToDatabase_NewServer() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        when(discordServerRepository.findByServerId(GUILD_ID_1)).thenReturn(Optional.empty());

        // Act
        serverListJdaBot.syncGuildsToDatabase();

        // Assert
        verify(discordServerRepository).findByServerId(GUILD_ID_1);
        verify(discordServerRepository).save(argThat(server ->
                server.getServerId().equals(GUILD_ID_1) &&
                        server.getServerName().equals(GUILD_NAME_1) &&
                        server.getOwnerId().equals(OWNER_ID)
        ));
    }

    @Test
    void testSaveGuildToDatabase_UpdateExistingServer() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        DiscordServer existingServer = new DiscordServer(GUILD_ID_1, "Old Name", "old_owner");
        when(discordServerRepository.findByServerId(GUILD_ID_1)).thenReturn(Optional.of(existingServer));

        // Act
        serverListJdaBot.syncGuildsToDatabase();

        // Assert
        verify(discordServerRepository).findByServerId(GUILD_ID_1);
        verify(discordServerRepository).save(argThat(server ->
                server.getServerId().equals(GUILD_ID_1) &&
                        server.getServerName().equals(GUILD_NAME_1) &&
                        server.getOwnerId().equals(OWNER_ID)
        ));
    }

    @Test
    void testSaveGuildToDatabase_DatabaseException() {
        // Arrange
        setupGuildForDTO(guild1, GUILD_ID_1, GUILD_NAME_1, OWNER_ID, 100);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));
        when(discordServerRepository.findByServerId(GUILD_ID_1))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> serverListJdaBot.syncGuildsToDatabase());
    }

    @Test
    void testEmptyGuildsList() {
        // Arrange
        when(jdaBean.getGuilds()).thenReturn(Collections.emptyList());

        // Act
        List<Guild> allGuilds = serverListJdaBot.getAllGuilds();
        List<Guild> adminGuilds = serverListJdaBot.getGuildsWhereUserIsAdmin(USER_ID);
        ServerListDTO allServersDTO = serverListJdaBot.getAllServersDTO();

        // Assert
        assertTrue(allGuilds.isEmpty());
        assertTrue(adminGuilds.isEmpty());
        assertTrue(allServersDTO.getServers().isEmpty());
    }

    @Test
    void testConvertGuildToServerDTO() {
        // Arrange
        OffsetDateTime creationTime = OffsetDateTime.now();
        when(guild1.getId()).thenReturn(GUILD_ID_1);
        when(guild1.getName()).thenReturn(GUILD_NAME_1);
        when(guild1.getOwnerId()).thenReturn(OWNER_ID);
        when(guild1.getMemberCount()).thenReturn(100);
        when(guild1.getTimeCreated()).thenReturn(creationTime);
        when(jdaBean.getGuilds()).thenReturn(List.of(guild1));

        // Act
        ServerListDTO result = serverListJdaBot.getAllServersDTO();

        // Assert
        assertEquals(1, result.getServers().size());
        ServerDTO serverDTO = result.getServers().get(0);
        assertEquals(GUILD_ID_1, serverDTO.getId());
        assertEquals(GUILD_NAME_1, serverDTO.getName());
        assertEquals(OWNER_ID, serverDTO.getOwnerId());
        assertEquals(100, serverDTO.getMemberCount());
        assertEquals(creationTime, serverDTO.getCreationDate());
    }

    // Helper method to setup guild mocks for DTO conversion
    private void setupGuildForDTO(Guild guild, String id, String name, String ownerId, int memberCount) {
        lenient().when(guild.getId()).thenReturn(id);
        lenient().when(guild.getName()).thenReturn(name);
        lenient().when(guild.getOwnerId()).thenReturn(ownerId);
        lenient().when(guild.getMemberCount()).thenReturn(memberCount);
        lenient().when(guild.getTimeCreated()).thenReturn(OffsetDateTime.now());
        lenient().when(guild.getOwnerIdLong()).thenReturn(Long.parseLong(ownerId));
    }
}