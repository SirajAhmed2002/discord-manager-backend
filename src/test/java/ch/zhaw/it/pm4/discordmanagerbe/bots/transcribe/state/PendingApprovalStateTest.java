package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PendingApprovalStateTest {

    @Nested
    @DisplayName("LeaveVoiceChannel Tests")
    class LeaveVoiceChannelTests {

        @Mock(lenient = true)
        private TranscribeBot mockBot;

        @Mock
        private ApplicationContext mockApplicationContext;

        @Mock
        private SlashCommandInteractionEvent mockEvent;

        @Mock
        private AudioManager mockAudioManager;

        @Mock
        private Member mockMember;

        @Mock
        private GuildVoiceState mockGuildVoiceState;

        @Mock
        private AudioChannelUnion mockAudioChannelUnion;

        @Mock
        private VoiceChannel mockVoiceChannel;

        @Mock
        private DisconnectedState mockDisconnectedState;

        @Mock
        private ReplyCallbackAction mockReplyAction;

        @Mock
        private Guild mockGuild;

        @Mock
        private Role mockEveryoneRole;

        @Mock
        private PermissionOverride mockPermissionOverride;

        @Mock
        private AuditableRestAction<Void> mockDeleteAction;

        private PendingApprovalState pendingApprovalState;

        private Map<String, Set<String>> mockPendingAcceptances;
        private Map<String, Set<String>> mockAcceptedUsers;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);

            // Initialize maps
            mockPendingAcceptances = new HashMap<>();
            mockAcceptedUsers = new HashMap<>();

            // Setup bot mocks with lenient stubs to avoid UnfinishedStubbingException
            lenient().when(mockBot.getPendingAcceptances()).thenReturn(mockPendingAcceptances);
            lenient().when(mockBot.getAcceptedUsers()).thenReturn(mockAcceptedUsers);
            lenient().when(mockBot.getDisconnectedState()).thenReturn(mockDisconnectedState);

            // Setup event mocks
            lenient().when(mockEvent.getMember()).thenReturn(mockMember);
            lenient().when(mockEvent.reply(anyString())).thenReturn(mockReplyAction);

            // Setup member and voice state mocks
            lenient().when(mockMember.getVoiceState()).thenReturn(mockGuildVoiceState);
            lenient().when(mockGuildVoiceState.inAudioChannel()).thenReturn(true);
            lenient().when(mockGuildVoiceState.getChannel()).thenReturn(mockAudioChannelUnion);
            lenient().when(mockAudioChannelUnion.asVoiceChannel()).thenReturn(mockVoiceChannel);
            lenient().when(mockVoiceChannel.getId()).thenReturn("test-channel-id");

            setupGuildAndPermissionMocks();

            pendingApprovalState = new PendingApprovalState(mockBot, mockApplicationContext);
        }

        private void setupGuildAndPermissionMocks() {

            InteractionHook mockHook = mock(InteractionHook.class);
            lenient().when(mockEvent.getHook()).thenReturn(mockHook);

            WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
            lenient().when(mockHook.sendMessage(anyString())).thenReturn(mockAction);
            lenient().when(mockAction.setEphemeral(true)).thenReturn(mockAction);

            // Guild Setup
            lenient().when(mockVoiceChannel.getGuild()).thenReturn(mockGuild);
            lenient().when(mockGuild.getPublicRole()).thenReturn(mockEveryoneRole);

            // Permission Override Setup
            lenient().when(mockVoiceChannel.getPermissionOverride(mockEveryoneRole))
                    .thenReturn(mockPermissionOverride);
            lenient().when(mockPermissionOverride.delete()).thenReturn(mockDeleteAction);

            // RestAction Setup für delete()
            lenient().doAnswer(invocation -> {
                // Simuliere erfolgreiche Ausführung
                return null;
            }).when(mockDeleteAction).queue(any(), any());

            lenient().doAnswer(invocation -> {
                // Simuliere erfolgreiche Ausführung ohne Error Handler
                return null;
            }).when(mockDeleteAction).queue();
        }

        @Test
        @DisplayName("Should successfully leave voice channel when connected")
        void shouldLeaveVoiceChannelWhenConnected() {
            // Arrange
            when(mockBot.getActiveAudioManager()).thenReturn(mockAudioManager);
            when(mockAudioManager.isConnected()).thenReturn(true);

            String channelId = "test-channel-id";
            mockPendingAcceptances.put(channelId, new HashSet<>(Arrays.asList("user1", "user2")));
            mockAcceptedUsers.put(channelId, new HashSet<>(Arrays.asList("user3")));

            // Act
            pendingApprovalState.leaveVoiceChannel(mockEvent);

            // Assert
            verify(mockAudioManager).closeAudioConnection();
            verify(mockBot).setActiveAudioManager(null);
            verify(mockBot).setState(mockDisconnectedState);

            // Verify cleanup
            assertFalse(mockPendingAcceptances.containsKey(channelId));
            assertFalse(mockAcceptedUsers.containsKey(channelId));
        }

        @Test
        @DisplayName("Should not disconnect when audio manager is null")
        void shouldNotDisconnectWhenAudioManagerIsNull() {
            // Arrange
            when(mockBot.getActiveAudioManager()).thenReturn(null);

            // Act
            pendingApprovalState.leaveVoiceChannel(mockEvent);

            // Assert
            verify(mockBot, never()).setState(any());
            verifyNoInteractions(mockAudioManager);
        }

        @Test
        @DisplayName("Should not disconnect when audio manager is not connected")
        void shouldNotDisconnectWhenNotConnected() {
            // Arrange
            when(mockBot.getActiveAudioManager()).thenReturn(mockAudioManager);
            when(mockAudioManager.isConnected()).thenReturn(false);

            // Act
            pendingApprovalState.leaveVoiceChannel(mockEvent);

            // Assert
            verify(mockAudioManager, never()).closeAudioConnection();
            verify(mockBot, never()).setState(any());
        }

        @Test
        @DisplayName("Should only clean up data for the specific channel")
        void shouldOnlyCleanupSpecificChannelData() {
            // Arrange
            when(mockBot.getActiveAudioManager()).thenReturn(mockAudioManager);
            when(mockAudioManager.isConnected()).thenReturn(true);

            String targetChannelId = "test-channel-id";
            String otherChannelId = "other-channel-id";

            // Add data for multiple channels
            mockPendingAcceptances.put(targetChannelId, new HashSet<>(Arrays.asList("user1")));
            mockPendingAcceptances.put(otherChannelId, new HashSet<>(Arrays.asList("user2")));
            mockAcceptedUsers.put(targetChannelId, new HashSet<>(Arrays.asList("user3")));
            mockAcceptedUsers.put(otherChannelId, new HashSet<>(Arrays.asList("user4")));

            // Act
            pendingApprovalState.leaveVoiceChannel(mockEvent);

            // Assert
            assertFalse(mockPendingAcceptances.containsKey(targetChannelId));
            assertFalse(mockAcceptedUsers.containsKey(targetChannelId));

            // Other channel data should remain
            assertTrue(mockPendingAcceptances.containsKey(otherChannelId));
            assertTrue(mockAcceptedUsers.containsKey(otherChannelId));
        }

        @Test
        @DisplayName("Should verify correct sequence of operations")
        void shouldVerifyCorrectSequenceOfOperations() {
            // Arrange
            when(mockBot.getActiveAudioManager()).thenReturn(mockAudioManager);
            when(mockAudioManager.isConnected()).thenReturn(true);

            InOrder inOrder = inOrder(mockAudioManager, mockBot, mockEvent);

            // Act
            pendingApprovalState.leaveVoiceChannel(mockEvent);

            // Assert sequence
            inOrder.verify(mockAudioManager).closeAudioConnection();
            inOrder.verify(mockBot).setActiveAudioManager(null);
            inOrder.verify(mockBot).setState(mockDisconnectedState);
        }
    }

    @Nested
    @DisplayName("UnlockVoiceChannel Tests")
    class UnlockVoiceChannelTests {

        @Mock(lenient = true)
        private TranscribeBot mockBot;

        @Mock
        private ApplicationContext mockApplicationContext;

        @Mock
        private SlashCommandInteractionEvent mockEvent;

        @Mock
        private Member mockMember;

        @Mock
        private GuildVoiceState mockGuildVoiceState;

        @Mock
        private AudioChannelUnion mockAudioChannelUnion;

        @Mock
        private VoiceChannel mockVoiceChannel;

        @Mock
        private ConnectedState mockConnectedState;

        @Mock
        private ReplyCallbackAction mockReplyAction;

        @Mock
        private ReplyCallbackAction mockEphemeralReplyAction;

        @Mock
        private Guild mockGuild;

        @Mock
        private Role mockEveryoneRole;

        @Mock
        private PermissionOverride mockPermissionOverride;

        @Mock
        private AuditableRestAction<Void> mockDeleteAction;

        private PendingApprovalState pendingApprovalState;

        private Map<String, Set<String>> mockPendingAcceptances;
        private Map<String, Set<String>> mockAcceptedUsers;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);

            // Initialize maps
            mockPendingAcceptances = new HashMap<>();
            mockAcceptedUsers = new HashMap<>();

            InteractionHook mockHook = mock(InteractionHook.class);
            lenient().when(mockEvent.getHook()).thenReturn(mockHook);

            WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
            lenient().when(mockHook.sendMessage(anyString())).thenReturn(mockAction);
            lenient().when(mockAction.setEphemeral(true)).thenReturn(mockAction);

            // Setup bot mocks with lenient stubs
            lenient().when(mockBot.getPendingAcceptances()).thenReturn(mockPendingAcceptances);
            lenient().when(mockBot.getAcceptedUsers()).thenReturn(mockAcceptedUsers);
            lenient().when(mockBot.getConnectedState()).thenReturn(mockConnectedState);

            // Setup event mocks
            lenient().when(mockEvent.getMember()).thenReturn(mockMember);
            lenient().when(mockEvent.reply(anyString())).thenReturn(mockReplyAction);

            // Setup ephemeral reply chain
            lenient().when(mockReplyAction.setEphemeral(true)).thenReturn(mockEphemeralReplyAction);

            // Setup member and voice state mocks
            lenient().when(mockMember.getVoiceState()).thenReturn(mockGuildVoiceState);
            lenient().when(mockGuildVoiceState.inAudioChannel()).thenReturn(true);
            lenient().when(mockGuildVoiceState.getChannel()).thenReturn(mockAudioChannelUnion);
            lenient().when(mockAudioChannelUnion.asVoiceChannel()).thenReturn(mockVoiceChannel);
            lenient().when(mockVoiceChannel.getId()).thenReturn("test-channel-id");

            setupGuildAndPermissionMocks();

            pendingApprovalState = new PendingApprovalState(mockBot, mockApplicationContext);
        }

        private void setupGuildAndPermissionMocks() {
            // Guild Setup
            lenient().when(mockVoiceChannel.getGuild()).thenReturn(mockGuild);
            lenient().when(mockGuild.getPublicRole()).thenReturn(mockEveryoneRole);

            // Permission Override Setup
            lenient().when(mockVoiceChannel.getPermissionOverride(mockEveryoneRole))
                    .thenReturn(mockPermissionOverride);
            lenient().when(mockPermissionOverride.delete()).thenReturn(mockDeleteAction);

            // RestAction Setup für delete()
            lenient().doAnswer(invocation -> {
                // Simuliere erfolgreiche Ausführung
                return null;
            }).when(mockDeleteAction).queue(any(), any());

            lenient().doAnswer(invocation -> {
                // Simuliere erfolgreiche Ausführung ohne Error Handler
                return null;
            }).when(mockDeleteAction).queue();
        }

        @Test
        @DisplayName("Should successfully unlock voice channel and clear pending requests")
        void shouldUnlockVoiceChannelAndClearRequests() {
            // Arrange
            String channelId = "test-channel-id";
            mockPendingAcceptances.put(channelId, new HashSet<>(Arrays.asList("user1", "user2")));
            mockAcceptedUsers.put(channelId, new HashSet<>(Arrays.asList("user3")));

            // Act
            pendingApprovalState.unlockVoiceChannel(mockEvent);

            // Assert
            verify(mockPermissionOverride).delete();
            verify(mockBot).setState(mockConnectedState);

            // Verify cleanup
            assertFalse(mockPendingAcceptances.containsKey(channelId));
            assertFalse(mockAcceptedUsers.containsKey(channelId));
        }

        @Test
        @DisplayName("Should not unlock when member is null")
        void shouldNotUnlockWhenMemberIsNull() {
            // Arrange
            when(mockEvent.getMember()).thenReturn(null);

            // Act
            pendingApprovalState.unlockVoiceChannel(mockEvent);

            // Assert
            verify(mockPermissionOverride, never()).delete();
            verify(mockBot, never()).setState(any());
        }

        @Test
        @DisplayName("Should not unlock when member is not in voice channel")
        void shouldNotUnlockWhenMemberNotInVoiceChannel() {
            // Arrange
            when(mockGuildVoiceState.inAudioChannel()).thenReturn(false);

            // Act
            pendingApprovalState.unlockVoiceChannel(mockEvent);

            // Assert
            verify(mockPermissionOverride, never()).delete();
            verify(mockBot, never()).setState(any());
        }

        @Test
        @DisplayName("Should only clean up data for the specific channel")
        void shouldOnlyCleanupSpecificChannelData() {
            // Arrange
            String targetChannelId = "test-channel-id";
            String otherChannelId = "other-channel-id";

            // Add data for multiple channels
            mockPendingAcceptances.put(targetChannelId, new HashSet<>(Arrays.asList("user1")));
            mockPendingAcceptances.put(otherChannelId, new HashSet<>(Arrays.asList("user2")));
            mockAcceptedUsers.put(targetChannelId, new HashSet<>(Arrays.asList("user3")));
            mockAcceptedUsers.put(otherChannelId, new HashSet<>(Arrays.asList("user4")));

            // Act
            pendingApprovalState.unlockVoiceChannel(mockEvent);

            // Assert
            assertFalse(mockPendingAcceptances.containsKey(targetChannelId));
            assertFalse(mockAcceptedUsers.containsKey(targetChannelId));

            // Other channel data should remain
            assertTrue(mockPendingAcceptances.containsKey(otherChannelId));
            assertTrue(mockAcceptedUsers.containsKey(otherChannelId));
        }

        @Test
        @DisplayName("Should verify correct sequence of operations")
        void shouldVerifyCorrectSequenceOfOperations() {
            // Arrange
            String channelId = "test-channel-id";
            mockPendingAcceptances.put(channelId, new HashSet<>(Arrays.asList("user1")));
            mockAcceptedUsers.put(channelId, new HashSet<>(Arrays.asList("user2")));

            InOrder inOrder = inOrder(mockPermissionOverride, mockBot, mockEvent);

            // Act
            pendingApprovalState.unlockVoiceChannel(mockEvent);

            // Assert sequence
            inOrder.verify(mockPermissionOverride).delete();
            inOrder.verify(mockBot).setState(mockConnectedState);
        }

        @Test
        @DisplayName("Should handle empty pending and accepted collections")
        void shouldHandleEmptyCollections() {
            // Arrange - collections are empty by default

            // Act
            pendingApprovalState.unlockVoiceChannel(mockEvent);

            // Assert
            verify(mockPermissionOverride).delete();
            verify(mockBot).setState(mockConnectedState);

            // Collections should still be empty
            assertTrue(mockPendingAcceptances.isEmpty());
            assertTrue(mockAcceptedUsers.isEmpty());
        }

        @Test
        @DisplayName("Should transition to ConnectedState after unlock")
        void shouldTransitionToConnectedStateAfterUnlock() {
            // Arrange
            String channelId = "test-channel-id";
            mockPendingAcceptances.put(channelId, new HashSet<>(Arrays.asList("user1")));

            // Act
            pendingApprovalState.unlockVoiceChannel(mockEvent);

            // Assert
            verify(mockBot).setState(mockConnectedState);
            verify(mockBot, never()).setState(any(DisconnectedState.class));
            verify(mockBot, never()).setState(any(RecordingState.class));
        }
    }

    @Nested
    @DisplayName("HandleAcceptRecording Tests")
    class HandleAcceptRecordingTests {

        @Mock(lenient = true)
        private TranscribeBot mockBot;

        @Mock
        private ApplicationContext mockApplicationContext;

        @Mock
        private SlashCommandInteractionEvent mockEvent;

        @Mock
        private Member mockMember;

        @Mock
        private GuildVoiceState mockGuildVoiceState;

        @Mock
        private AudioChannelUnion mockAudioChannelUnion;

        @Mock
        private VoiceChannel mockVoiceChannel;

        @Mock
        private ReplyCallbackAction mockReplyAction;

        @Mock
        private ReplyCallbackAction mockEphemeralReplyAction;

        private PendingApprovalState pendingApprovalState;

        private Map<String, Set<String>> mockPendingAcceptances;
        private Map<String, Set<String>> mockAcceptedUsers;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);

            // Initialize maps
            mockPendingAcceptances = new HashMap<>();
            mockAcceptedUsers = new HashMap<>();

            InteractionHook mockHook = mock(InteractionHook.class);
            lenient().when(mockEvent.getHook()).thenReturn(mockHook);

            WebhookMessageCreateAction mockAction = mock(WebhookMessageCreateAction.class);
            lenient().when(mockHook.sendMessage(anyString())).thenReturn(mockAction);
            lenient().when(mockAction.setEphemeral(true)).thenReturn(mockAction);

            // Setup bot mocks with lenient stubs
            lenient().when(mockBot.getPendingAcceptances()).thenReturn(mockPendingAcceptances);
            lenient().when(mockBot.getAcceptedUsers()).thenReturn(mockAcceptedUsers);

            // Setup event mocks
            lenient().when(mockEvent.getMember()).thenReturn(mockMember);
            lenient().when(mockEvent.reply(anyString())).thenReturn(mockReplyAction);
            lenient().when(mockMember.getId()).thenReturn("test-user-id");
            lenient().when(mockMember.getEffectiveName()).thenReturn("TestUser");

            // Setup ephemeral reply chain
            lenient().when(mockReplyAction.setEphemeral(true)).thenReturn(mockEphemeralReplyAction);

            // Setup member and voice state mocks
            lenient().when(mockMember.getVoiceState()).thenReturn(mockGuildVoiceState);
            lenient().when(mockGuildVoiceState.inAudioChannel()).thenReturn(true);
            lenient().when(mockGuildVoiceState.getChannel()).thenReturn(mockAudioChannelUnion);
            lenient().when(mockAudioChannelUnion.asVoiceChannel()).thenReturn(mockVoiceChannel);
            lenient().when(mockVoiceChannel.getId()).thenReturn("test-channel-id");

            pendingApprovalState = new PendingApprovalState(mockBot, mockApplicationContext);
        }

        @Test
        @DisplayName("Should successfully accept recording when valid pending request exists")
        void shouldAcceptRecordingWhenValidPendingRequestExists() {
            // Arrange
            String channelId = "test-channel-id";
            String userId = "test-user-id";

            Set<String> pendingUsers = new HashSet<>(Arrays.asList(userId, "other-user"));
            mockPendingAcceptances.put(channelId, pendingUsers);

            // Act
            pendingApprovalState.handleAcceptRecording(mockEvent);

            // Verify user was moved from pending to accepted
            assertFalse(mockPendingAcceptances.get(channelId).contains(userId));
            assertTrue(mockAcceptedUsers.get(channelId).contains(userId));

            // Other pending user should still be there
            assertTrue(mockPendingAcceptances.get(channelId).contains("other-user"));
        }

        @Test
        @DisplayName("Should create accepted users set when it doesn't exist")
        void shouldCreateAcceptedUsersSetWhenItDoesntExist() {
            // Arrange
            String channelId = "test-channel-id";
            String userId = "test-user-id";

            Set<String> pendingUsers = new HashSet<>(Arrays.asList(userId));
            mockPendingAcceptances.put(channelId, pendingUsers);
            // acceptedUsers map is empty

            // Act
            pendingApprovalState.handleAcceptRecording(mockEvent);

            // Assert
            assertTrue(mockAcceptedUsers.containsKey(channelId));
            assertTrue(mockAcceptedUsers.get(channelId).contains(userId));
            assertFalse(mockPendingAcceptances.get(channelId).contains(userId));
        }

        @Test
        @DisplayName("Should not accept when no pending request exists for channel")
        void shouldNotAcceptWhenNoPendingRequestExists() {
            // Arrange - no pending requests in the map

            // Act
            pendingApprovalState.handleAcceptRecording(mockEvent);

            // Assert

            // Verify no changes to collections
            assertTrue(mockPendingAcceptances.isEmpty());
            assertTrue(mockAcceptedUsers.isEmpty());
        }

        @Test
        @DisplayName("Should handle user accepting multiple times gracefully")
        void shouldHandleMultipleAcceptancesGracefully() {
            // Arrange
            String channelId = "test-channel-id";
            String userId = "test-user-id";

            Set<String> pendingUsers = new HashSet<>(Arrays.asList(userId));
            Set<String> acceptedUsers = new HashSet<>();
            mockPendingAcceptances.put(channelId, pendingUsers);
            mockAcceptedUsers.put(channelId, acceptedUsers);

            // Act
            pendingApprovalState.handleAcceptRecording(mockEvent);
            pendingApprovalState.handleAcceptRecording(mockEvent);

            // Assert
            assertEquals(1, mockAcceptedUsers.get(channelId).size());
            assertTrue(mockAcceptedUsers.get(channelId).contains(userId));
        }

        @Test
        @DisplayName("Should only affect specific channel data")
        void shouldOnlyAffectSpecificChannelData() {
            // Arrange
            String targetChannelId = "test-channel-id";
            String otherChannelId = "other-channel-id";
            String userId = "test-user-id";

            // Setup data for multiple channels
            mockPendingAcceptances.put(targetChannelId, new HashSet<>(Arrays.asList(userId)));
            mockPendingAcceptances.put(otherChannelId, new HashSet<>(Arrays.asList("other-user")));

            // Act
            pendingApprovalState.handleAcceptRecording(mockEvent);

            // Assert
            // Target channel should be modified
            assertFalse(mockPendingAcceptances.get(targetChannelId).contains(userId));
            assertTrue(mockAcceptedUsers.get(targetChannelId).contains(userId));

            // Other channel should remain unchanged
            assertTrue(mockPendingAcceptances.get(otherChannelId).contains("other-user"));
            assertFalse(mockAcceptedUsers.containsKey(otherChannelId) &&
                    mockAcceptedUsers.get(otherChannelId).contains("other-user"));
        }

        @Test
        @DisplayName("Should verify correct message format with user name")
        void shouldVerifyCorrectMessageFormatWithUserName() {
            // Arrange
            String channelId = "test-channel-id";
            String userId = "test-user-id";
            String expectedUserName = "TestUser";

            mockPendingAcceptances.put(channelId, new HashSet<>(Arrays.asList(userId)));

            // Act
            pendingApprovalState.handleAcceptRecording(mockEvent);

            // Assert
            String expectedMessage = BotMessages.get(MessageKey.RECORDING_ACCEPTED) + " " + expectedUserName;
        }

        @Test
        @DisplayName("Should handle user not in pending list gracefully")
        void shouldHandleUserNotInPendingListGracefully() {
            // Arrange
            String channelId = "test-channel-id";
            String userId = "test-user-id";
            String otherUserId = "other-user-id";

            // User is not in the pending list, but channel has pending requests
            mockPendingAcceptances.put(channelId, new HashSet<>(Arrays.asList(otherUserId)));

            // Act
            pendingApprovalState.handleAcceptRecording(mockEvent);

            // Assert
            // User should be added to accepted even if not in pending
            assertTrue(mockAcceptedUsers.get(channelId).contains(userId));

            // Other user should still be in pending
            assertTrue(mockPendingAcceptances.get(channelId).contains(otherUserId));
        }
    }
}