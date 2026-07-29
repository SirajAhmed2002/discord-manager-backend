package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.recordingsession;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SpeechSequenceEntry;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.UserSpeechSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SequenceManagerTest {

    private SequenceManager sequenceManager;

    @BeforeEach
    void setUp() {
        sequenceManager = new SequenceManager();
    }

    @Test
    void reset_shouldClearAllDataAndResetCounter() {
        // Arrange
        String userId = "user123";
        String username = "TestUser";
        long currentTime = System.currentTimeMillis();
        byte[] audioData = new byte[]{1, 2, 3, 4};
        long silenceThreshold = 1000L;

        sequenceManager.processAudioData(userId, username, currentTime, audioData, silenceThreshold);
        sequenceManager.finalizeAllSequences();

        List<SpeechSequenceEntry> entriesBeforeReset = sequenceManager.getSequenceEntries();
        assertFalse(entriesBeforeReset.isEmpty());
        int sequenceIdBeforeReset = sequenceManager.getNextSequenceId();
        assertTrue(sequenceIdBeforeReset > 1);

        // Act
        sequenceManager.reset();

        // Assert
        List<SpeechSequenceEntry> entriesAfterReset = sequenceManager.getSequenceEntries();
        assertTrue(entriesAfterReset.isEmpty());

        int sequenceIdAfterReset = sequenceManager.getNextSequenceId();
        assertEquals(1, sequenceIdAfterReset);
    }

    @Test
    void reset_shouldClearInternalDataStructures() {
        // Arrange
        String userId = "user123";
        String username = "TestUser";
        long currentTime = System.currentTimeMillis();
        byte[] audioData = new byte[]{1, 2, 3, 4};
        long silenceThreshold = 1000L;

        sequenceManager.processAudioData(userId, username, currentTime, audioData, silenceThreshold);

        // Act
        sequenceManager.reset();

        // Assert
        Map<?, ?> userSpeechSequences = (Map<?, ?>) ReflectionTestUtils.getField(sequenceManager, "userSpeechSequences");
        List<?> sequenceEntries = (List<?>) ReflectionTestUtils.getField(sequenceManager, "sequenceEntries");
        AtomicInteger sequenceIdCounter = (AtomicInteger) ReflectionTestUtils.getField(sequenceManager, "sequenceIdCounter");

        assertNotNull(userSpeechSequences);
        assertNotNull(sequenceEntries);
        assertNotNull(sequenceIdCounter);

        assertTrue(userSpeechSequences.isEmpty());
        assertTrue(sequenceEntries.isEmpty());
        assertEquals(1, sequenceIdCounter.get());
    }

    @Test
    void reset_afterMultipleUsersAndSequences_shouldClearAllData() {
        // Arrange
        String userId1 = "user123";
        String username1 = "TestUser1";
        String userId2 = "user456";
        String username2 = "TestUser2";
        long currentTime = System.currentTimeMillis();
        byte[] audioData = new byte[]{1, 2, 3, 4};
        long silenceThreshold = 1000L;

        sequenceManager.processAudioData(userId1, username1, currentTime, audioData, silenceThreshold);
        sequenceManager.processAudioData(userId2, username2, currentTime + 100, audioData, silenceThreshold);

        sequenceManager.processAudioData(userId1, username1, currentTime + silenceThreshold + 500, audioData, silenceThreshold);
        sequenceManager.processAudioData(userId2, username2, currentTime + silenceThreshold + 600, audioData, silenceThreshold);

        sequenceManager.finalizeAllSequences();

        List<SpeechSequenceEntry> entriesBeforeReset = sequenceManager.getSequenceEntries();
        assertTrue(entriesBeforeReset.size() > 2);

        // Act
        sequenceManager.reset();

        // Assert
        List<SpeechSequenceEntry> entriesAfterReset = sequenceManager.getSequenceEntries();
        assertTrue(entriesAfterReset.isEmpty());
        assertEquals(1, sequenceManager.getNextSequenceId());
    }

    @Test
    void processAudioData_firstCall_shouldCreateNewSequence() {
        // Arrange
        String userId = "user123";
        String username = "TestUser";
        long currentTime = System.currentTimeMillis();
        byte[] audioData = new byte[]{1, 2, 3, 4};
        long silenceThreshold = 1000L;

        // Act
        sequenceManager.processAudioData(userId, username, currentTime, audioData, silenceThreshold);

        // Assert
        Map<String, UserSpeechSequence> userSpeechSequences =
                (Map<String, UserSpeechSequence>) ReflectionTestUtils.getField(sequenceManager, "userSpeechSequences");

        assertNotNull(userSpeechSequences);
        assertEquals(1, userSpeechSequences.size());
        assertTrue(userSpeechSequences.containsKey(userId));

        UserSpeechSequence sequence = userSpeechSequences.get(userId);
        assertEquals(userId, sequence.getUserId());
        assertEquals(username, sequence.getUsername());
        assertEquals(currentTime, sequence.getStartTime());
        assertEquals(currentTime, sequence.getLastUpdateTime());

        ByteArrayOutputStream buffer = sequence.getAudioBuffer();
        assertArrayEquals(audioData, buffer.toByteArray());
    }

    @Test
    void processAudioData_multipleCallsSameUser_shouldAppendToSameSequence() {
        // Arrange
        String userId = "user123";
        String username = "TestUser";
        long currentTime = System.currentTimeMillis();
        byte[] audioData1 = new byte[]{1, 2, 3, 4};
        byte[] audioData2 = new byte[]{5, 6, 7, 8};
        long silenceThreshold = 1000L;

        // Act
        sequenceManager.processAudioData(userId, username, currentTime, audioData1, silenceThreshold);
        sequenceManager.processAudioData(userId, username, currentTime + 500, audioData2, silenceThreshold);

        // Assert
        Map<String, UserSpeechSequence> userSpeechSequences =
                (Map<String, UserSpeechSequence>) ReflectionTestUtils.getField(sequenceManager, "userSpeechSequences");
        UserSpeechSequence sequence = userSpeechSequences.get(userId);

        assertEquals(currentTime + 500, sequence.getLastUpdateTime(), "Die letzte Aktualisierungszeit sollte aktualisiert worden sein");

        ByteArrayOutputStream buffer = sequence.getAudioBuffer();
        byte[] expectedCombinedData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        assertArrayEquals(expectedCombinedData, buffer.toByteArray(), "Die Audiodaten sollten zusammengeführt worden sein");

        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertTrue(entries.isEmpty(), "Es sollten keine finalisierten Sequenzen vorhanden sein");
    }

    @Test
    void processAudioData_exceededSilenceThreshold_shouldCreateNewSequence() {
        // Arrange
        String userId = "user123";
        String username = "TestUser";
        long currentTime = System.currentTimeMillis();
        byte[] audioData1 = new byte[]{1, 2, 3, 4};
        byte[] audioData2 = new byte[]{5, 6, 7, 8};
        long silenceThreshold = 1000L;

        // Act
        sequenceManager.processAudioData(userId, username, currentTime, audioData1, silenceThreshold);

        sequenceManager.processAudioData(userId, username, currentTime + silenceThreshold + 100, audioData2, silenceThreshold);

        // Assert
        Map<String, UserSpeechSequence> userSpeechSequences =
                (Map<String, UserSpeechSequence>) ReflectionTestUtils.getField(sequenceManager, "userSpeechSequences");
        UserSpeechSequence sequence = userSpeechSequences.get(userId);

        assertEquals(currentTime + silenceThreshold + 100, sequence.getStartTime());

        ByteArrayOutputStream buffer = sequence.getAudioBuffer();
        assertArrayEquals(audioData2, buffer.toByteArray());

        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertEquals(1, entries.size());
        SpeechSequenceEntry entry = entries.getFirst();
        assertEquals(userId, entry.getUserId());
        assertEquals(username, entry.getUsername());
        assertEquals(currentTime, entry.getStartTime());
        assertArrayEquals(audioData1, entry.getAudioData());
    }

    @Test
    void processAudioData_multipleUsers_shouldCreateSeparateSequences() {
        // Arrange
        String userId1 = "user123";
        String username1 = "TestUser1";
        String userId2 = "user456";
        String username2 = "TestUser2";
        long currentTime = System.currentTimeMillis();
        byte[] audioData1 = new byte[]{1, 2, 3, 4};
        byte[] audioData2 = new byte[]{5, 6, 7, 8};
        long silenceThreshold = 1000L;

        // Act
        sequenceManager.processAudioData(userId1, username1, currentTime, audioData1, silenceThreshold);
        sequenceManager.processAudioData(userId2, username2, currentTime + 100, audioData2, silenceThreshold);

        // Assert
        Map<String, UserSpeechSequence> userSpeechSequences =
                (Map<String, UserSpeechSequence>) ReflectionTestUtils.getField(sequenceManager, "userSpeechSequences");

        assertEquals(2, userSpeechSequences.size());
        assertTrue(userSpeechSequences.containsKey(userId1));
        assertTrue(userSpeechSequences.containsKey(userId2));

        UserSpeechSequence sequence1 = userSpeechSequences.get(userId1);
        UserSpeechSequence sequence2 = userSpeechSequences.get(userId2);

        assertEquals(username1, sequence1.getUsername());
        assertEquals(username2, sequence2.getUsername());

        assertArrayEquals(audioData1, sequence1.getAudioBuffer().toByteArray());
        assertArrayEquals(audioData2, sequence2.getAudioBuffer().toByteArray());
    }

    @Test
    void processAudioData_shouldCallFinalizeSequenceWhenInactive() {
        // Arrange
        SequenceManager spyManager = Mockito.spy(sequenceManager);
        String userId = "user123";
        String username = "TestUser";
        long currentTime = System.currentTimeMillis();
        byte[] audioData1 = new byte[]{1, 2, 3, 4};
        byte[] audioData2 = new byte[]{5, 6, 7, 8};
        long silenceThreshold = 1000L;

        // Act
        spyManager.processAudioData(userId, username, currentTime, audioData1, silenceThreshold);

        clearInvocations(spyManager);

        spyManager.processAudioData(userId, username, currentTime + silenceThreshold + 100, audioData2, silenceThreshold);

        // Assert
        verify(spyManager, times(1)).finalizeSequence(any(UserSpeechSequence.class));

        Map<String, UserSpeechSequence> userSpeechSequences =
                (Map<String, UserSpeechSequence>) ReflectionTestUtils.getField(spyManager, "userSpeechSequences");
        UserSpeechSequence sequence = userSpeechSequences.get(userId);
        assertArrayEquals(audioData2, sequence.getAudioBuffer().toByteArray(),
                "Die aktuelle Sequenz sollte die zweiten Audiodaten enthalten");
    }

    @Test
    void processAudioData_zeroSilenceThreshold_shouldAlwaysCreateNewSequence() {
        // Arrange
        String userId = "user123";
        String username = "TestUser";
        long currentTime = System.currentTimeMillis();
        byte[] audioData1 = new byte[]{1, 2, 3, 4};
        byte[] audioData2 = new byte[]{5, 6, 7, 8};
        long silenceThreshold = 0L;

        // Act
        sequenceManager.processAudioData(userId, username, currentTime, audioData1, silenceThreshold);
        sequenceManager.processAudioData(userId, username, currentTime + 10, audioData2, silenceThreshold);

        // Assert
        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertEquals(1, entries.size());

        Map<String, UserSpeechSequence> userSpeechSequences =
                (Map<String, UserSpeechSequence>) ReflectionTestUtils.getField(sequenceManager, "userSpeechSequences");
        UserSpeechSequence currentSequence = userSpeechSequences.get(userId);

        assertArrayEquals(audioData2, currentSequence.getAudioBuffer().toByteArray());
    }

    @Test
    void finalizeSequence_withValidData_shouldCreateEntry() {
        // Arrange
        String userId = "user123";
        String username = "TestUser";
        long startTime = System.currentTimeMillis();
        long lastUpdateTime = startTime + 500;
        byte[] audioData = new byte[]{1, 2, 3, 4};
        int sequenceId = 42;

        UserSpeechSequence sequence = new UserSpeechSequence(userId, username);
        ReflectionTestUtils.setField(sequence, "sequenceId", sequenceId);
        ReflectionTestUtils.setField(sequence, "startTime", startTime);
        ReflectionTestUtils.setField(sequence, "lastUpdateTime", lastUpdateTime);

        sequence.addAudioData(audioData, lastUpdateTime);

        // Act
        sequenceManager.finalizeSequence(sequence);

        // Assert
        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertEquals(1, entries.size());

        SpeechSequenceEntry entry = entries.getFirst();
        assertEquals(sequenceId, entry.getSequenceId());
        assertEquals(userId, entry.getUserId());
        assertEquals(username, entry.getUsername());
        assertEquals(startTime, entry.getStartTime());
        assertEquals(lastUpdateTime, entry.getEndTime());
        assertArrayEquals(audioData, entry.getAudioData());
    }

    @Test
    void finalizeSequence_withEmptyData_shouldNotCreateEntry() {
        // Arrange
        UserSpeechSequence emptySequence = new UserSpeechSequence("user123", "TestUser");

        // Act
        sequenceManager.finalizeSequence(emptySequence);

        // Assert
        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertTrue(entries.isEmpty());
    }

    @Test
    void finalizeSequence_multipleSequences_shouldCreateMultipleEntries() {
        // Arrange
        UserSpeechSequence sequence1 = createTestSequence("user1", "User1", 1, new byte[]{1, 2, 3});
        UserSpeechSequence sequence2 = createTestSequence("user2", "User2", 2, new byte[]{4, 5, 6});
        UserSpeechSequence sequence3 = createTestSequence("user1", "User1", 3, new byte[]{7, 8, 9});

        // Act
        sequenceManager.finalizeSequence(sequence1);
        sequenceManager.finalizeSequence(sequence2);
        sequenceManager.finalizeSequence(sequence3);

        // Assert
        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertEquals(3, entries.size());

        assertEquals(1, entries.get(0).getSequenceId());
        assertEquals(2, entries.get(1).getSequenceId());
        assertEquals(3, entries.get(2).getSequenceId());
    }

    @Test
    void finalizeSequence_sameSequenceTwice_shouldCreateTwoEntries() {
        // Arrange
        UserSpeechSequence sequence = createTestSequence("user1", "User1", 1, new byte[]{1, 2, 3});

        // Act
        sequenceManager.finalizeSequence(sequence);
        sequenceManager.finalizeSequence(sequence);

        // Assert
        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertEquals(2, entries.size());

        assertEquals(entries.get(0).getSequenceId(), entries.get(1).getSequenceId());
    }

    @Test
    void finalizeSequence_withNullSequence_shouldHandleGracefully() {
        // Act & Assert
        assertDoesNotThrow(() -> sequenceManager.finalizeSequence(null));

        List<SpeechSequenceEntry> entries = sequenceManager.getSequenceEntries();
        assertTrue(entries.isEmpty());
    }

    private UserSpeechSequence createTestSequence(String userId, String username, int sequenceId, byte[] audioData) {
        UserSpeechSequence sequence = new UserSpeechSequence(userId, username);
        ReflectionTestUtils.setField(sequence, "sequenceId", sequenceId);

        long currentTime = System.currentTimeMillis();
        ReflectionTestUtils.setField(sequence, "startTime", currentTime - 500);
        ReflectionTestUtils.setField(sequence, "lastUpdateTime", currentTime);

        sequence.addAudioData(audioData, currentTime);

        return sequence;
    }
}