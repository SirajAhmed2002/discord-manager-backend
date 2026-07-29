package ch.zhaw.it.pm4.discordmanagerbe.data.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity representing a task with title, description, due date, and reminders.
 * Provides formatted string representation for Discord display.
 */
@Entity
@Table(name = "tasks")
public class TaskEntity {

    /** Unique identifier for the task */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    /** Discord user ID who owns this task */
    @Column(nullable = false)
    private String userId;

    /** Title of the task */
    @Column(nullable = false)
    private String title;

    /** Optional description of the task */
    @Column
    private String description;

    /** Due date timestamp in milliseconds */
    @Column(name = "time_to_be_done")
    private long timeToBeDone;

    /** List of reminders associated with this task */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TaskReminderEntity> reminders = new ArrayList<>();

    /**
     * Sets the Discord user ID.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId){
        this.userId = userId;
    }

    /**
     * Sets the task title.
     *
     * @param title the task title
     */
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * Sets the task description.
     *
     * @param description the task description
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Sets the due date timestamp.
     *
     * @param timeToBeDone due date in milliseconds
     */
    public void setTimeToBeDone(long timeToBeDone){
        this.timeToBeDone = timeToBeDone;
    }

    /**
     * Gets the task ID.
     *
     * @return the task ID
     */
    public Long getTaskId(){
        return taskId;
    }

    /**
     * Gets the Discord user ID.
     *
     * @return the user ID
     */
    public String getUserId(){
        return userId;
    }

    /**
     * Gets the task title.
     *
     * @return the task title
     */
    public String getTitle(){
        return title;
    }

    /**
     * Gets the task description.
     *
     * @return the task description
     */
    public String getDescription(){
        return description;
    }

    /**
     * Gets the due date timestamp.
     *
     * @return due date in milliseconds
     */
    public long getTimeToBeDone(){
        return timeToBeDone;
    }

    /**
     * Gets the list of reminders.
     *
     * @return the reminders list
     */
    public List<TaskReminderEntity> getReminders(){
        return reminders;
    }

    /**
     * Formats a timestamp to readable date-time string.
     * Returns empty string for Long.MAX_VALUE.
     *
     * @param dateTime timestamp in milliseconds
     * @return formatted date-time string
     */
    private String getFormatedDateTime(long dateTime){
        return (dateTime == Long.MAX_VALUE) ? "" : LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    /**
     * Returns a formatted string representation for Discord display.
     * Includes title, description, due date, and reminder times.
     *
     * @return formatted task information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Titel:** ").append(title).append("\n");
        sb.append("**Beschreibung:** ").append(description).append("\n");
        sb.append("**Fälligkeitsdatum:** ").append(getFormatedDateTime(timeToBeDone)).append("\n");
        sb.append("**Reminder:**\n")
                .append(reminders.stream()
                        .map(reminder -> getFormatedDateTime(reminder.getReminderTime()))
                        .collect(Collectors.joining(",\n")));

        return sb.toString();
    }
}