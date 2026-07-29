package ch.zhaw.it.pm4.discordmanagerbe.bots.guildinvite;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.InviteAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateInviteLinkBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private Guild guild;

    @Mock
    private TextChannel textChannel;

    @Mock
    private TextChannel systemChannel;

    @Mock
    private InviteAction inviteAction;

    @Mock
    private Invite invite;

    private CreateInviteLinkBot createInviteLinkBot;

    private static final String VALID_GUILD_ID = "123456789";
    private static final String INVITE_CODE = "abc123";
    private static final String EXPECTED_INVITE_LINK = "https://discord.gg/" + INVITE_CODE;

    @BeforeEach
    void setUp() {
        createInviteLinkBot = new CreateInviteLinkBot(jdaBean);
    }

    @Test
    void testCreateInviteLink_Success_WithSystemChannel() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(systemChannel);
        when(systemChannel.createInvite()).thenReturn(inviteAction);
        setupInviteActionMocks();

        // Act
        String result = createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 10, 24);

        // Assert
        assertEquals(EXPECTED_INVITE_LINK, result);
        assertEquals(EXPECTED_INVITE_LINK, createInviteLinkBot.getInviteLink());
        verify(inviteAction).setMaxUses(10);
        verify(inviteAction).setMaxAge(24 * 3600); // 24 hours in seconds
        verify(inviteAction).complete(); // Now using complete() instead of queue()
    }

    @Test
    void testCreateInviteLink_Success_WithoutSystemChannel() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(null);
        when(guild.getTextChannels()).thenReturn(List.of(textChannel));
        when(textChannel.createInvite()).thenReturn(inviteAction);
        setupInviteActionMocks();

        // Act
        String result = createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 5, 12);

        // Assert
        assertEquals(EXPECTED_INVITE_LINK, result);
        verify(inviteAction).setMaxUses(5);
        verify(inviteAction).setMaxAge(12 * 3600);
        verify(inviteAction).complete();
    }

    @Test
    void testCreateInviteLink_Success_UnlimitedUsersAndAge() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(systemChannel);
        when(systemChannel.createInvite()).thenReturn(inviteAction);
        setupInviteActionMocks();

        // Act
        String result = createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 0, 0);

        // Assert
        assertEquals(EXPECTED_INVITE_LINK, result);
        verify(inviteAction).setMaxUses(0);
        verify(inviteAction).setMaxAge(0);
        verify(inviteAction).complete();
    }

    @Test
    void testCreateInviteLink_NullGuildId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createInviteLinkBot.createInviteLink(null, 10, 24)
        );
        assertEquals("Guild ID must be set and not empty.", exception.getMessage());
    }

    @Test
    void testCreateInviteLink_EmptyGuildId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createInviteLinkBot.createInviteLink("", 10, 24)
        );
        assertEquals("Guild ID must be set and not empty.", exception.getMessage());
    }

    @Test
    void testCreateInviteLink_WhitespaceGuildId_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createInviteLinkBot.createInviteLink("   ", 10, 24)
        );
        assertEquals("Guild ID must be set and not empty.", exception.getMessage());
    }

    @Test
    void testCreateInviteLink_GuildNotFound_ThrowsException() {
        // Arrange
        when(jdaBean.getGuildById(VALID_GUILD_ID)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 10, 24)
        );
        assertEquals("Guild not found: " + VALID_GUILD_ID, exception.getMessage());
    }

    @Test
    void testCreateInviteLink_NoTextChannels_ThrowsException() {
        // Arrange
        when(jdaBean.getGuildById(VALID_GUILD_ID)).thenReturn(guild);
        when(guild.getSystemChannel()).thenReturn(null);
        when(guild.getTextChannels()).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 10, 24)
        );
        assertEquals("No text channels found in guild", exception.getMessage());
    }

    @Test
    void testCreateInviteLink_InviteCreationFails_ThrowsException() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(systemChannel);
        when(systemChannel.createInvite()).thenReturn(inviteAction);
        when(inviteAction.setMaxUses(anyInt())).thenReturn(inviteAction);
        when(inviteAction.setMaxAge(anyInt())).thenReturn(inviteAction);

        // Simulate failure in complete() method
        when(inviteAction.complete()).thenThrow(new RuntimeException("Discord API Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 10, 24)
        );
        assertTrue(exception.getMessage().contains("Error creating invite for guild"));
        assertTrue(exception.getMessage().contains("Discord API Error"));
    }

    @Test
    void testGetInviteLink_NoInviteGenerated_ReturnsNull() {
        // Act & Assert
        assertNull(createInviteLinkBot.getInviteLink());
    }

    @Test
    void testGetInviteLink_AfterSuccessfulCreation_ReturnsLink() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(systemChannel);
        when(systemChannel.createInvite()).thenReturn(inviteAction);
        setupInviteActionMocks();

        // Act
        createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 10, 24);

        // Assert
        assertEquals(EXPECTED_INVITE_LINK, createInviteLinkBot.getInviteLink());
    }

    @Test
    void testCreateInviteLink_MultipleInvocations_CachesLatestLink() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(systemChannel);
        when(systemChannel.createInvite()).thenReturn(inviteAction);

        // First invite
        setupInviteActionMocks();
        String firstResult = createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 10, 24);

        // Second invite with different code
        String secondInviteCode = "def456";
        String secondExpectedLink = "https://discord.gg/" + secondInviteCode;

        // Create new mock for second invite
        Invite secondInvite = mock(Invite.class);
        when(secondInvite.getCode()).thenReturn(secondInviteCode);
        when(inviteAction.complete()).thenReturn(secondInvite);

        // Act
        String secondResult = createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 5, 12);

        // Assert
        assertEquals(EXPECTED_INVITE_LINK, firstResult);
        assertEquals(secondExpectedLink, secondResult);
        assertEquals(secondExpectedLink, createInviteLinkBot.getInviteLink()); // Latest cached
    }

    @Test
    void testCreateInviteLink_LargeValues_HandledCorrectly() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(systemChannel);
        when(systemChannel.createInvite()).thenReturn(inviteAction);
        setupInviteActionMocks();

        int maxUsers = 1000;
        int maxAgeHours = 168; // 7 days

        // Act
        String result = createInviteLinkBot.createInviteLink(VALID_GUILD_ID, maxUsers, maxAgeHours);

        // Assert
        assertEquals(EXPECTED_INVITE_LINK, result);
        verify(inviteAction).setMaxUses(maxUsers);
        verify(inviteAction).setMaxAge(maxAgeHours * 3600);
        verify(inviteAction).complete();
    }

    @Test
    void testCreateInviteLink_ExceptionDuringInviteCreation_PropagatesCorrectly() {
        // Arrange
        setupSuccessfulMocks();
        when(guild.getSystemChannel()).thenReturn(systemChannel);
        when(guild.getId()).thenReturn(VALID_GUILD_ID);
        when(systemChannel.createInvite()).thenReturn(inviteAction);
        when(inviteAction.setMaxUses(anyInt())).thenReturn(inviteAction);
        when(inviteAction.setMaxAge(anyInt())).thenReturn(inviteAction);

        RuntimeException originalException = new RuntimeException("Network timeout");
        when(inviteAction.complete()).thenThrow(originalException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> createInviteLinkBot.createInviteLink(VALID_GUILD_ID, 10, 24)
        );

        assertEquals(originalException, exception.getCause());
        assertTrue(exception.getMessage().contains("Error creating invite for guild " + VALID_GUILD_ID));
    }

    // Helper methods

    private void setupSuccessfulMocks() {
        when(jdaBean.getGuildById(VALID_GUILD_ID)).thenReturn(guild);
    }

    private void setupInviteActionMocks() {
        when(inviteAction.setMaxUses(anyInt())).thenReturn(inviteAction);
        when(inviteAction.setMaxAge(anyInt())).thenReturn(inviteAction);
        when(invite.getCode()).thenReturn(INVITE_CODE);
        when(inviteAction.complete()).thenReturn(invite); // Now using complete() instead of queue()
    }
}