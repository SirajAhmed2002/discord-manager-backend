package ch.zhaw.it.pm4.discordmanagerbe.dto;

/**
 * DTO for subject summary data.
 * Contains subject statistics including name, average, credits, and grade count.
 */
public class SubjectSummary {
    /** The name of the subject */
    private final String name;
    /** The average grade for the subject */
    private final double average;
    /** The number of credits for the subject */
    private final int credits;
    /** The number of grades recorded for the subject */
    private final int gradeCount;

    /**
     * Constructor for subject summary.
     * @param name The subject name
     * @param average The average grade for the subject
     * @param credits The number of credits
     * @param gradeCount The number of grades recorded
     */
    public SubjectSummary(String name, double average, int credits, int gradeCount) {
        this.name = name;
        this.average = average;
        this.credits = credits;
        this.gradeCount = gradeCount;
    }

    /**
     * Gets the subject name.
     * @return The subject name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the subject average grade.
     * @return The average grade for the subject
     */
    public double getAverage() {
        return average;
    }

    /**
     * Gets the number of credits.
     * @return The number of credits for the subject
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Gets the grade count.
     * @return The number of grades recorded for the subject
     */
    public int getGradeCount() {
        return gradeCount;
    }
}