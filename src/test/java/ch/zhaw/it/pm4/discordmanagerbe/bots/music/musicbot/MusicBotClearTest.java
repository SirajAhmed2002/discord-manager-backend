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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MusicBotClearTest {

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
    private static final String TEST_GUILD_ID = "987654321";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(event.deferReply()).thenReturn(replyAction);
        when(event.getHook()).thenReturn(interactionHook);
        when(interactionHook.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(webhookAction);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(event.getGuild()).thenReturn(guild);

        musicBot = new MusicBot(jdaBean, slashCommandService, slashCommandListener);

        Field musicManagerField = MusicBot.class.getDeclaredField("musicManager");
        musicManagerField.setAccessible(true);
        musicManagerField.set(musicBot, musicManager);

        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);
        when(audioTrack.getInfo()).thenReturn(new AudioTrackInfo("Test Song", "Test Artist", 180000, "abc123", false, "https://example.com"));
    }


    @Test
    void testHandleClearWithCurrentTrack() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);

        BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
        queue.add(mock(AudioTrack.class));
        queue.add(mock(AudioTrack.class));
        queue.add(mock(AudioTrack.class));
        when(trackScheduler.getQueue()).thenReturn(queue);

        Method handleClearMethod = MusicBot.class.getDeclaredMethod("handleClear", SlashCommandInteractionEvent.class);
        handleClearMethod.setAccessible(true);
        handleClearMethod.invoke(musicBot, event);

        verify(event).deferReply();

        verify(audioPlayer).stopTrack();

        verify(trackScheduler).clearQueue();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("🗑️ Warteschlange geleert", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Aktueller Song gestoppt und 3 Tracks"));
    }

    @Test
    void testHandleClearNoCurrentTrack() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioPlayer.getPlayingTrack()).thenReturn(null); // Kein aktueller Track

        BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
        for (int i = 0; i < 5; i++) {
            queue.add(mock(AudioTrack.class));
        }
        when(trackScheduler.getQueue()).thenReturn(queue);

        Method handleClearMethod = MusicBot.class.getDeclaredMethod("handleClear", SlashCommandInteractionEvent.class);
        handleClearMethod.setAccessible(true);
        handleClearMethod.invoke(musicBot, event);

        verify(event).deferReply();

        verify(audioPlayer).stopTrack();

        verify(trackScheduler).clearQueue();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("🗑️ Warteschlange geleert", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("5 Tracks wurden aus der Warteschlange entfernt"));
    }
}