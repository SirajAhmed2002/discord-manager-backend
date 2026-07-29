package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdaBotFactoryTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private JDA jda;

    @Mock
    private SlashCommandTestBot slashCommandTestBot;

    @Mock
    private ServerTestBot serverTestBot;

    @Mock
    private AnotherSlashCommandBot anotherSlashCommandBot;

    private JdaBotFactory jdaBotFactory;

    @BeforeEach
    void setUp() {
        jdaBotFactory = new JdaBotFactory(context);
    }

    // Test classes for mocking
    @BotIdentifier(category = BotIdentifier.BotCategory.SLASH_COMMAND, slashCommand = SlashCommandBotType.MUSIC)
    static class SlashCommandTestBot extends AbstractJdaBot {
        public SlashCommandTestBot(JDA jdaBean) {
            super(jdaBean);
        }
    }

    @BotIdentifier(category = BotIdentifier.BotCategory.SERVER, server = ServerBotType.GUILD_CONFIG)
    static class ServerTestBot extends AbstractJdaBot {
        public ServerTestBot(JDA jdaBean) {
            super(jdaBean);
        }
    }

    @BotIdentifier(category = BotIdentifier.BotCategory.SLASH_COMMAND, slashCommand = SlashCommandBotType.TODO)
    static class AnotherSlashCommandBot extends AbstractJdaBot {
        public AnotherSlashCommandBot(JDA jdaBean) {
            super(jdaBean);
        }
    }

    static class NotAnnotatedBot extends AbstractJdaBot {
        public NotAnnotatedBot(JDA jdaBean) {
            super(jdaBean);
        }
    }

    // Tests for initialize() method
    @Test
    void initialize_shouldRegisterBotsWithBotIdentifierAnnotation() {
        // Given
        Map<String, Object> annotatedBeans = new HashMap<>();
        SlashCommandTestBot slashBotInstance = new SlashCommandTestBot(jda);
        ServerTestBot serverBotInstance = new ServerTestBot(jda);

        annotatedBeans.put("slashCommandTestBot", slashBotInstance);
        annotatedBeans.put("serverTestBot", serverBotInstance);

        when(context.getBeansWithAnnotation(BotIdentifier.class)).thenReturn(annotatedBeans);

        // When
        jdaBotFactory.initialize();

        // Then
        verify(context).getBeansWithAnnotation(BotIdentifier.class);

        // Verify that bots can be created (indirect verification of registration)
        when(context.getBean(SlashCommandTestBot.class)).thenReturn(slashBotInstance);
        when(context.getBean(ServerTestBot.class)).thenReturn(serverBotInstance);

        JdaBotEntry slashBotEntry = jdaBotFactory.createSlashCommandBot(SlashCommandBotType.MUSIC);
        JdaBotEntry serverBotEntry = jdaBotFactory.createServerBot(ServerBotType.GUILD_CONFIG);

        assertNotNull(slashBotEntry);
        assertNotNull(serverBotEntry);
        assertEquals("MUSIC", slashBotEntry.getIdentifier());
        assertEquals("GUILD_CONFIG", serverBotEntry.getIdentifier());
    }

    @Test
    void initialize_shouldHandleEmptyBeanMap() {
        // Given
        Map<String, Object> emptyBeanMap = new HashMap<>();
        when(context.getBeansWithAnnotation(BotIdentifier.class)).thenReturn(emptyBeanMap);

        // When & Then
        assertDoesNotThrow(() -> jdaBotFactory.initialize());
        verify(context).getBeansWithAnnotation(BotIdentifier.class);
    }

    @Test
    void initialize_shouldIgnoreBeansWithoutBotIdentifierAnnotation() {
        // Given
        Map<String, Object> annotatedBeans = new HashMap<>();
        NotAnnotatedBot notAnnotatedBot = new NotAnnotatedBot(jda);
        annotatedBeans.put("notAnnotatedBot", notAnnotatedBot);

        when(context.getBeansWithAnnotation(BotIdentifier.class)).thenReturn(annotatedBeans);

        // When
        jdaBotFactory.initialize();

        // Then
        verify(context).getBeansWithAnnotation(BotIdentifier.class);

        // Verify that non-annotated bot cannot be created
        assertThrows(IllegalArgumentException.class,
                () -> jdaBotFactory.createSlashCommandBot(SlashCommandBotType.MUSIC));
    }

    // Tests for createSlashCommandBot() method
    @Test
    void createSlashCommandBot_shouldReturnBotEntryForValidBotType() {
        // Given
        setupBotsInFactory();
        SlashCommandTestBot slashBotInstance = new SlashCommandTestBot(jda);
        when(context.getBean(SlashCommandTestBot.class)).thenReturn(slashBotInstance);

        // When
        JdaBotEntry result = jdaBotFactory.createSlashCommandBot(SlashCommandBotType.MUSIC);

        // Then
        assertNotNull(result);
        assertEquals("MUSIC", result.getIdentifier());
        assertEquals(SlashCommandTestBot.class, result.getBotClass());
        assertEquals(slashBotInstance, result.getBotInstance());
        verify(context).getBean(SlashCommandTestBot.class);
    }

    @Test
    void createSlashCommandBot_shouldThrowExceptionForUnknownBotType() {
        // Given
        setupBotsInFactory();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jdaBotFactory.createSlashCommandBot(SlashCommandBotType.TRANSCRIPTION));

        assertTrue(exception.getMessage().contains("No bot found for category: SLASH_COMMAND"));
        verify(context, never()).getBean(any(Class.class));
    }

    // Tests for createServerBot() method
    @Test
    void createServerBot_shouldReturnBotEntryForValidBotType() {
        // Given
        setupBotsInFactory();
        ServerTestBot serverBotInstance = new ServerTestBot(jda);
        when(context.getBean(ServerTestBot.class)).thenReturn(serverBotInstance);

        // When
        JdaBotEntry result = jdaBotFactory.createServerBot(ServerBotType.GUILD_CONFIG);

        // Then
        assertNotNull(result);
        assertEquals("GUILD_CONFIG", result.getIdentifier());
        assertEquals(ServerTestBot.class, result.getBotClass());
        assertEquals(serverBotInstance, result.getBotInstance());
        verify(context).getBean(ServerTestBot.class);
    }

    @Test
    void createServerBot_shouldThrowExceptionForUnknownBotType() {
        // Given
        setupBotsInFactory();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> jdaBotFactory.createServerBot(ServerBotType.GUILD_LIST));

        assertTrue(exception.getMessage().contains("No bot found for category: SERVER"));
        verify(context, never()).getBean(any(Class.class));
    }

    @Test
    void createBots_shouldCreateMultipleDifferentBotTypes() {
        // Given
        setupBotsInFactory();
        SlashCommandTestBot slashBotInstance = new SlashCommandTestBot(jda);
        ServerTestBot serverBotInstance = new ServerTestBot(jda);
        AnotherSlashCommandBot anotherSlashBotInstance = new AnotherSlashCommandBot(jda);

        when(context.getBean(SlashCommandTestBot.class)).thenReturn(slashBotInstance);
        when(context.getBean(ServerTestBot.class)).thenReturn(serverBotInstance);
        when(context.getBean(AnotherSlashCommandBot.class)).thenReturn(anotherSlashBotInstance);

        // When
        JdaBotEntry slashBotEntry = jdaBotFactory.createSlashCommandBot(SlashCommandBotType.MUSIC);
        JdaBotEntry serverBotEntry = jdaBotFactory.createServerBot(ServerBotType.GUILD_CONFIG);
        JdaBotEntry anotherSlashBotEntry = jdaBotFactory.createSlashCommandBot(SlashCommandBotType.TODO);

        // Then
        assertNotNull(slashBotEntry);
        assertNotNull(serverBotEntry);
        assertNotNull(anotherSlashBotEntry);

        assertEquals("MUSIC", slashBotEntry.getIdentifier());
        assertEquals("GUILD_CONFIG", serverBotEntry.getIdentifier());
        assertEquals("TODO", anotherSlashBotEntry.getIdentifier());

        assertEquals(SlashCommandTestBot.class, slashBotEntry.getBotClass());
        assertEquals(ServerTestBot.class, serverBotEntry.getBotClass());
        assertEquals(AnotherSlashCommandBot.class, anotherSlashBotEntry.getBotClass());

        verify(context).getBean(SlashCommandTestBot.class);
        verify(context).getBean(ServerTestBot.class);
        verify(context).getBean(AnotherSlashCommandBot.class);
    }

    @Test
    void createSlashCommandBot_shouldHandleSpringBeanCreationException() {
        // Given
        setupBotsInFactory();
        when(context.getBean(SlashCommandTestBot.class)).thenThrow(new RuntimeException("Spring bean creation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jdaBotFactory.createSlashCommandBot(SlashCommandBotType.MUSIC));

        assertEquals("Spring bean creation failed", exception.getMessage());
        verify(context).getBean(SlashCommandTestBot.class);
    }

    @Test
    void createServerBot_shouldHandleSpringBeanCreationException() {
        // Given
        setupBotsInFactory();
        when(context.getBean(ServerTestBot.class)).thenThrow(new RuntimeException("Spring bean creation failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> jdaBotFactory.createServerBot(ServerBotType.GUILD_CONFIG));

        assertEquals("Spring bean creation failed", exception.getMessage());
        verify(context).getBean(ServerTestBot.class);
    }

    @Test
    void initialize_shouldRegisterBotsBasedOnCategory() {
        // Given
        Map<String, Object> annotatedBeans = new HashMap<>();
        SlashCommandTestBot slashBotInstance = new SlashCommandTestBot(jda);
        ServerTestBot serverBotInstance = new ServerTestBot(jda);
        AnotherSlashCommandBot anotherSlashBotInstance = new AnotherSlashCommandBot(jda);

        annotatedBeans.put("slashCommandTestBot", slashBotInstance);
        annotatedBeans.put("serverTestBot", serverBotInstance);
        annotatedBeans.put("anotherSlashCommandBot", anotherSlashBotInstance);

        when(context.getBeansWithAnnotation(BotIdentifier.class)).thenReturn(annotatedBeans);

        // When
        jdaBotFactory.initialize();

        // Then
        verify(context).getBeansWithAnnotation(BotIdentifier.class);

        // Verify correct registration by testing bot creation
        when(context.getBean(SlashCommandTestBot.class)).thenReturn(slashBotInstance);
        when(context.getBean(ServerTestBot.class)).thenReturn(serverBotInstance);
        when(context.getBean(AnotherSlashCommandBot.class)).thenReturn(anotherSlashBotInstance);

        // Should be able to create all registered bots
        assertDoesNotThrow(() -> jdaBotFactory.createSlashCommandBot(SlashCommandBotType.MUSIC));
        assertDoesNotThrow(() -> jdaBotFactory.createServerBot(ServerBotType.GUILD_CONFIG));
        assertDoesNotThrow(() -> jdaBotFactory.createSlashCommandBot(SlashCommandBotType.TODO));
    }

    // Helper method to setup bots in factory for testing
    private void setupBotsInFactory() {
        Map<String, Object> annotatedBeans = new HashMap<>();
        SlashCommandTestBot slashBotInstance = new SlashCommandTestBot(jda);
        ServerTestBot serverBotInstance = new ServerTestBot(jda);
        AnotherSlashCommandBot anotherSlashBotInstance = new AnotherSlashCommandBot(jda);

        annotatedBeans.put("slashCommandTestBot", slashBotInstance);
        annotatedBeans.put("serverTestBot", serverBotInstance);
        annotatedBeans.put("anotherSlashCommandBot", anotherSlashBotInstance);

        lenient().when(context.getBeansWithAnnotation(BotIdentifier.class)).thenReturn(annotatedBeans);

        // Initialize the factory
        jdaBotFactory.initialize();
    }
}