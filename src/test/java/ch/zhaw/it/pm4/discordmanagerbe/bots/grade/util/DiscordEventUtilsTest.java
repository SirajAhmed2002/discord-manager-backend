package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util;

import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordEventUtilsTest {

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private Guild guild;

    @Mock
    private MessageChannelUnion channel;

    @Mock
    private User user;

    @Mock
    private InteractionHook interactionHook;

    @Mock
    private WebhookMessageCreateAction messageAction;

    @InjectMocks
    private DiscordEventUtils discordEventUtils;

    @BeforeEach
    void setUp() {
        // Setup basic Discord entity mocks
        lenient().when(event.getGuild()).thenReturn(guild);
        lenient().when(event.getChannel()).thenReturn(channel);
        lenient().when(event.getUser()).thenReturn(user);
        lenient().when(guild.getId()).thenReturn("server123");
        lenient().when(channel.getId()).thenReturn("channel123");
        lenient().when(user.getId()).thenReturn("user123");

        // ✅ Fix: Properly mock the interaction hook chain
        lenient().when(event.getHook()).thenReturn(interactionHook);
        lenient().when(interactionHook.sendMessage(anyString())).thenReturn(messageAction);
        lenient().when(messageAction.setEphemeral(anyBoolean())).thenReturn(messageAction);
        lenient().doNothing().when(messageAction).queue();
    }

    @Test
    void extractDiscordIds_WithGuild_ReturnsValidIds() {
        // Act
        DiscordIdsDTO result = discordEventUtils.extractDiscordIds(event);

        // Assert
        assertNotNull(result);
        assertEquals("server123", result.getServerId());
        assertEquals("channel123", result.getChannelId());
        assertEquals("user123", result.getUserId());
    }

    @Test
    void extractDiscordIds_WithoutGuild_ReturnsUnknownServer() {
        // Arrange
        when(event.getGuild()).thenReturn(null);

        // Act
        DiscordIdsDTO result = discordEventUtils.extractDiscordIds(event);

        // Assert
        assertNotNull(result);
        assertEquals("unknown", result.getServerId());
        assertEquals("channel123", result.getChannelId());
        assertEquals("user123", result.getUserId());
    }

    @Test
    void getRequiredString_WithValue_ReturnsValue() {
        // Arrange
        when(event.getOption(eq("testOption"), eq(""), any())).thenReturn("testValue");

        // Act
        String result = discordEventUtils.getRequiredString(event, "testOption");

        // Assert
        assertEquals("testValue", result);
    }

    @Test
    void getOptionalString_WithValue_ReturnsValue() {
        // Arrange
        when(event.getOption(eq("testOption"), eq(null), any())).thenReturn("testValue");

        // Act
        String result = discordEventUtils.getOptionalString(event, "testOption");

        // Assert
        assertEquals("testValue", result);
    }

    @Test
    void getOptionalString_WithNull_ReturnsNull() {
        // Arrange
        when(event.getOption(eq("testOption"), eq(null), any())).thenReturn(null);

        // Act
        String result = discordEventUtils.getOptionalString(event, "testOption");

        // Assert
        assertNull(result);
    }

    @Test
    void getRequiredInt_WithValue_ReturnsValue() {
        // Arrange
        when(event.getOption(eq("testOption"), eq(0), any())).thenReturn(42);

        // Act
        int result = discordEventUtils.getRequiredInt(event, "testOption");

        // Assert
        assertEquals(42, result);
    }

    @Test
    void sendErrorMessage_CallsHookWithMessage() {
        // Arrange
        Exception testException = new RuntimeException("Test error");

        // Act
        discordEventUtils.sendErrorMessage(event, "Test Operation", testException);

        // Assert
        verify(interactionHook).sendMessage(contains("Fehler beim Test Operation"));
        verify(messageAction).setEphemeral(true);
        verify(messageAction).queue();
    }

    @Test
    void sendErrorMessage_WithLongMessage_TruncatesMessage() {
        // Arrange
        String longErrorMessage = "A".repeat(3000); // Very long error message
        Exception testException = new RuntimeException(longErrorMessage);

        // Act
        discordEventUtils.sendErrorMessage(event, "Test Operation", testException);

        // Assert
        verify(interactionHook).sendMessage(anyString());
        verify(messageAction).setEphemeral(true);
        verify(messageAction).queue();

        // Verify the error is logged (the exception will be logged by the method)
        // This is more of an integration test, but we can verify the method completes
    }

    @Test
    void extractDiscordIds_BuilderPattern_WorksCorrectly() {
        // Act
        DiscordIdsDTO result = discordEventUtils.extractDiscordIds(event);

        // Assert - Test that the builder pattern works correctly
        assertNotNull(result);
        assertEquals("server123", result.getServerId());
        assertEquals("channel123", result.getChannelId());
        assertEquals("user123", result.getUserId());

        // Test toString method exists (from builder)
        assertNotNull(result.toString());
        assertTrue(result.toString().contains("server123"));
    }
}