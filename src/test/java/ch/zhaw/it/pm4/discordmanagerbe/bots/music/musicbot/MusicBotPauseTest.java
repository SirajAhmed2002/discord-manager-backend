package ch.zhaw.it.pm4.discordmanagerbe.bots.music.musicbot;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.music.MusicBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.music.MusicManager;
import ch.zhaw.it.pm4.discordmanagerbe.bots.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.entities.Message;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MusicBotPauseTest {

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
    private ReplyCallbackAction replyAction;

    @Mock
    private Guild guild;

    @Mock
    private MusicManager musicManager;

    @Mock
    private AudioPlayer audioPlayer;

    @Mock
    private TrackScheduler trackScheduler;

    @Mock
    private AudioTrack audioTrack;

    @Mock
    private WebhookMessageCreateAction<Message> webhookAction;

    private MusicBot musicBot;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(event.getGuild()).thenReturn(guild);
        when(event.deferReply()).thenReturn(replyAction);
        when(event.getHook()).thenReturn(interactionHook);
        when(interactionHook.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(webhookAction);

        musicBot = new MusicBot(jdaBean, slashCommandService, slashCommandListener);

        Field musicManagerField = MusicBot.class.getDeclaredField("musicManager");
        musicManagerField.setAccessible(true);
        musicManagerField.set(musicBot, musicManager);

        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);
        when(audioTrack.getInfo()).thenReturn(new AudioTrackInfo("Test Song", "Test Artist", 180000, "abc123", false, "https://example.com"));
    }

    @Test
    void testHandlePause() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioPlayer.isPaused()).thenReturn(false);

        Method handlePauseMethod = MusicBot.class.getDeclaredMethod("handlePause", SlashCommandInteractionEvent.class);
        handlePauseMethod.setAccessible(true);
        handlePauseMethod.invoke(musicBot, event);

        verify(event).deferReply();

        verify(audioPlayer).isPaused();

        verify(audioPlayer).setPaused(true);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("⏸️ Musik pausiert", sentEmbed.getTitle());
        assertEquals("Die Musik wurde pausiert.", sentEmbed.getDescription());
    }

    @Test
    void testHandlePauseAlreadyPaused() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioPlayer.isPaused()).thenReturn(true);

        Method handlePauseMethod = MusicBot.class.getDeclaredMethod("handlePause", SlashCommandInteractionEvent.class);
        handlePauseMethod.setAccessible(true);
        handlePauseMethod.invoke(musicBot, event);

        verify(event).deferReply();

        verify(audioPlayer).isPaused();

        verify(audioPlayer, never()).setPaused(anyBoolean());

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("⏸️ Bereits pausiert", sentEmbed.getTitle());
        assertEquals("Die Musik ist bereits pausiert.", sentEmbed.getDescription());
    }
}