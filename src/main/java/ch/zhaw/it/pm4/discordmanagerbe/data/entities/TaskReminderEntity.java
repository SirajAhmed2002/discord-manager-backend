package ch.zhaw.it.pm4.discordmanagerbe.data.entities;

import jakarta.persistence.*;

/**
 * Entity representing a reminder for a specific task.
 * Links reminder times to their corresponding tasks.
 */
@Entity
@Table(name = "task_reminders")
public class TaskReminderEntity {

    /** Unique identifier for the reminder */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reminder timestamp in milliseconds */
    private Long reminderTime;

    /** Task this reminder belongs to */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false, foreignKey = @ForeignKey(name = "fk_task_reminder_task"))
    private TaskEntity task;

    /**
     * Sets the associated task.
     *
     * @param task the task this reminder belongs to
     */
    public void setTask(TaskEntity task) {
        this.task = task;
    }

    /**
     * Sets the reminder timestamp.
     *
     * @param reminderTime reminder time in milliseconds
     */
    public void setReminderTime(Long reminderTime) {
        this.reminderTime = reminderTime;
    }

    /**
     * Gets the reminder ID.
     *
     * @return the reminder ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the reminder timestamp.
     *
     * @return reminder time in milliseconds
     */
    public Long getReminderTime(){
        return reminderTime;
    }

    /**
     * Gets the associated task.
     *
     * @return the task this reminder belongs to
     */
    public TaskEntity getTask(){
        return task;
    }
}