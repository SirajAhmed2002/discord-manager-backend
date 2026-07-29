package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuildEntityServiceTest {

    @Mock
    private JDA jda;
    
    @Mock
    private Guild guild;
    
    @Mock
    private GuildChannel channel;
    
    @Mock
    private Role role;

    @InjectMocks
    private GuildEntityService guildEntityService;

    private static final String GUILD_ID = "123456789";
    private static final String CHANNEL_ID = "987654321";
    private static final String ROLE_ID = "555666777";

    @Test
    void getGuild_Found() {
        // Arrange
        when(jda.getGuildById(GUILD_ID)).thenReturn(guild);

        // Act
        Optional<Guild> result = guildEntityService.getGuild(GUILD_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(guild, result.get());
    }

    @Test
    void getGuild_NotFound() {
        // Arrange
        when(jda.getGuildById(GUILD_ID)).thenReturn(null);

        // Act
        Optional<Guild> result = guildEntityService.getGuild(GUILD_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getChannel_Found() {
        // Arrange
        when(guild.getGuildChannelById(CHANNEL_ID)).thenReturn(channel);

        // Act
        Optional<GuildChannel> result = guildEntityService.getChannel(guild, CHANNEL_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(channel, result.get());
    }

    @Test
    void getChannel_NotFound() {
        // Arrange
        when(guild.getGuildChannelById(CHANNEL_ID)).thenReturn(null);

        // Act
        Optional<GuildChannel> result = guildEntityService.getChannel(guild, CHANNEL_ID);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getRole_Found() {
        // Arrange
        when(guild.getRoleById(ROLE_ID)).thenReturn(role);

        // Act
        Optional<Role> result = guildEntityService.getRole(guild, ROLE_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(role, result.get());
    }

    @Test
    void getRole_NotFound() {
        // Arrange
        when(guild.getRoleById(ROLE_ID)).thenReturn(null);

        // Act
        Optional<Role> result = guildEntityService.getRole(guild, ROLE_ID);

        // Assert
        assertTrue(result.isEmpty());
    }
}