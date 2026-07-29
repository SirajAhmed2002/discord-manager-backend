package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service;

import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.model.Task;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskEntity;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskReminderEntity;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.TaskReminderRepository;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskReminderRepository taskReminderRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskEntity testTaskEntity;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "user123";

        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTimeToBeDone(System.currentTimeMillis() + 3600000); // 1 hour from now

        testTaskEntity = new TaskEntity();
        testTaskEntity.setUserId(testUserId);
        testTaskEntity.setTitle("Test Task");
        testTaskEntity.setDescription("Test Description");
        testTaskEntity.setTimeToBeDone(System.currentTimeMillis() + 3600000);
    }

    @Nested
    @DisplayName("Task Creation Tests")
    class TaskCreationTests {

        @Test
        @DisplayName("Should create task successfully with valid data")
        void shouldCreateTaskSuccessfully() {
            // Given
            when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTaskEntity);

            // When
            TaskEntity result = taskService.createTask(testTask, testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getUserId()).isEqualTo(testUserId);

            verify(taskRepository).save(argThat(entity ->
                    entity.getTitle().equals("Test Task") &&
                            entity.getUserId().equals(testUserId) &&
                            entity.getDescription().equals("Test Description")
            ));
        }

        @Test
        @DisplayName("Should throw exception when task is null")
        void shouldThrowExceptionWhenTaskIsNull() {
            // When & Then
            assertThatThrownBy(() -> taskService.createTask(null, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task data cannot be null");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> taskService.createTask(testTask, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User ID cannot be null or empty");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when userId is empty")
        void shouldThrowExceptionWhenUserIdIsEmpty() {
            // When & Then
            assertThatThrownBy(() -> taskService.createTask(testTask, "  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User ID cannot be null or empty");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when task title is null")
        void shouldThrowExceptionWhenTitleIsNull() {
            // Given
            testTask.setTitle(null);

            // When & Then
            assertThatThrownBy(() -> taskService.createTask(testTask, testUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task title cannot be null or empty");

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should sanitize text input")
        void shouldSanitizeTextInput() {
            // Given
            testTask.setTitle("  Test Task  ");
            testTask.setDescription("  Test Description  ");
            when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTaskEntity);

            // When
            taskService.createTask(testTask, testUserId);

            // Then
            verify(taskRepository).save(argThat(entity ->
                    entity.getTitle().equals("Test Task") &&
                            entity.getDescription().equals("Test Description")
            ));
        }
    }

    @Nested
    @DisplayName("Task Reminder Creation Tests")
    class ReminderCreationTests {

        @Test
        @DisplayName("Should create reminders successfully")
        void shouldCreateRemindersSuccessfully() {
            // Given
            Long taskId = 1L;
            List<Long> reminderTimes = Arrays.asList(3600000L, 7200000L); // 1 and 2 hours
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTaskEntity));

            // When
            taskService.createTaskReminders(taskId, reminderTimes);

            // Then
            verify(taskReminderRepository, times(2)).save(any(TaskReminderEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when taskId is null")
        void shouldThrowExceptionWhenTaskIdIsNull() {
            // Given
            List<Long> reminderTimes = Arrays.asList(3600000L);

            // When & Then
            assertThatThrownBy(() -> taskService.createTaskReminders(null, reminderTimes))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task ID cannot be null");

            verify(taskReminderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when reminderTimes is null")
        void shouldThrowExceptionWhenReminderTimesIsNull() {
            // When & Then
            assertThatThrownBy(() -> taskService.createTaskReminders(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Reminder times cannot be null or empty");

            verify(taskReminderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            Long taskId = 999L;
            List<Long> reminderTimes = Arrays.asList(3600000L);
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.createTaskReminders(taskId, reminderTimes))
                    .isInstanceOf(TaskService.TaskNotFoundException.class)
                    .hasMessage("Task not found with ID: " + taskId);

            verify(taskReminderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should skip invalid reminder times")
        void shouldSkipInvalidReminderTimes() {
            // Given
            Long taskId = 1L;
            List<Long> reminderTimes = Arrays.asList(3600000L, null, 0L, -1000L); // Mix of valid and invalid
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTaskEntity));

            // When
            taskService.createTaskReminders(taskId, reminderTimes);

            // Then
            verify(taskReminderRepository, times(1)).save(any(TaskReminderEntity.class)); // Only one valid reminder
        }
    }

    @Nested
    @DisplayName("Task Retrieval Tests")
    class TaskRetrievalTests {

        @Test
        @DisplayName("Should find newest task for user")
        void shouldFindNewestTaskForUser() {
            // Given
            when(taskRepository.findNewestTaskByUserId(testUserId)).thenReturn(testTaskEntity);

            // When
            Optional<TaskEntity> result = taskService.findNewestTaskForUser(testUserId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testTaskEntity);
            verify(taskRepository).findNewestTaskByUserId(testUserId);
        }

        @Test
        @DisplayName("Should return empty when no newest task found")
        void shouldReturnEmptyWhenNoNewestTaskFound() {
            // Given
            when(taskRepository.findNewestTaskByUserId(testUserId)).thenReturn(null);

            // When
            Optional<TaskEntity> result = taskService.findNewestTaskForUser(testUserId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for invalid userId")
        void shouldReturnEmptyForInvalidUserId() {
            // When
            Optional<TaskEntity> result1 = taskService.findNewestTaskForUser(null);
            Optional<TaskEntity> result2 = taskService.findNewestTaskForUser("  ");

            // Then
            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
            verify(taskRepository, never()).findNewestTaskByUserId(any());
        }

        @Test
        @DisplayName("Should find all tasks for user")
        void shouldFindAllTasksForUser() {
            // Given
            List<TaskEntity> expectedTasks = Arrays.asList(testTaskEntity);
            when(taskRepository.findAllTaskByUserId(testUserId)).thenReturn(expectedTasks);

            // When
            List<TaskEntity> result = taskService.findAllTasksForUser(testUserId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result).containsExactly(testTaskEntity);
            verify(taskRepository).findAllTaskByUserId(testUserId);
        }

        @Test
        @DisplayName("Should return empty list for invalid userId")
        void shouldReturnEmptyListForInvalidUserId() {
            // When
            List<TaskEntity> result1 = taskService.findAllTasksForUser(null);
            List<TaskEntity> result2 = taskService.findAllTasksForUser("  ");

            // Then
            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
            verify(taskRepository, never()).findAllTaskByUserId(any());
        }

        @Test
        @DisplayName("Should find task by id")
        void shouldFindTaskById() {
            // Given
            Long taskId = 1L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTaskEntity));

            // When
            Optional<TaskEntity> result = taskService.findTaskById(taskId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(testTaskEntity);
        }

        @Test
        @DisplayName("Should return empty for null taskId")
        void shouldReturnEmptyForNullTaskId() {
            // When
            Optional<TaskEntity> result = taskService.findTaskById(null);

            // Then
            assertThat(result).isEmpty();
            verify(taskRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Task Deletion Tests")
    class TaskDeletionTests {

        @Test
        @DisplayName("Should remove task successfully")
        void shouldRemoveTaskSuccessfully() {
            // Given
            Long taskId = 1L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTaskEntity));

            // When
            taskService.removeTask(taskId);

            // Then
            verify(taskRepository).delete(testTaskEntity);
        }

        @Test
        @DisplayName("Should throw exception when task not found for deletion")
        void shouldThrowExceptionWhenTaskNotFoundForDeletion() {
            // Given
            Long taskId = 999L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.removeTask(taskId))
                    .isInstanceOf(TaskService.TaskNotFoundException.class)
                    .hasMessage("Task not found with ID: " + taskId);

            verify(taskRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw exception when taskId is null for deletion")
        void shouldThrowExceptionWhenTaskIdIsNullForDeletion() {
            // When & Then
            assertThatThrownBy(() -> taskService.removeTask(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task ID cannot be null");

            verify(taskRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should remove task reminder successfully")
        void shouldRemoveTaskReminderSuccessfully() {
            // Given
            Long reminderId = 1L;

            // When
            taskService.removeTaskReminder(reminderId);

            // Then
            verify(taskReminderRepository).deleteTaskReminderById(reminderId);
        }

        @Test
        @DisplayName("Should throw exception when reminderId is null")
        void shouldThrowExceptionWhenReminderIdIsNull() {
            // When & Then
            assertThatThrownBy(() -> taskService.removeTaskReminder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task reminder ID cannot be null");

            verify(taskReminderRepository, never()).deleteTaskReminderById(any());
        }
    }

    @Nested
    @DisplayName("Scheduler Support Tests")
    class SchedulerSupportTests {

        @Test
        @DisplayName("Should find due reminders")
        void shouldFindDueReminders() {
            // Given
            long currentTime = System.currentTimeMillis();
            List<TaskReminderEntity> expectedReminders = Arrays.asList(new TaskReminderEntity());
            when(taskReminderRepository.findAllDueReminder(currentTime)).thenReturn(expectedReminders);

            // When
            List<TaskReminderEntity> result = taskService.findDueReminders(currentTime);

            // Then
            assertThat(result).isEqualTo(expectedReminders);
            verify(taskReminderRepository).findAllDueReminder(currentTime);
        }

        @Test
        @DisplayName("Should find expired tasks")
        void shouldFindExpiredTasks() {
            // Given
            long currentTime = System.currentTimeMillis();
            List<TaskEntity> expectedTasks = Arrays.asList(testTaskEntity);
            when(taskRepository.findAllDueTasks(currentTime)).thenReturn(expectedTasks);

            // When
            List<TaskEntity> result = taskService.findExpiredTasks(currentTime);

            // Then
            assertThat(result).isEqualTo(expectedTasks);
            verify(taskRepository).findAllDueTasks(currentTime);
        }
    }
}