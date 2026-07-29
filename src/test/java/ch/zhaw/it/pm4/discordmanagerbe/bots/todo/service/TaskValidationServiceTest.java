package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ReminderUnit;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskValidationServiceTest {

    private TaskValidationService validationService;
    private Task testTask;

    @BeforeEach
    void setUp() {
        validationService = new TaskValidationService();
        
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setTimeToBeDone(System.currentTimeMillis() + 3600000); // 1 hour from now
        testTask.setReminderUnit(ReminderUnit.HOURS);
    }

    @Nested
    class TitleValidationTests {

        @Test
        void shouldValidateValidTitleSuccessfully() {
            // When
            TaskValidationService.ValidationResult<Void> result =
                validationService.validateTaskTitle("Valid Title");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.getErrorMessage()).isNull();
        }

        @Test
        void shouldRejectNullTitle() {
            // When
            TaskValidationService.ValidationResult<Void> result =
                validationService.validateTaskTitle(null);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Title cannot be null or empty");
        }

        @Test
        void shouldRejectEmptyTitle() {
            // When
            TaskValidationService.ValidationResult<Void> result =
                validationService.validateTaskTitle("");

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Title cannot be null or empty");
        }

        @Test
        void shouldRejectWhitespaceOnlyTitle() {
            // When
            TaskValidationService.ValidationResult<Void> result =
                validationService.validateTaskTitle("   ");

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Title cannot be null or empty");
        }
    }

    @Nested
    class DateValidationTests {

        @Test
        void shouldValidateEmptyDateAsAllowed() {
            // When
            TaskValidationService.ValidationResult<LocalDate> result =
                validationService.validateAndParseDate("");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNull();
        }

        @Test
        void shouldValidateNullDateAsAllowed() {
            // When
            TaskValidationService.ValidationResult<LocalDate> result =
                validationService.validateAndParseDate(null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNull();
        }

        @Test
        void shouldValidateTodaysDate() {
            // Given
            String todayDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            // When
            TaskValidationService.ValidationResult<LocalDate> result =
                validationService.validateAndParseDate(todayDate);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldValidateFutureDate() {
            // Given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            String futureDateStr = futureDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            // When
            TaskValidationService.ValidationResult<LocalDate> result =
                validationService.validateAndParseDate(futureDateStr);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(futureDate);
        }

        @Test
        void shouldRejectPastDate() {
            // Given
            LocalDate pastDate = LocalDate.now().minusDays(1);
            String pastDateStr = pastDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            // When
            TaskValidationService.ValidationResult<LocalDate> result =
                validationService.validateAndParseDate(pastDateStr);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Date must be in the future or today");
        }

        @Test
        void shouldRejectInvalidDateFormat() {
            // When
            TaskValidationService.ValidationResult<LocalDate> result =
                validationService.validateAndParseDate("invalid-date");

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Invalid date format");
        }

        @Test
        void shouldHandleVariousDateSeparators() {
            // Given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            String expectedDay = String.format("%02d", futureDate.getDayOfMonth());
            String expectedMonth = String.format("%02d", futureDate.getMonthValue());
            String expectedYear = String.valueOf(futureDate.getYear());

            // When
            TaskValidationService.ValidationResult<LocalDate> result1 =
                validationService.validateAndParseDate(expectedDay + "." + expectedMonth + "." + expectedYear);
            TaskValidationService.ValidationResult<LocalDate> result2 =
                validationService.validateAndParseDate(expectedDay + "-" + expectedMonth + "-" + expectedYear);
            TaskValidationService.ValidationResult<LocalDate> result3 =
                validationService.validateAndParseDate(expectedDay + " " + expectedMonth + " " + expectedYear);

            // Then
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
            assertThat(result3.isSuccess()).isTrue();
            assertThat(result1.getData()).isEqualTo(futureDate);
            assertThat(result2.getData()).isEqualTo(futureDate);
            assertThat(result3.getData()).isEqualTo(futureDate);
        }
    }

    @Nested
    class TimeValidationTests {

        @Test
        void shouldValidateValidTimeFormat() {
            // When
            TaskValidationService.ValidationResult<LocalTime> result =
                validationService.validateAndParseTime("14:30");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(LocalTime.of(14, 30));
        }

        @Test
        void shouldReturnMidnightForNullTime() {
            // When
            TaskValidationService.ValidationResult<LocalTime> result =
                validationService.validateAndParseTime(null);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        void shouldReturnMidnightForEmptyTime() {
            // When
            TaskValidationService.ValidationResult<LocalTime> result =
                validationService.validateAndParseTime("");

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(LocalTime.MIDNIGHT);
        }

        @Test
        void shouldHandleVariousTimeSeparators() {
            // When
            TaskValidationService.ValidationResult<LocalTime> result1 =
                validationService.validateAndParseTime("14:30");
            TaskValidationService.ValidationResult<LocalTime> result2 =
                validationService.validateAndParseTime("14.30");
            TaskValidationService.ValidationResult<LocalTime> result3 =
                validationService.validateAndParseTime("14-30");

            // Then
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
            assertThat(result3.isSuccess()).isTrue();
            assertThat(result1.getData()).isEqualTo(LocalTime.of(14, 30));
            assertThat(result2.getData()).isEqualTo(LocalTime.of(14, 30));
            assertThat(result3.getData()).isEqualTo(LocalTime.of(14, 30));
        }

        @Test
        void shouldRejectInvalidTimeFormat() {
            // When
            TaskValidationService.ValidationResult<LocalTime> result =
                validationService.validateAndParseTime("25:70"); // Invalid hour and minute

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo("00:00");
        }
    }

    @Nested
    class DateTimeValidationTests {

        @Test
        void shouldValidateFutureDateTime() {
            // Given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            LocalTime futureTime = LocalTime.of(14, 30);

            // When
            TaskValidationService.ValidationResult<Void> result =
                validationService.validateScheduledDateTime(futureTime, futureDate);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        void shouldValidateTodayWithFutureTime() {
            // Given
            LocalDate today = LocalDate.now();
            LocalTime futureTime = LocalTime.now().plusHours(1);

            // When
            TaskValidationService.ValidationResult<Void> result =
                validationService.validateScheduledDateTime(futureTime, today);

            // Then
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        void shouldAllowNullValues() {
            // When
            TaskValidationService.ValidationResult<Void> result1 =
                validationService.validateScheduledDateTime(null, LocalDate.now());
            TaskValidationService.ValidationResult<Void> result2 =
                validationService.validateScheduledDateTime(LocalTime.now(), null);
            TaskValidationService.ValidationResult<Void> result3 =
                validationService.validateScheduledDateTime(null, null);

            // Then
            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
            assertThat(result3.isSuccess()).isTrue();
        }

        @Test
        void shouldRejectPastDateTime() {
            // Given
            LocalDate today = LocalDate.now();
            LocalTime pastTime = LocalTime.now().minusHours(1);

            // When
            TaskValidationService.ValidationResult<Void> result =
                validationService.validateScheduledDateTime(pastTime, today);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Date and time must be in the future");
        }
    }

    @Nested
    class ReminderValidationTests {

        @Test
        void shouldRejectNullReminderValue() {
            // Given
            long currentTime = System.currentTimeMillis();

            // When
            TaskValidationService.ValidationResult<Long> result =
                validationService.validateReminderConfiguration(null, testTask, currentTime);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Reminder value cannot be empty");
        }

        @Test
        void shouldRejectEmptyReminderValue() {
            // Given
            long currentTime = System.currentTimeMillis();

            // When
            TaskValidationService.ValidationResult<Long> result =
                validationService.validateReminderConfiguration("", testTask, currentTime);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Reminder value cannot be empty");
        }

        @Test
        void shouldRejectInvalidNumberFormat() {
            // Given
            long currentTime = System.currentTimeMillis();

            // When
            TaskValidationService.ValidationResult<Long> result =
                validationService.validateReminderConfiguration("invalid", testTask, currentTime);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Invalid reminder value format");
        }

        @Test
        void shouldRejectNegativeReminderValue() {
            // Given
            long currentTime = System.currentTimeMillis();

            // When
            TaskValidationService.ValidationResult<Long> result =
                validationService.validateReminderConfiguration("-1", testTask, currentTime);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Reminder value must be positive");
        }

        @Test
        void shouldRejectZeroReminderValue() {
            // Given
            long currentTime = System.currentTimeMillis();

            // When
            TaskValidationService.ValidationResult<Long> result =
                validationService.validateReminderConfiguration("0", testTask, currentTime);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Reminder value must be positive");
        }

        @Test
        void shouldRejectWhenReminderUnitIsNull() {
            // Given
            testTask.setReminderUnit(null);
            long currentTime = System.currentTimeMillis();

            // When
            TaskValidationService.ValidationResult<Long> result =
                validationService.validateReminderConfiguration("2", testTask, currentTime);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Reminder unit must be selected");
        }

        @Test
        void shouldRejectWhenReminderTimeIsInThePast() {
            // Given
            testTask.setTimeToBeDone(System.currentTimeMillis() + 1000); // 1 second from now
            testTask.setReminderUnit(ReminderUnit.HOURS);
            long currentTime = System.currentTimeMillis();

            // When (trying to set reminder 1 hour before, but task is only 1 second away)
            TaskValidationService.ValidationResult<Long> result =
                validationService.validateReminderConfiguration("1", testTask, currentTime);

            // Then
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getErrorMessage()).isEqualTo("Reminder time is in the past");
        }
    }

    @Nested
    class ValidationResultTests {

        @Test
        void shouldCreateSuccessfulResultWithoutData() {
            // When
            TaskValidationService.ValidationResult<Void> result =
                TaskValidationService.ValidationResult.success();

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.getData()).isNull();
            assertThat(result.getErrorMessage()).isNull();
            assertThat(result.hasData()).isFalse();
        }

        @Test
        void shouldCreateSuccessfulResultWithData() {
            // Given
            String testData = "test";

            // When
            TaskValidationService.ValidationResult<String> result =
                TaskValidationService.ValidationResult.success(testData);

            // Then
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
            assertThat(result.getData()).isEqualTo(testData);
            assertThat(result.getErrorMessage()).isNull();
            assertThat(result.hasData()).isTrue();
        }

        @Test
        void shouldCreateFailureResult() {
            // Given
            String errorMessage = "Test error";

            // When
            TaskValidationService.ValidationResult<String> result =
                TaskValidationService.ValidationResult.failure(errorMessage);

            // Then
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getData()).isNull();
            assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
            assertThat(result.hasData()).isFalse();
        }
    }
}