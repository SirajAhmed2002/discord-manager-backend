package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BotMessagesTest {

    @Test
    void testGetMessageWithoutPlaceholders() {
        String message = BotMessages.get(MessageKey.COMMAND_NOT_AVAILABLE);
        assertNotNull(message);
        assertTrue(message.contains("**Dieser Befehl ist im aktuellen Bot-Zustand nicht verfügbar.**"));
        assertTrue(message.contains("/help"));
    }

    @Test
    void testGetMessageWithMultiplePlaceholders() {
        String users = "User1, User2, User3";
        String message = BotMessages.get(MessageKey.RECORDING_PERMISSION_REQUIRED, users);
        assertNotNull(message);
        assertTrue(message.contains("Warte auf Bestätigung von: **User1, User2, User3**"));
    }

    @Test
    void testKeyWithNoFormatSpecifiers() {
        String message = BotMessages.get(MessageKey.PERMISSION_DENIED, "extra arg");
        assertEquals("Nur der Bot-Owner kann diesen Befehl ausführen.", message);
    }
}