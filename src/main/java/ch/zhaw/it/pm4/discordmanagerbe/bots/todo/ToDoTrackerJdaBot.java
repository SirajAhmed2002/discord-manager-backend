package ch.zhaw.it.pm4.discordmanagerbe.bots.todo;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractSlashCommandJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.CustomId;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ReminderUnit;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.model.Task;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service.TaskService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.service.TaskValidationService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.util.DateTimeUtils;
import ch.zhaw.it.pm4.discordmanagerbe.bots.todo.util.EmbedUtils;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.TaskEntity;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant.ToDoConstants.*;

/**
 * ToDoTrackerJdaBot is a Discord bot that helps users manage their tasks and set reminders.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SLASH_COMMAND,
        slashCommand = SlashCommandBotType.TODO)
@Component
public class ToDoTrackerJdaBot extends AbstractSlashCommandJdaBot {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ToDoTrackerJdaBot.class);

    /**
     * Service for managing tasks.
     */
    private final TaskService taskService;

    /**
     * Service for validating task data.
     */
    private final TaskValidationService validationService;

    /**
     * Executor service for handling asynchronous operations.
     */
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Map to store user tasks during task creation.
     */
    private final Map<String, Task> userTasks = new ConcurrentHashMap<>();

    /**
     * Constructor for ToDoTrackerJdaBot.
     * @param jdaBean the JDA instance for interacting with Discord
     * @param slashCommandService the service for handling slash commands
     * @param slashCommandListener the service for listening to slash command events
     * @param taskService the service for managing tasks
     * @param validationService the service for validating task data
     */
    @Autowired
    public ToDoTrackerJdaBot(JDA jdaBean, JdaSlashCommandService slashCommandService,
                             JdaEventListenerService slashCommandListener, 
                             TaskService taskService,
                             TaskValidationService validationService) {
        super(jdaBean, slashCommandService, slashCommandListener);
        this.taskService = taskService;
        this.validationService = validationService;
        setBotType(this.getClass().getAnnotation(BotIdentifier.class).slashCommand());
        setDescription("Todo Tracker Bot Ein Bot, der dir hilft, deine Aufgaben zu verwalten und Erinnerungen zu setzen.");
    }

    /**
     * Sets up the slash commands for this bot.
     */
    @Override
    protected void setupCommands() {
        // Add task command
        SlashCommandData addTaskCommand = createCommand(COMMAND_ADD_TASK, "Erstellt eine neue Aufgabe");
        registerCommand(COMMAND_ADD_TASK, "Erstellt eine neue Aufgabe", addTaskCommand,
                this::handleAddTaskCommand);

        // Remove task command
        SlashCommandData removeTaskCommand = createCommand(COMMAND_REMOVE_TASK, "Entfernt eine Aufgabe");
        registerCommand(COMMAND_REMOVE_TASK, "Entfernt eine Aufgabe", removeTaskCommand,
                this::handleRemoveTaskCommand);

        // List tasks command
        SlashCommandData listTasksCommand = createCommand(COMMAND_LIST_TASKS, "Listet alle Aufgaben auf");
        registerCommand(COMMAND_LIST_TASKS, "Listet alle Aufgaben auf", listTasksCommand,
                this::handleListTasksCommand);
    }

    /**
     * Registers interaction handlers for buttons, string selections, and modals.
     */
    @Override
    protected void registerButtonInteractionHandlers() {
        registerButtonInteractionHandler(CustomId.NO_REMINDER.getId(), this::handleNoReminderButton);
        registerButtonInteractionHandler(CustomId.REMINDER_ADD.getId(), this::handleReminderAddButton);
        registerButtonInteractionHandler(CustomId.REMINDER_DONE.getId(), this::handleReminderDoneButton);
        registerButtonInteractionHandler(CustomId.TASK_TO_REMOVE.getId(), this::handleTaskRemoveButton);
    }

    /**
     * Registers interaction handlers for string selections.
     */
    @Override
    protected void registerStringInteractionHandlers() {
        registerStringInteractionHandler(CustomId.REMINDER_VALUE.getId(), this::handleReminderValueSelection);
        registerStringInteractionHandler(CustomId.REMINDER_UNIT.getId(), this::handleReminderUnitSelection);
    }

    /**
     * Registers interaction handlers for modals.
     */
    @Override
    protected void registerModalInteractionHandlers() {
        registerModalInteractionHandler(CustomId.TASK_MODAL.getId(), this::handleTaskModal);
    }

    /**
     * Handles the "Add Task" slash command.
     * @param event the event triggered by the slash command
     */
    void handleAddTaskCommand(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();

        // Initialize new task for this user
        userTasks.put(userId, new Task());

        Modal modal = createTaskModal();
        event.replyModal(modal).queue();
    }

    /**
     * Handles the "Remove Task" slash command.
     * @param event the event triggered by the slash command
     */
    void handleRemoveTaskCommand(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        String userId = event.getUser().getId();
        List<TaskEntity> userTasks = taskService.findAllTasksForUser(userId);

        if (userTasks.isEmpty()) {
            MessageEmbed embed = EmbedUtils.createNoTasksFoundEmbed();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        // Send each task as a separate message with remove button
        for (TaskEntity task : userTasks) {
            MessageEmbed embed = EmbedUtils.createTaskDisplayEmbed(task.getTitle(), task.toString());
            Button removeButton = Button.danger(
                    CustomId.TASK_TO_REMOVE.getId() + task.getTaskId(), 
                    UI.BTN_REMOVE_TASK
            );

            event.getHook().sendMessageEmbeds(embed)
                    .addComponents(ActionRow.of(removeButton))
                    .setEphemeral(true)
                    .queue();
        }
    }

    /**
     * Handles the "List Tasks" slash command.
     * @param event the event triggered by the slash command
     */
    void handleListTasksCommand(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        String userId = event.getUser().getId();
        List<TaskEntity> userTasks = taskService.findAllTasksForUser(userId);

        if (userTasks.isEmpty()) {
            MessageEmbed embed = EmbedUtils.createNoTasksFoundEmbed();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        String taskList = userTasks.stream()
                .map(TaskEntity::toString)
                .collect(Collectors.joining(UI.TASK_LIST_SEPARATOR));

        MessageEmbed embed = EmbedUtils.createTaskListEmbed(taskList);
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    public Map<String, Task> getUserTasks() {
        return userTasks;
    }

    /**
     * Handles the task modal interaction.
     * @param event the modal interaction event
     */
    public void handleTaskModal(ModalInteractionEvent event) {
        event.deferReply(true).queue();

        String userId = event.getUser().getId();
        Task task = userTasks.get(userId);

        if (task == null) {
            MessageEmbed embed = EmbedUtils.createSessionExpiredEmbed();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        // Extract and validate form data
        TaskFormData formData = extractFormData(event);
        
        // Validate title
        TaskValidationService.ValidationResult<Void> titleValidation = validationService.validateTaskTitle(formData.title);
        if (titleValidation.isFailure()) {
            MessageEmbed embed = EmbedUtils.createInvalidTitleEmbed();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        // Set basic task data
        task.setTitle(formData.title);
        task.setDescription(formData.description);

        // Handle date and time validation
        if (formData.dueDateStr.isEmpty()) {
            task.setTimeToBeDone(Long.MAX_VALUE);
            saveTaskAndRespond(event, userId, task);
            return;
        }

        TaskValidationService.ValidationResult<LocalDate> dateValidation = 
                validationService.validateAndParseDate(formData.dueDateStr);
        if (dateValidation.isFailure()) {
            MessageEmbed embed = dateValidation.getErrorMessage().contains("Invalid date format") 
                    ? EmbedUtils.createInvalidDateEmbed()
                    : EmbedUtils.createDateInPastEmbed();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        TaskValidationService.ValidationResult<LocalTime> timeValidation = 
                validationService.validateAndParseTime(formData.dueTimeStr);
        if (timeValidation.isFailure()) {
            MessageEmbed embed = EmbedUtils.createInvalidTimeEmbed();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        LocalDate date = dateValidation.getData();
        LocalTime time = timeValidation.getData();

        TaskValidationService.ValidationResult<Void> dateTimeValidation = 
                validationService.validateScheduledDateTime(time, date);
        if (dateTimeValidation.isFailure()) {
            MessageEmbed embed = EmbedUtils.createTimeInPastEmbed();
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        task.setTimeToBeDone(DateTimeUtils.toEpochMillis(date, time));
        showReminderSelection(event);
    }

    /**
     * Handles the "No Reminder" button interaction.
     * @param event the button interaction event
     */
    void handleNoReminderButton(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        Task task = userTasks.get(userId);

        if (task == null) {
            MessageEmbed embed = EmbedUtils.createSessionExpiredEmbed();
            event.getHook().editOriginalEmbeds(embed).setComponents().queue();
            return;
        }

        saveTaskAndRespond(event, userId, task);
    }

    /**
     * Handles the "Add Reminder" button interaction.
     * @param event the button interaction event
     */
    void handleReminderAddButton(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        Task task = userTasks.get(userId);

        if (task == null) {
            MessageEmbed embed = EmbedUtils.createSessionExpiredEmbed();
            event.getHook().editOriginalEmbeds(embed).setComponents().queue();
            return;
        }

        if (!validateAndAddReminder(task)) {
            showReminderSelection(event, true);
            return;
        }

        // Reset reminder values and show selection again
        task.setReminderValue(null);
        task.setReminderUnit(null);
        showReminderSelection(event, false);
    }

    /**
     * Handles the "Done" button interaction for reminders.
     * @param event the button interaction event
     */
    void handleReminderDoneButton(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        Task task = userTasks.get(userId);

        if (task == null) {
            MessageEmbed embed = EmbedUtils.createSessionExpiredEmbed();
            event.getHook().editOriginalEmbeds(embed).setComponents().queue();
            return;
        }

        if (!validateAndAddReminder(task)) {
            showReminderSelection(event, true);
            return;
        }

        saveTaskAndRespond(event, userId, task);
    }

    /**
     * Handles the task removal button interaction.
     * @param event the button interaction event
     */
    void handleTaskRemoveButton(ButtonInteractionEvent event) {
        event.deferEdit().queue();

        String customId = event.getComponentId();
        long taskId = Long.parseLong(customId.substring(CustomId.TASK_TO_REMOVE.getId().length()));

        Optional<TaskEntity> taskOpt = taskService.findTaskById(taskId);
        if (taskOpt.isPresent()) {
            TaskEntity taskEntity = taskOpt.get();
            try {
                taskService.removeTask(taskEntity.getTaskId());
                MessageEmbed embed = EmbedUtils.createTaskRemovedEmbed(taskEntity.getTitle());
                event.getHook().editOriginalEmbeds(embed).setComponents().queue();

                // Delete message after delay
                CompletableFuture.delayedExecutor(Config.MESSAGE_DELETE_DELAY_SECONDS, TimeUnit.SECONDS, executorService)
                        .execute(() -> event.getHook().deleteOriginal().queue(null,
                                error -> logger.debug("Could not delete message (already deleted?): {}", error.getMessage())));
            } catch (TaskService.TaskNotFoundException e) {
                MessageEmbed embed = EmbedUtils.createTaskNotFoundEmbed();
                event.getHook().editOriginalEmbeds(embed).setComponents().queue();
            }
        } else {
            MessageEmbed embed = EmbedUtils.createTaskNotFoundEmbed();
            event.getHook().editOriginalEmbeds(embed).setComponents().queue();
        }
    }

    /**
     * Handles the selection of reminder value from the dropdown.
     * @param event the string select interaction event
     */
    void handleReminderValueSelection(StringSelectInteractionEvent event) {
        String userId = event.getUser().getId();
        Task task = userTasks.get(userId);

        if (task != null) {
            task.setReminderValue(event.getValues().getFirst());
        }

        event.deferEdit().queue();
    }

    /**
     * Handles the selection of reminder unit from the dropdown.
     * @param event the string select interaction event
     */
    void handleReminderUnitSelection(StringSelectInteractionEvent event) {
        String userId = event.getUser().getId();
        Task task = userTasks.get(userId);

        if (task != null) {
            task.setReminderUnit(ReminderUnit.fromString(event.getValues().getFirst()));
        }

        event.deferEdit().queue();
    }

    /**
     * Creates the modal for adding a task.
     * @return the modal for task creation
     */
    Modal createTaskModal() {
        TextInput titleInput = TextInput.create(CustomId.TASK_TITLE.getId(), UI.FIELD_TASK_TITLE, TextInputStyle.SHORT)
                .setPlaceholder(UI.PLACEHOLDER_TASK_TITLE)
                .setRequired(true)
                .build();

        TextInput descriptionInput = TextInput.create(CustomId.TASK_DESCRIPTION.getId(), UI.FIELD_DESCRIPTION, TextInputStyle.PARAGRAPH)
                .setRequired(false)
                .setMaxLength(Config.DESCRIPTION_MAX_LENGTH)
                .build();

        TextInput dueDateInput = TextInput.create(CustomId.TASK_DUE_DATE.getId(), UI.FIELD_DUE_DATE, TextInputStyle.SHORT)
                .setPlaceholder(DateTimeUtils.getCurrentDateFormatted())
                .setRequired(false)
                .setMaxLength(Config.DATE_INPUT_MAX_LENGTH)
                .build();

        TextInput dueTimeInput = TextInput.create(CustomId.TASK_DUE_TIME.getId(), UI.FIELD_DUE_TIME, TextInputStyle.SHORT)
                .setPlaceholder(DateTimeUtils.getCurrentTimeFormatted())
                .setRequired(false)
                .setMaxLength(Config.TIME_INPUT_MAX_LENGTH)
                .build();

        return Modal.create(CustomId.TASK_MODAL.getId(), UI.MODAL_ADD_TASK)
                .addComponents(
                        ActionRow.of(titleInput),
                        ActionRow.of(descriptionInput),
                        ActionRow.of(dueDateInput),
                        ActionRow.of(dueTimeInput))
                .build();
    }

    /**
     * Extracts form data from the modal interaction event.
     * @param event the modal interaction event
     * @return the extracted task form data
     */
    TaskFormData extractFormData(ModalInteractionEvent event) {
        String title = Objects.requireNonNull(event.getValue(CustomId.TASK_TITLE.getId())).getAsString();
        
        String description = "";
        ModalMapping descriptionMapping = event.getValue(CustomId.TASK_DESCRIPTION.getId());
        if (descriptionMapping != null) {
            description = descriptionMapping.getAsString();
        }

        String dueDateStr = "";
        ModalMapping dueDateMapping = event.getValue(CustomId.TASK_DUE_DATE.getId());
        if (dueDateMapping != null) {
            dueDateStr = dueDateMapping.getAsString();
        }

        String dueTimeStr = DEFAULT_TIME;
        ModalMapping dueTimeMapping = event.getValue(CustomId.TASK_DUE_TIME.getId());
        if (dueTimeMapping != null && !dueTimeMapping.getAsString().trim().isEmpty()) {
            dueTimeStr = dueTimeMapping.getAsString().trim();
        }

        return new TaskFormData(title, description, dueDateStr, dueTimeStr);
    }

    /**
     * Shows the reminder selection menu to the user.
     * @param event the modal interaction event
     */
    private void showReminderSelection(ModalInteractionEvent event) {
        showReminderSelectionInternal(event, null, false, true);
    }

    /**
     * Shows the reminder selection menu to the user.
     * @param event the button interaction event
     * @param hasError indicates if there was an error in the previous reminder selection
     */
    private void showReminderSelection(ButtonInteractionEvent event, boolean hasError) {
        showReminderSelectionInternal(null, event, hasError, false);
    }

    /**
     * Internal method to show the reminder selection menu.
     * @param modalEvent the modal interaction event (can be null if called from button)
     * @param buttonEvent the button interaction event (can be null if called from modal)
     * @param hasError indicates if there was an error in the previous reminder selection
     * @param isModalResponse indicates if this is a response to a modal interaction
     */
    private void showReminderSelectionInternal(ModalInteractionEvent modalEvent, ButtonInteractionEvent buttonEvent,
                                               boolean hasError, boolean isModalResponse) {

        StringSelectMenu valueMenu = createReminderValueMenu();
        StringSelectMenu unitMenu = createReminderUnitMenu();
        
        Button doneButton = Button.success(CustomId.REMINDER_DONE.getId(), UI.BTN_FINISH);
        Button addButton = Button.primary(CustomId.REMINDER_ADD.getId(), UI.BTN_ADD_REMINDER);
        Button skipButton = Button.danger(CustomId.NO_REMINDER.getId(), hasError ? UI.BTN_SKIP : UI.BTN_NO_REMINDER);

        MessageEmbed embed = EmbedUtils.createReminderSelectionEmbed(hasError);

        if (isModalResponse) {
            modalEvent.getHook().sendMessageEmbeds(embed)
                    .addComponents(
                            ActionRow.of(valueMenu),
                            ActionRow.of(unitMenu),
                            ActionRow.of(doneButton, addButton, skipButton)
                    )
                    .queue();
        } else {
            buttonEvent.getHook().editOriginalEmbeds(embed)
                    .setComponents(
                            ActionRow.of(valueMenu),
                            ActionRow.of(unitMenu),
                            ActionRow.of(doneButton, addButton, skipButton)
                    )
                    .queue();
        }
    }

    /**
     * Creates the reminder value selection menu.
     * @return the string select menu for reminder values
     */
    StringSelectMenu createReminderValueMenu() {
        StringSelectMenu.Builder builder = StringSelectMenu.create(CustomId.REMINDER_VALUE.getId())
                .setPlaceholder(UI.PLACEHOLDER_SELECT_NUMBER);

        for (int i = 1; i <= Config.MAX_REMINDER_VALUE; i++) {
            builder.addOption(String.valueOf(i), String.valueOf(i));
        }

        return builder.build();
    }

    /**
     * Creates the reminder unit selection menu.
     * @return the string select menu for reminder units
     */
    StringSelectMenu createReminderUnitMenu() {
        return StringSelectMenu.create(CustomId.REMINDER_UNIT.getId())
                .setPlaceholder(UI.PLACEHOLDER_SELECT_TIME_UNIT)
                .addOption(UI.OPTION_HOURS, ReminderUnit.HOURS.getUnit())
                .addOption(UI.OPTION_DAYS, ReminderUnit.DAYS.getUnit())
                .addOption(UI.OPTION_WEEKS, ReminderUnit.WEEKS.getUnit())
                .build();
    }

    /**
     * Validates the reminder configuration and adds it to the task.
     * @param task the task to which the reminder should be added
     * @return true if the reminder was successfully added, false otherwise
     */
    boolean validateAndAddReminder(Task task) {
        TaskValidationService.ValidationResult<Long> validation = 
                validationService.validateReminderConfiguration(task.getReminderValue(), task, System.currentTimeMillis());
        
        if (validation.isFailure()) {
            return false;
        }

        try {
            long reminderTime = Long.parseLong(task.getReminderValue()) * task.getReminderUnit().getMillis();
            task.addReminder(reminderTime);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Saves the task and sends a response to the user.
     * @param event the modal interaction event
     * @param userId the ID of the user who created the task
     * @param task the task to be saved
     */
    private void saveTaskAndRespond(ModalInteractionEvent event, String userId, Task task) {
        try {
            TaskEntity savedTask = taskService.createTask(task, userId);
            
            if (!task.getReminderDateTime().isEmpty()) {
                taskService.createTaskReminders(savedTask.getTaskId(), task.getReminderDateTime());
            }

            // Get updated task with reminders
            Optional<TaskEntity> refreshedTaskOpt = taskService.findTaskById(savedTask.getTaskId());
            String taskDescription = refreshedTaskOpt.map(TaskEntity::toString).orElse(savedTask.toString());

            MessageEmbed embed = EmbedUtils.createTaskCreatedEmbed(taskDescription);
            event.getHook().sendMessageEmbeds(embed).queue();

            userTasks.remove(userId);
        } catch (Exception e) {
            logger.error("Error saving task for user {}: {}", userId, e.getMessage(), e);
            MessageEmbed embed = EmbedUtils.createErrorEmbed(Messages.ERROR, "Fehler beim Speichern der Aufgabe.");
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

    /**
     * Saves the task and sends a response to the user.
     * @param event the button interaction event
     * @param userId the ID of the user who created the task
     * @param task the task to be saved
     */
    private void saveTaskAndRespond(ButtonInteractionEvent event, String userId, Task task) {
        try {
            TaskEntity savedTask = taskService.createTask(task, userId);
            
            if (!task.getReminderDateTime().isEmpty()) {
                taskService.createTaskReminders(savedTask.getTaskId(), task.getReminderDateTime());
            }

            // Get updated task with reminders
            Optional<TaskEntity> refreshedTaskOpt = taskService.findTaskById(savedTask.getTaskId());
            String taskDescription = refreshedTaskOpt.map(TaskEntity::toString).orElse(savedTask.toString());

            MessageEmbed embed = EmbedUtils.createTaskCreatedEmbed(taskDescription);
            event.getHook().editOriginalEmbeds(embed).setComponents().queue();

            userTasks.remove(userId);
        } catch (Exception e) {
            logger.error("Error saving task for user {}: {}", userId, e.getMessage(), e);
            MessageEmbed embed = EmbedUtils.createErrorEmbed(Messages.ERROR, "Fehler beim Speichern der Aufgabe.");
            event.getHook().editOriginalEmbeds(embed).setComponents().queue();
        }
    }

    /**
     * Sends a private message to a user.
     * @param userId the ID of the user to send the message to
     * @param title the title of the message
     * @param message the content of the message
     */
    public void sendPrivateMessage(String userId, String title, String message) {
        try {
            User user = jdaBean.getUserById(userId);
            if (user != null) {
                user.openPrivateChannel().queue(privateChannel -> {
                    MessageEmbed embed = EmbedUtils.createErrorEmbed(title, message);
                    privateChannel.sendMessageEmbeds(embed).queue();
                }, error -> {
                    logger.error("Failed to send private message to user {}: {}", userId, error.getMessage());
                });
            }
        } catch (Exception e) {
            logger.error("Error sending private message to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Unregisters all commands and cleans up resources.
     */
    @Override
    public void unregisterCommands() {
        // Clean up executor service
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Clear user tasks
        userTasks.clear();

        super.unregisterCommands();
    }

    /**
     * Data class to hold task form data extracted from the modal interaction.
     */
    static class TaskFormData {
        /**
         * The title of the task.
         */
        final String title;

        /**
         * The description of the task.
         */
        final String description;

        /**
         * The due date of the task as a string.
         */
        final String dueDateStr;

        /**
         * The due time of the task as a string.
         */
        final String dueTimeStr;

        /**
         * Constructor for TaskFormData.
         * @param title the title of the task
         * @param description the description of the task
         * @param dueDateStr the due date of the task as a string
         * @param dueTimeStr the due time of the task as a string
         */
        TaskFormData(String title, String description, String dueDateStr, String dueTimeStr) {
            this.title = title;
            this.description = description;
            this.dueDateStr = dueDateStr;
            this.dueTimeStr = dueTimeStr;
        }
    }
}