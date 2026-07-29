package ch.zhaw.it.pm4.discordmanagerbe.bots.guildconfig.handler;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniversalDiscordHandlerTest {

    @InjectMocks
    private UniversalDiscordHandler handler;

    @Mock
    private Guild guild;

    @Mock
    private Category category;

    @Mock
    private Category parentCategory;

    @Mock
    private TextChannel textChannel;

    @Mock
    private VoiceChannel voiceChannel;

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

    private Function<String, String> idResolver;

    @BeforeEach
    void setUp() {
        idResolver = id -> "resolved_" + id;
    }

    // ===== CATEGORY TESTS =====

    @Test
    void createCategory_Success() {
        // Arrange
        CategoryDto dto = new CategoryDto("Test Category");
        when(guild.createCategory("Test Category")).thenReturn(categoryChannelAction);
        when(categoryChannelAction.complete()).thenReturn(category);

        // Act
        Category result = handler.create(guild, dto, UniversalDiscordHandler.CATEGORY, idResolver);

        // Assert
        assertNotNull(result);
        assertEquals(category, result);
        verify(guild).createCategory("Test Category");
        verify(categoryChannelAction).complete();
    }

    @Test
    void createCategory_NullName_ThrowsException() {
        // Arrange
        CategoryDto dto = new CategoryDto(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                handler.create(guild, dto, UniversalDiscordHandler.CATEGORY, idResolver));
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        String categoryId = "123";
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "Updated Category Name");

        when(guild.getCategoryById(categoryId)).thenReturn(category);
        when(category.getManager()).thenReturn(categoryManager);
        when(categoryManager.setName("Updated Category Name")).thenReturn(categoryManager);
        doNothing().when(categoryManager).complete();

        // Act
        handler.update(guild, categoryId, changes, UniversalDiscordHandler.CATEGORY, idResolver);

        // Assert
        verify(guild).getCategoryById(categoryId);
        verify(category).getManager();
        verify(categoryManager).setName("Updated Category Name");
        verify(categoryManager).complete();
    }

    @Test
    void updateCategory_WithDesiredValue() {
        // Arrange
        String categoryId = "123";
        Map<String, Object> changes = new HashMap<>();
        Map<String, Object> changeValue = new HashMap<>();
        changeValue.put("desired", "New Category Name");
        changes.put("name", changeValue);

        when(guild.getCategoryById(categoryId)).thenReturn(category);
        when(category.getManager()).thenReturn(categoryManager);
        when(categoryManager.setName("New Category Name")).thenReturn(categoryManager);
        doNothing().when(categoryManager).complete();

        // Act
        handler.update(guild, categoryId, changes, UniversalDiscordHandler.CATEGORY, idResolver);

        // Assert
        verify(categoryManager).setName("New Category Name");
    }

    @Test
    void updateCategory_NotFound() {
        // Arrange
        String categoryId = "123";
        Map<String, Object> changes = new HashMap<>();
        when(guild.getCategoryById(categoryId)).thenReturn(null);

        // Act
        handler.update(guild, categoryId, changes, UniversalDiscordHandler.CATEGORY, idResolver);

        // Assert
        verify(guild).getCategoryById(categoryId);
        verifyNoInteractions(categoryManager);
    }

    @Test
    void updateCategoryPosition_Success() {
        // Arrange
        String categoryId = "123";
        int position = 5;

        when(guild.getCategoryById("resolved_123")).thenReturn(category);
        when(category.getManager()).thenReturn(categoryManager);
        when(categoryManager.setPosition(position)).thenReturn(categoryManager);
        doNothing().when(categoryManager).complete();

        // Act
        handler.updatePosition(guild, categoryId, position, UniversalDiscordHandler.CATEGORY, idResolver);

        // Assert
        verify(guild).getCategoryById("resolved_123");
        verify(categoryManager).setPosition(position);
    }

    // ===== TEXT CHANNEL TESTS =====

    @Test
    void createTextChannel_WithoutParent() {
        // Arrange
        TextChannelDto dto = new TextChannelDto("test-channel", null, "Test topic", false);
        when(guild.createTextChannel("test-channel")).thenReturn(textChannelAction);
        when(textChannelAction.complete()).thenReturn(textChannel);
        when(textChannel.getManager()).thenReturn(textChannelManager);
        when(textChannelManager.setTopic("Test topic")).thenReturn(textChannelManager);
        when(textChannel.isNSFW()).thenReturn(true);
        when(textChannelManager.setNSFW(false)).thenReturn(textChannelManager);
        doNothing().when(textChannelManager).complete();

        // Act
        TextChannel result = handler.create(guild, dto, UniversalDiscordHandler.TEXT_CHANNEL, idResolver);

        // Assert
        assertEquals(textChannel, result);
        verify(guild).createTextChannel("test-channel");
        verify(textChannelManager).setTopic("Test topic");
        verify(textChannelManager).setNSFW(false);
    }

    @Test
    void createTextChannel_WithParent() {
        // Arrange
        TextChannelDto dto = new TextChannelDto("test-channel", "parent123", "Test topic", false);
        when(guild.getCategoryById("resolved_parent123")).thenReturn(parentCategory);
        when(parentCategory.createTextChannel("test-channel")).thenReturn(textChannelAction);
        when(textChannelAction.complete()).thenReturn(textChannel);
        when(textChannel.getManager()).thenReturn(textChannelManager);
        when(textChannelManager.setTopic("Test topic")).thenReturn(textChannelManager);
        when(textChannel.isNSFW()).thenReturn(false);
        doNothing().when(textChannelManager).complete();

        // Act
        TextChannel result = handler.create(guild, dto, UniversalDiscordHandler.TEXT_CHANNEL, idResolver);

        // Assert
        assertEquals(textChannel, result);
        verify(parentCategory).createTextChannel("test-channel");
        verify(textChannelManager, never()).setNSFW(anyBoolean());
    }

    @Test
    void updateTextChannel_AllProperties() {
        // Arrange
        String channelId = "456";
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "updated-channel");
        changes.put("topic", "Updated topic");
        changes.put("nsfw", true);
        changes.put("parentCategoryId", "newParent");

        when(guild.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.getManager()).thenReturn(textChannelManager);
        when(textChannel.getGuild()).thenReturn(guild);
        when(guild.getCategoryById("resolved_newParent")).thenReturn(parentCategory);
        when(textChannelManager.setName("updated-channel")).thenReturn(textChannelManager);
        when(textChannelManager.setTopic("Updated topic")).thenReturn(textChannelManager);
        when(textChannelManager.setNSFW(true)).thenReturn(textChannelManager);
        when(textChannelManager.setParent(parentCategory)).thenReturn(textChannelManager);
        doNothing().when(textChannelManager).complete();

        // Act
        handler.update(guild, channelId, changes, UniversalDiscordHandler.TEXT_CHANNEL, idResolver);

        // Assert
        verify(textChannelManager).setName("updated-channel");
        verify(textChannelManager).setTopic("Updated topic");
        verify(textChannelManager).setNSFW(true);
        verify(textChannelManager).setParent(parentCategory);
        verify(textChannelManager).complete();
    }

    @Test
    void updateTextChannel_RemoveParent() {
        // Arrange
        String channelId = "456";
        Map<String, Object> changes = new HashMap<>();
        changes.put("parentCategoryId", "");

        when(guild.getTextChannelById(channelId)).thenReturn(textChannel);
        when(textChannel.getManager()).thenReturn(textChannelManager);
        when(textChannelManager.setParent(null)).thenReturn(textChannelManager);
        doNothing().when(textChannelManager).complete();

        // Act
        handler.update(guild, channelId, changes, UniversalDiscordHandler.TEXT_CHANNEL, idResolver);

        // Assert
        verify(textChannelManager).setParent(null);
    }

    // ===== VOICE CHANNEL TESTS =====

    @Test
    void createVoiceChannel_WithProperties() {
        // Arrange
        VoiceChannelDto dto = new VoiceChannelDto("Voice Channel", "parent123", 10, 64000);
        when(guild.getCategoryById("resolved_parent123")).thenReturn(parentCategory);
        when(parentCategory.createVoiceChannel("Voice Channel")).thenReturn(voiceChannelAction);
        when(voiceChannelAction.complete()).thenReturn(voiceChannel);
        when(voiceChannel.getManager()).thenReturn(voiceChannelManager);
        when(voiceChannel.getUserLimit()).thenReturn(0);
        when(voiceChannel.getBitrate()).thenReturn(48000);
        when(voiceChannelManager.setUserLimit(10)).thenReturn(voiceChannelManager);
        when(voiceChannelManager.setBitrate(64000)).thenReturn(voiceChannelManager);
        doNothing().when(voiceChannelManager).complete();

        // Act
        VoiceChannel result = handler.create(guild, dto, UniversalDiscordHandler.VOICE_CHANNEL, idResolver);

        // Assert
        assertEquals(voiceChannel, result);
        verify(voiceChannelManager).setUserLimit(10);
        verify(voiceChannelManager).setBitrate(64000);
    }

    @Test
    void createVoiceChannel_NoPropertyChangesNeeded() {
        // Arrange
        VoiceChannelDto dto = new VoiceChannelDto("Voice Channel", null, 10, 64000);
        when(guild.createVoiceChannel("Voice Channel")).thenReturn(voiceChannelAction);
        when(voiceChannelAction.complete()).thenReturn(voiceChannel);
        when(voiceChannel.getManager()).thenReturn(voiceChannelManager);
        when(voiceChannel.getUserLimit()).thenReturn(10);
        when(voiceChannel.getBitrate()).thenReturn(64000);
        doNothing().when(voiceChannelManager).complete();

        // Act
        VoiceChannel result = handler.create(guild, dto, UniversalDiscordHandler.VOICE_CHANNEL, idResolver);

        // Assert
        assertEquals(voiceChannel, result);
        verify(voiceChannelManager, never()).setUserLimit(anyInt());
        verify(voiceChannelManager, never()).setBitrate(anyInt());
    }

    @Test
    void updateVoiceChannel_AllProperties() {
        // Arrange
        String channelId = "789";
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "Updated Voice");
        changes.put("userLimit", 20);
        changes.put("bitrate", 96000);
        changes.put("parentCategoryId", "newParent");

        when(guild.getVoiceChannelById(channelId)).thenReturn(voiceChannel);
        when(voiceChannel.getManager()).thenReturn(voiceChannelManager);
        when(voiceChannel.getGuild()).thenReturn(guild);
        when(guild.getCategoryById("resolved_newParent")).thenReturn(parentCategory);
        when(voiceChannelManager.setName("Updated Voice")).thenReturn(voiceChannelManager);
        when(voiceChannelManager.setUserLimit(20)).thenReturn(voiceChannelManager);
        when(voiceChannelManager.setBitrate(96000)).thenReturn(voiceChannelManager);
        when(voiceChannelManager.setParent(parentCategory)).thenReturn(voiceChannelManager);
        doNothing().when(voiceChannelManager).complete();

        // Act
        handler.update(guild, channelId, changes, UniversalDiscordHandler.VOICE_CHANNEL, idResolver);

        // Assert
        verify(voiceChannelManager).setName("Updated Voice");
        verify(voiceChannelManager).setUserLimit(20);
        verify(voiceChannelManager).setBitrate(96000);
        verify(voiceChannelManager).setParent(parentCategory);
        verify(voiceChannelManager).complete();
    }

    // ===== ERROR HANDLING TESTS =====

    @Test
    void create_ThrowsException_WrapsInRuntimeException() {
        // Arrange
        CategoryDto dto = new CategoryDto("Test");
        when(guild.createCategory("Test")).thenThrow(new RuntimeException("Discord API error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                handler.create(guild, dto, UniversalDiscordHandler.CATEGORY, idResolver));

        assertTrue(exception.getMessage().contains("Failed to create Category"));
        assertEquals("Discord API error", exception.getCause().getMessage());
    }

    @Test
    void update_InvalidProperty_ContinuesWithOtherProperties() {
        // Arrange
        String categoryId = "123";
        Map<String, Object> changes = new HashMap<>();
        changes.put("invalidProperty", "value");
        changes.put("name", "Valid Name");

        when(guild.getCategoryById(categoryId)).thenReturn(category);
        when(category.getManager()).thenReturn(categoryManager);
        when(categoryManager.setName("Valid Name")).thenReturn(categoryManager);
        doNothing().when(categoryManager).complete();

        // Act - should not throw exception
        handler.update(guild, categoryId, changes, UniversalDiscordHandler.CATEGORY, idResolver);

        // Assert
        verify(categoryManager).setName("Valid Name");
        verify(categoryManager).complete();
    }

    // ===== ENTITY TYPE TESTS =====

    @Test
    void entityType_HasCorrectConfiguration() {
        // Test Category entity type
        assertEquals("Category", UniversalDiscordHandler.CATEGORY.name());
        assertNotNull(UniversalDiscordHandler.CATEGORY.creator());
        assertNotNull(UniversalDiscordHandler.CATEGORY.finder());
        assertNotNull(UniversalDiscordHandler.CATEGORY.updater());
        assertNotNull(UniversalDiscordHandler.CATEGORY.deleter());
        assertNotNull(UniversalDiscordHandler.CATEGORY.positionUpdater());

        // Test TextChannel entity type
        assertEquals("TextChannel", UniversalDiscordHandler.TEXT_CHANNEL.name());
        assertNotNull(UniversalDiscordHandler.TEXT_CHANNEL.creator());
        assertNotNull(UniversalDiscordHandler.TEXT_CHANNEL.finder());
        assertNotNull(UniversalDiscordHandler.TEXT_CHANNEL.updater());
        assertNotNull(UniversalDiscordHandler.TEXT_CHANNEL.deleter());
        assertNotNull(UniversalDiscordHandler.TEXT_CHANNEL.positionUpdater());

        // Test VoiceChannel entity type
        assertEquals("VoiceChannel", UniversalDiscordHandler.VOICE_CHANNEL.name());
        assertNotNull(UniversalDiscordHandler.VOICE_CHANNEL.creator());
        assertNotNull(UniversalDiscordHandler.VOICE_CHANNEL.finder());
        assertNotNull(UniversalDiscordHandler.VOICE_CHANNEL.updater());
        assertNotNull(UniversalDiscordHandler.VOICE_CHANNEL.deleter());
        assertNotNull(UniversalDiscordHandler.VOICE_CHANNEL.positionUpdater());
    }

    // ===== TEST DTOs =====

    private static class CategoryDto {
        private final String name;

        public CategoryDto(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static class TextChannelDto {
        private final String name;
        private final String parentCategoryId;
        private final String topic;
        private final Boolean nsfw;

        public TextChannelDto(String name, String parentCategoryId, String topic, Boolean nsfw) {
            this.name = name;
            this.parentCategoryId = parentCategoryId;
            this.topic = topic;
            this.nsfw = nsfw;
        }

        public String getName() {
            return name;
        }

        public String getParentCategoryId() {
            return parentCategoryId;
        }

        public String getTopic() {
            return topic;
        }

        public Boolean isNsfw() {
            return nsfw;
        }
    }

    private static class VoiceChannelDto {
        private final String name;
        private final String parentCategoryId;
        private final Integer userLimit;
        private final Integer bitrate;

        public VoiceChannelDto(String name, String parentCategoryId, Integer userLimit, Integer bitrate) {
            this.name = name;
            this.parentCategoryId = parentCategoryId;
            this.userLimit = userLimit;
            this.bitrate = bitrate;
        }

        public String getName() {
            return name;
        }

        public String getParentCategoryId() {
            return parentCategoryId;
        }

        public Integer getUserLimit() {
            return userLimit;
        }

        public Integer getBitrate() {
            return bitrate;
        }
    }
}