package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ToDoConstants.*;

/**
 * Clean utility class for date and time operations.
 */
public final class DateTimeUtils {

    /**
     * Pattern to normalize date separators (spaces, dashes, colons).
     */
    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[\\s\\-:]");

    /**
     * Pattern to normalize time separators (spaces, dashes, dots).
     */
    private static final Pattern TIME_SEPARATOR_PATTERN = Pattern.compile("[\\s\\-.]");

    /**
     * Utility class should not be instantiated.
     */
    private DateTimeUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
    
    /**
     * Parses a date string with flexible separator handling.
     * 
     * @param dateString the date string to parse (nullable)
     * @return parsed LocalDate or null if invalid/empty
     */
    public static LocalDate parseDate(String dateString) {
        if (isNullOrEmpty(dateString)) {
            return null;
        }

        String normalizedDate = normalizeDateSeparators(dateString);
        return attemptDateParsing(normalizedDate);
    }

    /**
     * Parses a time string with flexible separator handling.
     * 
     * @param timeString the time string to parse (nullable)
     * @return parsed LocalTime or midnight if invalid/empty
     */
    public static LocalTime parseTime(String timeString) {
        if (isNullOrEmpty(timeString)) {
            return LocalTime.MIDNIGHT;
        }

        String normalizedTime = normalizeTimeSeparators(timeString);
        return attemptTimeParsing(normalizedTime);
    }

    /**
     * Validates if a date is today or in the future.
     * 
     * @param date the date to validate (nullable)
     * @return true if date is valid for task scheduling
     */
    public static boolean isValidFutureDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }

    /**
     * Validates if a datetime combination is in the future.
     * 
     * @param time the time component (nullable)
     * @param date the date component (nullable)
     * @return true if the datetime is in the future
     */
    public static boolean isValidFutureDateTime(LocalTime time, LocalDate date) {
        if (time == null || date == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return date.isAfter(today) || 
               (date.equals(today) && time.isAfter(LocalTime.now()));
    }

    /**
     * Converts date and time to epoch milliseconds.
     * 
     * @param date the date component (nullable)
     * @param time the time component (nullable)
     * @return epoch milliseconds or 0 if invalid input
     */
    public static long toEpochMillis(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return 0L;
        }
        
        return date.atTime(time)
                   .atZone(ZoneId.systemDefault())
                   .toInstant()
                   .toEpochMilli();
    }

    /**
     * Formats current date for display purposes.
     * 
     * @return formatted current date string
     */
    public static String getCurrentDateFormatted() {
        return LocalDate.now().format(createDateFormatter());
    }

    /**
     * Formats current time for display purposes.
     * 
     * @return formatted current time string
     */
    public static String getCurrentTimeFormatted() {
        return LocalTime.now().format(createTimeFormatter());
    }

    /**
     * Checks if a string is null or empty.
     * @param input the string to check (nullable)
     * @return true if the string is null or empty, false otherwise
     */
    private static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Normalizes date string by replacing common separators with dots.
     * @param dateString the date string to normalize (nullable)
     * @return normalized date string or original if null/empty
     */
    private static String normalizeDateSeparators(String dateString) {
        return SEPARATOR_PATTERN.matcher(dateString).replaceAll(".");
    }

    /**
     * Normalizes time string by replacing common separators with colons.
     * @param timeString the time string to normalize (nullable)
     * @return normalized time string or original if null/empty
     */
    private static String normalizeTimeSeparators(String timeString) {
        return TIME_SEPARATOR_PATTERN.matcher(timeString).replaceAll(":");
    }

    /**
     * Attempts to parse a date string using the defined date formatter.
     * @param normalizedDate the normalized date string to parse
     * @return parsed LocalDate or null if parsing fails
     */
    private static LocalDate attemptDateParsing(String normalizedDate) {
        try {
            return LocalDate.parse(normalizedDate, createDateFormatter());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Attempts to parse a time string using the defined time formatter.
     * @param normalizedTime the normalized time string to parse
     * @return parsed LocalTime or midnight if parsing fails
     */
    private static LocalTime attemptTimeParsing(String normalizedTime) {
        try {
            return LocalTime.parse(normalizedTime, createTimeFormatter());
        } catch (DateTimeParseException e) {
            return LocalTime.MIDNIGHT;
        }
    }

    /**
     * Creates a DateTimeFormatter for date parsing.
     * @return DateTimeFormatter for date
     */
    private static DateTimeFormatter createDateFormatter() {
        return DateTimeFormatter.ofPattern(DATE_PATTERN);
    }

    /**
     * Creates a DateTimeFormatter for time parsing.
     * @return DateTimeFormatter for time
     */
    private static DateTimeFormatter createTimeFormatter() {
        return DateTimeFormatter.ofPattern(TIME_PATTERN);
    }
}