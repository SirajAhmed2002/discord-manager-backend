package ch.zhaw.it.pm4.discordmanagerbe.bots.music.musicbot;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.music.MusicBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.entities.Message;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MusicBotRegistrationTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private JdaSlashCommandService slashCommandService;

    @Mock
    private JdaEventListenerService slashCommandListener;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private InteractionHook interactionHook;


    @Mock
    private Guild guild;

    @Mock
    private CommandListUpdateAction commandListUpdateAction;

    @Mock
    private WebhookMessageCreateAction<Message> webhookAction;

    private MusicBot musicBot;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(guild.updateCommands()).thenReturn(commandListUpdateAction);
        when(commandListUpdateAction.addCommands(anyList())).thenReturn(commandListUpdateAction);
        when(event.getHook()).thenReturn(interactionHook); // Hook korrekt mit Event verknüpfen

        musicBot = new MusicBot(jdaBean, slashCommandService, slashCommandListener);
    }

    @Test
    void testSetupCommands() throws Exception {
        Method setupCommandsMethod = MusicBot.class.getDeclaredMethod("setupCommands");
        setupCommandsMethod.setAccessible(true);
        assertDoesNotThrow(() -> setupCommandsMethod.invoke(musicBot));
    }

    @Test
    void testRegisterButtonInteractionHandlers() throws Exception {
        Method registerButtonsMethod = MusicBot.class.getDeclaredMethod("registerButtonInteractionHandlers");
        registerButtonsMethod.setAccessible(true);

        assertDoesNotThrow(() -> registerButtonsMethod.invoke(musicBot));
    }

    @Test
    void testRegisterStringInteractionHandlers() throws Exception {
        Method registerStringMethod = MusicBot.class.getDeclaredMethod("registerStringInteractionHandlers");
        registerStringMethod.setAccessible(true);

        assertDoesNotThrow(() -> registerStringMethod.invoke(musicBot));
    }

    @Test
    void testRegisterModalInteractionHandlers() throws Exception {
        Method registerModalMethod = MusicBot.class.getDeclaredMethod("registerModalInteractionHandlers");
        registerModalMethod.setAccessible(true);

        assertDoesNotThrow(() -> registerModalMethod.invoke(musicBot));
    }

    @Test
    void testUnregisterCommands() throws Exception {
        Field executorServiceField = MusicBot.class.getDeclaredField("executorService");
        executorServiceField.setAccessible(true);
        ExecutorService executorService = (ExecutorService) executorServiceField.get(musicBot);

        MusicBot spyMusicBot = spy(musicBot);

        spyMusicBot.unregisterCommands();

        verify(spyMusicBot).unregisterCommands();
        verify(slashCommandService).unregisterCommandsForBot(SlashCommandBotType.MUSIC);

        assertTrue(executorService.isShutdown(), "ExecutorService sollte heruntergefahren sein");
    }

    @Test
    void testCheckGuildAndHookFromGuild() throws Exception {
        when(event.isFromGuild()).thenReturn(true);

        Method checkGuildAndHookMethod = MusicBot.class.getDeclaredMethod("checkGuildAndHook", SlashCommandInteractionEvent.class);
        checkGuildAndHookMethod.setAccessible(true);
        boolean result = (boolean) checkGuildAndHookMethod.invoke(musicBot, event);

        assertTrue(result);

        verify(interactionHook, never()).sendMessage(anyString());
    }

    @Test
    void testCheckGuildAndHookNotFromGuild() throws Exception {
        when(event.isFromGuild()).thenReturn(false);
        when(interactionHook.sendMessage(anyString())).thenReturn(webhookAction);
        when(webhookAction.setEphemeral(true)).thenReturn(webhookAction);

        Method checkGuildAndHookMethod = MusicBot.class.getDeclaredMethod("checkGuildAndHook", SlashCommandInteractionEvent.class);
        checkGuildAndHookMethod.setAccessible(true);
        boolean result = (boolean) checkGuildAndHookMethod.invoke(musicBot, event);

        assertFalse(result);

        verify(interactionHook).sendMessage("Dieser Befehl funktioniert nur auf Servern.");
        verify(webhookAction).setEphemeral(true);
        verify(webhookAction).queue();
    }
}