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
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
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

public class MusicBotPlayTest {

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
    private Member member;


    @Mock
    private Guild guild;

    @Mock
    private GuildVoiceState voiceState;

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
    private AudioChannelUnion audioChannel;

    @Mock
    private WebhookMessageCreateAction<Message> webhookAction;

    @Mock
    private WebhookMessageEditAction<Message> editAction; // Statt WebhookMessageCreateAction

    private MusicBot musicBot;
    private static final String TEST_GUILD_ID = "987654321";
    private static final String VALID_URL = "https://www.example.com/music";
    private static final String INVALID_URL = "not_a_url";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(event.deferReply()).thenReturn(replyAction);
        when(event.getHook()).thenReturn(interactionHook);
        when(interactionHook.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(webhookAction);
        when(interactionHook.editOriginalEmbeds(any(MessageEmbed.class))).thenReturn(editAction);
        when(guild.getIdLong()).thenReturn(Long.parseLong(TEST_GUILD_ID));
        when(guild.getAudioManager()).thenReturn(audioManager);
        when(event.getGuild()).thenReturn(guild);
        when(event.getMember()).thenReturn(member);
        when(member.getVoiceState()).thenReturn(voiceState);
        when(voiceState.getChannel()).thenReturn(audioChannel);
        when(event.getOption("url")).thenReturn(optionMapping);

        musicBot = new MusicBot(jdaBean, slashCommandService, slashCommandListener);

        Field musicManagerField = MusicBot.class.getDeclaredField("musicManager");
        musicManagerField.setAccessible(true);
        musicManagerField.set(musicBot, musicManager);

        when(musicManager.getPlayer(anyLong())).thenReturn(audioPlayer);
        when(musicManager.getTrackScheduler(anyLong())).thenReturn(trackScheduler);
        when(musicManager.getSendHandler(anyLong())).thenReturn(null);
        when(audioTrack.getInfo()).thenReturn(new AudioTrackInfo("Test Song", "Test Artist", 180000, "abc123", false, "https://example.com"));
    }

    @Test
    void testHandlePlaySuccess() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(optionMapping.getAsString()).thenReturn(VALID_URL);
        when(voiceState.inAudioChannel()).thenReturn(true);

        Method handlePlayMethod = MusicBot.class.getDeclaredMethod("handlePlay", SlashCommandInteractionEvent.class);
        handlePlayMethod.setAccessible(true);
        handlePlayMethod.invoke(musicBot, event);

        verify(event).deferReply();
        verify(optionMapping).getAsString();
        verify(voiceState).inAudioChannel();
        verify(audioManager).openAudioConnection(audioChannel);
        verify(audioManager).setSendingHandler(any());
        verify(musicManager).loadAndPlay(eq(Long.parseLong(TEST_GUILD_ID)), eq(VALID_URL));

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("⏳ Lade Musik", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains(VALID_URL));
    }

    @Test
    void testHandlePlayInvalidUrl() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(optionMapping.getAsString()).thenReturn(INVALID_URL);

        Method handlePlayMethod = MusicBot.class.getDeclaredMethod("handlePlay", SlashCommandInteractionEvent.class);
        handlePlayMethod.setAccessible(true);
        handlePlayMethod.invoke(musicBot, event);

        verify(event).deferReply();
        verify(optionMapping).getAsString();
        verify(musicManager, never()).loadAndPlay(anyLong(), anyString());

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("🌐 Ungültige URL", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("gültige HTTP/HTTPS URL"));
    }

    @Test
    void testHandlePlayNotInVoiceChannel() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(optionMapping.getAsString()).thenReturn(VALID_URL);
        when(voiceState.inAudioChannel()).thenReturn(false);

        Method handlePlayMethod = MusicBot.class.getDeclaredMethod("handlePlay", SlashCommandInteractionEvent.class);
        handlePlayMethod.setAccessible(true);
        handlePlayMethod.invoke(musicBot, event);

        verify(event).deferReply();
        verify(optionMapping).getAsString();
        verify(voiceState).inAudioChannel();
        verify(musicManager, never()).loadAndPlay(anyLong(), anyString());

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("❌ Fehler", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("in einem Sprachkanal sein"));
    }

    @Test
    void testHandlePlayNoGuild() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getOption("url")).thenReturn(optionMapping);
        when(optionMapping.getAsString()).thenReturn("https://example.com/music.mp3");
        when(member.getVoiceState()).thenReturn(voiceState);
        when(voiceState.inAudioChannel()).thenReturn(true);
        when(event.getGuild()).thenReturn(null);

        Method handlePlayMethod = MusicBot.class.getDeclaredMethod("handlePlay", SlashCommandInteractionEvent.class);
        handlePlayMethod.setAccessible(true);
        handlePlayMethod.invoke(musicBot, event);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("❌ Fehler", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("auf Servern"));
    }

    @Test
    void testHandlePlayNullMember() throws Exception {
        when(event.isFromGuild()).thenReturn(true);
        when(optionMapping.getAsString()).thenReturn(VALID_URL);
        when(event.getMember()).thenReturn(null);

        Method handlePlayMethod = MusicBot.class.getDeclaredMethod("handlePlay", SlashCommandInteractionEvent.class);
        handlePlayMethod.setAccessible(true);
        handlePlayMethod.invoke(musicBot, event);

        verify(event).deferReply();
        verify(optionMapping).getAsString();
        verify(musicManager, never()).loadAndPlay(anyLong(), anyString());

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).sendMessageEmbeds(embedCaptor.capture());

        MessageEmbed sentEmbed = embedCaptor.getValue();
        assertEquals("❌ Fehler", sentEmbed.getTitle());
        assertTrue(sentEmbed.getDescription().contains("Mitglied nicht identifizieren"));
    }
}