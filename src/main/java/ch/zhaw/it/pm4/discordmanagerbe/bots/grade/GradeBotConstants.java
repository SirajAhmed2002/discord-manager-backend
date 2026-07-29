package ch.zhaw.it.pm4.discordmanagerbe.bots.grade;

import java.awt.Color;

/**
 * Constants for the grade calculator bot
 */
public final class GradeBotConstants {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GradeBotConstants() {
        // Utility class - no instantiation
    }

    /**
     * The name of the bot.
     */
    public static final String BOT_DESCRIPTION = 
        "Notenrechner Bot für Discord, der dir hilft, deine Noten zu verwalten und deinen Notendurchschnitt zu berechnen.";

    /**
     * The prefix for commands used by the bot.
     */
    public static final String CMD_CREATE_SUBJECT = "fach-erstellen";

    /**
     * Command to add a grade to a subject.
     */
    public static final String CMD_ADD_GRADE = "note-hinzufuegen";

    /**
     * Command to show subjects, grades, averages, and semesters.
     */
    public static final String CMD_SHOW_SUBJECTS = "faecher-anzeigen";

    /**
     * Command to show grades for a specific subject.
     */
    public static final String CMD_SHOW_GRADES = "noten-anzeigen";

    /**
     * Command to show the average grade.
     */
    public static final String CMD_SHOW_AVERAGE = "durchschnitt";

    /**
     * Command to show all semesters.
     */
    public static final String CMD_SHOW_SEMESTERS = "semester-anzeigen";

    /**
     * Command to remove a subject and all its grades.
     */
    public static final String CMD_REMOVE_SUBJECT = "fach-loeschen";

    /**
     * Command to remove all grades from a subject.
     */
    public static final String CMD_REMOVE_GRADES = "noten-loeschen";

    /**
     * Command to remove a semester and all its subjects.
     */
    public static final String CMD_REMOVE_SEMESTER = "semester-loeschen";

    /**
     * Option "name" for commands that require a subject name.
     */
    public static final String OPT_NAME = "name";

    /**
     * Option "fach" for commands that require a subject name.
     */
    public static final String OPT_FACH = "fach";

    /**
     * Option "note" for commands that require a grade.
     */
    public static final String OPT_NOTE = "note";

    /**
     * Option "gewichtung" for commands that require a weight for the grade.
     */
    public static final String OPT_GEWICHTUNG = "gewichtung";

    /**
     * Option "credits" for commands that require the number of credits for a subject.
     */
    public static final String OPT_CREDITS = "credits";

    /**
     * Option "semester" for commands that require a semester.
     */
    public static final String OPT_SEMESTER = "semester";

    /**
     * Option "beschreibung" for commands that allow adding a description to a grade.
     */
    public static final String OPT_BESCHREIBUNG = "beschreibung";

    /**
     * Success color for successful operations. Is typically green.
     */
    public static final Color SUCCESS_COLOR = Color.GREEN;

    /**
     * Info color for informational messages. Is typically blue.
     */
    public static final Color INFO_COLOR = Color.BLUE;

    /**
     * Error color for error messages. Is typically red.
     */
    public static final Color ERROR_COLOR = Color.RED;

    /**
     * Tahiti Gold color, used for highlighting important information.
     */
    public static final Color TAHITI_GOLD = new Color(255, 165, 0);

    /**
     * Unknown server identifier.
     */
    public static final String UNKNOWN_SERVER = "unknown";

    /**
     * Default value for no semester
     */
    public static final String NO_SEMESTER = "Ohne Semester";

    /**
     * Default value for no description
     */
    public static final String NO_DESCRIPTION = "Keine Beschreibung";

    /**
     * Default value for no grades
     */
    public static final String NO_GRADES = "Keine Noten";

    /**
     * Default value for an empty string, used in various contexts where a string is expected but no value is provided.
     */
    public static final String EMPTY_STRING = "";

    /**
     * Grade normalization factor for converting grades > 6.0 to a scale of 1.0 to 6.0.
     */
    public static final double GRADE_NORMALIZATION_FACTOR = 10.0;

    /**
     * Weight normalization factor for converting weights > 1.0 to a scale of 0.0 to 1.0.
     */
    public static final double WEIGHT_NORMALIZATION_FACTOR = 100.0;

    /**
     * Min grade
     */
    public static final double MIN_GRADE = 1.0;

    /**
     * Max grade
     */
    public static final double MAX_GRADE = 6.0;

    /**
     * Min grade weight
     */
    public static final double MIN_WEIGHT = 0.0;

    /**
     * Max grade weight
     */
    public static final double MAX_WEIGHT = 1.0;

    /**
     * Percentage threshold for grade weights, used to determine if a weight is considered valid.
     */
    public static final double PERCENTAGE_THRESHOLD = 1.0;

    /**
     * Grade threshold for determining if a grade is considered passing.
     */
    public static final double GRADE_THRESHOLD = 6.0;
    
    /**
     * Message no subjects created.
     */
    public static final String MSG_NO_SUBJECTS_CREATED = "Du hast noch keine Fächer angelegt. Nutze `/%s` um zu beginnen.";

    /**
     * Message indicating no subjects for a specific semester.
     */
    public static final String MSG_NO_SUBJECTS_FOR_SEMESTER = "Du hast noch keine Fächer für das Semester %s angelegt.";

    /**
     * Message no grades entered.
     */
    public static final String MSG_NO_GRADES_ENTERED = "Du hast noch keine Noten eingetragen.";

    /**
     * Message indicating no semesters created.
     */
    public static final String MSG_NO_SEMESTERS = "Du hast noch keine Semester angelegt.";

    /**
     * Message no data for a specific semester.
     */
    public static final String MSG_NO_DATA_FOR_SEMESTER = "Keine Daten für das Semester %s gefunden.";

    /**
     * Message indicating a subject was not found.
     */
    public static final String MSG_SUBJECT_NOT_FOUND = "Fach '%s' wurde nicht gefunden.";

    /**
     * Message no grades to remove for a specific subject.
     */
    public static final String MSG_NO_GRADES_TO_REMOVE = "Das Fach '%s' hat keine Noten zum Löschen.";

    /**
     * Message indicating a semester was not found or is already empty.
     */
    public static final String MSG_SEMESTER_NOT_FOUND = "Semester '%s' wurde nicht gefunden oder ist bereits leer.";
}