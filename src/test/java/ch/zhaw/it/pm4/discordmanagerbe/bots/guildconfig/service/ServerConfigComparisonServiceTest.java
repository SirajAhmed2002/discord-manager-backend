package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.service;

import ch.zhaw.it.pm4.discordmanagerbe.dto.CategoryDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.TextChannelDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.VoiceChannelDTO;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerConfigComparisonServiceTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private Guild guild;

    @Mock
    private Category existingCategory;

    @Mock
    private TextChannel existingTextChannel;

    @Mock
    private VoiceChannel existingVoiceChannel;

    private ServerConfigComparisonService service;
    private final String TEST_GUILD_ID = "123456789";

    @BeforeEach
    void setUp() {
        service = new ServerConfigComparisonService(jdaBean);
    }

    @Test
    void compareWithDiscordServer_GuildNotFound_ThrowsException() {
        // Given
        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(null);
        ServerConfigDTO config = new ServerConfigDTO();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.compareWithDiscordServer(TEST_GUILD_ID, config));

        assertEquals("Guild not found: " + TEST_GUILD_ID, exception.getMessage());
    }

    @Test
    void compareWithDiscordServer_EmptyServerAndConfig_ReturnsEmptyDiff() {
        // Given
        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(Collections.emptyList());
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(Collections.emptyList());
        config.setTextChannels(Collections.emptyList());
        config.setVoiceChannels(Collections.emptyList());

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void compareWithDiscordServer_CreateNewCategory_ReturnsCreateDiff() {
        // Given
        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(Collections.emptyList());
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        CategoryDTO newCategory = createCategoryDTO("new-category", "New Category", 0);
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(List.of(newCategory));
        config.setTextChannels(Collections.emptyList());
        config.setVoiceChannels(Collections.emptyList());

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertNotNull(result.get("categories"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryDiffs = (List<Map<String, Object>>) result.get("categories");
        assertEquals(1, categoryDiffs.size());

        Map<String, Object> diff = categoryDiffs.getFirst();
        assertEquals("create", diff.get("action"));
        assertEquals(newCategory, diff.get("data"));
    }

    @Test
    void compareWithDiscordServer_UpdateExistingCategory_ReturnsUpdateDiff() {
        // Given
        String categoryId = "123456";
        setupMockCategory(categoryId, "Old Name", 0);

        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(List.of(existingCategory));
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        CategoryDTO updatedCategory = createCategoryDTO(categoryId, "New Name", 1);
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(List.of(updatedCategory));
        config.setTextChannels(Collections.emptyList());
        config.setVoiceChannels(Collections.emptyList());

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertNotNull(result.get("categories"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryDiffs = (List<Map<String, Object>>) result.get("categories");
        assertEquals(1, categoryDiffs.size());

        Map<String, Object> diff = categoryDiffs.getFirst();
        assertEquals("update", diff.get("action"));
        assertEquals(categoryId, diff.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> changes = (Map<String, Object>) diff.get("changes");
        assertTrue(changes.containsKey("name"));
        assertTrue(changes.containsKey("position"));
    }

    @Test
    void compareWithDiscordServer_DeleteExistingCategory_ReturnsDeleteDiff() {
        // Given
        String categoryId = "123456";
        setupMockCategory(categoryId, "Category Name", 0);

        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(List.of(existingCategory));
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(Collections.emptyList());
        config.setTextChannels(Collections.emptyList());
        config.setVoiceChannels(Collections.emptyList());

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertNotNull(result.get("categories"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryDiffs = (List<Map<String, Object>>) result.get("categories");
        assertEquals(1, categoryDiffs.size());

        Map<String, Object> diff = categoryDiffs.getFirst();
        assertEquals("delete", diff.get("action"));
        assertEquals(categoryId, diff.get("id"));
    }

    @Test
    void compareWithDiscordServer_CreateNewTextChannel_ReturnsCreateDiff() {
        // Given
        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(Collections.emptyList());
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        TextChannelDTO newTextChannel = createTextChannelDTO("new-channel", "New Channel", "123", "Topic", false, 0);
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(Collections.emptyList());
        config.setTextChannels(List.of(newTextChannel));
        config.setVoiceChannels(Collections.emptyList());

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertNotNull(result.get("textChannels"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> textChannelDiffs = (List<Map<String, Object>>) result.get("textChannels");
        assertEquals(1, textChannelDiffs.size());

        Map<String, Object> diff = textChannelDiffs.getFirst();
        assertEquals("create", diff.get("action"));
        assertEquals(newTextChannel, diff.get("data"));
    }

    @Test
    void compareWithDiscordServer_UpdateExistingTextChannel_ReturnsUpdateDiff() {
        // Given
        String channelId = "654321";
        String categoryId = "123456";
        setupMockTextChannel(channelId, "Old Channel", categoryId, "Old Topic", false, 0);

        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(Collections.emptyList());
        when(guild.getTextChannels()).thenReturn(List.of(existingTextChannel));
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        TextChannelDTO updatedTextChannel = createTextChannelDTO(channelId, "New Channel", categoryId, "New Topic", true, 1);
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(Collections.emptyList());
        config.setTextChannels(List.of(updatedTextChannel));
        config.setVoiceChannels(Collections.emptyList());

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertNotNull(result.get("textChannels"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> textChannelDiffs = (List<Map<String, Object>>) result.get("textChannels");
        assertEquals(1, textChannelDiffs.size());

        Map<String, Object> diff = textChannelDiffs.getFirst();
        assertEquals("update", diff.get("action"));
        assertEquals(channelId, diff.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> changes = (Map<String, Object>) diff.get("changes");
        assertTrue(changes.containsKey("name"));
        assertTrue(changes.containsKey("topic"));
        assertTrue(changes.containsKey("nsfw"));
        assertTrue(changes.containsKey("position"));
    }

    @Test
    void compareWithDiscordServer_CreateNewVoiceChannel_ReturnsCreateDiff() {
        // Given
        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(Collections.emptyList());
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        VoiceChannelDTO newVoiceChannel = createVoiceChannelDTO("new-voice", "New Voice", "123", 10, 64, 0);
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(Collections.emptyList());
        config.setTextChannels(Collections.emptyList());
        config.setVoiceChannels(List.of(newVoiceChannel));

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertNotNull(result.get("voiceChannels"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> voiceChannelDiffs = (List<Map<String, Object>>) result.get("voiceChannels");
        assertEquals(1, voiceChannelDiffs.size());

        Map<String, Object> diff = voiceChannelDiffs.getFirst();
        assertEquals("create", diff.get("action"));
        assertEquals(newVoiceChannel, diff.get("data"));
    }

    @Test
    void compareWithDiscordServer_UpdateExistingVoiceChannel_ReturnsUpdateDiff() {
        // Given
        String channelId = "789012";
        String categoryId = "123456";
        setupMockVoiceChannel(channelId, "Old Voice", categoryId, 5, 96000, 0);

        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(Collections.emptyList());
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(List.of(existingVoiceChannel));

        VoiceChannelDTO updatedVoiceChannel = createVoiceChannelDTO(channelId, "New Voice", categoryId, 10, 128, 1);
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(Collections.emptyList());
        config.setTextChannels(Collections.emptyList());
        config.setVoiceChannels(List.of(updatedVoiceChannel));

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertNotNull(result.get("voiceChannels"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> voiceChannelDiffs = (List<Map<String, Object>>) result.get("voiceChannels");
        assertEquals(1, voiceChannelDiffs.size());

        Map<String, Object> diff = voiceChannelDiffs.getFirst();
        assertEquals("update", diff.get("action"));
        assertEquals(channelId, diff.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> changes = (Map<String, Object>) diff.get("changes");
        assertTrue(changes.containsKey("name"));
        assertTrue(changes.containsKey("userLimit"));
        assertTrue(changes.containsKey("bitrate"));
        assertTrue(changes.containsKey("position"));
    }

    @Test
    void compareWithDiscordServer_NoChangesNeeded_ReturnsNull() {
        // Given
        String categoryId = "123456";
        setupMockCategory(categoryId, "Same Name", 0);

        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(List.of(existingCategory));
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        CategoryDTO sameCategory = createCategoryDTO(categoryId, "Same Name", 0);
        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(List.of(sameCategory));
        config.setTextChannels(Collections.emptyList());
        config.setVoiceChannels(Collections.emptyList());

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void compareWithDiscordServer_ComplexScenario_ReturnsCorrectDiffs() {
        // Given
        String existingCategoryId = "111111";
        String existingTextChannelId = "222222";
        String existingVoiceChannelId = "333333";

        setupMockCategory(existingCategoryId, "Existing Category", 0);
        setupMockTextChannel(existingTextChannelId, "Existing Text", existingCategoryId, "Topic", false, 0);
        setupMockVoiceChannel(existingVoiceChannelId, "Existing Voice", existingCategoryId, 10, 64000, 0);

        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(List.of(existingCategory));
        when(guild.getTextChannels()).thenReturn(List.of(existingTextChannel));
        when(guild.getVoiceChannels()).thenReturn(List.of(existingVoiceChannel));

        // Config: Update category, delete text channel, keep voice channel, create new text channel
        CategoryDTO updatedCategory = createCategoryDTO(existingCategoryId, "Updated Category", 1);
        TextChannelDTO newTextChannel = createTextChannelDTO("new-text", "New Text Channel", existingCategoryId, "New Topic", false, 1);
        VoiceChannelDTO sameVoiceChannel = createVoiceChannelDTO(existingVoiceChannelId, "Existing Voice", existingCategoryId, 10, 64, 0);

        ServerConfigDTO config = new ServerConfigDTO();
        config.setCategories(List.of(updatedCategory));
        config.setTextChannels(List.of(newTextChannel)); // No existing text channel = delete
        config.setVoiceChannels(List.of(sameVoiceChannel));

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertEquals(2, result.size()); // categories and textChannels should have diffs

        // Check category update
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryDiffs = (List<Map<String, Object>>) result.get("categories");
        assertEquals(1, categoryDiffs.size());
        assertEquals("update", categoryDiffs.getFirst().get("action"));

        // Check text channel create and delete
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> textChannelDiffs = (List<Map<String, Object>>) result.get("textChannels");
        assertEquals(2, textChannelDiffs.size());

        boolean hasCreate = textChannelDiffs.stream().anyMatch(diff -> "create".equals(diff.get("action")));
        boolean hasDelete = textChannelDiffs.stream().anyMatch(diff -> "delete".equals(diff.get("action")));
        assertTrue(hasCreate);
        assertTrue(hasDelete);

        // Voice channels should not be in diff (no changes)
        assertNull(result.get("voiceChannels"));
    }

    @Test
    void compareWithDiscordServer_NullConfigLists_HandlesGracefully() {
        // Given
        when(jdaBean.getGuildById(TEST_GUILD_ID)).thenReturn(guild);
        when(guild.getCategories()).thenReturn(Collections.emptyList());
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());
        when(guild.getVoiceChannels()).thenReturn(Collections.emptyList());

        ServerConfigDTO config = new ServerConfigDTO();
        // Leave lists as null

        // When
        Map<String, Object> result = service.compareWithDiscordServer(TEST_GUILD_ID, config);

        // Then
        assertTrue(result.isEmpty());
    }

    // Helper methods for creating DTOs
    private CategoryDTO createCategoryDTO(String id, String name, int position) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPosition(position);
        return dto;
    }

    private TextChannelDTO createTextChannelDTO(String id, String name, String parentCategoryId, String topic, boolean nsfw, int position) {
        TextChannelDTO dto = new TextChannelDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setParentCategoryId(parentCategoryId);
        dto.setTopic(topic);
        dto.setNsfw(nsfw);
        dto.setPosition(position);
        return dto;
    }

    private VoiceChannelDTO createVoiceChannelDTO(String id, String name, String parentCategoryId, int userLimit, int bitrate, int position) {
        VoiceChannelDTO dto = new VoiceChannelDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setParentCategoryId(parentCategoryId);
        dto.setUserLimit(userLimit);
        dto.setBitrate(bitrate);
        dto.setPosition(position);
        return dto;
    }

    // Helper methods for setting up mocks
    private void setupMockCategory(String id, String name, int position) {
        lenient().when(existingCategory.getId()).thenReturn(id);
        lenient().when(existingCategory.getName()).thenReturn(name);
        lenient().when(existingCategory.getPosition()).thenReturn(position);
    }

    private void setupMockTextChannel(String id, String name, String parentCategoryId, String topic, boolean nsfw, int position) {
        lenient().when(existingTextChannel.getId()).thenReturn(id);
        lenient().when(existingTextChannel.getName()).thenReturn(name);
        lenient().when(existingTextChannel.getTopic()).thenReturn(topic);
        lenient().when(existingTextChannel.isNSFW()).thenReturn(nsfw);
        lenient().when(existingTextChannel.getPosition()).thenReturn(position);

        if (parentCategoryId != null) {
            Category parentCategory = mock(Category.class);
            lenient().when(parentCategory.getId()).thenReturn(parentCategoryId);
            lenient().when(existingTextChannel.getParentCategory()).thenReturn(parentCategory);
        } else {
            lenient().when(existingTextChannel.getParentCategory()).thenReturn(null);
        }
    }

    private void setupMockVoiceChannel(String id, String name, String parentCategoryId, int userLimit, int bitrate, int position) {
        lenient().when(existingVoiceChannel.getId()).thenReturn(id);
        lenient().when(existingVoiceChannel.getName()).thenReturn(name);
        lenient().when(existingVoiceChannel.getUserLimit()).thenReturn(userLimit);
        lenient().when(existingVoiceChannel.getBitrate()).thenReturn(bitrate);
        lenient().when(existingVoiceChannel.getPosition()).thenReturn(position);

        if (parentCategoryId != null) {
            Category parentCategory = mock(Category.class);
            lenient().when(parentCategory.getId()).thenReturn(parentCategoryId);
            lenient().when(existingVoiceChannel.getParentCategory()).thenReturn(parentCategory);
        } else {
            lenient().when(existingVoiceChannel.getParentCategory()).thenReturn(null);
        }
    }
}