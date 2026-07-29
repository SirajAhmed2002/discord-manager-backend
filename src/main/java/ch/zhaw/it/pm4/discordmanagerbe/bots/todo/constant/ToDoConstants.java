package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant;

import java.awt.Color;

/**
 * Constants used throughout the ToDo tracker bot.
 * Following clean code principles: centralized configuration, meaningful names, and logical grouping.
 */
public final class ToDoConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private ToDoConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Command name add task
     */
    public static final String COMMAND_ADD_TASK = "add-task";

    /**
     * Command name remove task
     */
    public static final String COMMAND_REMOVE_TASK = "remove-task";

    /**
     * Command name list tasks
     */
    public static final String COMMAND_LIST_TASKS = "list-tasks";

    /**
     * Date format
     */
    public static final String DATE_PATTERN = "dd.MM.yyyy";

    /**
     * Time format
     */
    public static final String TIME_PATTERN = "HH:mm";

    /**
     * Default time
     */
    public static final String DEFAULT_TIME = "00:00";
    
    /**
     * Color success
     */
    public static final Color COLOR_SUCCESS = Color.GREEN;

    /**
     * Primary colors used in the bot's UI.
     */
    public static final Color COLOR_PRIMARY = Color.BLUE;

    /**
     * Error and warning colors used in the bot's UI.
     */
    public static final Color COLOR_ERROR = Color.RED;

    /**
     * Warning color used in the bot's UI.
     */
    public static final Color COLOR_WARNING = Color.ORANGE;

    /**
     * Messages used in the ToDo tracker bot.
     */
    public static final class Messages {

        /**
         * Private constructor to prevent instantiation.
         */
        private Messages() {}

        /**
         * Message no tasks found.
         */
        public static final String NO_TASKS_FOUND = "Keine Aufgaben gefunden";

        /**
         * Message indicating no tasks have been created yet.
         */
        public static final String NO_TASKS_CREATED = "Du hast noch keine Aufgaben erstellt.";

        /**
         * Message indicating the task creation process has started.
         */
        public static final String TASK_CREATED = "Aufgabe erstellt";

        /**
         * Message indicating the task has been successfully deleted.
         */
        public static final String TASK_REMOVED = "Aufgabe entfernt";

        /**
         * Message indicating the task has been successfully removed.
         */
        public static final String TASK_REMOVED_SUCCESS = "Die Aufgabe \"%s\" wurde erfolgreich entfernt.";

        /**
         * Message indicating that the task was not found.
         */
        public static final String TASK_NOT_FOUND = "Aufgabe nicht gefunden.";

        /**
         * Message indicating that the session has expired.
         */
        public static final String SESSION_EXPIRED = "Sitzung abgelaufen";

        /**
         * Message prompting the user to restart the process.
         */
        public static final String RESTART_PROCESS = "Bitte starte den Vorgang erneut.";

        /**
         * Message indicating an error occurred during task creation.
         */
        public static final String ERROR = "Fehler";

        /**
         * Message indicating that the task title is invalid.
         */
        public static final String INVALID_TITLE = "Ungültiger Titel";

        /**
         * Message indicating that the task title must not be empty.
         */
        public static final String TITLE_NOT_EMPTY = "Der Titel darf nicht leer sein.";

        /**
         * Message indicating that the date is invalid.
         */
        public static final String INVALID_DATE = "Ungültiges Datum";

        /**
         * Message indicating that the date is not valid.
         */
        public static final String DATE_NOT_VALID = "Das eingegebene Datum ist nicht gültig.";

        /**
         * Message indicating that the date is in the past.
         */
        public static final String DATE_IN_PAST = "Datum in der Vergangenheit";

        /**
         * Message indicating that the date must be in the future.
         */
        public static final String DATE_MUST_BE_FUTURE = "Das Datum muss in der Zukunft liegen.";

        /**
         * Message indicating that the time is invalid.
         */
        public static final String INVALID_TIME = "Ungültige Zeit";

        /**
         * Message indicating that the time is not valid.
         */
        public static final String TIME_NOT_VALID = "Die eingegebene Zeit ist nicht gültig.";

        /**
         * Message indicating that the time is in the past.
         */
        public static final String TIME_IN_PAST = "Zeit in der Vergangenheit";

        /**
         * Message indicating that the time must be in the future.
         */
        public static final String TIME_MUST_BE_FUTURE = "Die Zeit muss in der Zukunft liegen.";

        /**
         * Message indicating that the reminder is invalid.
         */
        public static final String INVALID_REMINDER = "❌ Ungültiger Reminder";

        /**
         * Message indicating that the reminder is in the past.
         */
        public static final String REMINDER_IN_PAST = "Der Reminder liegt in der Vergangenheit. Bitte wähle einen anderen Zeitpunkt.";

        /**
         * Message prompting the user to add a reminder.
         */
        public static final String ADD_REMINDER = "🕒 Reminder hinzufügen";

        /**
         * Message prompting the user to select a reminder time.
         */
        public static final String REMINDER_QUESTION = "Wie viel Zeit vor der Fälligkeit möchtest du erinnert werden?";

        /**
         * Message indicating the user is viewing their tasks.
         */
        public static final String YOUR_TASKS = "Deine Aufgaben";
    }

    /**
     * UI-related constants for the ToDo tracker bot.
     */
    public static final class UI {

        /**
         * Private constructor to prevent instantiation.
         */
        private UI() {}

        /**
         * Placeholder text for task title input.
         */
        public static final String PLACEHOLDER_TASK_TITLE = "Abgabe Arbeit";

        /**
         * Placeholder text for task description input.
         */
        public static final String PLACEHOLDER_SELECT_NUMBER = "Wähle eine Zahl";

        /**
         * Placeholder text for selecting a time unit for reminders.
         */
        public static final String PLACEHOLDER_SELECT_TIME_UNIT = "Wähle eine Zeiteinheit";

        /**
         * Buton label for finishing task creation.
         */
        public static final String BTN_FINISH = "Fertigstellen";

        /**
         * Button label for adding another reminder.
         */
        public static final String BTN_ADD_REMINDER = "Weiterer Reminder";

        /**
         * Button label for skipping reminder creation.
         */
        public static final String BTN_NO_REMINDER = "Ohne Reminder";

        /**
         * Button label for skipping the task creation process.
         */
        public static final String BTN_SKIP = "Überspringen";

        /**
         * Button label for removing a task.
         */
        public static final String BTN_REMOVE_TASK = "Aufgabe entfernen";

        /**
         * Modal titel for adding a new task.
         */
        public static final String MODAL_ADD_TASK = "Aufgabe hinzufügen";

        /**
         * Field labels for task creation.
         */
        public static final String FIELD_TASK_TITLE = "Aufgaben-Titel";

        /**
         * Field labels for task details.
         */
        public static final String FIELD_DESCRIPTION = "Beschreibung";

        /**
         * Field labels for due date and time.
         */
        public static final String FIELD_DUE_DATE = "Fälligkeitsdatum";

        /**
         * Field labels for due time.
         */
        public static final String FIELD_DUE_TIME = "Fälligkeitszeit";

        /**
         * Option hours
         */
        public static final String OPTION_HOURS = "Stunden";

        /**
         * Option days
         */
        public static final String OPTION_DAYS = "Tage";

        /**
         * Option weeks
         */
        public static final String OPTION_WEEKS = "Wochen";

        /**
         * Separator used in task lists to visually separate tasks.
         */
        public static final String TASK_LIST_SEPARATOR = "\n──────────────\n";
    }

    /**
     * Configuration constants for the ToDo bot.
     */
    public static final class Config {
        /**
         * Private constructor to prevent instantiation.
         */
        private Config() {}

        /**
         * Maximum number of reminders allowed for a task.
         */
        public static final int MAX_REMINDER_VALUE = 25;

        /**
         * Delay in seconds before a message is deleted.
         */
        public static final int MESSAGE_DELETE_DELAY_SECONDS = 60;

        /**
         * Interval in milliseconds for the scheduler to run.
         */
        public static final int SCHEDULER_INTERVAL_MS = 30_000;

        /**
         * Maximum lengths for various input fields.
         */
        public static final int DESCRIPTION_MAX_LENGTH = 1000;

        /**
         * Maximum lengths for date and time inputs.
         */
        public static final int DATE_INPUT_MAX_LENGTH = 10;

        /**
         * Maximum length for time input in HH:mm format.
         */
        public static final int TIME_INPUT_MAX_LENGTH = 5;
    }

    /**
     * Constants for notification messages.
     */
    public static final class Notifications {

        /**
         * Private constructor to prevent instantiation.
         */
        private Notifications() {}

        /**
         * Prefix for reminder messages.
         */
        public static final String REMINDER_PREFIX = "⏰ Reminder for your task: \n";

        /**
         * Prefix for task expiration messages.
         */
        public static final String TASK_EXPIRED_PREFIX = "❌ Your task has been deleted because the time is up: \n";
    }
}