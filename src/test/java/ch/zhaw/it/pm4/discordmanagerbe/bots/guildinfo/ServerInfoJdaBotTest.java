package ch.zhaw.it.pm4.discordmanagerbe.bots.guildinfo;

import ch.zhaw.it.pm4.discordmanagerbe.dto.CategoryDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.TextChannelDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.VoiceChannelDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerInfoJdaBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Guild guild;

    @Mock
    private Category category1;

    @Mock
    private Category category2;

    @Mock
    private TextChannel textChannel1;

    @Mock
    private TextChannel textChannel2;

    @Mock
    private VoiceChannel voiceChannel1;

    @Mock
    private VoiceChannel voiceChannel2;

    private ServerInfoJdaBot serverInfoJdaBot;

    @BeforeEach
    void setUp() {
        serverInfoJdaBot = new ServerInfoJdaBot(jdaBean, objectMapper);
    }

    @Test
    void getServerInfo_ValidServerId_ReturnsServerConfigDTO() {
        // Arrange
        String serverId = "123456789";
        String guildName = "Test Guild";

        when(jdaBean.getGuildById(serverId)).thenReturn(guild);
        when(guild.getId()).thenReturn(serverId);
        when(guild.getName()).thenReturn(guildName);

        // Setup categories
        List<Category> categories = Arrays.asList(category1, category2);
        when(guild.getCategories()).thenReturn(categories);
        when(category1.getId()).thenReturn("cat1");
        when(category1.getName()).thenReturn("Category 1");
        when(category1.getPosition()).thenReturn(0);
        when(category2.getId()).thenReturn("cat2");
        when(category2.getName()).thenReturn("Category 2");
        when(category2.getPosition()).thenReturn(1);

        // Setup text channels
        List<TextChannel> textChannels = Arrays.asList(textChannel1, textChannel2);
        when(guild.getTextChannels()).thenReturn(textChannels);
        when(textChannel1.getId()).thenReturn("text1");
        when(textChannel1.getName()).thenReturn("general");
        when(textChannel1.getPosition()).thenReturn(0);
        when(textChannel1.getParentCategory()).thenReturn(category1);
        when(textChannel2.getId()).thenReturn("text2");
        when(textChannel2.getName()).thenReturn("random");
        when(textChannel2.getPosition()).thenReturn(1);
        when(textChannel2.getParentCategory()).thenReturn(null);

        // Setup voice channels
        List<VoiceChannel> voiceChannels = Arrays.asList(voiceChannel1, voiceChannel2);
        when(guild.getVoiceChannels()).thenReturn(voiceChannels);
        when(voiceChannel1.getId()).thenReturn("voice1");
        when(voiceChannel1.getName()).thenReturn("General Voice");
        when(voiceChannel1.getPosition()).thenReturn(0);
        when(voiceChannel1.getUserLimit()).thenReturn(10);
        when(voiceChannel1.getBitrate()).thenReturn(64000);
        when(voiceChannel1.getParentCategory()).thenReturn(category1);
        when(voiceChannel2.getId()).thenReturn("voice2");
        when(voiceChannel2.getName()).thenReturn("Music Voice");
        when(voiceChannel2.getPosition()).thenReturn(1);
        when(voiceChannel2.getUserLimit()).thenReturn(5);
        when(voiceChannel2.getBitrate()).thenReturn(96000);
        when(voiceChannel2.getParentCategory()).thenReturn(null);

        // Act
        ServerConfigDTO result = serverInfoJdaBot.getServerInfo(serverId);

        // Assert
        assertNotNull(result);
        assertEquals(serverId, result.getId());
        assertEquals(guildName, result.getName());

        // Assert categories
        assertEquals(2, result.getCategories().size());
        CategoryDTO cat1 = result.getCategories().getFirst();
        assertEquals("cat1", cat1.getId());
        assertEquals("Category 1", cat1.getName());
        assertEquals(0, cat1.getPosition());

        // Assert text channels
        assertEquals(2, result.getTextChannels().size());
        TextChannelDTO text1 = result.getTextChannels().getFirst();
        assertEquals("text1", text1.getId());
        assertEquals("general", text1.getName());
        assertEquals(0, text1.getPosition());
        assertEquals("cat1", text1.getParentCategoryId());

        TextChannelDTO text2 = result.getTextChannels().get(1);
        assertEquals("text2", text2.getId());
        assertEquals("random", text2.getName());
        assertEquals(1, text2.getPosition());
        assertNull(text2.getParentCategoryId());

        // Assert voice channels
        assertEquals(2, result.getVoiceChannels().size());
        VoiceChannelDTO voice1 = result.getVoiceChannels().getFirst();
        assertEquals("voice1", voice1.getId());
        assertEquals("General Voice", voice1.getName());
        assertEquals(0, voice1.getPosition());
        assertEquals(10, voice1.getUserLimit());
        assertEquals(64000, voice1.getBitrate());
        assertEquals("cat1", voice1.getParentCategoryId());

        VoiceChannelDTO voice2 = result.getVoiceChannels().get(1);
        assertEquals("voice2", voice2.getId());
        assertEquals("Music Voice", voice2.getName());
        assertEquals(1, voice2.getPosition());
        assertEquals(5, voice2.getUserLimit());
        assertEquals(96000, voice2.getBitrate());
        assertNull(voice2.getParentCategoryId());

        verify(jdaBean).getGuildById(serverId);
    }

    @Test
    void getServerInfo_GuildNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        String serverId = "nonexistent";
        when(jdaBean.getGuildById(serverId)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> serverInfoJdaBot.getServerInfo(serverId));

        verify(jdaBean).getGuildById(serverId);
    }

    @Test
    void getServerInfo_InvalidServerId_ThrowsIllegalArgumentException() {
        // Arrange
        String invalidServerId = "invalid";
        when(jdaBean.getGuildById(invalidServerId)).thenThrow(new NumberFormatException("Invalid ID"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serverInfoJdaBot.getServerInfo(invalidServerId)
        );

        assertInstanceOf(NumberFormatException.class, exception.getCause());
        verify(jdaBean).getGuildById(invalidServerId);
    }

    @Test
    void getServerInfoJson_ValidServerId_ReturnsJsonString() throws Exception {
        // Arrange
        String serverId = "123456789";
        String expectedJson = "{\"id\":\"123456789\",\"name\":\"Test Guild\"}";

        when(jdaBean.getGuildById(serverId)).thenReturn(guild);
        when(guild.getId()).thenReturn(serverId);
        when(guild.getName()).thenReturn("Test Guild");
        when(guild.getCategories()).thenReturn(List.of());
        when(guild.getTextChannels()).thenReturn(List.of());
        when(guild.getVoiceChannels()).thenReturn(List.of());

        when(objectMapper.writeValueAsString(any(ServerConfigDTO.class))).thenReturn(expectedJson);

        // Act
        String result = serverInfoJdaBot.getServerInfoJson(serverId);

        // Assert
        assertEquals(expectedJson, result);
        verify(objectMapper).writeValueAsString(any(ServerConfigDTO.class));
    }

    @Test
    void getServerInfo_EmptyGuild_ReturnsEmptyLists() {
        // Arrange
        String serverId = "123456789";
        String guildName = "Empty Guild";

        when(jdaBean.getGuildById(serverId)).thenReturn(guild);
        when(guild.getId()).thenReturn(serverId);
        when(guild.getName()).thenReturn(guildName);
        when(guild.getCategories()).thenReturn(List.of());
        when(guild.getTextChannels()).thenReturn(List.of());
        when(guild.getVoiceChannels()).thenReturn(List.of());

        // Act
        ServerConfigDTO result = serverInfoJdaBot.getServerInfo(serverId);

        // Assert
        assertNotNull(result);
        assertEquals(serverId, result.getId());
        assertEquals(guildName, result.getName());
        assertTrue(result.getCategories().isEmpty());
        assertTrue(result.getTextChannels().isEmpty());
        assertTrue(result.getVoiceChannels().isEmpty());
    }

    @Test
    void getServerInfo_ChannelsWithoutParentCategory_HandlesNullParent() {
        // Arrange
        String serverId = "123456789";

        when(jdaBean.getGuildById(serverId)).thenReturn(guild);
        when(guild.getId()).thenReturn(serverId);
        when(guild.getName()).thenReturn("Test Guild");
        when(guild.getCategories()).thenReturn(List.of());

        // Text channel without parent
        when(guild.getTextChannels()).thenReturn(List.of(textChannel1));
        when(textChannel1.getId()).thenReturn("text1");
        when(textChannel1.getName()).thenReturn("general");
        when(textChannel1.getPosition()).thenReturn(0);
        when(textChannel1.getParentCategory()).thenReturn(null);

        // Voice channel without parent
        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel1));
        when(voiceChannel1.getId()).thenReturn("voice1");
        when(voiceChannel1.getName()).thenReturn("General Voice");
        when(voiceChannel1.getPosition()).thenReturn(0);
        when(voiceChannel1.getUserLimit()).thenReturn(0); // No user limit
        when(voiceChannel1.getBitrate()).thenReturn(64000);
        when(voiceChannel1.getParentCategory()).thenReturn(null);

        // Act
        ServerConfigDTO result = serverInfoJdaBot.getServerInfo(serverId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTextChannels().size());
        assertNull(result.getTextChannels().getFirst().getParentCategoryId());

        assertEquals(1, result.getVoiceChannels().size());
        assertNull(result.getVoiceChannels().getFirst().getParentCategoryId());
        assertEquals(0, result.getVoiceChannels().getFirst().getUserLimit());
    }

    @Test
    void getServerInfo_VoiceChannelWithSpecialSettings_MapsCorrectly() {
        // Arrange
        String serverId = "123456789";

        when(jdaBean.getGuildById(serverId)).thenReturn(guild);
        when(guild.getId()).thenReturn(serverId);
        when(guild.getName()).thenReturn("Test Guild");
        when(guild.getCategories()).thenReturn(List.of());
        when(guild.getTextChannels()).thenReturn(List.of());

        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel1));
        when(voiceChannel1.getId()).thenReturn("voice1");
        when(voiceChannel1.getName()).thenReturn("High Quality Voice");
        when(voiceChannel1.getPosition()).thenReturn(5);
        when(voiceChannel1.getUserLimit()).thenReturn(99); // Max user limit
        when(voiceChannel1.getBitrate()).thenReturn(384000); // High bitrate
        when(voiceChannel1.getParentCategory()).thenReturn(null);

        // Act
        ServerConfigDTO result = serverInfoJdaBot.getServerInfo(serverId);

        // Assert
        VoiceChannelDTO voiceChannel = result.getVoiceChannels().getFirst();
        assertEquals("voice1", voiceChannel.getId());
        assertEquals("High Quality Voice", voiceChannel.getName());
        assertEquals(5, voiceChannel.getPosition());
        assertEquals(99, voiceChannel.getUserLimit());
        assertEquals(384000, voiceChannel.getBitrate());
        assertNull(voiceChannel.getParentCategoryId());
    }
}