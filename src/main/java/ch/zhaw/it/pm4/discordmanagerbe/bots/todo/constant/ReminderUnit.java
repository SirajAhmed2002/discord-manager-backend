package ch.zhaw.it.pm4.discordmanagerbe.bots.todo.constant;

/**
 * Enum representing different time units for reminders.
 */
public enum ReminderUnit {
    /**
     * Unit for seconds, equivalent to 1000 milliseconds.
     */
    HOURS("hours", 3600000),

    /**
     * Unit for minutes, equivalent to 60,000 milliseconds.
     */
    DAYS("days", 86400000),

    /**
     * Unit for weeks, equivalent to 604,800,000 milliseconds.
     */
    WEEKS("weeks", 604800000);

    /**
     * The string representation of the reminder unit.
     */
    private final String unit;

    /**
     * The time in milliseconds that corresponds to the reminder unit.
     */
    private final long millis;

    /**
     * Constructor for ReminderUnit.
     * @param unit the string representation of the reminder unit
     * @param millis the time in milliseconds that corresponds to the reminder unit
     */
    ReminderUnit(String unit, long millis) {
        this.unit = unit;
        this.millis = millis;
    }

    /**
     * Returns the string representation of the reminder unit.
     * @return the string representation of the reminder unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns the time in milliseconds that corresponds to the reminder unit.
     * @return the time in milliseconds that corresponds to the reminder unit
     */
    public long getMillis() {
        return millis;
    }

    /**
     * Converts a string representation of a reminder unit to its corresponding ReminderUnit enum.
     * @param unit the string representation of the reminder unit
     * @return the corresponding ReminderUnit enum
     */
    public static ReminderUnit fromString(String unit) {
        for (ReminderUnit reminderUnit : ReminderUnit.values()) {
            if (reminderUnit.unit.equalsIgnoreCase(unit)) {
                return reminderUnit;
            }
        }
        throw new IllegalArgumentException("Unknown reminder unit: " + unit);
    }
}