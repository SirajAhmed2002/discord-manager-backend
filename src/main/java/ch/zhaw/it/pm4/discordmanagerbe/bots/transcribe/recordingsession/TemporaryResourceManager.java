package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

/**
 * Manages temporary resources created during the transcription process.
 * Handles creation and cleanup of temporary directories and files.
 */
@Component
public class TemporaryResourceManager {

    /** logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(TemporaryResourceManager.class);

    /**
     * Creates a temporary directory for storing transcription-related files.
     * The directory is marked for deletion when the JVM exits.
     *
     * @return A File object representing the created temporary directory
     * @throws IOException If an error occurs during directory creation
     */
    public File createTemporaryDirectory() throws IOException {
        File tempDir = Files.createTempDirectory("discord_transcription_temp").toFile();
        tempDir.deleteOnExit();

        logger.info("Created temporary directory for transcription: {}", tempDir.getAbsolutePath());
        return tempDir;
    }

    /**
     * Cleans up a temporary directory and all files within it.
     * Attempts to delete all files in the directory first, then the directory itself.
     * Logs warnings if any deletions fail.
     *
     * @param tempDir The temporary directory to clean up
     */
    public void cleanupTempDirectory(File tempDir) {
        if (tempDir.exists()) {
            logger.info("Cleaning up temporary directory: {}", tempDir.getAbsolutePath());

            Optional.ofNullable(tempDir.listFiles())
                    .ifPresent(files -> Arrays.stream(files)
                            .forEach(file -> {
                                if (!file.delete()) {
                                    logger.warn("Failed to delete temporary file: {}", file.getAbsolutePath());
                                }
                            }));

            if (!tempDir.delete()) {
                logger.warn("Failed to delete temporary directory: {}", tempDir.getAbsolutePath());
            } else {
                logger.info("Successfully deleted temporary directory");
            }
        }
    }
}
