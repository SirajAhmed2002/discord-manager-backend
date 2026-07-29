package ch.zhaw.it.pm4.discordmanagerbe.bots.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MusicManagerTest {

    @Mock
    private AudioPlayerManager mockPlayerManager;

    @Mock
    private AudioPlayer mockPlayer;

    @Mock
    private AudioTrack mockTrack;

    @Mock
    private AudioPlaylist mockPlaylist;


    private MusicManager musicManager;
    private static final long TEST_GUILD_ID = 123456789L;
    private static final String TEST_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
    private static final String INVALID_URL = "not-a-valid-url";

    @BeforeEach
    void setUp() throws Exception {
        musicManager = new MusicManager();

        Field playerManagerField = MusicManager.class.getDeclaredField("playerManager");
        playerManagerField.setAccessible(true);
        playerManagerField.set(musicManager, mockPlayerManager);

        lenient().when(mockPlayerManager.createPlayer()).thenReturn(mockPlayer);

        AudioTrackInfo trackInfo = new AudioTrackInfo(
                "Test Track",
                "Test Artist",
                60000,
                "identifier123",
                false,
                "https://example.com/track"
        );
        lenient().when(mockTrack.getInfo()).thenReturn(trackInfo);
    }

    @Test
    void testConstructor() {
        MusicManager newManager = new MusicManager();
        assertNotNull(newManager);
    }

    @Test
    void testGetPlayer_CreatesNewPlayer() {
        AudioPlayer player = musicManager.getPlayer(TEST_GUILD_ID);

        assertNotNull(player);
        verify(mockPlayerManager).createPlayer();
        verify(mockPlayer).setVolume(75); // DEFAULT_VOLUME
        verify(mockPlayer).addListener(any(TrackScheduler.class));
    }

    @Test
    void testGetPlayer_ReturnsSamePlayerForSameGuild() {
        AudioPlayer player1 = musicManager.getPlayer(TEST_GUILD_ID);
        AudioPlayer player2 = musicManager.getPlayer(TEST_GUILD_ID);

        assertSame(player1, player2);
        verify(mockPlayerManager, times(1)).createPlayer();
    }

    @Test
    void testGetPlayer_CreatesDifferentPlayersForDifferentGuilds() {
        long guildId2 = 987654321L;
        AudioPlayer mockPlayer2 = mock(AudioPlayer.class);
        when(mockPlayerManager.createPlayer())
                .thenReturn(mockPlayer)
                .thenReturn(mockPlayer2);

        AudioPlayer player1 = musicManager.getPlayer(TEST_GUILD_ID);
        AudioPlayer player2 = musicManager.getPlayer(guildId2);

        assertNotSame(player1, player2);
        verify(mockPlayerManager, times(2)).createPlayer();
    }

    @Test
    void testGetTrackScheduler_ReturnsScheduler() {
        TrackScheduler scheduler = musicManager.getTrackScheduler(TEST_GUILD_ID);

        assertNotNull(scheduler);
        verify(mockPlayerManager).createPlayer();
    }

    @Test
    void testGetSendHandler_ReturnsAudioSendHandler() {
        AudioSendHandler handler = musicManager.getSendHandler(TEST_GUILD_ID);

        assertNotNull(handler);
        assertInstanceOf(AudioPlayerSendHandler.class, handler);
    }

    @Test
    void testLoadAndPlay_ValidUrl_TrackLoaded() {
        ArgumentCaptor<AudioLoadResultHandler> handlerCaptor = ArgumentCaptor.forClass(AudioLoadResultHandler.class);

        musicManager.loadAndPlay(TEST_GUILD_ID, TEST_URL);

        verify(mockPlayerManager).loadItem(eq(TEST_URL), handlerCaptor.capture());

        AudioLoadResultHandler handler = handlerCaptor.getValue();
        handler.trackLoaded(mockTrack);

        verify(mockTrack).setPosition(0);
    }

    @Test
    void testLoadAndPlay_ValidUrl_PlaylistLoaded() {
        ArgumentCaptor<AudioLoadResultHandler> handlerCaptor = ArgumentCaptor.forClass(AudioLoadResultHandler.class);
        List<AudioTrack> tracks = Arrays.asList(mockTrack);
        when(mockPlaylist.getTracks()).thenReturn(tracks);
        when(mockPlaylist.isSearchResult()).thenReturn(false);

        musicManager.loadAndPlay(TEST_GUILD_ID, TEST_URL);

        verify(mockPlayerManager).loadItem(eq(TEST_URL), handlerCaptor.capture());

        AudioLoadResultHandler handler = handlerCaptor.getValue();
        handler.playlistLoaded(mockPlaylist);

        verify(mockPlaylist, times(2)).getTracks();
    }

    @Test
    void testLoadAndPlay_ValidUrl_SearchResultLoaded() {
        ArgumentCaptor<AudioLoadResultHandler> handlerCaptor = ArgumentCaptor.forClass(AudioLoadResultHandler.class);
        List<AudioTrack> tracks = Arrays.asList(mockTrack);

        when(mockPlaylist.isSearchResult()).thenReturn(true);
        when(mockPlaylist.getTracks()).thenReturn(tracks);

        musicManager.loadAndPlay(TEST_GUILD_ID, TEST_URL);

        verify(mockPlayerManager).loadItem(eq(TEST_URL), handlerCaptor.capture());

        AudioLoadResultHandler handler = handlerCaptor.getValue();
        handler.playlistLoaded(mockPlaylist);

        verify(mockPlaylist).getTracks();
    }

    @Test
    void testLoadAndPlay_ValidUrl_NoMatches() {
        ArgumentCaptor<AudioLoadResultHandler> handlerCaptor = ArgumentCaptor.forClass(AudioLoadResultHandler.class);

        musicManager.loadAndPlay(TEST_GUILD_ID, TEST_URL);

        verify(mockPlayerManager).loadItem(eq(TEST_URL), handlerCaptor.capture());

        AudioLoadResultHandler handler = handlerCaptor.getValue();
        assertDoesNotThrow(() -> handler.noMatches());
    }

    @Test
    void testLoadAndPlay_ValidUrl_LoadFailed() {
        ArgumentCaptor<AudioLoadResultHandler> handlerCaptor = ArgumentCaptor.forClass(AudioLoadResultHandler.class);
        FriendlyException exception = new FriendlyException("Test error",
                FriendlyException.Severity.COMMON, null);

        musicManager.loadAndPlay(TEST_GUILD_ID, TEST_URL);

        verify(mockPlayerManager).loadItem(eq(TEST_URL), handlerCaptor.capture());

        AudioLoadResultHandler handler = handlerCaptor.getValue();
        assertDoesNotThrow(() -> handler.loadFailed(exception));
    }

    @Test
    void testLoadAndPlay_InvalidUrl_DoesNotLoad() {
        musicManager.loadAndPlay(TEST_GUILD_ID, INVALID_URL);

        verify(mockPlayerManager, never()).loadItem(anyString(), any(AudioLoadResultHandler.class));
    }

    @Test
    void testRemoveGuildPlayer_RemovesPlayerAndScheduler() throws Exception {
        musicManager.getPlayer(TEST_GUILD_ID);

        musicManager.removeGuildPlayer(TEST_GUILD_ID);

        verify(mockPlayer).stopTrack();
        verify(mockPlayer).destroy();

        AudioPlayer newPlayer = musicManager.getPlayer(TEST_GUILD_ID);
        assertNotNull(newPlayer);
        verify(mockPlayerManager, times(2)).createPlayer();
    }

    @Test
    void testRemoveGuildPlayer_NonExistentGuild_DoesNotThrow() {
        assertDoesNotThrow(() -> musicManager.removeGuildPlayer(999L));
    }

    @Test
    void testShutdown_CleansUpAllResources(){
        musicManager.getPlayer(TEST_GUILD_ID);
        musicManager.getPlayer(456L);

        musicManager.shutdown();

        verify(mockPlayer, atLeast(2)).stopTrack();
        verify(mockPlayer, atLeast(2)).destroy();
        verify(mockPlayerManager).shutdown();
    }

    @Test
    void testMultipleGuilds_IndependentPlayers() {
        long guildId1 = 111L;
        long guildId2 = 222L;
        AudioPlayer mockPlayer2 = mock(AudioPlayer.class);

        when(mockPlayerManager.createPlayer())
                .thenReturn(mockPlayer)
                .thenReturn(mockPlayer2);

        AudioPlayer player1 = musicManager.getPlayer(guildId1);
        AudioPlayer player2 = musicManager.getPlayer(guildId2);

        assertNotSame(player1, player2);
        verify(mockPlayerManager, times(2)).createPlayer();
        verify(mockPlayer).setVolume(75);
        verify(mockPlayer2).setVolume(75);
    }

    @Test
    void testConcurrentAccess_ThreadSafe() throws InterruptedException {
        final int threadCount = 10;
        final long[] guildIds = new long[threadCount];
        for (int i = 0; i < threadCount; i++) {
            guildIds[i] = i + 1000L;
        }

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                AudioPlayer player = musicManager.getPlayer(guildIds[index]);
                assertNotNull(player);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        verify(mockPlayerManager, times(threadCount)).createPlayer();
    }
}