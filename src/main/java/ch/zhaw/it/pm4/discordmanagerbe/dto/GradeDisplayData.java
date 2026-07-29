package ch.zhaw.it.pm4.discordmanagerbe.dto;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import java.util.List;
import java.util.Collections;

/**
 * DTO for displaying grade data.
 * Contains grades, average, subject, and semester information.
 */
public class GradeDisplayData {
    /** List of grades for the subject */
    private final List<Grade> grades;
    /** The calculated average of all grades */
    private final double average;
    /** The name of the subject */
    private final String subjectName;
    /** The semester of the grades */
    private final String semester;

    /**
     * Constructor for grade display data.
     * @param grades List of grades
     * @param average The calculated average
     * @param subjectName The subject name
     * @param semester The semester
     */
    public GradeDisplayData(List<Grade> grades, double average, String subjectName, String semester) {
        this.grades = grades;
        this.average = average;
        this.subjectName = subjectName;
        this.semester = semester;
    }

    /**
     * Creates an empty grade display data instance.
     * @return Empty GradeDisplayData with empty lists and zero values
     */
    public static GradeDisplayData empty() {
        return new GradeDisplayData(Collections.emptyList(), 0.0, "", "");
    }

    /**
     * Checks if the data is empty (no grades available).
     * @return true if grades list is empty
     */
    public boolean isEmpty() {
        return grades.isEmpty();
    }

    /**
     * Gets the list of grades.
     * @return List of grades
     */
    public List<Grade> getGrades() {
        return grades;
    }

    /**
     * Gets the calculated average.
     * @return The average grade value
     */
    public double getAverage() {
        return average;
    }

    /**
     * Gets the subject name.
     * @return The subject name
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * Gets the semester.
     * @return The semester
     */
    public String getSemester() {
        return semester;
    }
}