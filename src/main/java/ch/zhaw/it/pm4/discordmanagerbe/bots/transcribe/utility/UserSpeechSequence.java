package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Manages the ongoing speech sequence for a single user with buffer management.
 */
public class UserSpeechSequence {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(UserSpeechSequence.class);

    /** Discord ID of the user. */
    private final String userId;

    /** Discord username of the user. */
    private final String username;

    /** Timestamp when the sequence started. */
    private long startTime;

    /** Timestamp of the last audio data update. */
    private long lastUpdateTime;

    /** Buffer for storing accumulated audio data. */
    private ByteArrayOutputStream audioBuffer;

    /** Identifier for the current sequence. */
    private int sequenceId;

    /** Generator for creating unique sequence IDs. */
    private Supplier<Integer> sequenceIdGenerator;

    /**
     * Creates a new speech sequence tracker for a user.
     *
     * @param userId User's Discord ID
     * @param username User's Discord username
     */
    public UserSpeechSequence(String userId, String username) {
        this.userId = userId;
        this.username = username;
        this.audioBuffer = new ByteArrayOutputStream();
        this.sequenceIdGenerator = () -> this.sequenceId + 1;
    }

    public void setCustomSequenceIdGenerator(Supplier<Integer> generator) {
        this.sequenceIdGenerator = generator;
    }

    /**
     * Starts a new speech sequence with a unique ID.
     *
     * @param timestamp Start timestamp for the sequence
     */
    public void startNewSequence(long timestamp) {
        this.startTime = timestamp;
        this.lastUpdateTime = timestamp;
        this.audioBuffer = new ByteArrayOutputStream();
        this.sequenceId = sequenceIdGenerator.get();
    }

    /**
     * Adds audio data to the current sequence buffer.
     *
     * @param data Raw audio data bytes to add
     * @param timestamp Current timestamp for the update
     */
    public void addAudioData(byte[] data, long timestamp) {
        try {
            audioBuffer.write(data);
            lastUpdateTime = timestamp;
        } catch (IOException e) {
            logger.error("Error writing audio data", e);
        }
    }

    /**
     * Checks if this sequence is inactive based on the time threshold.
     *
     * @param currentTime Current timestamp to check against
     * @param threshold Inactivity threshold in milliseconds
     * @return True if the sequence is inactive or not started
     */
    public boolean isInactive(long currentTime, long threshold) {
        return sequenceId == -1 || (currentTime - lastUpdateTime) > threshold;
    }

    /**
     * @return True if the audio buffer contains data
     */
    public boolean hasData() {
        return audioBuffer.size() > 0;
    }

    /**
     * @return The user's Discord ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return The user's Discord username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return The current sequence identifier
     */
    public int getSequenceId() {
        return sequenceId;
    }

    /**
     * @return Timestamp when the sequence started
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return Timestamp of the last audio data update
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * @return The audio buffer containing all accumulated audio data
     */
    public ByteArrayOutputStream getAudioBuffer() {
        return audioBuffer;
    }
}