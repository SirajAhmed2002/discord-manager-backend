package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility;

/**
 * Represents a timeline entry for a speech sequence with metadata for visualization.
 */
public class SequenceTimelineEntry {

    /** Eindeutige Kennung der Sequenz.*/
    private final int sequenceId;

    /** Discord-ID des Benutzers. */
    private final String userId;

    /** Discord-Benutzername des Benutzers. */
    private final String username;

    /** Zeitstempel, wann die Sequenz gestartet wurde. */
    private final long startTime;

    /** Zeitstempel, wann die Sequenz beendet wurde. */
    private final long endTime;

    /** Größe der Audiodaten in Bytes. */
    private final int audioDataSize;

    /** Name der Audiodatei. */
    private final String audioFileName;

    /**
     * Creates a new timeline entry for speech sequence visualization.
     *
     * @param sequenceId Unique identifier for the sequence
     * @param userId User's Discord ID
     * @param username User's Discord username
     * @param startTime Sequence start timestamp
     * @param endTime Sequence end timestamp
     * @param audioDataSize Size of audio data in bytes
     * @param audioFileName Name of the audio file
     */
    public SequenceTimelineEntry(int sequenceId, String userId, String username,
                                 long startTime, long endTime, int audioDataSize, String audioFileName) {
        this.sequenceId = sequenceId;
        this.userId = userId;
        this.username = username;
        this.startTime = startTime;
        this.endTime = endTime;
        this.audioDataSize = audioDataSize;
        this.audioFileName = audioFileName;
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
     * @return Size of the audio data in bytes
     */
    public int getAudioDataSize() {
        return audioDataSize;
    }

    /**
     * @return Name of the audio file
     */
    public String getAudioFileName() {
        return audioFileName;
    }
}
