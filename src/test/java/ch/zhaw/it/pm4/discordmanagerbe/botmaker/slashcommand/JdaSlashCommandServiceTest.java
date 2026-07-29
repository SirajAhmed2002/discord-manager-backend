package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.DiscordServer;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.DiscordServerRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JdaSlashCommandServiceTest {

    @Mock private JDA jdaMock;
    @Mock private DiscordServerRepository discordServerRepositoryMock;
    @Mock private JdaEventListenerService commandListenerMock;
    @Mock private Guild guild1Mock;
    @Mock private Guild guild2Mock;
    @Mock private CommandListUpdateAction updateActionMock;

    private JdaSlashCommandService slashCommandService;
    private List<DiscordServer> discordServers;
    private List<Guild> guilds;

    @BeforeEach
    public void setUp() {
        discordServers = new ArrayList<>();
        guilds = new ArrayList<>();
        setupMocks();
        slashCommandService = new JdaSlashCommandService(jdaMock, discordServerRepositoryMock, commandListenerMock);
    }

    private void setupMocks() {
        // Mock guild IDs and names
        lenient().when(guild1Mock.getId()).thenReturn("guild1Id");
        lenient().when(guild1Mock.getName()).thenReturn("Guild 1");
        lenient().when(guild2Mock.getId()).thenReturn("guild2Id");
        lenient().when(guild2Mock.getName()).thenReturn("Guild 2");

        // Mock updateCommands() for guilds
        lenient().when(guild1Mock.updateCommands()).thenReturn(updateActionMock);
        lenient().when(guild2Mock.updateCommands()).thenReturn(updateActionMock);
        lenient().when(updateActionMock.addCommands(anyList())).thenReturn(updateActionMock);

        // Mock queue behavior
        lenient().doAnswer(invocation -> {
            Consumer<List<?>> callback = invocation.getArgument(0);
            callback.accept(Collections.emptyList());
            return null;
        }).when(updateActionMock).queue(any(Consumer.class), any(Consumer.class));

        // Setup guilds list
        guilds.add(guild1Mock);
        guilds.add(guild2Mock);
        lenient().when(jdaMock.getGuilds()).thenReturn(guilds);
    }

    // Helper methods to reduce code duplication
    private DiscordServer createServerWithBots(String id, String name, String owner, SlashCommandBotType... bots) {
        DiscordServer server = new DiscordServer(id, name, owner);
        Arrays.stream(bots).forEach(server::enableBot);
        return server;
    }

    private JdaSlashCommand createMockCommand() {
        JdaSlashCommand command = mock(JdaSlashCommand.class);
        SlashCommandData commandData = mock(SlashCommandData.class);
        lenient().when(command.getCommandData()).thenReturn(commandData);
        return command;
    }

    private Map<String, List<JdaSlashCommand>> createCommandMap(Object... pairs) {
        Map<String, List<JdaSlashCommand>> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            SlashCommandBotType bot = (SlashCommandBotType) pairs[i];
            @SuppressWarnings("unchecked")
            List<JdaSlashCommand> commands = (List<JdaSlashCommand>) pairs[i + 1];
            map.put(bot.name(), commands);
        }
        return map;
    }

    private void setCommandsByBotType(Map<String, List<JdaSlashCommand>> commands) {
        try {
            java.lang.reflect.Field field = JdaSlashCommandService.class.getDeclaredField("commandsByBotType");
            field.setAccessible(true);
            field.set(slashCommandService, commands);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    private void verifyBasicInteractions(int expectedAddCommandsCalls, int expectedQueueCalls) {
        verify(discordServerRepositoryMock).findAll();
        verify(guild1Mock).updateCommands();
        verify(guild2Mock).updateCommands();
        verify(updateActionMock, times(expectedAddCommandsCalls)).addCommands(anyList());
        verify(updateActionMock, times(expectedQueueCalls)).queue(any(Consumer.class), any(Consumer.class));
    }

    @Nested
    class UpdateCommandsForAllServersTests {

        @Test
        public void testWithMatchingServers() {
            // Setup servers with different bots
            DiscordServer server1 = createServerWithBots("guild1Id", "Guild 1", "owner1", SlashCommandBotType.MUSIC);
            DiscordServer server2 = createServerWithBots("guild2Id", "Guild 2", "owner2", SlashCommandBotType.TODO);
            discordServers.addAll(Arrays.asList(server1, server2));

            when(discordServerRepositoryMock.findAll()).thenReturn(discordServers);

            // Setup commands
            JdaSlashCommand musicCommand = createMockCommand();
            JdaSlashCommand todoCommand = createMockCommand();
            Map<String, List<JdaSlashCommand>> commandMap = createCommandMap(
                    SlashCommandBotType.MUSIC, List.of(musicCommand),
                    SlashCommandBotType.TODO, List.of(todoCommand)
            );
            setCommandsByBotType(commandMap);

            slashCommandService.updateCommandsForAllServers();

            verifyBasicInteractions(2, 2);
        }

        @Test
        public void testWithServerNotInDatabase() {
            when(discordServerRepositoryMock.findAll()).thenReturn(Collections.emptyList());

            slashCommandService.updateCommandsForAllServers();

            verify(discordServerRepositoryMock).findAll();
            verify(guild1Mock).updateCommands();
            verify(guild2Mock).updateCommands();
            verify(updateActionMock, never()).addCommands(anyList());
            verify(updateActionMock, times(2)).queue(any(Consumer.class), any(Consumer.class));
        }

        @Test
        public void testWithNoEnabledBots() {
            DiscordServer server1 = createServerWithBots("guild1Id", "Guild 1", "owner1");
            DiscordServer server2 = createServerWithBots("guild2Id", "Guild 2", "owner2");
            discordServers.addAll(Arrays.asList(server1, server2));

            when(discordServerRepositoryMock.findAll()).thenReturn(discordServers);

            slashCommandService.updateCommandsForAllServers();

            verify(discordServerRepositoryMock).findAll();
            verify(guild1Mock).updateCommands();
            verify(guild2Mock).updateCommands();
            verify(updateActionMock, never()).addCommands(anyList());
            verify(updateActionMock, times(2)).queue(any(Consumer.class), any(Consumer.class));
        }

        @Test
        public void testWithMultipleBots() {
            DiscordServer server = createServerWithBots("guild1Id", "Guild 1", "owner1",
                    SlashCommandBotType.MUSIC, SlashCommandBotType.TIMETABLE);
            discordServers.add(server);

            when(discordServerRepositoryMock.findAll()).thenReturn(discordServers);

            // Create multiple commands for music bot
            JdaSlashCommand musicCommand1 = createMockCommand();
            JdaSlashCommand musicCommand2 = createMockCommand();
            JdaSlashCommand timetableCommand = createMockCommand();

            Map<String, List<JdaSlashCommand>> commandMap = createCommandMap(
                    SlashCommandBotType.MUSIC, Arrays.asList(musicCommand1, musicCommand2),
                    SlashCommandBotType.TIMETABLE, List.of(timetableCommand)
            );
            setCommandsByBotType(commandMap);

            slashCommandService.updateCommandsForAllServers();

            verify(discordServerRepositoryMock).findAll();
            verify(guild1Mock).updateCommands();

            ArgumentCaptor<List> commandsCaptor = ArgumentCaptor.forClass(List.class);
            verify(updateActionMock).addCommands(commandsCaptor.capture());

            verify(musicCommand1).getCommandData();
            verify(musicCommand2).getCommandData();
            verify(timetableCommand).getCommandData();
        }

        @Test
        public void testMixedServerStatus() {
            DiscordServer server1 = createServerWithBots("guild1Id", "Guild 1", "owner1",
                    SlashCommandBotType.MUSIC, SlashCommandBotType.GRADE_CALCULATOR);
            discordServers.add(server1);

            when(discordServerRepositoryMock.findAll()).thenReturn(discordServers);

            JdaSlashCommand musicCommand = createMockCommand();
            JdaSlashCommand gradeCommand = createMockCommand();

            Map<String, List<JdaSlashCommand>> commandMap = createCommandMap(
                    SlashCommandBotType.MUSIC, List.of(musicCommand),
                    SlashCommandBotType.GRADE_CALCULATOR, List.of(gradeCommand)
            );
            setCommandsByBotType(commandMap);

            slashCommandService.updateCommandsForAllServers();

            verifyBasicInteractions(1, 2);
        }
    }

    @Nested
    class RegisterCommandsForBotTests {

        @Test
        public void testWithValidCommands() {
            JdaSlashCommand command1 = createMockCommand();
            JdaSlashCommand command2 = createMockCommand();
            List<JdaSlashCommand> commands = Arrays.asList(command1, command2);

            JdaSlashCommandService spyService = spy(slashCommandService);
            doNothing().when(spyService).updateCommandsForAllServers();

            spyService.registerCommandsForBot(SlashCommandBotType.MUSIC, commands);

            List<JdaSlashCommand> storedCommands = spyService.getCommandsForBotType(SlashCommandBotType.MUSIC.name());
            assertEquals(2, storedCommands.size());
            assertTrue(storedCommands.containsAll(commands));

            verify(commandListenerMock).appendCommands(commands);
            verify(spyService).updateCommandsForAllServers();
        }

        @Test
        public void testWithEmptyCommandList() {
            List<JdaSlashCommand> emptyCommands = Collections.emptyList();

            JdaSlashCommandService spyService = spy(slashCommandService);
            doNothing().when(spyService).updateCommandsForAllServers();

            spyService.registerCommandsForBot(SlashCommandBotType.TODO, emptyCommands);

            List<JdaSlashCommand> storedCommands = spyService.getCommandsForBotType(SlashCommandBotType.TODO.name());
            assertEquals(0, storedCommands.size());

            verify(commandListenerMock).appendCommands(emptyCommands);
            verify(spyService).updateCommandsForAllServers();
        }

        @Test
        public void testOverwritesExistingCommands() {
            JdaSlashCommand oldCommand = createMockCommand();
            JdaSlashCommand newCommand1 = createMockCommand();
            JdaSlashCommand newCommand2 = createMockCommand();

            JdaSlashCommandService spyService = spy(slashCommandService);
            doNothing().when(spyService).updateCommandsForAllServers();

            // First registration
            spyService.registerCommandsForBot(SlashCommandBotType.GRADE_CALCULATOR, List.of(oldCommand));

            // Second registration
            List<JdaSlashCommand> newCommands = Arrays.asList(newCommand1, newCommand2);
            spyService.registerCommandsForBot(SlashCommandBotType.GRADE_CALCULATOR, newCommands);

            List<JdaSlashCommand> storedCommands = spyService.getCommandsForBotType(SlashCommandBotType.GRADE_CALCULATOR.name());
            assertEquals(2, storedCommands.size());
            assertFalse(storedCommands.contains(oldCommand));
            assertTrue(storedCommands.containsAll(newCommands));

            verify(spyService, times(2)).updateCommandsForAllServers();
        }
    }

    @Nested
    class UnregisterCommandsForBotTests {

        @Test
        public void testWithExistingCommands() {
            JdaSlashCommand command1 = createMockCommand();
            JdaSlashCommand command2 = createMockCommand();
            List<JdaSlashCommand> commands = Arrays.asList(command1, command2);

            JdaSlashCommandService spyService = spy(slashCommandService);
            doNothing().when(spyService).updateCommandsForAllServers();

            spyService.registerCommandsForBot(SlashCommandBotType.TRANSCRIPTION, commands);
            spyService.unregisterCommandsForBot(SlashCommandBotType.TRANSCRIPTION);

            List<JdaSlashCommand> storedCommands = spyService.getCommandsForBotType(SlashCommandBotType.TRANSCRIPTION.name());
            assertEquals(0, storedCommands.size());

            verify(commandListenerMock).removeCommands(commands);
            verify(spyService, times(2)).updateCommandsForAllServers();
        }

        @Test
        public void testWithNonExistentBot() {
            JdaSlashCommandService spyService = spy(slashCommandService);

            spyService.unregisterCommandsForBot(SlashCommandBotType.TIMETABLE);

            verify(commandListenerMock, never()).removeCommands(any());
            verify(spyService, never()).updateCommandsForAllServers();
        }

        @Test
        public void testWithEmptyCommandList() {
            JdaSlashCommandService spyService = spy(slashCommandService);
            doNothing().when(spyService).updateCommandsForAllServers();

            spyService.registerCommandsForBot(SlashCommandBotType.MUSIC, Collections.emptyList());
            spyService.unregisterCommandsForBot(SlashCommandBotType.MUSIC);

            List<JdaSlashCommand> storedCommands = spyService.getCommandsForBotType(SlashCommandBotType.MUSIC.name());
            assertEquals(0, storedCommands.size());

            verify(spyService, times(1)).updateCommandsForAllServers(); // Only from register call
        }
    }

    @Nested
    class ServerSpecificCommandTests {

        private void setupServerMocks(String serverId, Guild guild, DiscordServer server) {
            lenient().when(jdaMock.getGuildById(serverId)).thenReturn(guild);
            lenient().when(discordServerRepositoryMock.findById(serverId)).thenReturn(Optional.ofNullable(server));
        }

        @Test
        public void testRegisterCommandsForServer_WithValidServerAndBot() {
            String serverId = "guild1Id";
            DiscordServer server = createServerWithBots(serverId, "Guild 1", "owner1",
                    SlashCommandBotType.MUSIC, SlashCommandBotType.TODO);

            setupServerMocks(serverId, guild1Mock, server);

            JdaSlashCommand musicCommand = createMockCommand();
            JdaSlashCommand todoCommand = createMockCommand();

            Map<String, List<JdaSlashCommand>> commandMap = createCommandMap(
                    SlashCommandBotType.MUSIC, List.of(musicCommand),
                    SlashCommandBotType.TODO, List.of(todoCommand)
            );
            setCommandsByBotType(commandMap);

            slashCommandService.registerCommandsForServer(serverId, "MUSIC");

            verify(jdaMock).getGuildById(serverId);
            verify(discordServerRepositoryMock).findById(serverId);
            verify(guild1Mock).updateCommands();
            verify(updateActionMock).addCommands(anyList());
            verify(updateActionMock).queue(any(Consumer.class), any(Consumer.class));
        }

        @Test
        public void testRegisterCommandsForServer_WithInvalidGuildId() {
            String invalidServerId = "invalidGuildId";
            setupServerMocks(invalidServerId, null, null);

            slashCommandService.registerCommandsForServer(invalidServerId, "MUSIC");

            verify(jdaMock).getGuildById(invalidServerId);
            verify(discordServerRepositoryMock, never()).findById(anyString());
        }

        @Test
        public void testRegisterCommandsForServer_WithServerNotInDatabase() {
            String serverId = "guild1Id";
            setupServerMocks(serverId, guild1Mock, null);

            slashCommandService.registerCommandsForServer(serverId, "MUSIC");

            verify(jdaMock).getGuildById(serverId);
            verify(discordServerRepositoryMock).findById(serverId);
            verify(guild1Mock, never()).updateCommands();
        }

        @Test
        public void testUnregisterCommandsForServer_WithValidServerAndBot() {
            String serverId = "guild2Id";
            DiscordServer server = createServerWithBots(serverId, "Guild 2", "owner2", SlashCommandBotType.MUSIC);

            setupServerMocks(serverId, guild2Mock, server);

            JdaSlashCommand musicCommand = createMockCommand();
            Map<String, List<JdaSlashCommand>> commandMap = createCommandMap(
                    SlashCommandBotType.MUSIC, List.of(musicCommand)
            );
            setCommandsByBotType(commandMap);

            slashCommandService.unregisterCommandsForServer(serverId, "TODO");

            verify(jdaMock).getGuildById(serverId);
            verify(discordServerRepositoryMock).findById(serverId);
            verify(guild2Mock).updateCommands();
            verify(updateActionMock).addCommands(anyList());
            verify(updateActionMock).queue(any(Consumer.class), any(Consumer.class));
        }
    }

    @Nested
    class GetCommandsTests {

        @Test
        public void testGetCommandsForBotType_WithExistingBot() {
            JdaSlashCommand command1 = createMockCommand();
            JdaSlashCommand command2 = createMockCommand();

            Map<String, List<JdaSlashCommand>> commandMap = createCommandMap(
                    SlashCommandBotType.MUSIC, new ArrayList<>(Arrays.asList(command1, command2))
            );
            setCommandsByBotType(commandMap);

            List<JdaSlashCommand> result = slashCommandService.getCommandsForBotType(SlashCommandBotType.MUSIC.name());

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsAll(Arrays.asList(command1, command2)));
        }

        @Test
        public void testGetCommandsForBotType_WithNonExistentBot() {
            List<JdaSlashCommand> result = slashCommandService.getCommandsForBotType(SlashCommandBotType.TODO.name());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        public void testGetCommandsForServer_WithValidServerAndEnabledBots() {
            String serverId = "testServerId";
            DiscordServer server = createServerWithBots(serverId, "Test Server", "testOwner",
                    SlashCommandBotType.MUSIC, SlashCommandBotType.TODO);

            when(discordServerRepositoryMock.findById(serverId)).thenReturn(Optional.of(server));

            JdaSlashCommand musicCommand1 = createMockCommand();
            JdaSlashCommand musicCommand2 = createMockCommand();
            JdaSlashCommand todoCommand = createMockCommand();
            JdaSlashCommand gradeCommand = createMockCommand(); // Not enabled

            Map<String, List<JdaSlashCommand>> commandMap = createCommandMap(
                    SlashCommandBotType.MUSIC, Arrays.asList(musicCommand1, musicCommand2),
                    SlashCommandBotType.TODO, List.of(todoCommand),
                    SlashCommandBotType.GRADE_CALCULATOR, List.of(gradeCommand)
            );
            setCommandsByBotType(commandMap);

            Map<String, List<JdaSlashCommand>> result = slashCommandService.getCommandsForServer(serverId);

            assertNotNull(result);
            assertEquals(2, result.size()); // Only enabled bots

            assertTrue(result.containsKey(SlashCommandBotType.MUSIC.name()));
            assertEquals(2, result.get(SlashCommandBotType.MUSIC.name()).size());

            assertTrue(result.containsKey(SlashCommandBotType.TODO.name()));
            assertEquals(1, result.get(SlashCommandBotType.TODO.name()).size());

            assertFalse(result.containsKey(SlashCommandBotType.GRADE_CALCULATOR.name()));
        }

        @Test
        public void testGetCommandsForServer_WithServerNotInDatabase() {
            String serverId = "nonExistentServerId";
            when(discordServerRepositoryMock.findById(serverId)).thenReturn(Optional.empty());

            Map<String, List<JdaSlashCommand>> result = slashCommandService.getCommandsForServer(serverId);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void testRegisterAndUnregisterMultipleBots() {
        JdaSlashCommandService spyService = spy(slashCommandService);
        doNothing().when(spyService).updateCommandsForAllServers();

        JdaSlashCommand musicCommand = createMockCommand();
        JdaSlashCommand todoCommand = createMockCommand();
        JdaSlashCommand gradeCommand = createMockCommand();

        // Register multiple bots
        spyService.registerCommandsForBot(SlashCommandBotType.MUSIC, List.of(musicCommand));
        spyService.registerCommandsForBot(SlashCommandBotType.TODO, List.of(todoCommand));
        spyService.registerCommandsForBot(SlashCommandBotType.GRADE_CALCULATOR, List.of(gradeCommand));

        assertEquals(1, spyService.getCommandsForBotType(SlashCommandBotType.MUSIC.name()).size());
        assertEquals(1, spyService.getCommandsForBotType(SlashCommandBotType.TODO.name()).size());
        assertEquals(1, spyService.getCommandsForBotType(SlashCommandBotType.GRADE_CALCULATOR.name()).size());
        assertEquals(3, spyService.getBotTypeCount());

        // Unregister one bot
        spyService.unregisterCommandsForBot(SlashCommandBotType.TODO);

        assertEquals(1, spyService.getCommandsForBotType(SlashCommandBotType.MUSIC.name()).size());
        assertEquals(0, spyService.getCommandsForBotType(SlashCommandBotType.TODO.name()).size());
        assertEquals(1, spyService.getCommandsForBotType(SlashCommandBotType.GRADE_CALCULATOR.name()).size());
        assertEquals(2, spyService.getBotTypeCount());

        verify(commandListenerMock).removeCommands(List.of(todoCommand));
        verify(spyService, times(4)).updateCommandsForAllServers(); // 3 registers + 1 unregister
    }
}