package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SpeechSequenceEntry;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.UserSpeechSequence;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages speech sequences for multiple users during a recording session.
 * Handles the creation, tracking, and finalization of audio sequences.
 */
@Component
public class SequenceManager {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(SequenceManager.class);

    /** Map for managing speech sequences for multiple users during a recording session. */
    private final Map<String, UserSpeechSequence> userSpeechSequences = new ConcurrentHashMap<>();

    /** List for storing all finalized speech sequence entries. */
    private final List<SpeechSequenceEntry> sequenceEntries = Collections.synchronizedList(new ArrayList<>());

    /** Counter for generating unique sequence IDs. */
    private final AtomicInteger sequenceIdCounter = new AtomicInteger(1);

    /**
     * Resets all sequence data and counters.
     * Clears all user speech sequences, sequence entries, and resets the ID counter.
     */
    public void reset() {
        userSpeechSequences.clear();
        sequenceEntries.clear();
        sequenceIdCounter.set(1);
        logger.info("SequenceManager reset - all sequences cleared and ID counter reset");
    }

    /**
     * Generates and returns the next unique sequence ID.
     *
     * @return A unique integer ID for a new sequence
     */
    public int getNextSequenceId() {
        return sequenceIdCounter.getAndIncrement();
    }

    /**
     * Processes incoming audio data for a specific user.
     * Creates a new sequence if the user was inactive for longer than the silence threshold.
     *
     * @param userId User identifier
     * @param username User's display name
     * @param currentTimeMillis Current timestamp in milliseconds
     * @param audioData Raw audio data bytes
     * @param silenceThresholdMs Duration in milliseconds to consider silence as a sequence break
     */
    public void processAudioData(String userId, String username, long currentTimeMillis,
                                 byte[] audioData, long silenceThresholdMs) {
        UserSpeechSequence sequence = getUserSpeechSequence(userId, username);

        if (sequence.isInactive(currentTimeMillis, silenceThresholdMs)) {
            finalizeSequence(sequence);
            sequence.startNewSequence(currentTimeMillis);
        }

        sequence.addAudioData(audioData, currentTimeMillis);
    }

    /**
     * Finalizes all active user speech sequences.
     * Should be called when recording ends to ensure all sequences are properly stored.
     */
    public void finalizeAllSequences() {
        for (UserSpeechSequence sequence : userSpeechSequences.values()) {
            finalizeSequence(sequence);
        }
    }

    /**
     * Retrieves or creates a speech sequence for a user.
     *
     * @param userId User identifier
     * @param username User's display name
     * @return The user's speech sequence object
     */
    private UserSpeechSequence getUserSpeechSequence(String userId, String username) {
        return userSpeechSequences.computeIfAbsent(userId,
                id -> {
                    UserSpeechSequence sequence = new UserSpeechSequence(userId, username);
                    // Setze die Sequenz-ID auf einen garantiert einzigartigen Wert
                    sequence.setCustomSequenceIdGenerator(this::getNextSequenceId);
                    return sequence;
                });
    }

    /**
     * Finalizes a speech sequence and adds it to the sequence entries list.
     * Creates a SpeechSequenceEntry with the audio data and metadata.
     *
     * @param sequence The sequence to finalize
     */
    public void finalizeSequence(UserSpeechSequence sequence) {
        if (sequence == null || !sequence.hasData()) return;

        byte[] audioData = sequence.getAudioBuffer().toByteArray();

        SpeechSequenceEntry entry = new SpeechSequenceEntry(
                sequence.getSequenceId(),
                sequence.getUserId(),
                sequence.getUsername(),
                sequence.getStartTime(),
                sequence.getLastUpdateTime(),
                audioData
        );

        sequenceEntries.add(entry);

        logger.info("Finalized speech sequence {} for user {} ({}) with duration {}ms",
                sequence.getSequenceId(), sequence.getUsername(), sequence.getUserId(),
                entry.getEndTime() - entry.getStartTime());
    }

    /**
     * Returns a copy of all finalized speech sequence entries.
     *
     * @return List of all speech sequence entries
     */
    public List<SpeechSequenceEntry> getSequenceEntries() {
        return new ArrayList<>(sequenceEntries);
    }
}
