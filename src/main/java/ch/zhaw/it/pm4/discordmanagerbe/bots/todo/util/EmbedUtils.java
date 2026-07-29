package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ToDoConstants.*;

/**
 * Clean utility for creating Discord embeds.
 */
public final class EmbedUtils {

    /**
     * Utility class should not be instantiated.
     */
    private EmbedUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Creates a generic embed with title, description, and color.
     * @param title title of the embed
     * @param description description of the embed
     * @param color color of the embed
     * @return a MessageEmbed object
     */
    public static MessageEmbed createEmbed(String title, String description, Color color) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .build();
    }

    /**
     * Creates a success embed with a title and description.
     * @param title title of the embed
     * @param description description of the embed
     * @return a MessageEmbed object with success color
     */
    public static MessageEmbed createSuccessEmbed(String title, String description) {
        return createEmbed(title, description, COLOR_SUCCESS);
    }

    /**
     * Creates an error embed with a title and description.
     * @param title title of the embed
     * @param description description of the embed
     * @return a MessageEmbed object with error color
     */
    public static MessageEmbed createErrorEmbed(String title, String description) {
        return createEmbed(title, description, COLOR_ERROR);
    }

    /**
     * Creates a warning embed with a title and description.
     * @param title title of the embed
     * @param description description of the embed
     * @return a MessageEmbed object with warning color
     */
    public static MessageEmbed createWarningEmbed(String title, String description) {
        return createEmbed(title, description, COLOR_WARNING);
    }

    /**
     * Creates an informational embed with a title and description.
     * @param title title of the embed
     * @param description description of the embed
     * @return a MessageEmbed object with primary color
     */
    public static MessageEmbed createInfoEmbed(String title, String description) {
        return createEmbed(title, description, COLOR_PRIMARY);
    }

    /**
     * Creates a task display embed with a title and content.
     * @param taskTitle title of the task
     * @param taskContent content of the task
     * @return a MessageEmbed object for displaying task information
     */
    public static MessageEmbed createTaskDisplayEmbed(String taskTitle, String taskContent) {
        return createInfoEmbed(taskTitle, taskContent);
    }

    /**
     * Creates an embed for displaying a list of tasks.
     * @param taskList the list of tasks to display
     * @return a MessageEmbed object containing the task list
     */
    public static MessageEmbed createTaskListEmbed(String taskList) {
        return createInfoEmbed(Messages.YOUR_TASKS, taskList);
    }

    /**
     * Creates an embed for session expiration.
     * @return a MessageEmbed object indicating session expiration
     */
    public static MessageEmbed createSessionExpiredEmbed() {
        return createErrorEmbed(Messages.SESSION_EXPIRED, Messages.RESTART_PROCESS);
    }

    /**
     * Creates an embed indicating no tasks were found.
     * @return a MessageEmbed object indicating no tasks found
     */
    public static MessageEmbed createNoTasksFoundEmbed() {
        return createErrorEmbed(Messages.NO_TASKS_FOUND, Messages.NO_TASKS_CREATED);
    }

    /**
     * Creates an embed indicating a task was not found.
     * @return a MessageEmbed object indicating task not found
     */
    public static MessageEmbed createTaskNotFoundEmbed() {
        return createErrorEmbed(Messages.ERROR, Messages.TASK_NOT_FOUND);
    }

    /**
     * Creates an embed indicating an invalid task title.
     * @return a MessageEmbed object indicating invalid title
     */
    public static MessageEmbed createInvalidTitleEmbed() {
        return createErrorEmbed(Messages.INVALID_TITLE, Messages.TITLE_NOT_EMPTY);
    }

    /**
     * Creates an embed indicating an invalid date.
     * @return a MessageEmbed object indicating invalid date
     */
    public static MessageEmbed createInvalidDateEmbed() {
        return createErrorEmbed(Messages.INVALID_DATE, Messages.DATE_NOT_VALID);
    }

    /**
     * Creates an embed indicating a date in the past.
     * @return a MessageEmbed object indicating date must be in the future
     */
    public static MessageEmbed createDateInPastEmbed() {
        return createErrorEmbed(Messages.DATE_IN_PAST, Messages.DATE_MUST_BE_FUTURE);
    }

    /**
     * Creates an embed indicating an invalid time.
     * @return a MessageEmbed object indicating invalid time
     */
    public static MessageEmbed createInvalidTimeEmbed() {
        return createErrorEmbed(Messages.INVALID_TIME, Messages.TIME_NOT_VALID);
    }

    /**
     * Creates an embed indicating a time in the past.
     * @return a MessageEmbed object indicating time must be in the future
     */
    public static MessageEmbed createTimeInPastEmbed() {
        return createErrorEmbed(Messages.TIME_IN_PAST, Messages.TIME_MUST_BE_FUTURE);
    }

    /**
     * Creates an embed indicating a task was successfully created.
     * @param taskDescription description of the created task
     * @return a MessageEmbed object indicating success
     */
    public static MessageEmbed createTaskCreatedEmbed(String taskDescription) {
        return createSuccessEmbed(Messages.TASK_CREATED, taskDescription);
    }

    /**
     * Creates an embed indicating a task was successfully updated.
     * @param taskTitle title of the updated task
     * @return a MessageEmbed object indicating success
     */
    public static MessageEmbed createTaskRemovedEmbed(String taskTitle) {
        String description = String.format(Messages.TASK_REMOVED_SUCCESS, taskTitle);
        return createSuccessEmbed(Messages.TASK_REMOVED, description);
    }

    /**
     * Creates an embed for selecting a reminder.
     * @param hasValidationError indicates if there was a validation error
     * @return a MessageEmbed object for reminder selection
     */
    public static MessageEmbed createReminderSelectionEmbed(boolean hasValidationError) {
        return hasValidationError 
            ? createInvalidReminderEmbed()
            : createAddReminderEmbed();
    }

    /**
     * Creates an embed indicating an invalid reminder.
     * @return a MessageEmbed object indicating invalid reminder
     */
    private static MessageEmbed createInvalidReminderEmbed() {
        return createErrorEmbed(Messages.INVALID_REMINDER, Messages.REMINDER_IN_PAST);
    }

    /**
     * Creates an embed for adding a reminder.
     * @return a MessageEmbed object for adding a reminder
     */
    private static MessageEmbed createAddReminderEmbed() {
        return createWarningEmbed(Messages.ADD_REMINDER, Messages.REMINDER_QUESTION);
    }
}