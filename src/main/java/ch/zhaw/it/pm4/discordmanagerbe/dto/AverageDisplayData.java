package ch.zhaw.it.pm4.discordmanagerbe.dto;

import java.util.List;
import java.util.Collections;

/**
 * DTO for displaying average grade data.
 * Contains overall average and semester-specific summaries.
 */
public class AverageDisplayData {
    /** The overall average across all semesters */
    private final double overallAverage;
    /** List of semester summaries */
    private final List<SemesterSummary> semesters;
    /** The specifically requested semester */
    private final String requestedSemester;

    /**
     * Constructor for average display data.
     * @param overallAverage The overall average grade
     * @param semesters List of semester summaries
     * @param requestedSemester The requested semester filter
     */
    public AverageDisplayData(double overallAverage, List<SemesterSummary> semesters, String requestedSemester) {
        this.overallAverage = overallAverage;
        this.semesters = semesters;
        this.requestedSemester = requestedSemester;
    }

    /**
     * Creates an empty average display data instance.
     * @return Empty AverageDisplayData with zero average and empty lists
     */
    public static AverageDisplayData empty() {
        return new AverageDisplayData(0.0, Collections.emptyList(), null);
    }

    /**
     * Checks if the data is empty (no grades available).
     * @return true if overall average is 0.0, false otherwise
     */
    public boolean isEmpty() {
        return overallAverage == 0.0;
    }

    /**
     * Gets the overall average grade.
     * @return The overall average
     */
    public double getOverallAverage() {
        return overallAverage;
    }

    /**
     * Gets the list of semester summaries.
     * @return List of semester summaries
     */
    public List<SemesterSummary> getSemesters() {
        return semesters;
    }

    /**
     * Gets the requested semester filter.
     * @return The requested semester
     */
    public String getRequestedSemester() {
        return requestedSemester;
    }
}