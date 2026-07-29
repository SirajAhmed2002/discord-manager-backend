package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;

/**
 * DTO for semester summary data.
 * Contains semester name, average grade, and list of subject summaries.
 */
public class SemesterSummary {
    /** The name of the semester */
    private final String name;
    /** The average grade for the semester */
    private final double average;
    /** List of subject summaries for this semester */
    private final List<SubjectSummary> subjects;

    /**
     * Constructor for semester summary.
     * @param name The semester name
     * @param average The average grade for the semester
     * @param subjects List of subject summaries
     */
    public SemesterSummary(String name, double average, List<SubjectSummary> subjects) {
        this.name = name;
        this.average = average;
        this.subjects = subjects;
    }

    /**
     * Gets the semester name.
     * @return The semester name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the semester average grade.
     * @return The average grade for the semester
     */
    public double getAverage() {
        return average;
    }

    /**
     * Gets the list of subject summaries.
     * @return List of subject summaries for this semester
     */
    public List<SubjectSummary> getSubjects() {
        return subjects;
    }
}