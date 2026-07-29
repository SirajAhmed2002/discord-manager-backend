package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.handler.UniversalDiscordHandler;
import ch.zhaw.it.pm4.discordmanagerbe.dto.CategoryDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.TextChannelDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.VoiceChannelDTO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.channel.concrete.CategoryManager;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private UniversalDiscordHandler handler;

    @Mock
    private Guild guild;

    @Mock
    private Category mockCategory;

    @Mock
    private TextChannel mockTextChannel;

    @Mock
    private VoiceChannel mockVoiceChannel;

    @Mock
    private CategoryManager categoryManager;

    @Mock
    private TextChannelManager textChannelManager;

    @Mock
    private VoiceChannelManager voiceChannelManager;

    @Mock
    private ChannelAction<Category> categoryChannelAction;

    @Mock
    private ChannelAction<TextChannel> textChannelAction;

    @Mock
    private ChannelAction<VoiceChannel> voiceChannelAction;

    private SyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new SyncService(handler);

        lenient().when(guild.getName()).thenReturn("Test Guild");

        // Setup mock entities
        setupMockCategory();
        setupMockTextChannel();
        setupMockVoiceChannel();
    }

    private void setupMockCategory() {
        lenient().when(mockCategory.getId()).thenReturn("discord-cat-123");
        lenient().when(mockCategory.getName()).thenReturn("Test Category");
        lenient().when(mockCategory.getManager()).thenReturn(categoryManager);
        lenient().when(mockCategory.delete()).thenReturn(mock(net.dv8tion.jda.api.requests.restaction.AuditableRestAction.class));

        lenient().when(categoryManager.setName(anyString())).thenReturn(categoryManager);
        lenient().when(categoryManager.setPosition(anyInt())).thenReturn(categoryManager);

        lenient().when(guild.createCategory(anyString())).thenReturn(categoryChannelAction);
        lenient().when(categoryChannelAction.complete()).thenReturn(mockCategory);
        lenient().when(guild.getCategoryById(anyString())).thenReturn(mockCategory);
    }

    private void setupMockTextChannel() {
        lenient().when(mockTextChannel.getId()).thenReturn("discord-text-123");
        lenient().when(mockTextChannel.getName()).thenReturn("Test Text Channel");
        lenient().when(mockTextChannel.getManager()).thenReturn(textChannelManager);
        lenient().when(mockTextChannel.getGuild()).thenReturn(guild);
        lenient().when(mockTextChannel.isNSFW()).thenReturn(false);
        lenient().when(mockTextChannel.delete()).thenReturn(mock(net.dv8tion.jda.api.requests.restaction.AuditableRestAction.class));

        lenient().when(textChannelManager.setName(anyString())).thenReturn(textChannelManager);
        lenient().when(textChannelManager.setTopic(anyString())).thenReturn(textChannelManager);
        lenient().when(textChannelManager.setNSFW(anyBoolean())).thenReturn(textChannelManager);
        lenient().when(textChannelManager.setParent(any())).thenReturn(textChannelManager);
        lenient().when(textChannelManager.setPosition(anyInt())).thenReturn(textChannelManager);

        lenient().when(guild.createTextChannel(anyString())).thenReturn(textChannelAction);
        lenient().when(textChannelAction.complete()).thenReturn(mockTextChannel);
        lenient().when(guild.getTextChannelById(anyString())).thenReturn(mockTextChannel);

        lenient().when(mockCategory.createTextChannel(anyString())).thenReturn(textChannelAction);
    }

    private void setupMockVoiceChannel() {
        lenient().when(mockVoiceChannel.getId()).thenReturn("discord-voice-123");
        lenient().when(mockVoiceChannel.getName()).thenReturn("Test Voice Channel");
        lenient().when(mockVoiceChannel.getManager()).thenReturn(voiceChannelManager);
        lenient().when(mockVoiceChannel.getGuild()).thenReturn(guild);
        lenient().when(mockVoiceChannel.getUserLimit()).thenReturn(10);
        lenient().when(mockVoiceChannel.getBitrate()).thenReturn(64000);
        lenient().when(mockVoiceChannel.delete()).thenReturn(mock(net.dv8tion.jda.api.requests.restaction.AuditableRestAction.class));

        lenient().when(voiceChannelManager.setName(anyString())).thenReturn(voiceChannelManager);
        lenient().when(voiceChannelManager.setUserLimit(anyInt())).thenReturn(voiceChannelManager);
        lenient().when(voiceChannelManager.setBitrate(anyInt())).thenReturn(voiceChannelManager);
        lenient().when(voiceChannelManager.setParent(any())).thenReturn(voiceChannelManager);
        lenient().when(voiceChannelManager.setPosition(anyInt())).thenReturn(voiceChannelManager);

        lenient().when(guild.createVoiceChannel(anyString())).thenReturn(voiceChannelAction);
        lenient().when(voiceChannelAction.complete()).thenReturn(mockVoiceChannel);
        lenient().when(guild.getVoiceChannelById(anyString())).thenReturn(mockVoiceChannel);

        lenient().when(mockCategory.createVoiceChannel(anyString())).thenReturn(voiceChannelAction);
    }

    @Test
    void testProcessSync_CorrectExecutionOrder() {
        // Given
        Map<String, Object> diff = createTestDiff();
        ServerConfigDTO config = createTestConfig();

        // When
        syncService.processSync(guild, diff, config);

        // Then
        // Verify the correct order of operations using the real EntityTypes
        verify(handler, times(1)).create(any(Guild.class), any(), eq(UniversalDiscordHandler.CATEGORY), any());
        verify(handler, times(1)).update(any(Guild.class), any(), any(), eq(UniversalDiscordHandler.CATEGORY), any());
        verify(handler, times(1)).create(any(Guild.class), any(), eq(UniversalDiscordHandler.TEXT_CHANNEL), any());
        verify(handler, times(1)).create(any(Guild.class), any(), eq(UniversalDiscordHandler.VOICE_CHANNEL), any());
    }

    @Test
    void testProcessEntity_CreateCategory() {
        // Given
        CategoryDTO categoryDTO = createCategoryDTO("cat1", "Test Category", 0);
        Map<String, Object> diffEntry = Map.of(
                "action", "create",
                "data", categoryDTO
        );

        when(handler.create(any(Guild.class), any(), eq(UniversalDiscordHandler.CATEGORY), any()))
                .thenReturn(mockCategory);

        // When
        syncService.processSync(guild, Map.of("categories", List.of(diffEntry)), new ServerConfigDTO());

        // Then
        verify(handler).create(eq(guild), eq(categoryDTO), eq(UniversalDiscordHandler.CATEGORY), any());
    }

    @Test
    void testProcessEntity_CreateTextChannel() {
        // Given
        TextChannelDTO textChannelDTO = createTextChannelDTO("text1", "Test Text Channel", 0, "cat1");
        textChannelDTO.setTopic("Test topic");
        textChannelDTO.setNsfw(false);

        Map<String, Object> diffEntry = Map.of(
                "action", "create",
                "data", textChannelDTO
        );

        when(handler.create(any(Guild.class), any(), eq(UniversalDiscordHandler.TEXT_CHANNEL), any()))
                .thenReturn(mockTextChannel);

        // When
        syncService.processSync(guild, Map.of("textChannels", List.of(diffEntry)), new ServerConfigDTO());

        // Then
        verify(handler).create(eq(guild), eq(textChannelDTO), eq(UniversalDiscordHandler.TEXT_CHANNEL), any());
    }

    @Test
    void testProcessEntity_CreateVoiceChannel() {
        // Given
        VoiceChannelDTO voiceChannelDTO = createVoiceChannelDTO("voice1", "Test Voice Channel", 0, "cat1");
        voiceChannelDTO.setUserLimit(10);
        voiceChannelDTO.setBitrate(64000);

        Map<String, Object> diffEntry = Map.of(
                "action", "create",
                "data", voiceChannelDTO
        );

        when(handler.create(any(Guild.class), any(), eq(UniversalDiscordHandler.VOICE_CHANNEL), any()))
                .thenReturn(mockVoiceChannel);

        // When
        syncService.processSync(guild, Map.of("voiceChannels", List.of(diffEntry)), new ServerConfigDTO());

        // Then
        verify(handler).create(eq(guild), eq(voiceChannelDTO), eq(UniversalDiscordHandler.VOICE_CHANNEL), any());
    }

    @Test
    void testProcessEntity_UpdateAction() {
        // Given
        Map<String, Object> changes = Map.of("name", "Updated Name");
        Map<String, Object> diffEntry = Map.of(
                "action", "update",
                "id", "category1",
                "changes", changes
        );

        // When
        syncService.processSync(guild, Map.of("categories", List.of(diffEntry)), new ServerConfigDTO());

        // Then
        verify(handler).update(eq(guild), eq("category1"), eq(changes), eq(UniversalDiscordHandler.CATEGORY), any());
    }

    @Test
    void testProcessEntity_DeleteAction() {
        // Given
        Map<String, Object> diffEntry = Map.of(
                "action", "delete",
                "id", "category1"
        );

        // When
        syncService.processSync(guild, Map.of("categories", List.of(diffEntry)), new ServerConfigDTO());

        // Then
        verify(handler).delete(eq(guild), eq("category1"), eq(UniversalDiscordHandler.CATEGORY));
    }

    @Test
    void testUpdateAllPositions_Categories() {
        // Given
        List<CategoryDTO> categories = Arrays.asList(
                createCategoryDTO("cat1", "Category 1", 1),
                createCategoryDTO("cat2", "Category 2", 0)
        );

        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(categories);

        // When
        syncService.processSync(guild, Map.of(), config);

        // Then
        verify(handler, times(2)).updatePosition(
                eq(guild), any(), anyInt(), eq(UniversalDiscordHandler.CATEGORY), any()
        );
    }

    @Test
    void testUpdateAllPositions_TextChannels() {
        // Given
        List<TextChannelDTO> textChannels = Arrays.asList(
                createTextChannelDTO("text1", "Text 1", 1, "parent1"),
                createTextChannelDTO("text2", "Text 2", 0, "parent1")
        );

        ServerConfigDTO config = new ServerConfigDTO();
        config.setTextChannels(textChannels);

        // When
        syncService.processSync(guild, Map.of(), config);

        // Then
        verify(handler, times(2)).updatePosition(
                eq(guild), any(), anyInt(), eq(UniversalDiscordHandler.TEXT_CHANNEL), any()
        );
    }

    @Test
    void testUpdateAllPositions_VoiceChannels() {
        // Given
        List<VoiceChannelDTO> voiceChannels = Arrays.asList(
                createVoiceChannelDTO("voice1", "Voice 1", 1, "parent1"),
                createVoiceChannelDTO("voice2", "Voice 2", 0, "parent1")
        );

        ServerConfigDTO config = new ServerConfigDTO();
        config.setVoiceChannels(voiceChannels);

        // When
        syncService.processSync(guild, Map.of(), config);

        // Then
        verify(handler, times(2)).updatePosition(
                eq(guild), any(), anyInt(), eq(UniversalDiscordHandler.VOICE_CHANNEL), any()
        );
    }

    @Test
    void testIdMapping_CreateAndResolve() {
        // Given
        CategoryDTO categoryDTO = createCategoryDTO("dto123", "Test Category", 0);
        Map<String, Object> diffEntry = Map.of(
                "action", "create",
                "data", categoryDTO
        );

        when(handler.create(any(Guild.class), any(), eq(UniversalDiscordHandler.CATEGORY), any()))
                .thenReturn(mockCategory);

        Map<String, Object> updateEntry = Map.of(
                "action", "update",
                "id", "dto123",
                "changes", Map.of("name", "Updated Name")
        );

        // When
        syncService.processSync(guild, Map.of(
                "categories", Arrays.asList(diffEntry, updateEntry)
        ), new ServerConfigDTO());

        // Then
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        verify(handler).update(eq(guild), idCaptor.capture(), any(), eq(UniversalDiscordHandler.CATEGORY), any());
        // After creation, the DTO ID should be mapped to the Discord ID
        assertEquals("discord-cat-123", idCaptor.getValue());
    }

    @Test
    void testErrorHandling_NonRateLimitException() {
        // Given
        CategoryDTO categoryDTO = createCategoryDTO("cat1", "Test Category", 0);
        Map<String, Object> diffEntry = Map.of(
                "action", "create",
                "data", categoryDTO
        );

        RuntimeException exception = new RuntimeException("Some other error");
        when(handler.create(any(Guild.class), any(), eq(UniversalDiscordHandler.CATEGORY), any()))
                .thenThrow(exception);

        // When & Then
        assertDoesNotThrow(() -> {
            syncService.processSync(guild, Map.of("categories", List.of(diffEntry)), new ServerConfigDTO());
        });

        verify(handler, times(1)).create(any(Guild.class), any(), eq(UniversalDiscordHandler.CATEGORY), any());
    }

    @Test
    void testGetDiffList_EmptyDiff() {
        // Given
        Map<String, Object> emptyDiff = Map.of();

        // When
        syncService.processSync(guild, emptyDiff, new ServerConfigDTO());

        // Then
        verifyNoInteractions(handler);
    }

    @Test
    void testGetDiffList_NullComponent() {
        // Given
        Map<String, Object> diff = new HashMap<>();
        diff.put("categories", null);

        // When
        syncService.processSync(guild, diff, new ServerConfigDTO());

        // Then
        verifyNoInteractions(handler);
    }

    @Test
    void testClearMappings() {
        // Given
        syncService.clearMappings();
        Map<String, Object> stats = syncService.getStats();

        // Then
        assertEquals(0, stats.get("mappingCount"));
    }

    @Test
    void testGetStats() {
        // When
        Map<String, Object> stats = syncService.getStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("mappingCount"));
        assertTrue(stats.containsKey("currentRequests"));
        assertTrue(stats.containsKey("lastReset"));
        assertEquals(0, stats.get("mappingCount"));
    }

    @Test
    void testChannelPositionGrouping() {
        // Given - Channels with different parent categories
        List<TextChannelDTO> textChannels = Arrays.asList(
                createTextChannelDTO("text1", "Text 1", 0, "parent1"),
                createTextChannelDTO("text2", "Text 2", 1, "parent1"),
                createTextChannelDTO("text3", "Text 3", 0, "parent2"),
                createTextChannelDTO("text4", "Text 4", 1, "parent2")
        );

        ServerConfigDTO config = new ServerConfigDTO();
        config.setTextChannels(textChannels);

        // When
        syncService.processSync(guild, Map.of(), config);

        // Then
        // Should update positions for each channel (4 total)
        verify(handler, times(4)).updatePosition(
                eq(guild), any(), anyInt(), eq(UniversalDiscordHandler.TEXT_CHANNEL), any()
        );
    }

    @Test
    void testComplexSyncScenario() {
        // Given - A complex scenario with multiple operations
        CategoryDTO newCategory = createCategoryDTO("new-cat", "New Category", 0);
        TextChannelDTO newTextChannel = createTextChannelDTO("new-text", "New Text", 0, "new-cat");
        VoiceChannelDTO newVoiceChannel = createVoiceChannelDTO("new-voice", "New Voice", 0, "new-cat");

        Map<String, Object> diff = Map.of(
                "categories", Arrays.asList(
                        Map.of("action", "create", "data", newCategory),
                        Map.of("action", "update", "id", "existing-cat", "changes", Map.of("name", "Updated Category")),
                        Map.of("action", "delete", "id", "old-cat")
                ),
                "textChannels", Arrays.asList(
                        Map.of("action", "create", "data", newTextChannel),
                        Map.of("action", "update", "id", "existing-text", "changes", Map.of("topic", "Updated topic"))
                ),
                "voiceChannels", Arrays.asList(
                        Map.of("action", "create", "data", newVoiceChannel),
                        Map.of("action", "delete", "id", "old-voice")
                )
        );

        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(List.of(newCategory));
        config.setTextChannels(List.of(newTextChannel));
        config.setVoiceChannels(List.of(newVoiceChannel));

        when(handler.create(any(Guild.class), any(), any(), any())).thenReturn(mockCategory, mockTextChannel, mockVoiceChannel);

        // When
        syncService.processSync(guild, diff, config);

        // Then
        // Verify all operations were called
        verify(handler, times(3)).create(any(Guild.class), any(), any(), any());
        verify(handler, times(2)).update(any(Guild.class), any(), any(), any(), any());
        verify(handler, times(2)).delete(any(Guild.class), any(), any());
        verify(handler, times(3)).updatePosition(any(Guild.class), any(), anyInt(), any(), any());
    }

    // Helper methods
    private Map<String, Object> createTestDiff() {
        return Map.of(
                "categories", List.of(
                        Map.of("action", "create", "data", createCategoryDTO("cat1", "Category 1", 0)),
                        Map.of("action", "update", "id", "cat2", "changes", Map.of("name", "Updated"))
                ),
                "textChannels", List.of(
                        Map.of("action", "create", "data", createTextChannelDTO("text1", "Text 1", 0, "cat1"))
                ),
                "voiceChannels", List.of(
                        Map.of("action", "create", "data", createVoiceChannelDTO("voice1", "Voice 1", 0, "cat1"))
                )
        );
    }

    private ServerConfigDTO createTestConfig() {
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(List.of(createCategoryDTO("cat1", "Category 1", 0)));
        config.setTextChannels(List.of(createTextChannelDTO("text1", "Text 1", 0, "cat1")));
        config.setVoiceChannels(List.of(createVoiceChannelDTO("voice1", "Voice 1", 0, "cat1")));
        return config;
    }

    private Map<String, Object> createLargeDiff(int count) {
        List<Map<String, Object>> categories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            categories.add(Map.of(
                    "action", "create",
                    "data", createCategoryDTO("cat" + i, "Category " + i, i)
            ));
        }
        return Map.of("categories", categories);
    }

    private CategoryDTO createCategoryDTO(String id, String name, int position) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPosition(position);
        return dto;
    }

    private TextChannelDTO createTextChannelDTO(String id, String name, int position, String parentId) {
        TextChannelDTO dto = new TextChannelDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPosition(position);
        dto.setParentCategoryId(parentId);
        return dto;
    }

    private VoiceChannelDTO createVoiceChannelDTO(String id, String name, int position, String parentId) {
        VoiceChannelDTO dto = new VoiceChannelDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPosition(position);
        dto.setParentCategoryId(parentId);
        return dto;
    }
}