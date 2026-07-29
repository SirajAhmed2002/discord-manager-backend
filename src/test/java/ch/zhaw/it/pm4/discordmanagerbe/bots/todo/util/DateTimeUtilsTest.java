package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DateTimeUtils Tests")
class DateTimeUtilsTest {

    @Nested
    @DisplayName("Date Parsing Tests")
    class DateParsingTests {

        @Test
        @DisplayName("Should parse valid date with dots")
        void shouldParseValidDateWithDots() {
            // When
            LocalDate result = DateTimeUtils.parseDate("25.12.2023");

            // Then
            assertThat(result).isEqualTo(LocalDate.of(2023, 12, 25));
        }

        @Test
        @DisplayName("Should parse date with dashes")
        void shouldParseDateWithDashes() {
            // When
            LocalDate result = DateTimeUtils.parseDate("25-12-2023");

            // Then
            assertThat(result).isEqualTo(LocalDate.of(2023, 12, 25));
        }

        @Test
        @DisplayName("Should parse date with spaces")
        void shouldParseDateWithSpaces() {
            // When
            LocalDate result = DateTimeUtils.parseDate("25 12 2023");

            // Then
            assertThat(result).isEqualTo(LocalDate.of(2023, 12, 25));
        }

        @Test
        @DisplayName("Should parse date with colons")
        void shouldParseDateWithColons() {
            // When
            LocalDate result = DateTimeUtils.parseDate("25:12:2023");

            // Then
            assertThat(result).isEqualTo(LocalDate.of(2023, 12, 25));
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            // When
            LocalDate result = DateTimeUtils.parseDate(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for empty input")
        void shouldReturnNullForEmptyInput() {
            // When
            LocalDate result = DateTimeUtils.parseDate("");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for whitespace input")
        void shouldReturnNullForWhitespaceInput() {
            // When
            LocalDate result = DateTimeUtils.parseDate("   ");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for invalid date format")
        void shouldReturnNullForInvalidDateFormat() {
            // When
            LocalDate result = DateTimeUtils.parseDate("invalid-date");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null for invalid date values")
        void shouldReturnNullForInvalidDateValues() {
            // When
            LocalDate result = DateTimeUtils.parseDate("32.13.2023"); // Invalid day and month

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Time Parsing Tests")
    class TimeParsingTests {

        @Test
        @DisplayName("Should parse valid time with colons")
        void shouldParseValidTimeWithColons() {
            // When
            LocalTime result = DateTimeUtils.parseTime("14:30");

            // Then
            assertThat(result).isEqualTo(LocalTime.of(14, 30));
        }

        @Test
        @DisplayName("Should parse time with dots")
        void shouldParseTimeWithDots() {
            // When
            LocalTime result = DateTimeUtils.parseTime("14.30");

            // Then
            assertThat(result).isEqualTo(LocalTime.of(14, 30));
        }

        @Test
        @DisplayName("Should parse time with dashes")
        void shouldParseTimeWithDashes() {
            // When
            LocalTime result = DateTimeUtils.parseTime("14-30");

            // Then
            assertThat(result).isEqualTo(LocalTime.of(14, 30));
        }

        @Test
        @DisplayName("Should parse time with spaces")
        void shouldParseTimeWithSpaces() {
            // When
            LocalTime result = DateTimeUtils.parseTime("14 30");

            // Then
            assertThat(result).isEqualTo(LocalTime.of(14, 30));
        }

        @Test
        @DisplayName("Should return midnight for null input")
        void shouldReturnMidnightForNullInput() {
            // When
            LocalTime result = DateTimeUtils.parseTime(null);

            // Then
            assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("Should return midnight for empty input")
        void shouldReturnMidnightForEmptyInput() {
            // When
            LocalTime result = DateTimeUtils.parseTime("");

            // Then
            assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("Should return midnight for whitespace input")
        void shouldReturnMidnightForWhitespaceInput() {
            // When
            LocalTime result = DateTimeUtils.parseTime("   ");

            // Then
            assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("Should return midnight for invalid time format")
        void shouldReturnMidnightForInvalidTimeFormat() {
            // When
            LocalTime result = DateTimeUtils.parseTime("invalid-time");

            // Then
            assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        @DisplayName("Should return midnight for invalid time values")
        void shouldReturnMidnightForInvalidTimeValues() {
            // When
            LocalTime result = DateTimeUtils.parseTime("25:70"); // Invalid hour and minute

            // Then
            assertThat(result).isEqualTo(LocalTime.MIDNIGHT);
        }
    }

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        @Test
        @DisplayName("Should validate today as valid future date")
        void shouldValidateTodayAsValidFutureDate() {
            // Given
            LocalDate today = LocalDate.now();

            // When
            boolean result = DateTimeUtils.isValidFutureDate(today);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate future date as valid")
        void shouldValidateFutureDateAsValid() {
            // Given
            LocalDate futureDate = LocalDate.now().plusDays(1);

            // When
            boolean result = DateTimeUtils.isValidFutureDate(futureDate);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should invalidate past date")
        void shouldInvalidatePastDate() {
            // Given
            LocalDate pastDate = LocalDate.now().minusDays(1);

            // When
            boolean result = DateTimeUtils.isValidFutureDate(pastDate);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should invalidate null date")
        void shouldInvalidateNullDate() {
            // When
            boolean result = DateTimeUtils.isValidFutureDate(null);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("DateTime Validation Tests")
    class DateTimeValidationTests {

        @Test
        @DisplayName("Should validate future datetime")
        void shouldValidateFutureDateTime() {
            // Given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            LocalTime futureTime = LocalTime.of(14, 30);

            // When
            boolean result = DateTimeUtils.isValidFutureDateTime(futureTime, futureDate);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate today with future time")
        void shouldValidateTodayWithFutureTime() {
            // Given
            LocalDate today = LocalDate.now();
            LocalTime futureTime = LocalTime.now().plusHours(2); // 2 hours ahead to avoid flaky tests

            // When
            boolean result = DateTimeUtils.isValidFutureDateTime(futureTime, today);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should invalidate past datetime")
        void shouldInvalidatePastDateTime() {
            // Given
            LocalDate today = LocalDate.now();
            LocalTime pastTime = LocalTime.of(0, 0); // Midnight (likely in the past)

            // When
            boolean result = DateTimeUtils.isValidFutureDateTime(pastTime, today);

            // Then - This might be true if run at midnight, so we test with yesterday
            LocalDate yesterday = LocalDate.now().minusDays(1);
            boolean resultYesterday = DateTimeUtils.isValidFutureDateTime(pastTime, yesterday);
            assertThat(resultYesterday).isFalse();
        }

        @Test
        @DisplayName("Should invalidate null time")
        void shouldInvalidateNullTime() {
            // Given
            LocalDate today = LocalDate.now();

            // When
            boolean result = DateTimeUtils.isValidFutureDateTime(null, today);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should invalidate null date")
        void shouldInvalidateNullDate() {
            // Given
            LocalTime time = LocalTime.now();

            // When
            boolean result = DateTimeUtils.isValidFutureDateTime(time, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should invalidate both null")
        void shouldInvalidateBothNull() {
            // When
            boolean result = DateTimeUtils.isValidFutureDateTime(null, null);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Epoch Conversion Tests")
    class EpochConversionTests {

        @Test
        @DisplayName("Should convert valid date and time to epoch millis")
        void shouldConvertValidDateAndTimeToEpochMillis() {
            // Given
            LocalDate date = LocalDate.of(2023, 12, 25);
            LocalTime time = LocalTime.of(14, 30, 0);

            // Expected epoch millis for 2023-12-25 14:30:00 in system timezone
            long expected = date.atTime(time)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            // When
            long result = DateTimeUtils.toEpochMillis(date, time);

            // Then
            assertThat(result).isEqualTo(expected);
            assertThat(result).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should return 0 for null date")
        void shouldReturn0ForNullDate() {
            // Given
            LocalTime time = LocalTime.of(14, 30);

            // When
            long result = DateTimeUtils.toEpochMillis(null, time);

            // Then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return 0 for null time")
        void shouldReturn0ForNullTime() {
            // Given
            LocalDate date = LocalDate.of(2023, 12, 25);

            // When
            long result = DateTimeUtils.toEpochMillis(date, null);

            // Then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return 0 for both null")
        void shouldReturn0ForBothNull() {
            // When
            long result = DateTimeUtils.toEpochMillis(null, null);

            // Then
            assertThat(result).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Formatting Tests")
    class FormattingTests {

        @Test
        @DisplayName("Should format current date")
        void shouldFormatCurrentDate() {
            // When
            String result = DateTimeUtils.getCurrentDateFormatted();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).matches("\\d{2}\\.\\d{2}\\.\\d{4}"); // DD.MM.YYYY pattern

            // Verify it's actually today's date
            LocalDate today = LocalDate.now();
            String expected = today.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should format current time")
        void shouldFormatCurrentTime() {
            // When
            String result = DateTimeUtils.getCurrentTimeFormatted();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).matches("\\d{2}:\\d{2}"); // HH:MM pattern

            // The actual time will vary, but format should be correct
            String[] parts = result.split(":");
            assertThat(parts).hasSize(2);

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            assertThat(hours).isBetween(0, 23);
            assertThat(minutes).isBetween(0, 59);
        }
    }
}