package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

/**
 * CustomId is an enumeration that defines custom identifiers for various UI components
 * in the Discord bot's schedule management system. These identifiers are used to handle
 * interactions with buttons and input fields in the user interface.
 */
public enum CustomId{
    SUBMIT("SUBMIT_BUTTON"),
    CANCEL("CANCEL_BUTTON"),
    RESTART("RESTART_BUTTON"),
    CONFIRM_PREFIX("CONFIRM_BUTTON_"),

    USERNAME("USERNAME_FIELD"),
    DEPARTMENT("DEPARTMENT_FIELD"),
    SEMESTER("SEMESTER_FIELD"),
    WEEK("WEEK_FIELD");

    private final String customId;

    /**
     * Constructor for CustomId enum.
     * @param submitButton The custom ID string associated with the button or field.
     */
    CustomId(String submitButton){
        this.customId = submitButton;
    }

    /**
     * Returns the custom ID string associated with this CustomId.
     * @return The custom ID string.
     */
    public String getId() {
        return customId;
    }

    /**
     * Converts a string to a CustomId enum constant.
     * If the string starts with CONFIRM_PREFIX, it returns CONFIRM_PREFIX.
     * Otherwise, it checks for a matching CustomId and returns it.
     * @param id The string to convert.
     * @return The corresponding CustomId enum constant.
     * @throws IllegalArgumentException if no matching CustomId is found.
     */
    public static CustomId fromString(String id){
        if(id.startsWith(CONFIRM_PREFIX.getId())){
            return CONFIRM_PREFIX;
        }
        for (CustomId s : CustomId.values()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No enum constant for id: " + id);
    }
}
