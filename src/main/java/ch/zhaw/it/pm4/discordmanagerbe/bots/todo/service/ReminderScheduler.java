package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.JdaBotEntry;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.JdaBotService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.ToDoTrackerJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskEntity;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskReminderEntity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ToDoConstants.*;

/**
 * Clean scheduler service for processing task reminders and expirations.
 */
@Service
public class ReminderScheduler {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);

    /**
     * Service for task management operations.
     */
    private final TaskService taskService;

    /**
     * Service for managing JDA bot instances.
     */
    private final JdaBotService jdaBotService;

    /**
     * Constructor for ReminderScheduler.
     * @param jdaBotService Service for managing JDA bot instances
     * @param taskService Service for task management operations
     */
    @Autowired
    public ReminderScheduler(JdaBotService jdaBotService, TaskService taskService) {
        this.jdaBotService = jdaBotService;
        this.taskService = taskService;
    }

    /**
     * Scheduled method to process task reminders and expired tasks.
     */
    @Scheduled(fixedRate = Config.SCHEDULER_INTERVAL_MS)
    @Transactional
    public void processScheduledNotifications() {
        long currentTime = System.currentTimeMillis();
        
        try {
            int reminderCount = processAllDueReminders(currentTime);
            int expiredCount = processAllExpiredTasks(currentTime);
            
            logSchedulerRun(reminderCount, expiredCount);
        } catch (Exception e) {
            logger.error("Scheduler execution failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Processes all task reminders that are due.
     * @param currentTime the current system time in milliseconds
     * @return the number of successfully processed reminders
     */
    private int processAllDueReminders(long currentTime) {
        List<TaskReminderEntity> dueReminders = taskService.findDueReminders(currentTime);
        
        if (dueReminders.isEmpty()) {
            return 0;
        }
        
        logger.debug("Processing {} due reminders", dueReminders.size());
        return processReminderList(dueReminders);
    }

    /**
     * Processes a list of task reminders with error isolation.
     * @param reminders the list of TaskReminderEntity objects to process
     * @return the number of successfully processed reminders
     */
    private int processReminderList(List<TaskReminderEntity> reminders) {
        int successCount = 0;
        
        for (TaskReminderEntity reminder : reminders) {
            if (processSingleReminder(reminder)) {
                successCount++;
            }
        }
        
        return successCount;
    }

    /**
     * Processes a single task reminder.
     * @param reminder the TaskReminderEntity to process
     * @return true if the reminder was successfully processed, false otherwise
     */
    private boolean processSingleReminder(TaskReminderEntity reminder) {
        try {
            sendReminderNotification(reminder);
            taskService.removeTaskReminder(reminder.getId());
            
            logReminderSuccess(reminder);
            return true;
        } catch (Exception e) {
            logReminderFailure(reminder, e);
            return false;
        }
    }

    /**
     * Processes all expired tasks.
     * @param currentTime the current system time in milliseconds
     * @return the number of successfully processed expired tasks
     */
    private int processAllExpiredTasks(long currentTime) {
        List<TaskEntity> expiredTasks = taskService.findExpiredTasks(currentTime);
        
        if (expiredTasks.isEmpty()) {
            return 0;
        }
        
        logger.debug("Processing {} expired tasks", expiredTasks.size());
        return processExpiredTaskList(expiredTasks);
    }

    /**
     * Processes a list of expired tasks with error isolation.
     * @param expiredTasks the list of TaskEntity objects representing expired tasks
     * @return the number of successfully processed expired tasks
     */
    private int processExpiredTaskList(List<TaskEntity> expiredTasks) {
        int successCount = 0;
        
        for (TaskEntity task : expiredTasks) {
            if (processSingleExpiredTask(task)) {
                successCount++;
            }
        }
        
        return successCount;
    }

    /**
     * Processes a single expired task.
     * @param task the TaskEntity representing the expired task
     * @return true if the task was successfully processed, false otherwise
     */
    private boolean processSingleExpiredTask(TaskEntity task) {
        try {
            sendExpirationNotification(task);
            taskService.removeTask(task.getTaskId());
            
            logTaskExpirationSuccess(task);
            return true;
        } catch (Exception e) {
            logTaskExpirationFailure(task, e);
            return false;
        }
    }

    /**
     * Send reminder notification to user.
     * @param reminder the TaskReminderEntity containing reminder details
     */
    private void sendReminderNotification(TaskReminderEntity reminder) {
        TaskEntity task = reminder.getTask();
        String message = buildReminderMessage(task);
        
        dispatchNotificationToUser(task.getUserId(), task.getTitle(), message);
    }

    /**
     * Send expiration notification to user.
     * @param task the TaskEntity representing the expired task
     */
    private void sendExpirationNotification(TaskEntity task) {
        String message = buildExpirationMessage(task);
        
        dispatchNotificationToUser(task.getUserId(), task.getTitle(), message);
    }

    /**
     * Dispatches a notification to a user via the ToDoTrackerJdaBot.
     * @param userId the ID of the user to notify
     * @param title the title of the notification
     * @param message the message content of the notification
     */
    private void dispatchNotificationToUser(String userId, String title, String message) {
        ToDoTrackerJdaBot bot = retrieveToDoBot();
        bot.sendPrivateMessage(userId, title, message);
    }

    /**
     * Build the reminder message for a task.
     * @param task the TaskEntity for which the reminder is being built
     * @return the formatted reminder message
     */
    private String buildReminderMessage(TaskEntity task) {
        return Notifications.REMINDER_PREFIX + task.getDescription();
    }

    /**
     * Build the expiration message for a task.
     * @param task the TaskEntity for which the expiration message is being built
     * @return the formatted expiration message
     */
    private String buildExpirationMessage(TaskEntity task) {
        return Notifications.TASK_EXPIRED_PREFIX + task.getDescription();
    }

    /**
     * Retrieves the ToDoTrackerJdaBot instance.
     * @return the ToDoTrackerJdaBot instance
     */
    private ToDoTrackerJdaBot retrieveToDoBot() {
        Optional<JdaBotEntry> botEntry = jdaBotService.getBot(SlashCommandBotType.TODO);
        
        if (botEntry.isEmpty()) {
            throw new BotUnavailableException("ToDoTrackerJdaBot not found");
        }
        
        try {
            return (ToDoTrackerJdaBot) botEntry.get().getBotInstance();
        } catch (ClassCastException e) {
            throw new BotUnavailableException("Invalid bot instance type", e);
        }
    }

    /**
     * Logs the scheduler run details.
     * @param reminderCount the number of reminders processed
     * @param expiredCount the number of expired tasks processed
     */
    private void logSchedulerRun(int reminderCount, int expiredCount) {
        if (reminderCount > 0 || expiredCount > 0) {
            logger.info("Scheduler processed {} reminders and {} expired tasks", 
                       reminderCount, expiredCount);
        }
    }

    /**
     * Logs a successful reminder notification.
     * @param reminder the TaskReminderEntity for which the notification was sent
     */
    private void logReminderSuccess(TaskReminderEntity reminder) {
        TaskEntity task = reminder.getTask();
        logger.debug("Sent reminder notification for task '{}' to user {}", 
                    task.getTitle(), task.getUserId());
    }

    /**
     * Logs a failure when processing a reminder.
     * @param reminder the TaskReminderEntity that failed to process
     * @param e the exception that occurred
     */
    private void logReminderFailure(TaskReminderEntity reminder, Exception e) {
        logger.error("Failed to process reminder with ID {}: {}", 
                    reminder.getId(), e.getMessage());
    }

    /**
     * Logs a successful task expiration notification.
     * @param task the TaskEntity for which the expiration notification was sent
     */
    private void logTaskExpirationSuccess(TaskEntity task) {
        logger.debug("Sent expiration notification for task '{}' to user {}", 
                    task.getTitle(), task.getUserId());
    }

    /**
     * Logs a failure when processing an expired task.
     * @param task the TaskEntity that failed to process
     * @param e the exception that occurred
     */
    private void logTaskExpirationFailure(TaskEntity task, Exception e) {
        logger.error("Failed to process expired task with ID {}: {}", 
                    task.getTaskId(), e.getMessage());
    }

    /**
     * Exception thrown when the ToDoTrackerJdaBot is unavailable.
     */
    public static class BotUnavailableException extends RuntimeException {
        public BotUnavailableException(String message) {
            super(message);
        }
        
        public BotUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}