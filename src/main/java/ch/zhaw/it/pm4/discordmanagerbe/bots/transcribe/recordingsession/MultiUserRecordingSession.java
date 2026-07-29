package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.TranscriptionProcessor;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SpeechSequenceEntry;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Handles recording and processing of audio from multiple users in a Discord voice channel.
 * Implements both AudioReceiveHandler to capture incoming audio and RecordingSession to manage recording lifecycle.
 */
@Scope("prototype")
@Component
public class MultiUserRecordingSession implements AudioReceiveHandler, TranscribeBot.RecordingSession {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(MultiUserRecordingSession.class);

    /** Threshold in milliseconds to detect silence in audio streams. */
    private static final long SILENCE_THRESHOLD_MS = 1500;

    /** The Discord audio manager used for handling audio connections. */
    private AudioManager audioManager;

    /** The Discord voice channel being recorded. */
    private VoiceChannel channel;

    /** Flag indicating whether the session is currently recording. */
    private boolean isRecording;

    /** The start time of the recording session in milliseconds since epoch. */
    private final long startTimeMillis;

    /** Manager for handling and processing speech sequences. */
    private final SequenceManager sequenceManager;

    /** Manager for saving and handling audio files. */
    private final AudioFileManager audioFileManager;

    /** Manager for handling transcription processes. */
    private final TranscriptionManager transcriptionManager;

    /** Manager for handling temporary resources during the session. */
    private final TemporaryResourceManager tempResourceManager;

    /**
     * Creates a new recording session with required dependencies.
     *
     * @param transcriptionProcessor Processor for audio transcription
     * @param sequenceManager Manager for handling speech sequences
     * @param audioFileManager Manager for audio file operations
     * @param tempResourceManager Manager for temporary resources
     */
    @Autowired
    public MultiUserRecordingSession(
            TranscriptionProcessor transcriptionProcessor,
            SequenceManager sequenceManager,
            AudioFileManager audioFileManager,
            TemporaryResourceManager tempResourceManager) {
        this.isRecording = true;
        this.startTimeMillis = System.currentTimeMillis();
        this.sequenceManager = sequenceManager;
        this.audioFileManager = audioFileManager;
        this.tempResourceManager = tempResourceManager;
        this.transcriptionManager = new TranscriptionManager(transcriptionProcessor);
    }

    /**
     * Sets the audio manager for this recording session.
     *
     * @param audioManager The Discord audio manager to use
     */
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Sets the voice channel for this recording session.
     *
     * @param channel The Discord voice channel to record from
     */
    public void setChannel(VoiceChannel channel) {
        this.channel = channel;
    }

    /**
     * Stops the recording session and initiates the transcription process.
     *
     * @param textChannel The text channel to send transcription results to
     */
    @Override
    public void stopRecording(MessageChannel textChannel) {
        stopAudioRecording();

        try {
            sequenceManager.finalizeAllSequences();

            File tempDir = tempResourceManager.createTemporaryDirectory();
            List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();

            Map<Integer, File> sequenceFiles = audioFileManager.saveTemporaryAudioFiles(entries, tempDir);
            File timelineFile = audioFileManager.createTemporaryTimelineFile(entries, tempDir);

            transcriptionManager.startAsyncTranscription(textChannel, tempDir, sequenceFiles, timelineFile);

            sequenceManager.reset();

        } catch (Exception e) {
            logger.error("Error processing recording", e);
        }
    }

    /**
     * Stops the audio recording by disabling the receive handler.
     */
    private void stopAudioRecording() {
        isRecording = false;
        audioManager.setReceivingHandler(null);
    }

    /**
     * Indicates whether the handler can receive user audio.
     *
     * @return Always returns true to receive audio from all users
     */
    @Override
    public boolean canReceiveUser() {
        return true;
    }

    /**
     * Processes incoming audio data from a user.
     *
     * @param userAudio Audio data received from a user
     */
    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        if (!shouldProcessAudio(userAudio)) return;

        String userId = userAudio.getUser().getId();
        byte[] audioData = userAudio.getAudioData(1.0);

        if (isValidAudioData(audioData)) {
            processAudioForUser(userId, audioData);
        }
    }

    /**
     * Determines if the audio from a user should be processed.
     *
     * @param userAudio Audio data to evaluate
     * @return True if the audio should be processed, false otherwise
     */
    private boolean shouldProcessAudio(@NotNull UserAudio userAudio) {
        if (!isRecording) return false;
        User user = userAudio.getUser();
        return !user.isBot();
    }

    /**
     * Checks if the audio data is valid for processing.
     *
     * @param audioData Audio byte array to validate
     * @return True if the audio data is valid and non-empty
     */
    private boolean isValidAudioData(byte[] audioData) {
        return audioData != null && audioData.length > 0;
    }

    /**
     * Processes audio data for a specific user.
     *
     * @param userId ID of the user who sent the audio
     * @param audioData The audio data to process
     */
    private void processAudioForUser(String userId, byte[] audioData) {
        long currentTimeMillis = System.currentTimeMillis() - startTimeMillis;

        String username = "Unknown";
        Member member = findMemberById(userId);
        if (member != null) {
            username = member.getEffectiveName();
        }

        sequenceManager.processAudioData(userId, username, currentTimeMillis, audioData, SILENCE_THRESHOLD_MS);
    }

    /**
     * Finds a member in the current voice channel by their user ID.
     *
     * @param userId ID of the user to find
     * @return The Member object if found, null otherwise
     */
    private Member findMemberById(String userId) {
        return channel.getMembers().stream()
                .filter(member -> member.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Handles combined audio data from multiple users.
     * Currently not implemented as individual user audio is processed instead.
     *
     * @param combinedAudio The combined audio data
     */
    @Override
    public void handleCombinedAudio(@NotNull CombinedAudio combinedAudio) {}
}