package ch.zhaw.it.pm4.discordmanagerbe.bots.music;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages the audio players and their associated schedulers for each guild.
 * It handles loading and playing audio tracks from various sources, including YouTube.
 */
public class MusicManager {
    private static final Logger logger = LoggerFactory.getLogger(MusicManager.class);
    
    private final AudioPlayerManager playerManager;
    private final YoutubeAudioSourceManager youtubeAudioSourceManager;
    private final Map<Long, AudioPlayer> players = new ConcurrentHashMap<>();
    private final Map<Long, TrackScheduler> schedulers = new ConcurrentHashMap<>();
    private static final int DEFAULT_VOLUME = 75;

    /**
     * Constructor for the MusicManager class.
     * Initializes the audio player manager and registers the necessary source managers.
     */
    public MusicManager() {
        playerManager = new DefaultAudioPlayerManager();
        youtubeAudioSourceManager = new YoutubeAudioSourceManager();
        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    /**
     * Gets or creates an audio player for the specified guild ID.
     * @param guildId The ID of the guild.
     * @return The audio player associated with the guild.
     */
    private AudioPlayer getOrCreatePlayer(long guildId) {
        return players.computeIfAbsent(guildId, id -> {
            AudioPlayer player = playerManager.createPlayer();
            player.setVolume(DEFAULT_VOLUME);
            TrackScheduler scheduler = new TrackScheduler(player);
            player.addListener(scheduler);
            schedulers.put(id, scheduler);
            return player;
        });
    }

    /**
     * Gets or creates a track scheduler for the specified guild ID.
     * @param guildId The ID of the guild.
     * @return The track scheduler associated with the guild.
     */
    private TrackScheduler getOrCreateScheduler(long guildId) {
        getOrCreatePlayer(guildId); // Ensures scheduler exists
        return schedulers.get(guildId);
    }

    /**
     * Loads and plays a track from the specified URL.
     * @param guildId The ID of the guild.
     * @param url The URL of the track to load and play.
     */
    public void loadAndPlay(long guildId, String url) {
        if (!url.startsWith("http")) {
            logger.warn("Invalid URL: {}", url);
            return;
        }
        
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            // Called when the track is loaded successfully
            /**
             * This method is called when a track is successfully loaded.
             * It sets the position to 0 and queues the track for playback.
             * @param track The loaded audio track.
             */
            @Override
            public void trackLoaded(AudioTrack track) {
                logger.info("Track loaded: {}", track.getInfo().title);
                track.setPosition(0);
                getOrCreateScheduler(guildId).queue(track);
            }

            // Called when a playlist is loaded successfully
            /**
             * This method is called when a playlist is successfully loaded.
             * It checks if the playlist is a search result or a regular playlist,
             * and queues the tracks accordingly.
             * @param playlist The loaded audio playlist.
             */
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    AudioTrack firstTrack = playlist.getTracks().get(0);
                    logger.info("Search result loaded: {}", firstTrack.getInfo().title);
                    getOrCreateScheduler(guildId).queue(firstTrack);
                    return;
                }
                logger.info("Playlist loaded: {} with {} tracks", playlist.getName(), playlist.getTracks().size());
                for (AudioTrack track : playlist.getTracks()) {
                    getOrCreateScheduler(guildId).queue(track);
                }
            }

            // Called when no matches are found for the provided URL

            /**
             * This method is called when no matches are found for the provided URL.
             * It logs a warning message indicating that no matches were found.
             */
            @Override
            public void noMatches() {
                logger.warn("No matches found for: {}", url);
            }

            // Called when an error occurs while loading the track
            /**
             * This method is called when an error occurs while loading the track.
             * It logs the error message and stack trace for debugging purposes.
             * @param e The exception that occurred during loading.
             */
            @Override
            public void loadFailed(FriendlyException e) {
                logger.error("Failed to load: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Gets the audio send handler for the specified guild ID.
     * @param guildId The ID of the guild.
     * @return The audio send handler for the guild.
     */
    public AudioSendHandler getSendHandler(long guildId) {
        return new AudioPlayerSendHandler(getOrCreatePlayer(guildId));
    }

    /**
     * Gets the audio player for the specified guild ID.
     * @param guildId The ID of the guild.
     * @return The audio player for the guild.
     */
    public AudioPlayer getPlayer(long guildId) {
        return getOrCreatePlayer(guildId);
    }

    /**
     * Gets the track scheduler for the specified guild ID.
     * @param guildId The ID of the guild.
     * @return The track scheduler for the guild.
     */
    public TrackScheduler getTrackScheduler(long guildId) {
        return getOrCreateScheduler(guildId);
    }
    
    /**
     * Shuts down the music manager and cleans up all resources.
     * This method should be called when the bot is shutting down.
     */
    public void shutdown() {
        logger.info("Shutting down MusicManager");
        
        // Stop all players and clear their queues
        for (Map.Entry<Long, AudioPlayer> entry : players.entrySet()) {
            AudioPlayer player = entry.getValue();
            player.stopTrack();
            player.destroy();
        }
        
        // Clear all schedulers
        for (TrackScheduler scheduler : schedulers.values()) {
            scheduler.clearQueue();
        }
        
        // Clear maps
        players.clear();
        schedulers.clear();
        
        // Shutdown player manager
        playerManager.shutdown();
        
        logger.info("MusicManager shutdown complete");
    }
    
    /**
     * Removes a specific guild's player and scheduler.
     * This can be used when a bot leaves a guild.
     * @param guildId The ID of the guild.
     */
    public void removeGuildPlayer(long guildId) {
        AudioPlayer player = players.remove(guildId);
        if (player != null) {
            player.stopTrack();
            player.destroy();
        }
        
        TrackScheduler scheduler = schedulers.remove(guildId);
        if (scheduler != null) {
            scheduler.clearQueue();
        }
        
        logger.debug("Removed player and scheduler for guild: {}", guildId);
    }
}