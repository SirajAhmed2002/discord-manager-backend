package ch.zhaw.it.pm4.discordmanagerbe.bots.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackSchedulerTest {

    @Mock
    private AudioPlayer mockPlayer;

    @Mock
    private AudioTrack mockTrack;

    private TrackScheduler trackScheduler;

    @BeforeEach
    void setUp() {
        trackScheduler = new TrackScheduler(mockPlayer);
    }

    @Test
    void queue_shouldPlayImmediately_whenPlayerCanStartTrack() {
        when(mockPlayer.startTrack(mockTrack, true)).thenReturn(true);

        trackScheduler.queue(mockTrack);

        verify(mockPlayer).startTrack(mockTrack, true);
        assertTrue(trackScheduler.getQueue().isEmpty());
    }

    @Test
    void queue_shouldAddToQueue_whenPlayerCannotStartTrack() {
        when(mockPlayer.startTrack(mockTrack, true)).thenReturn(false);

        trackScheduler.queue(mockTrack);

        verify(mockPlayer).startTrack(mockTrack, true);
        assertEquals(1, trackScheduler.getQueue().size());
        assertTrue(trackScheduler.getQueue().contains(mockTrack));
    }

    @Test
    void nextTrack_shouldStartNextTrackFromQueue() {
        when(mockPlayer.startTrack(mockTrack, true)).thenReturn(false);
        trackScheduler.queue(mockTrack);

        trackScheduler.nextTrack();

        verify(mockPlayer).startTrack(mockTrack, false);
        assertTrue(trackScheduler.getQueue().isEmpty());
    }

    @Test
    void nextTrack_shouldDoNothing_whenQueueIsEmpty() {
        trackScheduler.nextTrack();

        verify(mockPlayer).startTrack(null, false);
    }

    @Test
    void onTrackEnd_shouldPlayNextTrack_whenReasonMayStartNext() {
        when(mockPlayer.startTrack(mockTrack, true)).thenReturn(false);
        trackScheduler.queue(mockTrack);

        trackScheduler.onTrackEnd(mockPlayer, mock(AudioTrack.class), AudioTrackEndReason.FINISHED);

        verify(mockPlayer).startTrack(mockTrack, false);
        assertTrue(trackScheduler.getQueue().isEmpty());
    }

    @Test
    void onTrackEnd_shouldNotPlayNextTrack_whenReasonCannotStartNext() {
        AudioTrackEndReason reason = AudioTrackEndReason.STOPPED;

        trackScheduler.onTrackEnd(mockPlayer, mockTrack, reason);

        verify(mockPlayer, never()).startTrack(any(), eq(false));
    }

    @Test
    void clearQueue_shouldRemoveAllTracksFromQueue() {
        when(mockPlayer.startTrack(any(), eq(true))).thenReturn(false);
        trackScheduler.queue(mockTrack);
        trackScheduler.queue(mock(AudioTrack.class));

        trackScheduler.clearQueue();

        assertTrue(trackScheduler.getQueue().isEmpty());
    }

    @Test
    void getQueue_shouldReturnQueue() {
        when(mockPlayer.startTrack(any(), eq(true))).thenReturn(false);
        AudioTrack track1 = mock(AudioTrack.class);
        AudioTrack track2 = mock(AudioTrack.class);
        trackScheduler.queue(track1);
        trackScheduler.queue(track2);

        BlockingQueue<AudioTrack> queue = trackScheduler.getQueue();

        assertEquals(2, queue.size());
        assertTrue(queue.contains(track1));
        assertTrue(queue.contains(track2));
    }

    @Test
    void queue_multipleTracksProcessedCorrectly() {
        AudioTrack track1 = mock(AudioTrack.class);
        AudioTrack track2 = mock(AudioTrack.class);
        AudioTrack track3 = mock(AudioTrack.class);

        when(mockPlayer.startTrack(track1, true)).thenReturn(true);
        when(mockPlayer.startTrack(track2, true)).thenReturn(false);
        when(mockPlayer.startTrack(track3, true)).thenReturn(false);

        trackScheduler.queue(track1);
        trackScheduler.queue(track2);
        trackScheduler.queue(track3);

        verify(mockPlayer).startTrack(track1, true);
        verify(mockPlayer).startTrack(track2, true);
        verify(mockPlayer).startTrack(track3, true);
        assertEquals(2, trackScheduler.getQueue().size());
        assertTrue(trackScheduler.getQueue().contains(track2));
        assertTrue(trackScheduler.getQueue().contains(track3));
    }

    @Test
    void nextTrack_playsTracksInQueueOrder() {
        AudioTrack track1 = mock(AudioTrack.class);
        AudioTrack track2 = mock(AudioTrack.class);
        AudioTrack track3 = mock(AudioTrack.class);

        when(mockPlayer.startTrack(any(), eq(true))).thenReturn(false);

        trackScheduler.queue(track1);
        trackScheduler.queue(track2);
        trackScheduler.queue(track3);

        trackScheduler.nextTrack();
        verify(mockPlayer).startTrack(track1, false);

        trackScheduler.nextTrack();
        verify(mockPlayer).startTrack(track2, false);

        trackScheduler.nextTrack();
        verify(mockPlayer).startTrack(track3, false);

        assertTrue(trackScheduler.getQueue().isEmpty());
    }
}