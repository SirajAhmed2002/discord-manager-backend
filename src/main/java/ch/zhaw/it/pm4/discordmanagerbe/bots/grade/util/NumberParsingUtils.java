package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for parsing numbers from Discord input
 */
@Component
public class NumberParsingUtils {

    /**
     * Parses a double from string input, handling German decimal separators
     * @param input the string input to parse
     */
    public double parseDouble(String input) throws NumberFormatException {
        if (input == null || input.trim().isEmpty()) {
            throw new NumberFormatException("Eingabe ist leer");
        }

        String normalized = input.trim().replace(",", ".");

        if (!normalized.matches("^-?\\d*\\.?\\d+([eE][+-]?\\d+)?$")) {
            throw new NumberFormatException("Ungültiges Zahlenformat: " + input);
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Konnte '" + input + "' nicht als Zahl interpretieren");
        }
    }
}