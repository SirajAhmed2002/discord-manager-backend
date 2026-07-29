package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SpeechSequenceEntry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the creation and storage of audio files for speech transcription.
 * Handles converting and saving audio data from Discord voice channels to WAV format.
 */
@Component
public class AudioFileManager {

    /** logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(AudioFileManager.class);

    /** Audio format for Opus (Discord) to PCM (WAV) */
    private static final AudioFormat OUTPUT_FORMAT = new AudioFormat(
            48000.0f,     // Sample Rate
            16,                     // Sample Size in Bits
            2,                      // Channels
            true,                   // Signed
            true                    // Little Endian (for PCM)
    );

    /**
     * Saves audio data from speech sequence entries to temporary WAV files.
     *
     * @param entries List of speech sequence entries containing audio data
     * @param tempDir Directory where temporary files will be stored
     * @return Map of sequence IDs to their corresponding audio files
     * @throws Exception If an error occurs during file creation or audio processing
     */
    public Map<Integer, File> saveTemporaryAudioFiles(List<SpeechSequenceEntry> entries, File tempDir) throws Exception {
        Map<Integer, File> result = new LinkedHashMap<>();

        for (SpeechSequenceEntry entry : entries) {
            if (entry.getAudioData().length > 0) {
                Integer sequenceId = entry.getSequenceId();

                String filePrefix = "seq_" + sequenceId;
                if (result.containsKey(sequenceId)) {
                    logger.warn("Duplicate sequence ID found: {}. Creating unique filename.", sequenceId);
                    filePrefix = "seq_" + sequenceId + "_dup_" + System.nanoTime();
                }

                File tempFile = File.createTempFile(
                        filePrefix + "_" + sanitizeFilename(entry.getUsername()) + "_",
                        ".wav",
                        tempDir);
                tempFile.deleteOnExit();

                saveAudioToFile(entry.getAudioData(), tempFile);
                logger.debug("Saved temporary audio file: {}", tempFile.getAbsolutePath());

                result.put(sequenceId, tempFile);
            }
        }

        return result;
    }

    /**
     * Creates a CSV file containing timeline information for speech sequences.
     *
     * @param entries List of speech sequence entries to include in the timeline
     * @param tempDir Directory where the temporary timeline file will be stored
     * @return The created timeline CSV file
     * @throws IOException If an error occurs during file creation or writing
     */
    public File createTemporaryTimelineFile(List<SpeechSequenceEntry> entries, File tempDir) throws IOException {
        File timelineFile = File.createTempFile("timeline_", ".csv", tempDir);
        timelineFile.deleteOnExit();

        try (PrintWriter writer = new PrintWriter(new FileWriter(timelineFile))) {
            writer.println("SequenceId,UserId,Username,StartTime,EndTime,DurationMs,AudioDataSize");

            entries.stream()
                    .sorted(Comparator.comparingLong(SpeechSequenceEntry::getStartTime))
                    .forEach(entry -> writer.printf("%d,%s,%s,%d,%d,%d,%d%n",
                            entry.getSequenceId(),
                            entry.getUserId(),
                            entry.getUsername(),
                            entry.getStartTime(),
                            entry.getEndTime(),
                            entry.getEndTime() - entry.getStartTime(),
                            entry.getAudioData().length));
        }

        logger.debug("Created temporary timeline file: {}", timelineFile.getAbsolutePath());
        return timelineFile;
    }

    /**
     * Saves audio data to a WAV file using the configured output format.
     *
     * @param audioData Raw audio data bytes to save
     * @param outputFile Destination file for the audio data
     * @throws Exception If an error occurs during audio processing or file writing
     */
    private void saveAudioToFile(@NotNull byte[] audioData, File outputFile) throws Exception {
        if (audioData.length == 0) {
            logger.warn("No audio data to save for file: {}", outputFile.getAbsolutePath());
            return;
        }

        AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audioData),
                OUTPUT_FORMAT,
                audioData.length / OUTPUT_FORMAT.getFrameSize());

        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
    }

    /**
     * Sanitizes a username for use in filenames by replacing invalid characters.
     *
     * @param input Username or string to sanitize
     * @return Sanitized string safe for use in filenames
     */
    private String sanitizeFilename(String input) {
        return input.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
