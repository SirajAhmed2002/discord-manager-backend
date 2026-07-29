package ch.zhaw.it.pm4.discordmanagerbe.dto;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import java.util.List;

/**
 * DTO for displaying subject data.
 * Contains subjects, average grade, and semester information.
 */
public class SubjectDisplayData {
    /** List of subjects */
    private final List<Subject> subjects;
    /** The calculated average across all subjects */
    private final double average;
    /** The semester these subjects belong to */
    private final String semester;

    /**
     * Constructor for subject display data.
     * @param subjects List of subjects
     * @param average The calculated average grade
     * @param semester The semester
     */
    public SubjectDisplayData(List<Subject> subjects, double average, String semester) {
        this.subjects = subjects;
        this.average = average;
        this.semester = semester;
    }

    /**
     * Checks if the data is empty (no subjects available).
     * @return true if subjects list is empty
     */
    public boolean isEmpty() {
        return subjects.isEmpty();
    }

    /**
     * Gets the list of subjects.
     * @return List of subjects
     */
    public List<Subject> getSubjects() {
        return subjects;
    }

    /**
     * Gets the calculated average.
     * @return The average grade across all subjects
     */
    public double getAverage() {
        return average;
    }

    /**
     * Gets the semester.
     * @return The semester these subjects belong to
     */
    public String getSemester() {
        return semester;
    }
}