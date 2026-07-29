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
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MusicBotQueueTest {

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
    void testHandleQueue() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);

        LinkedBlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
        AudioTrack track1 = mock(AudioTrack.class);
        AudioTrack track2 = mock(AudioTrack.class);

        AudioTrackInfo info1 = new AudioTrackInfo("Track 1", "Artist 1", 120000, "id1", false, "url1");
        AudioTrackInfo info2 = new AudioTrackInfo("Track 2", "Artist 2", 180000, "id2", false, "url2");
        when(track1.getInfo()).thenReturn(info1);
        when(track2.getInfo()).thenReturn(info2);

        queue.add(track1);
        queue.add(track2);

        when(trackScheduler.getQueue()).thenReturn(queue);

        Method handleQueueMethod = MusicBot.class.getDeclaredMethod("handleQueue", SlashCommandInteractionEvent.class);
        handleQueueMethod.setAccessible(true);
        handleQueueMethod.invoke(musicBot, event);

        verify(event).deferReply();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("📋 Musikwarteschlange", sentEmbed.getTitle());

        String description = sentEmbed.getDescription();
        assertTrue(description.contains("Aktuell:"));
        assertTrue(description.contains("Test Song"));
        assertTrue(description.contains("Track 1"));
        assertTrue(description.contains("Track 2"));
    }

    @Test
    void testHandleQueueEmpty() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioPlayer.getPlayingTrack()).thenReturn(null);

        when(trackScheduler.getQueue()).thenReturn(new LinkedBlockingQueue<>());

        Method handleQueueMethod = MusicBot.class.getDeclaredMethod("handleQueue", SlashCommandInteractionEvent.class);
        handleQueueMethod.setAccessible(true);
        handleQueueMethod.invoke(musicBot, event);

        verify(event).deferReply();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("📋 Musikwarteschlange", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Die Warteschlange ist leer"));
    }

    @Test
    void testHandleQueueMoreThanTenTracks() throws Exception {
        when(event.isFromGuild()).thenReturn(true);

        AudioTrackInfo currentTrackInfo = new AudioTrackInfo("Track Current", "Artist Current", 180000, "idCurrent", false, "urlCurrent");
        when(audioTrack.getInfo()).thenReturn(currentTrackInfo);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);

        LinkedBlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
        for (int i = 1; i <= 15; i++) {
            AudioTrack track = mock(AudioTrack.class);
            AudioTrackInfo info = new AudioTrackInfo("Track " + i, "Artist " + i, 120000, "id" + i, false, "url" + i);
            when(track.getInfo()).thenReturn(info);
            queue.add(track);
        }

        when(trackScheduler.getQueue()).thenReturn(queue);

        Method handleQueueMethod = MusicBot.class.getDeclaredMethod("handleQueue", SlashCommandInteractionEvent.class);
        handleQueueMethod.setAccessible(true);
        handleQueueMethod.invoke(musicBot, event);

        verify(event).deferReply();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("📋 Musikwarteschlange", sentEmbed.getTitle());

        String description = sentEmbed.getDescription();
        assertTrue(description.contains("... und 5 weitere Tracks"),
                "Die Nachricht sollte einen Hinweis auf 5 weitere Tracks enthalten");

        int trackCountInOutput = countOccurrences(description, "Track ");
        assertEquals(11, trackCountInOutput, "Es sollten 10 Tracks in der Queue und 1 aktueller Track sein");
    }

    @Test
    void testHandleNowPlayingWithQueueInfo() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);

        LinkedBlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
        queue.add(mock(AudioTrack.class));
        queue.add(mock(AudioTrack.class));
        when(trackScheduler.getQueue()).thenReturn(queue);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);

        Method handleNowPlayingMethod = MusicBot.class.getDeclaredMethod("handleNowPlaying", SlashCommandInteractionEvent.class);
        handleNowPlayingMethod.setAccessible(true);
        handleNowPlayingMethod.invoke(musicBot, event);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, timeout(1000)).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        boolean hasQueueField = false;
        for (MessageEmbed.Field field : sentEmbed.getFields()) {
            if (field.getName().equals("Warteschlange")) {
                hasQueueField = true;
                assertTrue(field.getValue().contains("2 Song(s) in der Warteschlange"));
                break;
            }
        }
        assertTrue(hasQueueField, "Das Feld 'Warteschlange' sollte vorhanden sein");
    }

    @Test
    void testHandleNowPlayingWithEmptyQueue() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);

        LinkedBlockingQueue<AudioTrack> emptyQueue = new LinkedBlockingQueue<>();
        when(trackScheduler.getQueue()).thenReturn(emptyQueue);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);

        Method handleNowPlayingMethod = MusicBot.class.getDeclaredMethod("handleNowPlaying", SlashCommandInteractionEvent.class);
        handleNowPlayingMethod.setAccessible(true);
        handleNowPlayingMethod.invoke(musicBot, event);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, timeout(1000)).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        boolean hasQueueField = false;
        for (MessageEmbed.Field field : sentEmbed.getFields()) {
            if (field.getName().equals("Warteschlange")) {
                hasQueueField = true;
                break;
            }
        }
        assertFalse(hasQueueField, "Das Feld 'Warteschlange' sollte nicht vorhanden sein");
    }

    // Hilfsmethode zum Zählen von Vorkommen eines Teilstrings
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}