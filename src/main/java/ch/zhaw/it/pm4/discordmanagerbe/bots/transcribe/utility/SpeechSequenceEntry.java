package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility;

/**
 * Represents a speech sequence entry with audio data for processing.
 */
public class SpeechSequenceEntry {

    /** Unique identifier for the sequence. */
    private final int sequenceId;

    /** Discord ID of the user. */
    private final String userId;

    /** Discord username of the user. */
    private final String username;

    /** Timestamp when the sequence started. */
    private final long startTime;

    /** Timestamp when the sequence ended. */
    private final long endTime;

    /** Raw audio data in bytes. */
    private final byte[] audioData;

    /**
     * Creates a new speech sequence entry.
     *
     * @param sequenceId Unique identifier for the sequence
     * @param userId User's Discord ID
     * @param username User's Discord username
     * @param startTime Sequence start timestamp
     * @param endTime Sequence end timestamp
     * @param audioData Raw audio data bytes
     */
    public SpeechSequenceEntry(int sequenceId, String userId, String username,
                               long startTime, long endTime, byte[] audioData) {
        this.sequenceId = sequenceId;
        this.userId = userId;
        this.username = username;
        this.startTime = startTime;
        this.endTime = endTime;
        this.audioData = audioData;
    }

    /**
     * @return The sequence identifier
     */
    public int getSequenceId() {
        return sequenceId;
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
     * @return Timestamp when the sequence started
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return Timestamp when the sequence ended
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @return The raw audio data bytes
     */
    public byte[] getAudioData() {
        return audioData;
    }
}