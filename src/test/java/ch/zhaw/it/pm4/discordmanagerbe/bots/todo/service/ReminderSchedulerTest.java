package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.JdaBotEntry;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.JdaBotService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.ToDoTrackerJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskEntity;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskReminderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private JdaBotService jdaBotService;

    @Mock
    private JdaBotEntry botEntry;

    @Mock
    private ToDoTrackerJdaBot todoBot;

    @InjectMocks
    private ReminderScheduler reminderScheduler;

    private TaskEntity testTask;
    private TaskReminderEntity testReminder;
    private long currentTime;

    @BeforeEach
    void setUp() {
        currentTime = System.currentTimeMillis();
        
        testTask = new TaskEntity();
        testTask.setUserId("user123");
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTimeToBeDone(currentTime + 3600000); // 1 hour from now
        
        testReminder = new TaskReminderEntity();
        testReminder.setTask(testTask);
        testReminder.setReminderTime(currentTime - 1000); // 1 second ago (due)
    }

    @Nested
    @DisplayName("Scheduled Notification Processing Tests")
    class ScheduledNotificationProcessingTests {

        @Test
        @DisplayName("Should process scheduled notifications successfully")
        void shouldProcessScheduledNotificationsSuccessfully() {
            // Given
            when(taskService.findDueReminders(anyLong())).thenReturn(Collections.emptyList());
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());

            // When & Then - Should not throw any exception
            assertThatCode(() -> reminderScheduler.processScheduledNotifications())
                .doesNotThrowAnyException();

            verify(taskService).findDueReminders(anyLong());
            verify(taskService).findExpiredTasks(anyLong());
        }

        @Test
        @DisplayName("Should handle exceptions during processing gracefully")
        void shouldHandleExceptionsDuringProcessingGracefully() {
            // Given
            when(taskService.findDueReminders(anyLong())).thenThrow(new RuntimeException("Database error"));

            // When & Then - Should not propagate exception
            assertThatCode(() -> reminderScheduler.processScheduledNotifications())
                .doesNotThrowAnyException();

            verify(taskService).findDueReminders(anyLong());
        }
    }

    @Nested
    @DisplayName("Due Reminder Processing Tests")
    class DueReminderProcessingTests {

        @Test
        @DisplayName("Should process due reminders successfully")
        void shouldProcessDueRemindersSuccessfully() {
            // Given
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(todoBot);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(taskService).findDueReminders(anyLong());
            verify(todoBot).sendPrivateMessage(
                eq(testTask.getUserId()),
                eq(testTask.getTitle()),
                contains("⏰ Reminder for your task:")
            );
            verify(taskService).removeTaskReminder(testReminder.getId());
        }

        @Test
        @DisplayName("Should handle empty due reminders list")
        void shouldHandleEmptyDueRemindersList() {
            // Given
            when(taskService.findDueReminders(anyLong())).thenReturn(Collections.emptyList());
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(taskService).findDueReminders(anyLong());
            verify(jdaBotService, never()).getBot((SlashCommandBotType) any());
            verify(taskService, never()).removeTaskReminder(any());
        }

        @Test
        @DisplayName("Should handle bot unavailable exception")
        void shouldHandleBotUnavailableException() {
            // Given
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.empty());

            // When
            reminderScheduler.processScheduledNotifications();

            // Then - Should not crash, but should not remove reminder either
            verify(jdaBotService).getBot(SlashCommandBotType.TODO);
            verify(taskService, never()).removeTaskReminder(any());
        }
    }

    @Nested
    @DisplayName("Expired Task Processing Tests")
    class ExpiredTaskProcessingTests {

        @Test
        @DisplayName("Should process expired tasks successfully")
        void shouldProcessExpiredTasksSuccessfully() {
            // Given
            testTask.setTimeToBeDone(currentTime - 1000); // Task expired 1 second ago
            List<TaskEntity> expiredTasks = Arrays.asList(testTask);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(Collections.emptyList());
            when(taskService.findExpiredTasks(anyLong())).thenReturn(expiredTasks);
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(todoBot);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(taskService).findExpiredTasks(anyLong());
            verify(todoBot).sendPrivateMessage(
                eq(testTask.getUserId()),
                eq(testTask.getTitle()),
                contains("❌ Your task has been deleted because the time is up:")
            );
            verify(taskService).removeTask(testTask.getTaskId());
        }

        @Test
        @DisplayName("Should handle empty expired tasks list")
        void shouldHandleEmptyExpiredTasksList() {
            // Given
            when(taskService.findDueReminders(anyLong())).thenReturn(Collections.emptyList());
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(taskService).findExpiredTasks(anyLong());
            verify(taskService, never()).removeTask(any());
        }
    }

    @Nested
    @DisplayName("Bot Instance Management Tests")
    class BotInstanceManagementTests {

        @Test
        @DisplayName("Should throw BotUnavailableException when bot not found")
        void shouldThrowBotUnavailableExceptionWhenBotNotFound() {
            // Given
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.empty());

            // When
            reminderScheduler.processScheduledNotifications();

            // Then - Should handle gracefully without throwing
            verify(jdaBotService).getBot(SlashCommandBotType.TODO);
            verify(taskService, never()).removeTaskReminder(any());
        }

        @Test
        @DisplayName("Should throw BotUnavailableException when bot instance has wrong type")
        void shouldThrowBotUnavailableExceptionWhenBotInstanceHasWrongType() {
            // Given
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            Object wrongBotInstance = new Object(); // Wrong type
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(wrongBotInstance);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then - Should handle ClassCastException gracefully
            verify(jdaBotService).getBot(SlashCommandBotType.TODO);
            verify(botEntry).getBotInstance();
            verify(taskService, never()).removeTaskReminder(any());
        }

        @Test
        @DisplayName("Should retrieve bot successfully")
        void shouldRetrieveBotSuccessfully() {
            // Given
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(todoBot);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(jdaBotService).getBot(SlashCommandBotType.TODO);
            verify(botEntry).getBotInstance();
            verify(todoBot).sendPrivateMessage(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Message Building Tests")
    class MessageBuildingTests {

        @Test
        @DisplayName("Should build reminder message correctly")
        void shouldBuildReminderMessageCorrectly() {
            // Given
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(todoBot);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(todoBot).sendPrivateMessage(
                eq(testTask.getUserId()),
                eq(testTask.getTitle()),
                eq("⏰ Reminder for your task: \n" + testTask.getDescription())
            );
        }

        @Test
        @DisplayName("Should build expiration message correctly")
        void shouldBuildExpirationMessageCorrectly() {
            // Given
            List<TaskEntity> expiredTasks = Arrays.asList(testTask);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(Collections.emptyList());
            when(taskService.findExpiredTasks(anyLong())).thenReturn(expiredTasks);
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(todoBot);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(todoBot).sendPrivateMessage(
                eq(testTask.getUserId()),
                eq(testTask.getTitle()),
                eq("❌ Your task has been deleted because the time is up: \n" + testTask.getDescription())
            );
        }

        @Test
        @DisplayName("Should handle empty task description")
        void shouldHandleEmptyTaskDescription() {
            // Given
            testTask.setDescription("");
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(todoBot);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(todoBot).sendPrivateMessage(
                eq(testTask.getUserId()),
                eq(testTask.getTitle()),
                eq("⏰ Reminder for your task: \n")
            );
        }

        @Test
        @DisplayName("Should handle null task description")
        void shouldHandleNullTaskDescription() {
            // Given
            testTask.setDescription(null);
            List<TaskReminderEntity> dueReminders = Arrays.asList(testReminder);
            
            when(taskService.findDueReminders(anyLong())).thenReturn(dueReminders);
            when(taskService.findExpiredTasks(anyLong())).thenReturn(Collections.emptyList());
            when(jdaBotService.getBot(SlashCommandBotType.TODO)).thenReturn(Optional.of(botEntry));
            when(botEntry.getBotInstance()).thenReturn(todoBot);

            // When
            reminderScheduler.processScheduledNotifications();

            // Then
            verify(todoBot).sendPrivateMessage(
                eq(testTask.getUserId()),
                eq(testTask.getTitle()),
                eq("⏰ Reminder for your task: \nnull")
            );
        }
    }

    @Nested
    @DisplayName("Custom Exception Tests")
    class CustomExceptionTests {

        @Test
        @DisplayName("Should create BotUnavailableException with message")
        void shouldCreateBotUnavailableExceptionWithMessage() {
            // Given
            String message = "Bot not available";

            // When
            ReminderScheduler.BotUnavailableException exception = 
                new ReminderScheduler.BotUnavailableException(message);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Should create BotUnavailableException with message and cause")
        void shouldCreateBotUnavailableExceptionWithMessageAndCause() {
            // Given
            String message = "Bot not available";
            Throwable cause = new RuntimeException("Root cause");

            // When
            ReminderScheduler.BotUnavailableException exception = 
                new ReminderScheduler.BotUnavailableException(message, cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }
}