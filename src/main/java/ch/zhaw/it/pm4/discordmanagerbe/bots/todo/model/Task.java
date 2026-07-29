package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.model;

import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ReminderUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task with a title, description, time to be done, and reminders.
 */
public class Task {

    /**
     * The title of the task.
     */
    private String title;

    /**
     * The description of the task.
     */
    private String description;

    /**
     * The time by which the task should be done, represented in milliseconds.
     */
    private long timeToBeDone;

    /**
     * List of reminder times, each represented as the time in milliseconds
     */
    private final List<Long> reminderDateTime = new ArrayList<>();

    /**
     * Reminder value, which can be a string representation of the reminder time.
     */
    private String reminderValue;

    /**
     * The unit of the reminder, such as hours, days, or weeks.
     */
    private ReminderUnit reminderUnit;

    /**
     * Default constructor for Task.
     * Initializes an empty task.
     */
    public Task() {
    }

    /**
     * Constructor for Task with parameters.
     * @param title the title of the task
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the description of the task.
     * @param description the description of the task
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the time by which the task should be done.
     * @param timeToBeDone the time in milliseconds
     */
    public void setTimeToBeDone(long timeToBeDone) {
        this.timeToBeDone = timeToBeDone;
    }

    /**
     * Adds a reminder to the task.
     * @param reminder the reminder time in milliseconds before the task's time to be done
     */
    public void addReminder(long reminder) {
        this.reminderDateTime.add(timeToBeDone - reminder);
    }

    /**
     * Sets the reminder value and unit for the task.
     * @param reminderValue the value of the reminder
     */
    public void setReminderValue(String reminderValue) {
        this.reminderValue = reminderValue;
    }

    /**
     * Sets the reminder unit for the task.
     * @param reminderUnit the unit of the reminder (e.g., hours, days, weeks)
     */
    public void setReminderUnit(ReminderUnit reminderUnit) {
        this.reminderUnit = reminderUnit;
    }

    /**
     * Gets the title of the task.
     * @return the title of the task
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the task.
     * @return the description of the task
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the time by which the task should be done.
     * @return the time in milliseconds
     */
    public long getTimeToBeDone() {
        return timeToBeDone;
    }

    /**
     * Gets the list of reminder times for the task.
     * @return a list of reminder times in milliseconds
     */
    public List<Long> getReminderDateTime() {
        return reminderDateTime;
    }

    /**
     * Gets the reminder value for the task.
     * @return the reminder value as a string
     */
    public String getReminderValue() {
        return reminderValue;
    }

    /**
     * Gets the unit of the reminder for the task.
     * @return the reminder unit
     */
    public ReminderUnit getReminderUnit() {
        return reminderUnit;
    }
}