package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberParsingUtilsTest {

    private NumberParsingUtils numberParsingUtils;

    @BeforeEach
    void setUp() {
        numberParsingUtils = new NumberParsingUtils();
    }

    @Test
    void parseDouble_ValidNumber_ReturnsDouble() {
        // Act & Assert
        assertEquals(4.5, numberParsingUtils.parseDouble("4.5"));
        assertEquals(4.5, numberParsingUtils.parseDouble("4,5")); // German format
        assertEquals(-2.3, numberParsingUtils.parseDouble("-2.3"));
        assertEquals(0.0, numberParsingUtils.parseDouble("0"));
    }

    @Test
    void parseDouble_ScientificNotation_ReturnsDouble() {
        // Act & Assert
        assertEquals(1.5e2, numberParsingUtils.parseDouble("1.5e2"));
        assertEquals(1.5e-2, numberParsingUtils.parseDouble("1.5e-2"));
    }

    @Test
    void parseDouble_WithWhitespace_TrimsAndParses() {
        // Act & Assert
        assertEquals(4.5, numberParsingUtils.parseDouble("  4.5  "));
        assertEquals(4.5, numberParsingUtils.parseDouble("\t4,5\n"));
    }

    @Test
    void parseDouble_EmptyString_ThrowsException() {
        // Act & Assert
        NumberFormatException exception1 = assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble(""));
        assertTrue(exception1.getMessage().contains("Eingabe ist leer"));

        NumberFormatException exception2 = assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble("   "));
        assertTrue(exception2.getMessage().contains("Eingabe ist leer"));

        NumberFormatException exception3 = assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble(null));
        assertTrue(exception3.getMessage().contains("Eingabe ist leer"));
    }

    @Test
    void parseDouble_InvalidFormat_ThrowsException() {
        // Act & Assert
        NumberFormatException exception1 = assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble("abc"));
        assertTrue(exception1.getMessage().contains("Ungültiges Zahlenformat"));

        NumberFormatException exception2 = assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble("4.5.6"));
        assertTrue(exception2.getMessage().contains("Ungültiges Zahlenformat"));

        NumberFormatException exception3 = assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble("4,5,6"));
        assertTrue(exception3.getMessage().contains("Ungültiges Zahlenformat"));
    }

    @Test
    void parseDouble_SpecialValues_ThrowsException() {
        // Act & Assert
        assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble("NaN"));
        
        assertThrows(NumberFormatException.class, () ->
                numberParsingUtils.parseDouble("Infinity"));
    }
}