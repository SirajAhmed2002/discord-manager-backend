package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant;

/**
 * Enum for custom IDs used in the ToDoTrackerJdaBot.
 * This enum is used to define the custom IDs for buttons, modals, and select menus in the bot.
 * The IDs are used to identify the components when they are interacted with.
 */
public enum CustomId {
    /**
     * Button Add_task
     */
    ADD_TASK("ADD_TASK_BUTTON"),

    /**
     * Button Remove_task
     */
    REMOVE_TASK("REMOVE_TASK_BUTTON"),

    /**
     * Button List_tasks
     */
    LIST_TASKS("LIST_TASKS_BUTTON"),

    /**
     * Button Clear_tasks
     */
    NO_REMINDER("NO_REMINDER_BUTTON"),

    /**
     * Button Add reminder
     */
    REMINDER_ADD("REMINDER_ADD_BUTTON"),

    /**
     * Button reminder done
     */
    REMINDER_DONE("REMINDER_DONE_BUTTON"),

    /**
     * Button Remove reminder
     */
    TASK_TO_REMOVE("TASK_TO_REMOVE_"),

    /**
     * Modal for adding or editing a task.
     */
    TASK_MODAL("TASK_MODAL"),

    /**
     * Text input fields for the task modal.
     */
    TASK_TITLE("TASK_TITLE_TEXT_INPUT"),

    /**
     * Text input fields for the task modal.
     */
    TASK_DESCRIPTION("TASK_DESCRIPTION_TEXT_INPUT"),

    /**
     * Text input fields for the task modal.
     */
    TASK_DUE_DATE("TASK_DUE_DATE_TEXT_INPUT"),

    /**
     * Text input fields for the task modal.
     */
    TASK_DUE_TIME("TASK_DUE_TIME_TEXT_INPUT"),

    /**
     * Select menu for choosing a reminder time.
     */
    REMINDER_VALUE("REMINDER_VALUE_SELECT"),

    /**
     * Select menu for choosing a reminder unit.
     */
    REMINDER_UNIT("REMINDER_UNIT_SELECT");

    /**
     * The custom ID string associated with this enum constant.
     */
    private final String customId;

    /**
     * Constructor for CustomId enum.
     * @param customId the custom ID string associated with the enum constant
     */
    CustomId(String customId) {
        this.customId = customId;
    }

    /**
     * Returns the custom ID string associated with this enum constant.
     * @return the custom ID string
     */
    public String getId() {
        return customId;
    }

    /**
     * Converts a string representation of a custom ID to its corresponding CustomId enum.
     * @param customId the string representation of the custom ID
     * @return the corresponding CustomId enum
     */
    public static CustomId fromString(String customId) {
        if (customId.startsWith(TASK_TO_REMOVE.getId())) {
            return TASK_TO_REMOVE;
        }

        for (CustomId s : CustomId.values()) {
            if (s.customId.equals(customId)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown custom ID: " + customId);
    }
}