package ch.zhaw.it.pm4.discordmanagerbe.bots.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is responsible for managing the audio track queue and handling track events.
 * It extends AudioEventAdapter to listen for track end events and manage the playback queue.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    /**
     * Constructor for TrackScheduler.
     * @param player The AudioPlayer instance to manage.
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Queues an audio track for playback.
     * If the player is not currently playing a track, the queued track will be played immediately.
     * @param track The audio track to queue.
     */
    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Plays the next track in the queue.
     * If there are no tracks in the queue, nothing happens.
     */
    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    /**
     * Called when a track ends.
     * If the track ended normally and the next track can be started, it will be played.
     * @param player The AudioPlayer instance.
     * @param track The audio track that ended.
     * @param endReason The reason why the track ended.
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    /**
     * Clears the track queue.
     * This will remove all tracks from the queue.
     */
    public void clearQueue() {
        queue.clear();
    }

    /**
     * Gets the current queue of audio tracks.
     * @return The queue of audio tracks.
     */
    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }
}