package ch.zhaw.it.pm4.discordmanagerbe.bots.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * This class is responsible for sending audio data to Discord.
 * It implements the AudioSendHandler interface from JDA.
 * The class uses the AudioPlayer from Lavaplayer to provide audio frames.
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    /**
     * Constructor for AudioPlayerSendHandler.
     * @param audioPlayer The AudioPlayer instance from Lavaplayer.
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    /**
     * Checks if the handler can provide audio data.
     * @return true if audio data is available, false otherwise.
     */
    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    /**
     * Provides the audio data to Discord.
     * @return A ByteBuffer containing the audio data.
     */
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    /**
     * Checks if the audio data is Opus encoded.
     * @return true if the audio data is Opus encoded, false otherwise.
     */
    @Override
    public boolean isOpus() {
        return true;
    }
}