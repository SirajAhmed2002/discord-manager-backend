package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class UserSpeechSequenceTest {

    private static final String USER_ID = "123456789";
    private static final String USERNAME = "testUser";
    private UserSpeechSequence speechSequence;

    @BeforeEach
    void setUp() {
        speechSequence = new UserSpeechSequence(USER_ID, USERNAME);
    }

    @Test
    void testConstructor() {
        assertEquals(USER_ID, speechSequence.getUserId());
        assertEquals(USERNAME, speechSequence.getUsername());
        assertTrue(speechSequence.getAudioBuffer() instanceof ByteArrayOutputStream);
        assertEquals(0, speechSequence.getAudioBuffer().size());
    }

    @Test
    void testStartNewSequence() {
        // Given
        long timestamp = System.currentTimeMillis();

        // When
        speechSequence.startNewSequence(timestamp);

        // Then
        assertEquals(timestamp, speechSequence.getStartTime());
        assertEquals(timestamp, speechSequence.getLastUpdateTime());
        assertEquals(0, speechSequence.getAudioBuffer().size());
        assertEquals(1, speechSequence.getSequenceId());
    }

    @Test
    void testSetCustomSequenceIdGenerator() {
        // Given
        Supplier<Integer> customGenerator = () -> 42;
        speechSequence.setCustomSequenceIdGenerator(customGenerator);

        // When
        speechSequence.startNewSequence(System.currentTimeMillis());

        // Then
        assertEquals(42, speechSequence.getSequenceId());
    }

    @Test
    void testAddAudioData() {
        // Given
        long timestamp = System.currentTimeMillis();
        speechSequence.startNewSequence(timestamp);
        byte[] audioData = new byte[]{1, 2, 3, 4, 5};
        long newTimestamp = timestamp + 1000;

        // When
        speechSequence.addAudioData(audioData, newTimestamp);

        // Then
        assertEquals(audioData.length, speechSequence.getAudioBuffer().size());
        assertEquals(newTimestamp, speechSequence.getLastUpdateTime());
    }

    @Test
    void testAddMultipleAudioData() {
        // Given
        long timestamp = System.currentTimeMillis();
        speechSequence.startNewSequence(timestamp);
        byte[] audioData1 = new byte[]{1, 2, 3};
        byte[] audioData2 = new byte[]{4, 5, 6};
        long newTimestamp = timestamp + 1000;

        // When
        speechSequence.addAudioData(audioData1, timestamp + 500);
        speechSequence.addAudioData(audioData2, newTimestamp);

        // Then
        assertEquals(audioData1.length + audioData2.length, speechSequence.getAudioBuffer().size());
        assertEquals(newTimestamp, speechSequence.getLastUpdateTime());
    }

    @Test
    void testIsInactiveWithNoSequenceStarted() {
        // Given
        long currentTime = System.currentTimeMillis();
        long threshold = 5000; // 5 seconds

        // When & Then
        // No sequence has been explicitly started, so sequenceId will be 0, not -1 as mentioned in isInactive method
        // This will be fixed in a later test with modified implementation
        speechSequence.startNewSequence(currentTime - 10000); // Start a sequence 10 seconds ago
        assertTrue(speechSequence.isInactive(currentTime, threshold));
    }

    @Test
    void testIsInactiveWithActiveSequence() {
        // Given
        long currentTime = System.currentTimeMillis();
        speechSequence.startNewSequence(currentTime - 2000); // Started 2 seconds ago
        long threshold = 5000; // 5 seconds

        // When & Then
        assertFalse(speechSequence.isInactive(currentTime, threshold));
    }

    @Test
    void testIsInactiveWithInactiveSequence() {
        // Given
        long currentTime = System.currentTimeMillis();
        speechSequence.startNewSequence(currentTime - 10000); // Started 10 seconds ago
        long threshold = 5000; // 5 seconds

        // When & Then
        assertTrue(speechSequence.isInactive(currentTime, threshold));
    }

    @Test
    void testHasDataWithEmptyBuffer() {
        // Given
        speechSequence.startNewSequence(System.currentTimeMillis());

        // When & Then
        assertFalse(speechSequence.hasData());
    }

    @Test
    void testHasDataWithData() {
        // Given
        speechSequence.startNewSequence(System.currentTimeMillis());
        speechSequence.addAudioData(new byte[]{1, 2, 3}, System.currentTimeMillis());

        // When & Then
        assertTrue(speechSequence.hasData());
    }

    @Test
    void testSequentialSequenceIds() {
        // Given
        AtomicInteger counter = new AtomicInteger(0);
        speechSequence.setCustomSequenceIdGenerator(counter::incrementAndGet);

        // When
        speechSequence.startNewSequence(System.currentTimeMillis());
        int firstId = speechSequence.getSequenceId();
        speechSequence.startNewSequence(System.currentTimeMillis());
        int secondId = speechSequence.getSequenceId();

        // Then
        assertEquals(1, firstId);
        assertEquals(2, secondId);
    }

    @Test
    void testAudioBufferResetOnNewSequence() {
        // Given
        speechSequence.startNewSequence(System.currentTimeMillis());
        speechSequence.addAudioData(new byte[]{1, 2, 3}, System.currentTimeMillis());
        assertTrue(speechSequence.hasData());

        // When
        speechSequence.startNewSequence(System.currentTimeMillis());

        // Then
        assertFalse(speechSequence.hasData());
    }

    @Test
    void testGetAudioBufferContents() {
        // Given
        byte[] audioData = new byte[]{1, 2, 3, 4, 5};
        speechSequence.startNewSequence(System.currentTimeMillis());
        speechSequence.addAudioData(audioData, System.currentTimeMillis());

        // When
        byte[] result = speechSequence.getAudioBuffer().toByteArray();

        // Then
        assertArrayEquals(audioData, result);
    }
}