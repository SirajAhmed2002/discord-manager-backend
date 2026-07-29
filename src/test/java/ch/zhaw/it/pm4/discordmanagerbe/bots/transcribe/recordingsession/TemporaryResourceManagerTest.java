package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TemporaryResourceManagerTest {

    private TemporaryResourceManager resourceManager;
    private File createdTempDir;

    @BeforeEach
    void setUp() {
        resourceManager = new TemporaryResourceManager();
    }

    @AfterEach
    void tearDown() {
        if (createdTempDir != null && createdTempDir.exists()) {
            resourceManager.cleanupTempDirectory(createdTempDir);
        }
    }

    @Test
    void testCreateTemporaryDirectory() throws IOException {
        createdTempDir = resourceManager.createTemporaryDirectory();

        assertTrue(createdTempDir.exists());
        assertTrue(createdTempDir.isDirectory());
        assertTrue(createdTempDir.getAbsolutePath().contains("discord_transcription_temp"));
    }

    @Test
    void testCleanupTempDirectory() throws IOException {
        createdTempDir = resourceManager.createTemporaryDirectory();

        File tempFile1 = new File(createdTempDir, "test1.txt");
        File tempFile2 = new File(createdTempDir, "test2.txt");

        assertTrue(tempFile1.createNewFile());
        assertTrue(tempFile2.createNewFile());

        resourceManager.cleanupTempDirectory(createdTempDir);

        assertFalse(tempFile1.exists());
        assertFalse(tempFile2.exists());
        assertFalse(createdTempDir.exists());

        createdTempDir = null;
    }

    @Test
    void testCleanupNonExistentDirectory() {
        File nonExistentDir = new File("/not/existing/directory");
        assertDoesNotThrow(() -> resourceManager.cleanupTempDirectory(nonExistentDir));
    }
}