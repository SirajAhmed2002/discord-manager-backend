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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MusicBotNowPlayingTest{

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
    private static final String TEST_USER_ID = "123456789";
    private static final String TEST_GUILD_ID = "987654321";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(event.getGuild()).thenReturn(guild);
        when(event.deferReply()).thenReturn(replyAction);
        when(replyAction.setEphemeral(anyBoolean())).thenReturn(replyAction);
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
    void testHandleNowPlayingNoTrack() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(audioPlayer.getPlayingTrack()).thenReturn(null);

        BlockingQueue<AudioTrack> emptyQueue = new LinkedBlockingQueue<>();
        when(trackScheduler.getQueue()).thenReturn(emptyQueue);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);

        Method handleNowPlayingMethod = MusicBot.class.getDeclaredMethod("handleNowPlaying", SlashCommandInteractionEvent.class);
        handleNowPlayingMethod.setAccessible(true);
        handleNowPlayingMethod.invoke(musicBot, event);

        verify(event).deferReply();

        Thread.sleep(100);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, timeout(1000)).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("🎵 Nichts spielt", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Es wird gerade keine Musik abgespielt."));
    }

    @Test
    void testHandleNowPlayingWithTrack() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);
        when(audioPlayer.isPaused()).thenReturn(false);
        when(audioPlayer.getPlayingTrack().getPosition()).thenReturn(30000L);

        AudioTrackInfo trackInfo = new AudioTrackInfo(
                "Test Song",
                "Test Artist",
                300000,
                "someTrackId",
                false,
                "https://example.com"
        );
        when(audioTrack.getInfo()).thenReturn(trackInfo);

        BlockingQueue<AudioTrack> emptyQueue = new LinkedBlockingQueue<>();
        when(trackScheduler.getQueue()).thenReturn(emptyQueue);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);

        Method handleNowPlayingMethod = MusicBot.class.getDeclaredMethod("handleNowPlaying", SlashCommandInteractionEvent.class);
        handleNowPlayingMethod.setAccessible(true);
        handleNowPlayingMethod.invoke(musicBot, event);

        verify(event).deferReply();
        Thread.sleep(100);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, timeout(1000)).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("🎵 Aktueller Song", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Test Song"));

        boolean hasArtistField = false;
        for (MessageEmbed.Field field : sentEmbed.getFields()) {
            if (field.getName().equals("Künstler") && field.getValue().equals("Test Artist")) {
                hasArtistField = true;
                break;
            }
        }
        assertTrue(hasArtistField);
    }

    @Test
    void testHandleNowPlayingPausedTrack() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);
        when(audioPlayer.isPaused()).thenReturn(true);
        when(audioPlayer.getPlayingTrack().getPosition()).thenReturn(60000L);

        BlockingQueue<AudioTrack> emptyQueue = new LinkedBlockingQueue<>();
        when(trackScheduler.getQueue()).thenReturn(emptyQueue);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);

        Method handleNowPlayingMethod = MusicBot.class.getDeclaredMethod("handleNowPlaying", SlashCommandInteractionEvent.class);
        handleNowPlayingMethod.setAccessible(true);
        handleNowPlayingMethod.invoke(musicBot, event);

        verify(event).deferReply();

        Thread.sleep(100);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, timeout(1000)).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("🎵 Aktueller Song", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Test Song"));
    }

    @Test
    void testHandleNowPlayingWithException() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));

        when(musicManager.getPlayer(anyLong())).thenThrow(new RuntimeException("Test exception"));

        Method handleNowPlayingMethod = MusicBot.class.getDeclaredMethod("handleNowPlaying", SlashCommandInteractionEvent.class);
        handleNowPlayingMethod.setAccessible(true);
        handleNowPlayingMethod.invoke(musicBot, event);

        verify(event).deferReply();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, timeout(1000)).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("❌ Fehler", sentEmbed.getTitle());
        assertEquals("Konnte aktuelle Wiedergabe nicht abrufen.", sentEmbed.getDescription());
    }
}