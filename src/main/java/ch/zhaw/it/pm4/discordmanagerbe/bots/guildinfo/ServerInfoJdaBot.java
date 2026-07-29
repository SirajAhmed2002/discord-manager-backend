package ch.zhaw.it.pm4.discordmanagerbe.bots.guildinfo;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.dto.CategoryDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.ServerConfigDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.TextChannelDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.VoiceChannelDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Discord bot component for retrieving server information and configuration.
 * Provides functionality to extract complete server details including categories,
 * text channels, and voice channels from Discord guilds.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SERVER,
        server = ServerBotType.GUILD_INFO)
@Component
public class ServerInfoJdaBot extends AbstractJdaBot {

    /** Logger for this class */
    private static final Logger log = LoggerFactory.getLogger(ServerInfoJdaBot.class);

    /** Object mapper for JSON serialization */
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new ServerInfoJdaBot with JDA bean and object mapper.
     *
     * @param jdaBean the JDA instance
     * @param objectMapper the object mapper for JSON processing
     */
    @Autowired
    public ServerInfoJdaBot(JDA jdaBean, ObjectMapper objectMapper) {
        super(jdaBean);
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves complete server information for a Discord server.
     *
     * @param serverId Discord server ID
     * @return ServerConfigDTO containing the server configuration
     * @throws IllegalArgumentException if the server ID is invalid or the server is not found
     */
    public ServerConfigDTO getServerInfo(String serverId) {
        log.info("Retrieving server info for server ID: {}", serverId);
        
        try {
            Guild guild = jdaBean.getGuildById(serverId);
            if (guild == null) {
                throw new IllegalArgumentException("Guild not found: " + serverId);
            }
            return extractServerInfo(guild);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid server ID: " + serverId, e);
        }
    }

    /**
     * Get server info as JSON string.
     *
     * @param serverId Discord server ID
     * @return JSON string of server configuration
     * @throws JsonProcessingException if there's an error processing JSON
     */
    public String getServerInfoJson(String serverId) throws JsonProcessingException {
        return objectMapper.writeValueAsString(getServerInfo(serverId));
    }

    /**
     * Extracts server configuration from a Discord guild.
     *
     * @param guild The Discord guild
     * @return The server configuration
     */
    private ServerConfigDTO extractServerInfo(Guild guild) {
        log.info("Extracting server info for guild: {}", guild.getName());
        
        ServerConfigDTO serverConfig = new ServerConfigDTO();
        serverConfig.setId(guild.getId());
        serverConfig.setName(guild.getName());
        
        // Extract categories
        List<CategoryDTO> categories = getCategories(guild);
        serverConfig.setCategories(categories);
        
        // Extract text channels
        List<TextChannelDTO> textChannels = getTextChannels(guild);
        serverConfig.setTextChannels(textChannels);
        
        // Extract voice channels
        List<VoiceChannelDTO> voiceChannels = getVoiceChannels(guild);
        serverConfig.setVoiceChannels(voiceChannels);
        
        return serverConfig;
    }

    /**
     * Extract category information from a guild.
     *
     * @param guild The Discord guild
     * @return List of category DTOs
     */
    private List<CategoryDTO> getCategories(Guild guild) {
        return guild.getCategories().stream()
            .map(this::mapCategory)
            .collect(Collectors.toList());
    }

    /**
     * Extract text channel information from a guild.
     *
     * @param guild The Discord guild
     * @return List of text channel DTOs
     */
    private List<TextChannelDTO> getTextChannels(Guild guild) {
        return guild.getTextChannels().stream()
            .map(this::mapTextChannel)
            .collect(Collectors.toList());
    }

    /**
     * Extract voice channel information from a guild.
     *
     * @param guild The Discord guild
     * @return List of voice channel DTOs
     */
    private List<VoiceChannelDTO> getVoiceChannels(Guild guild) {
        return guild.getVoiceChannels().stream()
            .map(this::mapVoiceChannel)
            .collect(Collectors.toList());
    }

    /**
     * Map a JDA Category to a CategoryDTO.
     *
     * @param category The JDA Category
     * @return A CategoryDTO
     */
    private CategoryDTO mapCategory(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setPosition(category.getPosition());
        return dto;
    }

    /**
     * Map a JDA TextChannel to a TextChannelDTO.
     *
     * @param textChannel The JDA TextChannel
     * @return A TextChannelDTO
     */
    private TextChannelDTO mapTextChannel(TextChannel textChannel) {
        TextChannelDTO dto = new TextChannelDTO();
        dto.setId(textChannel.getId());
        dto.setName(textChannel.getName());
        dto.setPosition(textChannel.getPosition());
        
        // Set category ID if the channel has a parent category
        Category parent = textChannel.getParentCategory();
        if (parent != null) {
            dto.setParentCategoryId(parent.getId());
        }
        
        return dto;
    }

    /**
     * Map a JDA VoiceChannel to a VoiceChannelDTO.
     *
     * @param voiceChannel The JDA VoiceChannel
     * @return A VoiceChannelDTO
     */
    private VoiceChannelDTO mapVoiceChannel(VoiceChannel voiceChannel) {
        VoiceChannelDTO dto = new VoiceChannelDTO();
        dto.setId(voiceChannel.getId());
        dto.setName(voiceChannel.getName());
        dto.setPosition(voiceChannel.getPosition());
        dto.setUserLimit(voiceChannel.getUserLimit());
        dto.setBitrate(voiceChannel.getBitrate());
        
        // Set category ID if the channel has a parent category
        Category parent = voiceChannel.getParentCategory();
        if (parent != null) {
            dto.setParentCategoryId(parent.getId());
        }
        
        return dto;
    }
}