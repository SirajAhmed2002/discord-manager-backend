package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import ch.zhaw.it.pm4.discordmanagerbe.dto.*;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Factory for creating Discord embeds for the grade calculator bot
 */
@Component
public class EmbedFactory {
    /**
     * Decimal format for displaying grades and averages
     */
    private static final DecimalFormat df = new DecimalFormat("#.##");

    /**
     * Date formatter for displaying creation dates of grades
     */
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Creates an embed for successful subject creation
     */
    public EmbedBuilder createSubjectCreatedEmbed(Subject subject, String semester) {
        EmbedBuilder embed = createSuccessEmbed("Fach erstellt")
                .addField("Fach", subject.getName(), true)
                .addField("Credits", String.valueOf(subject.getCredits()), true);
                
        if (semester != null && !semester.trim().isEmpty()) {
            embed.addField("Semester", semester, true);
        }
        
        return embed;
    }

    /**
     * Creates an embed for successful grade addition
     * @param grade the added grade
     * @param request the request containing grade details
     * @param average the current average after adding the grade
     * @return an EmbedBuilder with the success message and grade details
     */
    public EmbedBuilder createGradeAddedEmbed(Grade grade, AddGradeRequest request, double average) {
        EmbedBuilder embed = createSuccessEmbed("Note hinzugefügt")
            .addField("Fach", request.subjectName(), true)
            .addField("Note", df.format(grade.getValue()), true)
            .addField("Gewichtung", df.format(grade.getWeight() * 100) + "%", true)
            .addField("Aktueller Durchschnitt", df.format(average), false);
            
        addOptionalField(embed, "Semester", request.semester());
        addOptionalField(embed, "Beschreibung", request.description());
        
        return embed;
    }

    /**
     * Creates an embed for displaying subjects
     * @param displayData the data containing subjects and averages
     * @return an EmbedBuilder with the subjects and their averages
     */
    public EmbedBuilder createSubjectsDisplayEmbed(SubjectDisplayData displayData) {
        String title = displayData.getSemester() != null 
            ? "Deine Fächer für " + displayData.getSemester()
            : "Deine Fächer";
            
        EmbedBuilder embed = createInfoEmbed(title);
        
        addSubjectsToEmbed(embed, displayData.getSubjects());
        
        if (displayData.getAverage() > 0) {
            embed.addField("Gesamtdurchschnitt", df.format(displayData.getAverage()), false);
        }
        
        return embed;
    }

    /**
     * Creates an embed for displaying grades
     * @param displayData the data containing grades and average for a subject
     * @return an EmbedBuilder with the grades and their average
     */
    public EmbedBuilder createGradesDisplayEmbed(GradeDisplayData displayData) {
        String title = buildGradesTitle(displayData.getSubjectName(), displayData.getSemester());
        
        EmbedBuilder embed = createInfoEmbed(title)
                .setDescription("Aktueller Durchschnitt: " + df.format(displayData.getAverage()));

        addGradesToEmbed(embed, displayData.getGrades());
        
        return embed;
    }

    /**
     * Creates an embed for displaying semesters
     * @param semesters the list of semesters to display
     * @return an EmbedBuilder with the semesters
     */
    public EmbedBuilder createSemestersDisplayEmbed(List<String> semesters) {
        return createInfoEmbed("Deine Semester")
                .setDescription("Folgende Semester sind verfügbar:\n\n" +
                        semesters.stream().map(s -> "• " + s).collect(Collectors.joining("\n")));
    }

    /**
     * Creates an embed for successful subject removal
     * @param subjectName the name of the subject that was removed
     * @param semester the semester of the subject, can be null or empty
     * @return an EmbedBuilder with the success or error message
     */
    public EmbedBuilder createSubjectRemovedEmbed(String subjectName, String semester, boolean removed) {
        if (removed) {
            EmbedBuilder embed = createSuccessEmbed("Fach gelöscht")
                    .addField("Fach", subjectName, true);
            
            if (semester != null && !semester.trim().isEmpty()) {
                embed.addField("Semester", semester, true);
            }
            
            embed.setDescription("Das Fach und alle zugehörigen Noten wurden erfolgreich gelöscht.");
            return embed;
        } else {
            return createErrorEmbed("Fach nicht gefunden")
                    .setDescription(String.format(MSG_SUBJECT_NOT_FOUND, subjectName));
        }
    }

    /**
     * Creates an embed for successful grades removal
     * @param subjectName the name of the subject from which grades were removed
     * @param semester the semester of the subject, can be null or empty
     * @param removedCount the number of grades removed
     * @return an EmbedBuilder with the success message or info if no grades were removed
     */
    public EmbedBuilder createGradesRemovedEmbed(String subjectName, String semester, int removedCount) {
        if (removedCount > 0) {
            EmbedBuilder embed = createSuccessEmbed("Noten gelöscht")
                    .addField("Fach", subjectName, true)
                    .addField("Gelöschte Noten", String.valueOf(removedCount), true);
            
            if (semester != null && !semester.trim().isEmpty()) {
                embed.addField("Semester", semester, true);
            }
            
            embed.setDescription("Alle Noten wurden erfolgreich aus dem Fach gelöscht.");
            return embed;
        } else {
            return createInfoEmbed("Keine Noten vorhanden")
                    .setDescription(String.format(MSG_NO_GRADES_TO_REMOVE, subjectName));
        }
    }

    /**
     * Creates an embed for successful semester removal
     * @param semester the name of the semester that was removed
     * @param removedCount the number of subjects removed from the semester
     * @return an EmbedBuilder with the success message or error if the semester was not found
     */
    public EmbedBuilder createSemesterRemovedEmbed(String semester, int removedCount) {
        if (removedCount > 0) {
            return createSuccessEmbed("Semester gelöscht")
                    .addField("Semester", semester, true)
                    .addField("Gelöschte Fächer", String.valueOf(removedCount), true)
                    .setDescription("Das Semester und alle zugehörigen Fächer wurden erfolgreich gelöscht.");
        } else {
            return createErrorEmbed("Semester nicht gefunden")
                    .setDescription(String.format(MSG_SEMESTER_NOT_FOUND, semester));
        }
    }

    /**
     * Create an embed for displaying semester average and subjects.
     * @param semesterSummary the semester summary containing average and subjects
     * @return an EmbedBuilder with semester average and subject details
     */
    public EmbedBuilder createSemesterAverageEmbed(SemesterSummary semesterSummary) {
        EmbedBuilder embed = createWarningEmbed("Durchschnitt für " + semesterSummary.getName())
                .addField("Semesterdurchschnitt", df.format(semesterSummary.getAverage()), false)
                .setDescription("Fächer im Detail:");

        addSubjectSummariesToEmbed(embed, semesterSummary.getSubjects());
        
        return embed;
    }

    /**
     * Creates an embed for displaying overall averages.
     * @param displayData the data containing overall averages and semester summaries
     * @return an EmbedBuilder with overall averages and semester details
     */
    public EmbedBuilder createOverallAverageEmbed(AverageDisplayData displayData) {
        EmbedBuilder embed = createWarningEmbed("Deine Notendurchschnitte")
                .addField("Gesamtdurchschnitt", df.format(displayData.getOverallAverage()), false);

        for (SemesterSummary semester : displayData.getSemesters()) {
            addSemesterDetailsToEmbed(embed, semester);
        }

        return embed;
    }

    /**
     * Creates a success embed with the given title.
     * @param title the title of the success embed
     * @return an EmbedBuilder configured for success messages
     */
    private EmbedBuilder createSuccessEmbed(String title) {
        return new EmbedBuilder()
            .setColor(SUCCESS_COLOR)
            .setTitle(title);
    }

    /**
     * Creates an embed for informational messages.
     * @param title the title of the info embed
     * @return an EmbedBuilder configured for informational messages
     */
    private EmbedBuilder createInfoEmbed(String title) {
        return new EmbedBuilder()
            .setColor(INFO_COLOR)
            .setTitle(title);
    }

    /**
     * Creates an embed for warning messages.
     * @param title the title of the warning embed
     * @return an EmbedBuilder configured for warning messages
     */
    private EmbedBuilder createWarningEmbed(String title) {
        return new EmbedBuilder()
            .setColor(TAHITI_GOLD)
            .setTitle(title);
    }

    /**
     * Creates an embed for error messages.
     * @param title the title of the error embed
     * @return an EmbedBuilder configured for error messages
     */
    private EmbedBuilder createErrorEmbed(String title) {
        return new EmbedBuilder()
            .setColor(ERROR_COLOR)
            .setTitle(title);
    }

    /**
     * Adds an optional field to the embed if the value is not null or empty.
     * @param embed the embed to add the field to
     * @param name the name of the field
     * @param value the value of the field, can be null or empty
     */
    private void addOptionalField(EmbedBuilder embed, String name, String value) {
        if (value != null && !value.trim().isEmpty()) {
            embed.addField(name, value, name.equals("Semester"));
        }
    }

    /**
     * Adds subjects to the embed for display.
     * @param embed the embed to add subjects to
     * @param subjects the list of subjects to add
     */
    private void addSubjectsToEmbed(EmbedBuilder embed, List<Subject> subjects) {
        Map<String, List<Subject>> subjectsBySemester = subjects.stream()
            .collect(Collectors.groupingBy(subject -> 
                subject.getSemester() != null ? subject.getSemester() : NO_SEMESTER));

        for (Map.Entry<String, List<Subject>> entry : subjectsBySemester.entrySet()) {
            String semesterTitle = entry.getKey();
            List<Subject> semesterSubjects = entry.getValue();

            StringBuilder content = new StringBuilder();
            for (Subject subject : semesterSubjects) {
                double subjectAverage = subject.calculateAverage();
                String averageText = subjectAverage > 0 ? df.format(subjectAverage) : NO_GRADES;

                content.append("**").append(subject.getName()).append("** (")
                        .append(subject.getCredits()).append(" Credits): ")
                        .append("Durchschnitt: ").append(averageText)
                        .append(", Anzahl Noten: ").append(subject.getGrades().size())
                        .append("\n");
            }

            embed.addField(semesterTitle, content.toString(), false);
        }
    }

    /**
     * Adds grades to the embed for display.
     * @param embed the embed to add grades to
     * @param grades the list of grades to add
     */
    private void addGradesToEmbed(EmbedBuilder embed, List<Grade> grades) {
        for (int i = 0; i < grades.size(); i++) {
            Grade grade = grades.get(i);
            int gradeNumber = i + 1; // 1-based numbering for user display
            
            String gradeTitle = String.format("#%d - Note: %s (Gewichtung: %s%%)", 
                gradeNumber,
                df.format(grade.getValue()), 
                df.format(grade.getWeight() * 100)
            );
            
            String description = grade.getDescription().isEmpty() ? 
                NO_DESCRIPTION : grade.getDescription();
                
            // Add creation date for better identification
            if (grade.getCreatedAt() != null) {
                description += "\n*Erstellt: " + grade.getCreatedAt().format(dateFormatter) + "*";
            }
                
            embed.addField(gradeTitle, description, false);
        }
    }

    /**
     * Builds the title for the grades embed.
     * @param subjectName the name of the subject
     * @param semester the semester, can be null or empty
     * @return the formatted title string
     */
    private String buildGradesTitle(String subjectName, String semester) {
        StringBuilder title = new StringBuilder("Noten für ").append(subjectName);
        
        if (semester != null && !semester.isEmpty()) {
            title.append(" (").append(semester).append(")");
        }
        
        return title.toString();
    }

    /**
     * Adds subject summaries to the embed.
     * @param embed the embed to add subjects to
     * @param subjects the list of subject summaries to add
     */
    private void addSubjectSummariesToEmbed(EmbedBuilder embed, List<SubjectSummary> subjects) {
        for (SubjectSummary subject : subjects) {
            if (subject.getAverage() > 0) {
                embed.addField(
                        subject.getName() + " (" + subject.getCredits() + " Credits)",
                        "Durchschnitt: " + df.format(subject.getAverage()) + 
                        "\nAnzahl Noten: " + subject.getGradeCount(),
                        false
                );
            }
        }
    }

    /**
     * Adds detailed semester information to the embed.
     * @param embed the embed to add details to
     * @param semester the semester summary containing details
     */
    private void addSemesterDetailsToEmbed(EmbedBuilder embed, SemesterSummary semester) {
        StringBuilder semesterDetails = new StringBuilder();
        semesterDetails.append("Durchschnitt: **").append(df.format(semester.getAverage())).append("**\n\n");

        for (SubjectSummary subject : semester.getSubjects()) {
            if (subject.getAverage() > 0) {
                semesterDetails.append("• **").append(subject.getName()).append("** (")
                        .append(subject.getCredits()).append(" Credits): ")
                        .append(df.format(subject.getAverage())).append("\n");
            }
        }

        embed.addField(semester.getName(), semesterDetails.toString(), false);
    }
}