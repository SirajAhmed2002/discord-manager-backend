package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.TranscriptionProcessor;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the transcription process of recorded audio files.
 * Handles asynchronous transcription and cleanup of temporary resources.
 */
public class TranscriptionManager {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(TranscriptionManager.class);

    /** Processor responsible for handling the transcription of audio files. */
    private final TranscriptionProcessor transcriptionProcessor;

    /** Manager for handling temporary resources during the transcription process. */
    private final TemporaryResourceManager tempResourceManager;

    /**
     * Creates a new transcription manager with the specified transcription processor.
     * Initializes a new temporary resource manager.
     *
     * @param transcriptionProcessor The processor to use for transcription
     */
    public TranscriptionManager(TranscriptionProcessor transcriptionProcessor) {
        this.transcriptionProcessor = transcriptionProcessor;
        this.tempResourceManager = new TemporaryResourceManager();
    }

    /**
     * Creates a new transcription manager with the specified components.
     *
     * @param transcriptionProcessor The processor to use for transcription
     * @param tempResourceManager The manager for temporary resources
     */
    public TranscriptionManager(TranscriptionProcessor transcriptionProcessor,
                                TemporaryResourceManager tempResourceManager) {
        this.transcriptionProcessor = transcriptionProcessor;
        this.tempResourceManager = tempResourceManager;
    }

    /**
     * Starts an asynchronous transcription process.
     * The results will be sent to the specified text channel when complete.
     *
     * @param textChannel Discord text channel to send results to
     * @param tempDir Directory containing temporary files
     * @param sequenceFiles Map of sequence IDs to their corresponding audio files
     * @param timelineFile CSV file containing timeline information
     */
    public void startAsyncTranscription(
            MessageChannel textChannel,
            File tempDir,
            Map<Integer, File> sequenceFiles,
            File timelineFile) {

        CompletableFuture.runAsync(() -> {
            try {
                processTranscription(textChannel, tempDir, sequenceFiles, timelineFile);
            } catch (Exception e) {
                handleTranscriptionError(textChannel, tempDir, e);
            }
        });
    }

    /**
     * Processes the transcription and cleans up temporary resources.
     *
     * @param textChannel Discord text channel to send results to
     * @param tempDir Directory containing temporary files
     * @param sequenceFiles Map of sequence IDs to their corresponding audio files
     * @param timelineFile CSV file containing timeline information
     */
    private void processTranscription(
            MessageChannel textChannel,
            File tempDir,
            Map<Integer, File> sequenceFiles,
            File timelineFile) {

        transcriptionProcessor.transcribeTemporaryFiles(timelineFile, sequenceFiles, textChannel);
        tempResourceManager.cleanupTempDirectory(tempDir);
        logger.info("Transcription completed and temporary files cleaned up");
    }

    private void handleTranscriptionError(MessageChannel textChannel, File tempDir, Exception e) {
        logger.error("Error during transcription process", e);
        textChannel.sendMessage("Error creating transcription: " + e.getMessage()).queue();
        tempResourceManager.cleanupTempDirectory(tempDir);
    }
}
