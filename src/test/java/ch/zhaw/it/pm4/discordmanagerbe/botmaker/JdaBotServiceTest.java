package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdaBotServiceTest {

    @Mock
    private JdaBotFactory jdaBotFactory;

    @InjectMocks
    private JdaBotService jdaBotService;

    @Mock
    private JdaBotEntry slashCommandBotEntry;

    @Mock
    private JdaBotEntry serverBotEntry;

    @BeforeEach
    void setup() throws Exception {
        // Initialize botList HashSet via reflection
        Field botListField = JdaBotService.class.getDeclaredField("botList");
        botListField.setAccessible(true);
        botListField.set(jdaBotService, new HashSet<>());

        // Mock Setup für SlashCommand Bot
        lenient().when(slashCommandBotEntry.getIdentifier()).thenReturn("MUSIC");
        lenient().when(jdaBotFactory.createSlashCommandBot(SlashCommandBotType.MUSIC))
                .thenReturn(slashCommandBotEntry);

        // Mock Setup für Server Bot
        lenient().when(serverBotEntry.getIdentifier()).thenReturn("GUILD_CONFIG");
        lenient().when(jdaBotFactory.createServerBot(ServerBotType.GUILD_CONFIG))
                .thenReturn(serverBotEntry);

        // Setup all bot mocks
        setupAllBotMocks();
    }

    @Test
    @DisplayName("instantiateSlashCommandBot: Soll SlashCommand Bot erfolgreich instanziieren")
    void testInstantiateSlashCommandBot() {
        // Act
        jdaBotService.instantiateSlashCommandBot(SlashCommandBotType.MUSIC);

        // Assert
        verify(jdaBotFactory, times(1)).createSlashCommandBot(SlashCommandBotType.MUSIC);

        // Verify bot is added to list
        Optional<JdaBotEntry> retrievedBot = jdaBotService.getBot(SlashCommandBotType.MUSIC);
        assertTrue(retrievedBot.isPresent(), "SlashCommand Bot sollte nach Instanziierung verfügbar sein");
        assertEquals("MUSIC", retrievedBot.get().getIdentifier());
    }

    @Test
    @DisplayName("instantiateServerBot: Soll Server Bot erfolgreich instanziieren")
    void testInstantiateServerBot() {
        // Act
        jdaBotService.instantiateServerBot(ServerBotType.GUILD_CONFIG);

        // Assert
        verify(jdaBotFactory, times(1)).createServerBot(ServerBotType.GUILD_CONFIG);

        // Verify bot is added to list
        Optional<JdaBotEntry> retrievedBot = jdaBotService.getBot(ServerBotType.GUILD_CONFIG);
        assertTrue(retrievedBot.isPresent(), "Server Bot sollte nach Instanziierung verfügbar sein");
        assertEquals("GUILD_CONFIG", retrievedBot.get().getIdentifier());
    }

    @Test
    @DisplayName("getBot (SlashCommandBotType): Soll Bot zurückgeben wenn vorhanden")
    void testGetSlashCommandBot_Found() {
        // Arrange
        jdaBotService.instantiateSlashCommandBot(SlashCommandBotType.MUSIC);

        // Act
        Optional<JdaBotEntry> result = jdaBotService.getBot(SlashCommandBotType.MUSIC);

        // Assert
        assertTrue(result.isPresent(), "Bot sollte gefunden werden");
        assertEquals("MUSIC", result.get().getIdentifier());
    }

    @Test
    @DisplayName("getBot (ServerBotType): Soll Bot zurückgeben wenn vorhanden")
    void testGetServerBot_Found() {
        // Arrange
        jdaBotService.instantiateServerBot(ServerBotType.GUILD_CONFIG);

        // Act
        Optional<JdaBotEntry> result = jdaBotService.getBot(ServerBotType.GUILD_CONFIG);

        // Assert
        assertTrue(result.isPresent(), "Bot sollte gefunden werden");
        assertEquals("GUILD_CONFIG", result.get().getIdentifier());
    }

    @Test
    @DisplayName("getBot (SlashCommandBotType): Soll Optional.empty() zurückgeben wenn Bot nicht vorhanden")
    void testGetSlashCommandBot_NotFound() {
        // Act
        Optional<JdaBotEntry> result = jdaBotService.getBot(SlashCommandBotType.MUSIC);

        // Assert
        assertFalse(result.isPresent(), "Optional sollte leer sein wenn Bot nicht vorhanden");
    }

    @Test
    @DisplayName("getBot (ServerBotType): Soll Optional.empty() zurückgeben wenn Bot nicht vorhanden")
    void testGetServerBot_NotFound() {
        // Act
        Optional<JdaBotEntry> result = jdaBotService.getBot(ServerBotType.GUILD_CONFIG);

        // Assert
        assertFalse(result.isPresent(), "Optional sollte leer sein wenn Bot nicht vorhanden");
    }

    @Test
    @DisplayName("getAllSlashCommandBots: Soll alle SlashCommand Bots zurückgeben")
    void testGetAllSlashCommandBots() {
        // Arrange - Setup additional mock
        JdaBotEntry transcriptionBot = mock(JdaBotEntry.class);
        when(transcriptionBot.getIdentifier()).thenReturn("TRANSCRIPTION");
        when(jdaBotFactory.createSlashCommandBot(SlashCommandBotType.TRANSCRIPTION))
                .thenReturn(transcriptionBot);

        jdaBotService.instantiateSlashCommandBot(SlashCommandBotType.MUSIC);
        jdaBotService.instantiateSlashCommandBot(SlashCommandBotType.TRANSCRIPTION);
        jdaBotService.instantiateServerBot(ServerBotType.GUILD_CONFIG); // Server bot to test filtering

        // Act
        Set<JdaBotEntry> slashCommandBots = jdaBotService.getAllSlashCommandBots();

        // Assert
        assertEquals(2, slashCommandBots.size(), "Sollte genau 2 SlashCommand Bots zurückgeben");
        assertTrue(slashCommandBots.stream().anyMatch(bot -> "MUSIC".equals(bot.getIdentifier())));
        assertTrue(slashCommandBots.stream().anyMatch(bot -> "TRANSCRIPTION".equals(bot.getIdentifier())));
        assertFalse(slashCommandBots.stream().anyMatch(bot -> "GUILD_CONFIG".equals(bot.getIdentifier())));
    }

    @Test
    @DisplayName("getAllServerBots: Soll alle Server Bots zurückgeben")
    void testGetAllServerBots() {
        // Arrange - Setup additional mock
        JdaBotEntry guildListBot = mock(JdaBotEntry.class);
        when(guildListBot.getIdentifier()).thenReturn("GUILD_LIST");
        when(jdaBotFactory.createServerBot(ServerBotType.GUILD_LIST))
                .thenReturn(guildListBot);

        jdaBotService.instantiateServerBot(ServerBotType.GUILD_CONFIG);
        jdaBotService.instantiateServerBot(ServerBotType.GUILD_LIST);
        jdaBotService.instantiateSlashCommandBot(SlashCommandBotType.MUSIC); // SlashCommand bot to test filtering

        // Act
        Set<JdaBotEntry> serverBots = jdaBotService.getAllServerBots();

        // Assert
        assertEquals(2, serverBots.size(), "Sollte genau 2 Server Bots zurückgeben");
        assertTrue(serverBots.stream().anyMatch(bot -> "GUILD_CONFIG".equals(bot.getIdentifier())));
        assertTrue(serverBots.stream().anyMatch(bot -> "GUILD_LIST".equals(bot.getIdentifier())));
        assertFalse(serverBots.stream().anyMatch(bot -> "MUSIC".equals(bot.getIdentifier())));
    }

    @Test
    @DisplayName("getAllBots: Soll alle Bots (SlashCommand und Server) zurückgeben")
    void testGetAllBots() {
        // Arrange
        jdaBotService.instantiateSlashCommandBot(SlashCommandBotType.MUSIC);
        jdaBotService.instantiateServerBot(ServerBotType.GUILD_CONFIG);

        // Act
        Set<JdaBotEntry> allBots = jdaBotService.getAllBots();

        // Assert
        assertEquals(2, allBots.size(), "Sollte alle Bots (SlashCommand und Server) zurückgeben");
        assertTrue(allBots.stream().anyMatch(bot -> "MUSIC".equals(bot.getIdentifier())));
        assertTrue(allBots.stream().anyMatch(bot -> "GUILD_CONFIG".equals(bot.getIdentifier())));
    }

    @Test
    @DisplayName("initialize: Soll alle Bot-Typen beim Start instanziieren")
    void testInitialize() throws Exception {
        // Arrange - Create fresh service and setup mocks
        setupAllBotMocks();
        JdaBotService freshService = new JdaBotService(jdaBotFactory);

        // Set empty HashSet via reflection to simulate state before initialize
        Field botListField = JdaBotService.class.getDeclaredField("botList");
        botListField.setAccessible(true);
        botListField.set(freshService, new HashSet<>());

        // Act - Call initialize using reflection
        java.lang.reflect.Method initializeMethod = JdaBotService.class.getDeclaredMethod("initialize");
        initializeMethod.setAccessible(true);
        initializeMethod.invoke(freshService);

        // Assert - Verify all SlashCommand bots are created
        verify(jdaBotFactory).createSlashCommandBot(SlashCommandBotType.MUSIC);
        verify(jdaBotFactory).createSlashCommandBot(SlashCommandBotType.TRANSCRIPTION);
        verify(jdaBotFactory).createSlashCommandBot(SlashCommandBotType.TIMETABLE);
        verify(jdaBotFactory).createSlashCommandBot(SlashCommandBotType.GRADE_CALCULATOR);

        // Assert - Verify all Server bots are created
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_CONFIG);
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_LIST);
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_INFO);
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_MEMBER_LIST);
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_INVITE_CREATE);
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_PERMISSION_LIST);
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_ROLES_CONFIG);
        verify(jdaBotFactory).createServerBot(ServerBotType.GUILD_ROLES_LIST);
    }

    private void setupAllBotMocks() {
        // SlashCommand Bot Mocks
        for (SlashCommandBotType type : SlashCommandBotType.values()) {
            if (type != SlashCommandBotType.NONE) {
                JdaBotEntry mockBot = mock(JdaBotEntry.class);
                lenient().when(mockBot.getIdentifier()).thenReturn(type.toString());
                lenient().when(jdaBotFactory.createSlashCommandBot(type)).thenReturn(mockBot);
            }
        }

        // Server Bot Mocks
        for (ServerBotType type : ServerBotType.values()) {
            if (type != ServerBotType.NONE) {
                JdaBotEntry mockBot = mock(JdaBotEntry.class);
                lenient().when(mockBot.getIdentifier()).thenReturn(type.toString());
                lenient().when(jdaBotFactory.createServerBot(type)).thenReturn(mockBot);
            }
        }
    }
}