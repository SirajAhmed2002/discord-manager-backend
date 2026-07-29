package ch.zhaw.it.pm4.discordmanagerbe.bots.grade;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractSlashCommandJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.AverageCommandHandler;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.GradeCommandHandler;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.RemoveCommandHandler;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.SubjectCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Discord JDA Bot for grade calculation and management.
 * Delegates command handling to specialized handler classes.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SLASH_COMMAND,
        slashCommand = SlashCommandBotType.GRADE_CALCULATOR)
@Component
public class NotenRechnerJdaBot extends AbstractSlashCommandJdaBot {

    /**
     * Subject command handler for managing subjects.
     */
    private final SubjectCommandHandler subjectHandler;

    /**
     * Grade command handler for managing grades.
     */
    private final GradeCommandHandler gradeHandler;

    /**
     * Average command handler for calculating and displaying averages.
     */
    private final AverageCommandHandler averageHandler;

    /**
     * Remove command handler for deleting subjects and grades.
     */
    private final RemoveCommandHandler removeHandler;

    /**
     * Constructor to initialize the bot with required services.
     */
    @Autowired
    public NotenRechnerJdaBot(JDA jdaBean, 
                              JdaSlashCommandService slashCommandService, 
                              JdaEventListenerService slashCommandListener,
                              SubjectCommandHandler subjectHandler,
                              GradeCommandHandler gradeHandler,
                              AverageCommandHandler averageHandler,
                              RemoveCommandHandler removeHandler) {
        super(jdaBean, slashCommandService, slashCommandListener);
        this.subjectHandler = subjectHandler;
        this.gradeHandler = gradeHandler;
        this.averageHandler = averageHandler;
        this.removeHandler = removeHandler;
        setBotType(SlashCommandBotType.GRADE_CALCULATOR);
        setDescription(BOT_DESCRIPTION);
    }

    /**
     * Sets up the commands for the grade calculator bot.
     */
    @Override
    protected void setupCommands() {
        registerCreateSubjectCommand();
        registerAddGradeCommand();
        registerShowSubjectsCommand();
        registerShowGradesCommand();
        registerShowAverageCommand();
        registerShowSemestersCommand();
        registerRemoveSubjectCommand();
        registerRemoveGradesCommand();
        registerRemoveSemesterCommand();
    }

    /**
     * Registers button interaction handlers.
     */
    @Override
    protected void registerButtonInteractionHandlers() {
        // No button interactions needed
    }

    /**
     * Registers string interaction handlers.
     */
    @Override
    protected void registerStringInteractionHandlers() {
        // No string interactions needed
    }

    /**
     * Registers modal interaction handlers.
     */
    @Override
    protected void registerModalInteractionHandlers() {
        // No modal interactions needed
    }

    /**
     * Registers the command to create a new subject.
     */
    private void registerCreateSubjectCommand() {
        SlashCommandData command = createCommand(CMD_CREATE_SUBJECT, "Erstellt ein neues Fach")
                .addOption(OptionType.STRING, OPT_NAME, "Name des Fachs", true)
                .addOption(OptionType.INTEGER, OPT_CREDITS, "Anzahl Credits für das Fach", true)
                .addOption(OptionType.STRING, OPT_SEMESTER, "Semester (z.B. HS2023)", false);
                
        registerCommand(CMD_CREATE_SUBJECT, "Erstellt ein neues Fach", command, 
                this::handleCreateSubjectCommand);
    }

    /**
     * Registers the command to add a grade to a subject.
     */
    private void registerAddGradeCommand() {
        SlashCommandData command = createCommand(CMD_ADD_GRADE, "Fügt eine Note zu einem Fach hinzu")
                .addOption(OptionType.STRING, OPT_FACH, "Name des Fachs", true)
                .addOption(OptionType.STRING, OPT_NOTE, "Note (1.0 - 6.0, z.B. 4.5)", true)
                .addOption(OptionType.STRING, OPT_GEWICHTUNG, "Gewichtung der Note (0.0 - 1.0 oder 0-100%, z.B. 0.5 oder 50)", true)
                .addOption(OptionType.STRING, OPT_SEMESTER, "Semester (z.B. HS2023)", false)
                .addOption(OptionType.STRING, OPT_BESCHREIBUNG, "Beschreibung der Note (z.B. 'Zwischentest')", false);
                
        registerCommand(CMD_ADD_GRADE, "Fügt eine Note zu einem Fach hinzu", command, 
                this::handleAddGradeCommand);
    }

    /**
     * Registers the command to show all subjects.
     */
    private void registerShowSubjectsCommand() {
        SlashCommandData command = createCommand(CMD_SHOW_SUBJECTS, "Zeigt alle deine Fächer an")
                .addOption(OptionType.STRING, OPT_SEMESTER, "Nur Fächer dieses Semesters anzeigen", false);
                
        registerCommand(CMD_SHOW_SUBJECTS, "Zeigt alle deine Fächer an", command, 
                this::handleShowSubjectsCommand);
    }

    /**
     * Registers the command to show grades for a specific subject.
     */
    private void registerShowGradesCommand() {
        SlashCommandData command = createCommand(CMD_SHOW_GRADES, "Zeigt die Noten für ein bestimmtes Fach an")
                .addOption(OptionType.STRING, OPT_FACH, "Name des Fachs", true)
                .addOption(OptionType.STRING, OPT_SEMESTER, "Semester (z.B. HS2023)", false);
                
        registerCommand(CMD_SHOW_GRADES, "Zeigt die Noten für ein bestimmtes Fach an", command, 
                this::handleShowGradesCommand);
    }

    /**
     * Registers the command to show the average grade.
     */
    private void registerShowAverageCommand() {
        SlashCommandData command = createCommand(CMD_SHOW_AVERAGE, "Zeigt deinen aktuellen Notendurchschnitt an")
                .addOption(OptionType.STRING, OPT_SEMESTER, "Nur für dieses Semester (z.B. HS2023)", false);
                
        registerCommand(CMD_SHOW_AVERAGE, "Zeigt deinen aktuellen Notendurchschnitt an", command, 
                this::handleShowAverageCommand);
    }

    /**
     * Registers the command to show all semesters.
     */
    private void registerShowSemestersCommand() {
        SlashCommandData command = createCommand(CMD_SHOW_SEMESTERS, "Zeigt alle vorhandenen Semester an");
                
        registerCommand(CMD_SHOW_SEMESTERS, "Zeigt alle vorhandenen Semester an", command, 
                this::handleShowSemestersCommand);
    }

    /**
     * Registers the command to remove a subject and all its grades.
     */
    private void registerRemoveSubjectCommand() {
        SlashCommandData command = createCommand(CMD_REMOVE_SUBJECT, "Löscht ein Fach und alle zugehörigen Noten")
                .addOption(OptionType.STRING, OPT_FACH, "Name des zu löschenden Fachs", true)
                .addOption(OptionType.STRING, OPT_SEMESTER, "Semester (z.B. HS2023)", false);
                
        registerCommand(CMD_REMOVE_SUBJECT, "Löscht ein Fach und alle zugehörigen Noten", command, 
                this::handleRemoveSubjectCommand);
    }

    /**
     * Registers the command to remove all grades from a subject.
     */
    private void registerRemoveGradesCommand() {
        SlashCommandData command = createCommand(CMD_REMOVE_GRADES, "Löscht alle Noten aus einem Fach")
                .addOption(OptionType.STRING, OPT_FACH, "Name des Fachs", true)
                .addOption(OptionType.STRING, OPT_SEMESTER, "Semester (z.B. HS2023)", false);
                
        registerCommand(CMD_REMOVE_GRADES, "Löscht alle Noten aus einem Fach", command, 
                this::handleRemoveGradesCommand);
    }

    /**
     * Registers the command to remove an entire semester with all subjects and grades.
     */
    private void registerRemoveSemesterCommand() {
        SlashCommandData command = createCommand(CMD_REMOVE_SEMESTER, "Löscht ein ganzes Semester mit allen Fächern")
                .addOption(OptionType.STRING, OPT_SEMESTER, "Semester das gelöscht werden soll (z.B. HS2023)", true);
                
        registerCommand(CMD_REMOVE_SEMESTER, "Löscht ein ganzes Semester mit allen Fächern", command, 
                this::handleRemoveSemesterCommand);
    }

    /**
     * Handles the command to create a new subject.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleCreateSubjectCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        subjectHandler.handleCreateSubject(event);
    }

    /**
     * Handles the command to add a grade to a subject.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleAddGradeCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        gradeHandler.handleAddGrade(event);
    }

    /**
     * Handles the command to show all subjects.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleShowSubjectsCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        subjectHandler.handleShowSubjects(event);
    }

    /**
     * Handles the command to show grades for a specific subject.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleShowGradesCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        gradeHandler.handleShowGrades(event);
    }

    /**
     * Handles the command to show the average grade.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleShowAverageCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        averageHandler.handleShowAverage(event);
    }

    /**
     * Handles the command to show all semesters.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleShowSemestersCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        subjectHandler.handleShowSemesters(event);
    }

    /**
     * Handles the command to remove a subject and all its grades.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleRemoveSubjectCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        removeHandler.handleRemoveSubject(event);
    }

    /**
     * Handles the command to remove all grades from a subject.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleRemoveGradesCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        removeHandler.handleRemoveGrades(event);
    }

    /**
     * Handles the command to remove an entire semester with all subjects and grades.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    void handleRemoveSemesterCommand(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        removeHandler.handleRemoveSemester(event);
    }
}