package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.model.Task;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskEntity;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskReminderEntity;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.TaskReminderRepository;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Clean service for task management operations.
 */
@Service
public class TaskService {

    /**
     * Logger for TaskService operations.
     */
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    /**
     * Repositories for task and reminder management.
     */
    private final TaskRepository taskRepository;

    /**
     * Repository for task reminders.
     */
    private final TaskReminderRepository taskReminderRepository;

    /**
     * Constructor for TaskService.
     * @param taskRepository taskRepository
     * @param taskReminderRepository taskReminderRepository
     */
    public TaskService(TaskRepository taskRepository, TaskReminderRepository taskReminderRepository) {
        this.taskRepository = taskRepository;
        this.taskReminderRepository = taskReminderRepository;
    }

    /**
     * Creates a new task for a user.
     * @param taskData the task data to be saved
     * @param userId the ID of the user creating the task
     * @return the saved TaskEntity
     */
    @Transactional
    public TaskEntity createTask(Task taskData, String userId) {
        validateTaskCreationParameters(taskData, userId);
        
        TaskEntity taskEntity = buildTaskEntity(taskData, userId);
        TaskEntity savedTask = taskRepository.save(taskEntity);
        
        logTaskCreation(savedTask, userId);
        return savedTask;
    }

    /**
     * Creates task reminders for a specific task.
     * @param taskId the ID of the task to create reminders for
     * @param reminderTimes list of reminder times in milliseconds
     */
    @Transactional
    public void createTaskReminders(Long taskId, List<Long> reminderTimes) {
        validateReminderCreationParameters(taskId, reminderTimes);
        
        TaskEntity task = findExistingTask(taskId);
        int createdCount = createIndividualReminders(task, reminderTimes);
        
        logReminderCreation(taskId, createdCount);
    }

    /**
     * Finds the newest task for a specific user.
     * @param userId the ID of the user to find the newest task for
     * @return Optional containing the newest TaskEntity if found, otherwise empty
     */
    public Optional<TaskEntity> findNewestTaskForUser(String userId) {
        if (isInvalidUserId(userId)) {
            return Optional.empty();
        }
        
        TaskEntity newestTask = taskRepository.findNewestTaskByUserId(userId);
        return Optional.ofNullable(newestTask);
    }

    /**
     * Finds all tasks for a specific user.
     * Clean code: early return for invalid user ID, consistent return type.
     * @param userId the ID of the user to find tasks for
     * @return List of TaskEntity objects for the user, or empty list if no tasks found
     */
    public List<TaskEntity> findAllTasksForUser(String userId) {
        if (isInvalidUserId(userId)) {
            return List.of();
        }
        
        return taskRepository.findAllTaskByUserId(userId);
    }

    /**
     * Finds a task by its ID.
     * @param taskId the ID of the task to find
     * @return Optional containing the TaskEntity if found, otherwise empty
     */
    public Optional<TaskEntity> findTaskById(Long taskId) {
        if (taskId == null) {
            return Optional.empty();
        }
        
        return taskRepository.findById(taskId);
    }


    /**
     * Removes a specific task by its ID.
     * @param taskId the ID of the task to remove
     */
    @Transactional
    public void removeTask(Long taskId) {
        TaskEntity task = findExistingTask(taskId);
        taskRepository.delete(task);
        
        logTaskDeletion(taskId);
    }

    /**
     * Removes a specific task reminder by its ID.
     * @param taskReminderId the ID of the task reminder to remove
     */
    @Transactional
    public void removeTaskReminder(Long taskReminderId) {
        validateReminderId(taskReminderId);
        
        taskReminderRepository.deleteTaskReminderById(taskReminderId);
        logReminderDeletion(taskReminderId);
    }

    /**
     * Finds all task reminders that are due.
     * @param currentTime the current time in milliseconds to check for due reminders
     * @return List of TaskReminderEntity objects that are due
     */
    public List<TaskReminderEntity> findDueReminders(long currentTime) {
        return taskReminderRepository.findAllDueReminder(currentTime);
    }

    /**
     * Finds all tasks that are due or expired.
     * @param currentTime the current time in milliseconds to check for due tasks
     * @return List of TaskEntity objects that are due or expired
     */
    public List<TaskEntity> findExpiredTasks(long currentTime) {
        return taskRepository.findAllDueTasks(currentTime);
    }

    /**
     * Validates parameters for task creation.
     * @param taskData the task data to validate
     * @param userId the ID of the user creating the task
     */
    private void validateTaskCreationParameters(Task taskData, String userId) {
        if (taskData == null) {
            throw new IllegalArgumentException("Task data cannot be null");
        }
        if (isInvalidUserId(userId)) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (isInvalidTitle(taskData.getTitle())) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
    }

    /**
     * Validates parameters for task reminder creation.
     * @param taskId the ID of the task to create reminders for
     * @param reminderTimes list of reminder times in milliseconds
     */
    private void validateReminderCreationParameters(Long taskId, List<Long> reminderTimes) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (reminderTimes == null || reminderTimes.isEmpty()) {
            throw new IllegalArgumentException("Reminder times cannot be null or empty");
        }
    }

    /**
     * Validates the task reminder ID.
     * @param taskReminderId the ID of the task reminder to validate
     */
    private void validateReminderId(Long taskReminderId) {
        if (taskReminderId == null) {
            throw new IllegalArgumentException("Task reminder ID cannot be null");
        }
    }

    /**
     * Checks if the user ID is invalid.
     * @param userId the ID of the user to check
     * @return true if the user ID is null or empty, false otherwise
     */
    private boolean isInvalidUserId(String userId) {
        return userId == null || userId.trim().isEmpty();
    }

    /**
     * Checks if the task title is invalid.
     * @param title the title of the task to check
     * @return true if the title is null or empty, false otherwise
     */
    private boolean isInvalidTitle(String title) {
        return title == null || title.trim().isEmpty();
    }

    /**
     * Builds a TaskEntity from the provided task data.
     * @param taskData the task data to convert
     * @param userId the ID of the user creating the task
     * @return TaskEntity with sanitized fields
     */
    private TaskEntity buildTaskEntity(Task taskData, String userId) {
        TaskEntity entity = new TaskEntity();
        entity.setUserId(userId);
        entity.setTitle(sanitizeText(taskData.getTitle()));
        entity.setDescription(sanitizeText(taskData.getDescription()));
        entity.setTimeToBeDone(taskData.getTimeToBeDone());
        return entity;
    }

    /**
     * Sanitizes text input by trimming whitespace.
     * @param text the text to sanitize
     * @return trimmed text or empty string if null
     */
    private String sanitizeText(String text) {
        return text != null ? text.trim() : "";
    }

    /**
     * Finds an existing task by its ID.
     * @param taskId the ID of the task to find
     * @return TaskEntity if found
     */
    private TaskEntity findExistingTask(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
    }

    /**
     * Creates individual reminders for a task based on provided reminder times.
     * @param task the TaskEntity for which reminders are created
     * @param reminderTimes list of reminder times in milliseconds
     * @return the number of reminders created
     */
    private int createIndividualReminders(TaskEntity task, List<Long> reminderTimes) {
        int createdCount = 0;
        
        for (Long reminderTime : reminderTimes) {
            if (isValidReminderTime(reminderTime)) {
                TaskReminderEntity reminder = buildReminderEntity(task, reminderTime);
                taskReminderRepository.save(reminder);
                createdCount++;
            }
        }
        
        return createdCount;
    }

    /**
     * Validates if the reminder time is valid.
     * @param reminderTime the reminder time in milliseconds to validate
     * @return true if the reminder time is valid, false otherwise
     */
    private boolean isValidReminderTime(Long reminderTime) {
        return reminderTime != null && reminderTime > 0;
    }

    /**
     * Builds a TaskReminderEntity for a specific task and reminder time.
     * @param task the TaskEntity for which the reminder is created
     * @param reminderTime the reminder time in milliseconds
     * @return TaskReminderEntity with task and reminder time set
     */
    private TaskReminderEntity buildReminderEntity(TaskEntity task, Long reminderTime) {
        TaskReminderEntity reminder = new TaskReminderEntity();
        reminder.setTask(task);
        reminder.setReminderTime(reminderTime);
        return reminder;
    }

    /**
     * Logs the creation of a task for a user.
     * @param savedTask the saved TaskEntity
     * @param userId the ID of the user who created the task
     */
    private void logTaskCreation(TaskEntity savedTask, String userId) {
        logger.debug("Created task with ID {} for user {}", savedTask.getTaskId(), userId);
    }

    /**
     * Logs the creation of task reminders.
     * @param taskId the ID of the task for which reminders were created
     * @param createdCount the number of reminders created
     */
    private void logReminderCreation(Long taskId, int createdCount) {
        logger.debug("Created {} reminders for task ID {}", createdCount, taskId);
    }

    /**
     * Logs the deletion of a task.
     * @param taskId the ID of the task that was deleted
     */
    private void logTaskDeletion(Long taskId) {
        logger.debug("Deleted task with ID {}", taskId);
    }

    /**
     * Logs the deletion of a task reminder.
     * @param taskReminderId the ID of the task reminder that was deleted
     */
    private void logReminderDeletion(Long taskReminderId) {
        logger.debug("Deleted task reminder with ID {}", taskReminderId);
    }

    /**
     * Exception thrown when a task is not found.
     */
    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(String message) {
            super(message);
        }
        
        public TaskNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}