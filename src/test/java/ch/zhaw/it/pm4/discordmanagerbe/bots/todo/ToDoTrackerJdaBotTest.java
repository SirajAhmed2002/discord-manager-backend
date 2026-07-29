package ch.zhaw.it.pm4.discordmanagerbe.bots.todo;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.CustomId;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ReminderUnit;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.model.Task;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service.TaskService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service.TaskValidationService;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskEntity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ToDoConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToDoTrackerJdaBot Tests")
class ToDoTrackerJdaBotTest {

    @Mock private JDA jdaBean;
    @Mock private JdaSlashCommandService slashCommandService;
    @Mock private JdaEventListenerService eventListenerService;
    @Mock private TaskService taskService;
    @Mock private TaskValidationService validationService;

    // Event mocks
    @Mock private SlashCommandInteractionEvent slashCommandEvent;
    @Mock private ModalInteractionEvent modalEvent;
    @Mock private ButtonInteractionEvent buttonEvent;
    @Mock private StringSelectInteractionEvent selectEvent;
    @Mock private InteractionHook interactionHook;
    @Mock private ReplyCallbackAction replyAction;
    @Mock private ModalCallbackAction modalAction;
    @Mock private MessageEditCallbackAction messageEditAction;
    @Mock private WebhookMessageCreateAction<Message> webhookCreateAction;
    @Mock private WebhookMessageEditAction<Message> webhookEditAction;
    @Mock private User user;
    @Mock private PrivateChannel privateChannel;
    @Mock private RestAction<PrivateChannel> channelAction;
    @Mock private RestAction<Void> sendAction;

    private ToDoTrackerJdaBot todoBot;
    private TaskEntity testTask;
    private String testUserId;

    @BeforeEach
    void setUp() {
        todoBot = new ToDoTrackerJdaBot(jdaBean, slashCommandService, eventListenerService, 
                                        taskService, validationService);
        
        testUserId = "123456789";
        testTask = new TaskEntity();
        testTask.setUserId(testUserId);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTimeToBeDone(System.currentTimeMillis() + 3600000);

        // Setup common mock behavior
        lenient().when(slashCommandEvent.getUser()).thenReturn(user);
        lenient().when(modalEvent.getUser()).thenReturn(user);
        lenient().when(buttonEvent.getUser()).thenReturn(user);
        lenient().when(selectEvent.getUser()).thenReturn(user);
        lenient().when(user.getId()).thenReturn(testUserId);

        lenient().when(slashCommandEvent.deferReply(anyBoolean())).thenReturn(replyAction);
        lenient().when(modalEvent.deferReply(anyBoolean())).thenReturn(replyAction);
        lenient().when(buttonEvent.deferEdit()).thenReturn(messageEditAction);
        lenient().when(selectEvent.deferEdit()).thenReturn(messageEditAction);

        lenient().when(slashCommandEvent.getHook()).thenReturn(interactionHook);
        lenient().when(modalEvent.getHook()).thenReturn(interactionHook);
        lenient().when(buttonEvent.getHook()).thenReturn(interactionHook);
        lenient().when(selectEvent.getHook()).thenReturn(interactionHook);

        lenient().when(slashCommandEvent.replyModal(any(Modal.class))).thenReturn(modalAction);
        lenient().when(interactionHook.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(webhookCreateAction);
        lenient().when(interactionHook.editOriginalEmbeds(any(MessageEmbed.class))).thenReturn(webhookEditAction);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create bot instance successfully")
        void shouldCreateBotInstanceSuccessfully() {
            // When & Then
            assertThat(todoBot).isNotNull();
        }
    }

    @Nested
    @DisplayName("Command Handler Tests") 
    class CommandHandlerTests {

        @Test
        @DisplayName("Should handle add task command")
        void shouldHandleAddTaskCommand() {
            // When
            todoBot.handleAddTaskCommand(slashCommandEvent);

            // Then
            verify(slashCommandEvent).replyModal(any(Modal.class));
            assertThat(todoBot.getUserTasks()).containsKey(testUserId);
        }

        @Test
        @DisplayName("Should handle remove task command with no tasks")
        void shouldHandleRemoveTaskCommandWithNoTasks() {
            // Given
            when(taskService.findAllTasksForUser(testUserId)).thenReturn(Collections.emptyList());

            // When
            todoBot.handleRemoveTaskCommand(slashCommandEvent);

            // Then
            verify(slashCommandEvent).deferReply(true);
            verify(taskService).findAllTasksForUser(testUserId);
            verify(interactionHook).sendMessageEmbeds(any(MessageEmbed.class));
        }

        @Test
        @DisplayName("Should handle list tasks command")
        void shouldHandleListTasksCommand() {
            // Given
            when(taskService.findAllTasksForUser(testUserId)).thenReturn(Arrays.asList(testTask));

            // When
            todoBot.handleListTasksCommand(slashCommandEvent);

            // Then
            verify(slashCommandEvent).deferReply(true);
            verify(taskService).findAllTasksForUser(testUserId);
            verify(interactionHook).sendMessageEmbeds(any(MessageEmbed.class));
        }
    }

    @Nested
    @DisplayName("Modal Handler Tests")
    class ModalHandlerTests {

        @Mock private ModalMapping titleMapping;
        @Mock private ModalMapping descriptionMapping;
        @Mock private ModalMapping dateMapping;
        @Mock private ModalMapping timeMapping;

        @BeforeEach
        void setUpModal() {
            when(modalEvent.getValue(CustomId.TASK_TITLE.getId())).thenReturn(titleMapping);
            when(titleMapping.getAsString()).thenReturn("Test Task");
            
            when(modalEvent.getValue(CustomId.TASK_DESCRIPTION.getId())).thenReturn(descriptionMapping);
            when(descriptionMapping.getAsString()).thenReturn("Test Description");
            
            when(modalEvent.getValue(CustomId.TASK_DUE_DATE.getId())).thenReturn(dateMapping);
            when(dateMapping.getAsString()).thenReturn("25.12.2025");
            
            when(modalEvent.getValue(CustomId.TASK_DUE_TIME.getId())).thenReturn(timeMapping);
            when(timeMapping.getAsString()).thenReturn("14:30");
        }

        @Test
        @DisplayName("Should handle invalid title")
        void shouldHandleInvalidTitle() {
            // Given
            Task task = new Task();
            todoBot.getUserTasks().put(testUserId, task);
            
            when(validationService.validateTaskTitle("Test Task"))
                .thenReturn(TaskValidationService.ValidationResult.failure("Title invalid"));

            // When
            todoBot.handleTaskModal(modalEvent);

            // Then
            verify(modalEvent).deferReply(true);
            verify(validationService).validateTaskTitle("Test Task");
            verify(interactionHook).sendMessageEmbeds(any(MessageEmbed.class));
        }
    }

    @Nested
    @DisplayName("Select Handler Tests")
    class SelectHandlerTests {

        @Test
        @DisplayName("Should handle reminder value selection")
        void shouldHandleReminderValueSelection() {
            // Given
            Task task = new Task();
            todoBot.getUserTasks().put(testUserId, task);
            when(selectEvent.getValues()).thenReturn(Arrays.asList("5"));

            // When
            todoBot.handleReminderValueSelection(selectEvent);

            // Then
            verify(selectEvent).deferEdit();
            assertThat(task.getReminderValue()).isEqualTo("5");
        }

        @Test
        @DisplayName("Should handle reminder unit selection")
        void shouldHandleReminderUnitSelection() {
            // Given
            Task task = new Task();
            todoBot.getUserTasks().put(testUserId, task);
            when(selectEvent.getValues()).thenReturn(Arrays.asList("hours"));

            // When
            todoBot.handleReminderUnitSelection(selectEvent);

            // Then
            verify(selectEvent).deferEdit();
            assertThat(task.getReminderUnit()).isEqualTo(ReminderUnit.HOURS);
        }
    }

    @Nested
    @DisplayName("Helper Method Tests")
    class HelperMethodTests {

        @Test
        @DisplayName("Should create task modal")
        void shouldCreateTaskModal() {
            // When
            Modal result = todoBot.createTaskModal();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CustomId.TASK_MODAL.getId());
            assertThat(result.getTitle()).isEqualTo(UI.MODAL_ADD_TASK);
            assertThat(result.getComponents()).hasSize(4);
        }

        @Test
        @DisplayName("Should create reminder value menu")
        void shouldCreateReminderValueMenu() {
            // When
            StringSelectMenu result = todoBot.createReminderValueMenu();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CustomId.REMINDER_VALUE.getId());
            assertThat(result.getOptions()).hasSize(Config.MAX_REMINDER_VALUE);
        }

        @Test
        @DisplayName("Should create reminder unit menu")
        void shouldCreateReminderUnitMenu() {
            // When
            StringSelectMenu result = todoBot.createReminderUnitMenu();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CustomId.REMINDER_UNIT.getId());
            assertThat(result.getOptions()).hasSize(3); // Hours, Days, Weeks
        }

        @Test
        @DisplayName("Should validate and add reminder")
        void shouldValidateAndAddReminder() {
            // Given
            Task task = new Task();
            task.setReminderValue("2");
            task.setReminderUnit(ReminderUnit.HOURS);
            task.setTimeToBeDone(System.currentTimeMillis() + 7200000);
            
            when(validationService.validateReminderConfiguration(anyString(), any(Task.class), anyLong()))
                .thenReturn(TaskValidationService.ValidationResult.success(7200000L));

            // When
            boolean result = todoBot.validateAndAddReminder(task);

            // Then
            assertThat(result).isTrue();
            assertThat(task.getReminderDateTime()).hasSize(1);
        }

        @Test
        @DisplayName("Should extract form data")
        void shouldExtractFormData() {
            // Given
            ModalMapping titleMapping = mock(ModalMapping.class);
            ModalMapping descMapping = mock(ModalMapping.class);
            
            when(modalEvent.getValue(CustomId.TASK_TITLE.getId())).thenReturn(titleMapping);
            when(modalEvent.getValue(CustomId.TASK_DESCRIPTION.getId())).thenReturn(descMapping);
            when(modalEvent.getValue(CustomId.TASK_DUE_DATE.getId())).thenReturn(null);
            when(modalEvent.getValue(CustomId.TASK_DUE_TIME.getId())).thenReturn(null);
            
            when(titleMapping.getAsString()).thenReturn("Test Title");
            when(descMapping.getAsString()).thenReturn("Test Description");

            // When
            ToDoTrackerJdaBot.TaskFormData result = todoBot.extractFormData(modalEvent);

            // Then
            assertThat(result.title).isEqualTo("Test Title");
            assertThat(result.description).isEqualTo("Test Description");
            assertThat(result.dueDateStr).isEmpty();
            assertThat(result.dueTimeStr).isEqualTo(DEFAULT_TIME);
        }
    }

    @Nested
    @DisplayName("Private Message Tests")
    class PrivateMessageTests {

        @Test
        @DisplayName("Should handle null user gracefully")
        void shouldHandleNullUserGracefully() {
            // Given
            when(jdaBean.getUserById(testUserId)).thenReturn(null);

            // When & Then
            assertThatCode(() -> todoBot.sendPrivateMessage(testUserId, "Title", "Message"))
                .doesNotThrowAnyException();

            verify(jdaBean).getUserById(testUserId);
        }

        @Test
        @DisplayName("Should handle exception gracefully")
        void shouldHandleExceptionGracefully() {
            // Given
            when(jdaBean.getUserById(testUserId)).thenThrow(new RuntimeException("Test error"));

            // When & Then
            assertThatCode(() -> todoBot.sendPrivateMessage(testUserId, "Title", "Message"))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should unregister commands and clean up")
        void shouldUnregisterCommandsAndCleanUp() {
            // Given
            Task task = new Task();
            todoBot.getUserTasks().put(testUserId, task);

            // When
            todoBot.unregisterCommands();

            // Then
            assertThat(todoBot.getUserTasks()).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple unregister calls")
        void shouldHandleMultipleUnregisterCalls() {
            // When & Then
            assertThatCode(() -> {
                todoBot.unregisterCommands();
                todoBot.unregisterCommands();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("User Tasks Management Tests")
    class UserTasksManagementTests {

        @Test
        @DisplayName("Should provide access to user tasks")
        void shouldProvideAccessToUserTasks() {
            // Given
            Task task = new Task();
            
            // When
            todoBot.getUserTasks().put(testUserId, task);

            // Then
            assertThat(todoBot.getUserTasks()).containsKey(testUserId);
            assertThat(todoBot.getUserTasks().get(testUserId)).isEqualTo(task);
        }

        @Test
        @DisplayName("Should handle concurrent access to user tasks")
        void shouldHandleConcurrentAccessToUserTasks() {
            // Given
            Task task1 = new Task();
            Task task2 = new Task();

            // When & Then - Should not throw exception
            assertThatCode(() -> {
                todoBot.getUserTasks().put("user1", task1);
                todoBot.getUserTasks().put("user2", task2);
                todoBot.getUserTasks().remove("user1");
            }).doesNotThrowAnyException();

            assertThat(todoBot.getUserTasks()).hasSize(1);
            assertThat(todoBot.getUserTasks()).containsKey("user2");
        }
    }
}