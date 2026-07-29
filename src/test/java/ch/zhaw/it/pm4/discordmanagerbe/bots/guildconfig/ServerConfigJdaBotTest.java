package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service.ServerConfigComparisonService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service.SyncService;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerConfigJdaBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private ServerConfigComparisonService comparisonService;

    @Mock
    private SyncService syncService;

    @Mock
    private Guild guild;

    @Mock
    private ServerConfigDTO serverConfig;

    private ServerConfigJdaBot serverConfigJdaBot;

    private static final String TEST_SERVER_ID = "123456789";
    private static final String INVALID_SERVER_ID = "999999999";

    @BeforeEach
    void setUp() {
        serverConfigJdaBot = new ServerConfigJdaBot(jdaBean, comparisonService, syncService);
    }

    @Test
    void constructor_ShouldInitializeAllFields() {
        // Given & When
        ServerConfigJdaBot bot = new ServerConfigJdaBot(jdaBean, comparisonService, syncService);

        // Then
        assertNotNull(bot);
    }

    @Test
    void syncWithDiscordServer_WhenNoChangesNeeded_ShouldReturnNoChangesStatus() {
        // Given
        when(comparisonService.compareWithDiscordServer(TEST_SERVER_ID, serverConfig))
                .thenReturn(Collections.emptyMap());

        // When
        Map<String, Object> result = serverConfigJdaBot.syncWithDiscordServer(TEST_SERVER_ID, serverConfig);

        // Then
        assertEquals("No changes needed", result.get("status"));
        verify(syncService).clearMappings();
        verify(comparisonService).compareWithDiscordServer(TEST_SERVER_ID, serverConfig);
        verifyNoMoreInteractions(syncService);
        verifyNoInteractions(guild);
    }

    @Test
    void syncWithDiscordServer_WhenChangesNeeded_ShouldProcessSyncAndReturnEnrichedResult() {
        // Given
        Map<String, Object> initialDiff = Map.of(
                "channels", Map.of("create", List.of("channel1")),
                "roles", Map.of("update", List.of("role1"))
        );
        Map<String, Object> finalResult = Map.of("remaining", "none");
        Map<String, Object> stats = Map.of("created", 1, "updated", 1);

        when(comparisonService.compareWithDiscordServer(TEST_SERVER_ID, serverConfig))
                .thenReturn(initialDiff)  // First call - changes detected
                .thenReturn(finalResult); // Second call - after sync
        when(jdaBean.getGuildById(TEST_SERVER_ID)).thenReturn(guild);
        when(syncService.getStats()).thenReturn(stats);

        // When
        Map<String, Object> result = serverConfigJdaBot.syncWithDiscordServer(TEST_SERVER_ID, serverConfig);

        // Then
        assertNotNull(result);
        assertEquals(finalResult, result.get("sync"));
        assertEquals(stats, result.get("stats"));
        assertNotNull(result.get("timestamp"));
        assertInstanceOf(String.class, result.get("timestamp"));

        verify(syncService).clearMappings();
        verify(comparisonService, times(2)).compareWithDiscordServer(TEST_SERVER_ID, serverConfig);
        verify(jdaBean).getGuildById(TEST_SERVER_ID);
        verify(syncService).processSync(guild, initialDiff, serverConfig);
        verify(syncService).getStats();
    }

    @Test
    void syncWithDiscordServer_WhenGuildNotFound_ShouldThrowIllegalArgumentException() {
        // Given
        Map<String, Object> diff = Map.of("channels", Map.of("create", List.of("channel1")));
        when(comparisonService.compareWithDiscordServer(INVALID_SERVER_ID, serverConfig))
                .thenReturn(diff);
        when(jdaBean.getGuildById(INVALID_SERVER_ID)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serverConfigJdaBot.syncWithDiscordServer(INVALID_SERVER_ID, serverConfig)
        );

        assertEquals("Guild not found: " + INVALID_SERVER_ID, exception.getMessage());
        verify(syncService).clearMappings();
        verify(comparisonService).compareWithDiscordServer(INVALID_SERVER_ID, serverConfig);
        verify(jdaBean).getGuildById(INVALID_SERVER_ID);
        verifyNoMoreInteractions(syncService);
    }

    @Test
    void syncWithDiscordServer_ShouldLogProcessingInfo() {
        // Given
        Map<String, Object> diff = Map.of(
                "channels", Map.of("create", List.of("channel1")),
                "roles", Map.of("update", List.of("role1")),
                "permissions", Map.of("delete", List.of("perm1"))
        );
        Map<String, Object> finalResult = Collections.emptyMap();

        when(comparisonService.compareWithDiscordServer(TEST_SERVER_ID, serverConfig))
                .thenReturn(diff)
                .thenReturn(finalResult);
        when(jdaBean.getGuildById(TEST_SERVER_ID)).thenReturn(guild);
        when(syncService.getStats()).thenReturn(Collections.emptyMap());

        // When
        serverConfigJdaBot.syncWithDiscordServer(TEST_SERVER_ID, serverConfig);

        // Then
        verify(syncService).processSync(guild, diff, serverConfig);
        // Verify that we process exactly 3 change groups as mocked
        assertEquals(3, diff.size());
    }

    @Test
    void getStatus_ShouldReturnCompleteStatusInfo() {
        // Given
        Status jdaStatus = Status.CONNECTED;
        List<Guild> guilds = List.of(guild, mock(Guild.class));
        Map<String, Object> syncStats = Map.of("total_syncs", 5, "errors", 0);

        when(jdaBean.getStatus()).thenReturn(jdaStatus);
        when(jdaBean.getGuilds()).thenReturn(guilds);
        when(syncService.getStats()).thenReturn(syncStats);

        // When
        Map<String, Object> status = serverConfigJdaBot.getStatus();

        // Then
        assertNotNull(status);
        assertEquals("CONNECTED", status.get("jda"));
        assertEquals(2, status.get("guilds"));
        assertEquals(syncStats, status.get("sync"));

        verify(jdaBean).getStatus();
        verify(jdaBean).getGuilds();
        verify(syncService).getStats();
    }

    @Test
    void getStatus_WhenJdaDisconnected_ShouldReturnDisconnectedStatus() {
        // Given
        Status jdaStatus = Status.DISCONNECTED;
        when(jdaBean.getStatus()).thenReturn(jdaStatus);
        when(jdaBean.getGuilds()).thenReturn(Collections.emptyList());
        when(syncService.getStats()).thenReturn(Collections.emptyMap());

        // When
        Map<String, Object> status = serverConfigJdaBot.getStatus();

        // Then
        assertEquals("DISCONNECTED", status.get("jda"));
        assertEquals(0, status.get("guilds"));
    }

    @Test
    void syncWithDiscordServer_WithComplexDiffStructure_ShouldHandleCorrectly() {
        // Given
        Map<String, Object> complexDiff = Map.of(
                "channels", Map.of(
                        "create", List.of("new-channel-1", "new-channel-2"),
                        "update", List.of("existing-channel-1"),
                        "delete", List.of("old-channel-1")
                ),
                "roles", Map.of(
                        "create", List.of("new-role"),
                        "update", List.of("existing-role-1", "existing-role-2")
                ),
                "permissions", Map.of(
                        "update", List.of("permission-1")
                )
        );
        Map<String, Object> postSyncResult = Map.of("status", "completed");
        Map<String, Object> detailedStats = Map.of(
                "channels_created", 2,
                "channels_updated", 1,
                "channels_deleted", 1,
                "roles_created", 1,
                "roles_updated", 2,
                "permissions_updated", 1
        );

        when(comparisonService.compareWithDiscordServer(TEST_SERVER_ID, serverConfig))
                .thenReturn(complexDiff)
                .thenReturn(postSyncResult);
        when(jdaBean.getGuildById(TEST_SERVER_ID)).thenReturn(guild);
        when(syncService.getStats()).thenReturn(detailedStats);

        // When
        Map<String, Object> result = serverConfigJdaBot.syncWithDiscordServer(TEST_SERVER_ID, serverConfig);

        // Then
        assertNotNull(result);
        assertEquals(postSyncResult, result.get("sync"));
        assertEquals(detailedStats, result.get("stats"));
        assertNotNull(result.get("timestamp"));

        verify(syncService).processSync(guild, complexDiff, serverConfig);
    }

    @Test
    void syncWithDiscordServer_WhenSyncServiceThrowsException_ShouldPropagateException() {
        // Given
        Map<String, Object> diff = Map.of("channels", Map.of("create", List.of("channel1")));
        RuntimeException syncException = new RuntimeException("Sync failed");

        when(comparisonService.compareWithDiscordServer(TEST_SERVER_ID, serverConfig))
                .thenReturn(diff);
        when(jdaBean.getGuildById(TEST_SERVER_ID)).thenReturn(guild);
        doThrow(syncException).when(syncService).processSync(any(), any(), any());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> serverConfigJdaBot.syncWithDiscordServer(TEST_SERVER_ID, serverConfig)
        );

        assertEquals("Sync failed", exception.getMessage());
        verify(syncService).clearMappings();
        verify(syncService).processSync(guild, diff, serverConfig);
    }

    @Test
    void syncWithDiscordServer_WhenComparisonServiceThrowsException_ShouldPropagateException() {
        // Given
        RuntimeException comparisonException = new RuntimeException("Comparison failed");
        when(comparisonService.compareWithDiscordServer(TEST_SERVER_ID, serverConfig))
                .thenThrow(comparisonException);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> serverConfigJdaBot.syncWithDiscordServer(TEST_SERVER_ID, serverConfig)
        );

        assertEquals("Comparison failed", exception.getMessage());
        verify(syncService).clearMappings();
        verifyNoMoreInteractions(syncService);
        verifyNoInteractions(jdaBean);
    }
}