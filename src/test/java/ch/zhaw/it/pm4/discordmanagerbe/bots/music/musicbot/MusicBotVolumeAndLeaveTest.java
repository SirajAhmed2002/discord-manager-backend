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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
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

public class MusicBotVolumeAndLeaveTest {

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
    private AudioManager audioManager;

    @Mock
    private MusicManager musicManager;

    @Mock
    private AudioPlayer audioPlayer;

    @Mock
    private TrackScheduler trackScheduler;

    @Mock
    private AudioTrack audioTrack;

    @Mock
    private OptionMapping optionMapping;

    @Mock
    private WebhookMessageCreateAction<Message> webhookAction;

    private MusicBot musicBot;
    private static final String TEST_GUILD_ID = "987654321";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(event.deferReply()).thenReturn(replyAction);
        when(replyAction.setEphemeral(anyBoolean())).thenReturn(replyAction);
        when(event.getHook()).thenReturn(interactionHook);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(guild.getAudioManager()).thenReturn(audioManager);
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
    void testHandleVolume() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getOption("level")).thenReturn(optionMapping);
        when(optionMapping.getAsInt()).thenReturn(50);
        when(audioPlayer.getVolume()).thenReturn(75);

        Method handleVolumeMethod = MusicBot.class.getDeclaredMethod("handleVolume", SlashCommandInteractionEvent.class);
        handleVolumeMethod.setAccessible(true);
        handleVolumeMethod.invoke(musicBot, event);

        verify(event).deferReply();

        verify(audioPlayer).setVolume(50);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("🔊 Lautstärke eingestellt", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Lautstärke: 50%"));
        assertTrue(sentEmbed.getDescription().contains("(vorher: 75%)"));
    }

    @Test
    void testHandleVolumeInvalid() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getOption("level")).thenReturn(optionMapping);
        when(optionMapping.getAsInt()).thenReturn(150);

        Method handleVolumeMethod = MusicBot.class.getDeclaredMethod("handleVolume", SlashCommandInteractionEvent.class);
        handleVolumeMethod.setAccessible(true);
        handleVolumeMethod.invoke(musicBot, event);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("❌ Ungültige Lautstärke", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("zwischen 0 und 100"));
    }

    @Test
    void testHandleLeave() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioManager.isConnected()).thenReturn(true);
        when(audioPlayer.getPlayingTrack()).thenReturn(audioTrack);

        LinkedBlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
        queue.add(mock(AudioTrack.class));
        queue.add(mock(AudioTrack.class));
        when(trackScheduler.getQueue()).thenReturn(queue);

        Method handleLeaveMethod = MusicBot.class.getDeclaredMethod("handleLeave", SlashCommandInteractionEvent.class);
        handleLeaveMethod.setAccessible(true);
        handleLeaveMethod.invoke(musicBot, event);

        verify(event).deferReply();
        verify(audioPlayer).stopTrack();
        verify(trackScheduler).clearQueue();
        verify(audioManager).closeAudioConnection();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());
        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("👋 Bot verlässt den Kanal", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Bot hat den Sprachkanal verlassen"));
    }

    @Test
    void testHandleLeaveNotConnected() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(audioManager.isConnected()).thenReturn(false);

        Method handleLeaveMethod = MusicBot.class.getDeclaredMethod("handleLeave", SlashCommandInteractionEvent.class);
        handleLeaveMethod.setAccessible(true);
        handleLeaveMethod.invoke(musicBot, event);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("❌ Fehler", sentEmbed.getTitle());
        assertEquals("Bot ist in keinem Sprachkanal.", sentEmbed.getDescription());
    }
}