package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.model.Task;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.util.DateTimeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Clean validation service following single responsibility principle.
 * Encapsulates all task-related validation logic with clear, intention-revealing methods.
 */
@Service
public class TaskValidationService {

    /**
     * Validates task title according to business rules.
     * @param title the task title to validate
     * @return ValidationResult indicating success or failure with error message
     */
    public ValidationResult<Void> validateTaskTitle(String title) {
        if (isNullOrEmpty(title)) {
            return ValidationResult.failure("Title cannot be null or empty");
        }
        return ValidationResult.success();
    }

    /**
     * Validates and parses date string with business rules.
     * @param dateString the date string to validate and parse
     * @return ValidationResult containing parsed LocalDate or error message
     */
    public ValidationResult<LocalDate> validateAndParseDate(String dateString) {
        if (isNullOrEmpty(dateString)) {
            return ValidationResult.success(null); // Empty date is allowed
        }

        LocalDate parsedDate = DateTimeUtils.parseDate(dateString);
        if (parsedDate == null) {
            return ValidationResult.failure("Invalid date format");
        }

        if (!DateTimeUtils.isValidFutureDate(parsedDate)) {
            return ValidationResult.failure("Date must be in the future or today");
        }

        return ValidationResult.success(parsedDate);
    }

    /**
     * Validates and parses time string.
     * Clean code: consistent with date validation, handles edge cases.
     * @param timeString the time string to validate and parse
     * @return ValidationResult containing parsed LocalTime or error message
     */
    public ValidationResult<LocalTime> validateAndParseTime(String timeString) {
        LocalTime parsedTime = DateTimeUtils.parseTime(timeString);
        if (parsedTime == null) {
            return ValidationResult.failure("Invalid time format");
        }
        return ValidationResult.success(parsedTime);
    }

    /**
     * Validates datetime combination for task scheduling.
     * @param time the LocalTime to validate (nullable)
     * @param date the LocalDate to validate (nullable)
     * @return ValidationResult indicating if the datetime is valid for scheduling
     */
    public ValidationResult<Void> validateScheduledDateTime(LocalTime time, LocalDate date) {
        if (!isValidDateTimeForScheduling(time, date)) {
            return ValidationResult.failure("Date and time must be in the future");
        }
        return ValidationResult.success();
    }

    /**
     * Validates reminder parameters against business rules.
     * @param reminderValue the reminder value as a string
     * @param task the Task object containing reminder unit and time to be done
     * @param currentTime the current time in milliseconds
     * @return ValidationResult containing reminder duration or error message
     */
    public ValidationResult<Long> validateReminderConfiguration(String reminderValue, Task task, long currentTime) {
        ValidationResult<Long> parseResult = parseReminderValue(reminderValue);
        if (parseResult.isFailure()) {
            return parseResult;
        }

        ValidationResult<Void> unitResult = validateReminderUnit(task);
        if (unitResult.isFailure()) {
            return ValidationResult.failure(unitResult.getErrorMessage());
        }

        return validateReminderTiming(parseResult.getData(), task, currentTime);
    }

    /**
     * Checks if a string is null or empty.
     * @param input the string to check
     * @return true if the string is null or empty, false otherwise
     */
    private boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Validates if the provided time and date are suitable for scheduling a task.
     * @param time the LocalTime to validate (nullable)
     * @param date the LocalDate to validate (nullable)
     * @return true if the datetime is valid for scheduling, false otherwise
     */
    private boolean isValidDateTimeForScheduling(LocalTime time, LocalDate date) {
        return time == null || date == null || DateTimeUtils.isValidFutureDateTime(time, date);
    }

    /**
     * Parses the reminder value from a string to a long.
     * @param reminderValue the reminder value as a string
     * @return ValidationResult containing the parsed long value or error message
     */
    private ValidationResult<Long> parseReminderValue(String reminderValue) {
        if (isNullOrEmpty(reminderValue)) {
            return ValidationResult.failure("Reminder value cannot be empty");
        }

        try {
            long value = Long.parseLong(reminderValue);
            if (value <= 0) {
                return ValidationResult.failure("Reminder value must be positive");
            }
            return ValidationResult.success(value);
        } catch (NumberFormatException e) {
            return ValidationResult.failure("Invalid reminder value format");
        }
    }

    /**
     * Validates the reminder unit against business rules.
     * @param task the Task object containing the reminder unit
     * @return ValidationResult indicating success or failure with error message
     */
    private ValidationResult<Void> validateReminderUnit(Task task) {
        if (task.getReminderUnit() == null) {
            return ValidationResult.failure("Reminder unit must be selected");
        }
        return ValidationResult.success();
    }

    /**
     * Validates the timing of the reminder based on the task's time to be done.
     * @param reminderValue the reminder value in minutes
     * @param task the Task object containing time to be done and reminder unit
     * @param currentTime the current time in milliseconds
     * @return ValidationResult containing the reminder duration or error message
     */
    private ValidationResult<Long> validateReminderTiming(long reminderValue, Task task, long currentTime) {
        long reminderDuration = reminderValue * task.getReminderUnit().getMillis();
        long reminderDateTime = task.getTimeToBeDone() - reminderDuration;

        if (reminderDateTime < currentTime) {
            return ValidationResult.failure("Reminder time is in the past");
        }

        return ValidationResult.success(reminderDuration);
    }

    /**
     * Validation result class
     */
    public static class ValidationResult<T> {
        /**
         * Represents the result of a validation operation.
         */
        private final boolean successful;

        /**
         * Error message if validation failed.
         */
        private final String errorMessage;

        /**
         * Data returned if validation was successful.
         */
        private final T data;

        /**
         * Private constructor for ValidationResult.
         * @param successful indicates if validation was successful
         * @param errorMessage error message if validation failed
         * @param data data returned if validation was successful
         */
        private ValidationResult(boolean successful, String errorMessage, T data) {
            this.successful = successful;
            this.errorMessage = errorMessage;
            this.data = data;
        }

        /**
         * Creates a successful validation result with data.
         * @param data the data to return if validation was successful
         * @return ValidationResult indicating success with data
         * @param <T> the type of data returned
         */
        public static <T> ValidationResult<T> success(T data) {
            return new ValidationResult<>(true, null, data);
        }

        /**
         * Creates a successful validation result without data.
         * @return ValidationResult indicating success without data
         */
        public static ValidationResult<Void> success() {
            return new ValidationResult<>(true, null, null);
        }

        /**
         * Creates a failed validation result with an error message.
         * @param errorMessage the error message indicating why validation failed
         * @return ValidationResult indicating failure with error message
         * @param <T> the type of data returned (can be null for failure)
         */
        public static <T> ValidationResult<T> failure(String errorMessage) {
            return new ValidationResult<>(false, errorMessage, null);
        }

        /**
         * Checks if the validation was successful.
         * @return true if validation was successful, false otherwise
         */
        public boolean isSuccess() {
            return successful;
        }

        /**
         * Checks if the validation failed.
         * @return true if validation failed, false otherwise
         */
        public boolean isFailure() {
            return !successful;
        }

        /**
         * Gets the error message if validation failed.
         * @return the error message, or null if validation was successful
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Gets the data returned if validation was successful.
         * @return the data, or null if validation failed
         */
        public T getData() {
            return data;
        }

        /**
         * Checks if there is data available in the result.
         * @return true if data is present, false otherwise
         */
        public boolean hasData() {
            return data != null;
        }
    }
}