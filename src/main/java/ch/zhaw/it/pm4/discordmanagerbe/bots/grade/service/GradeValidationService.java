package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service;

import org.springframework.stereotype.Service;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Service for validating and normalizing grade-related inputs
 */
@Service
public class GradeValidationService {
    
    /**
     * Validates that a grade is within the valid range (1.0 - 6.0)
     * @param value the grade value to validate
     */
    public void validateGrade(double value) {
        if (value < MIN_GRADE || value > MAX_GRADE) {
            throw new IllegalArgumentException(
                String.format("Note muss zwischen %.1f und %.1f liegen! Eingegeben: %.2f", 
                    MIN_GRADE, MAX_GRADE, value)
            );
        }
    }
    
    /**
     * Validates that a weight is within the valid range (0.0 - 1.0)
     * @param weight the weight value to validate
     */
    public void validateWeight(double weight) {
        if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
            throw new IllegalArgumentException(
                String.format("Gewichtung muss zwischen %.1f und %.1f liegen! Eingegeben: %.2f", 
                    MIN_WEIGHT, MAX_WEIGHT, weight)
            );
        }
    }
    
    /**
     * Normalizes and validates a grade value.
     * Converts grades > 6.0 by dividing by 10 (e.g., 45 -> 4.5)
     * @param value the grade value to normalize
     */
    public double normalizeGrade(double value) {
        if (value > GRADE_THRESHOLD) {
            value = value / GRADE_NORMALIZATION_FACTOR;
        }
        validateGrade(value);
        return value;
    }
    
    /**
     * Normalizes and validates a weight value.
     * Converts weights > 1.0 by dividing by 100 (e.g., 50 -> 0.5)
     * @param weight the weight value to normalize
     */
    public double normalizeWeight(double weight) {
        if (weight > PERCENTAGE_THRESHOLD) {
            weight = weight / WEIGHT_NORMALIZATION_FACTOR;
        }
        validateWeight(weight);
        return weight;
    }
    
    /**
     * Validates that a string is not null or empty
     * @param value the string value to validate
     */
    public void validateNotEmpty(String value, String fieldName) {
        if (isNullOrEmpty(value)) {
            throw new IllegalArgumentException(fieldName + " darf nicht leer sein!");
        }
    }
    
    /**
     * Validates that credits are positive
     * @param credits the number of credits to validate
     */
    public void validateCredits(int credits) {
        if (credits <= 0) {
            throw new IllegalArgumentException("Credits müssen größer als 0 sein!");
        }
    }
    
    /**
     * Validates all parameters for adding a grade
     * @param subjectName the name of the subject
     */
    public void validateAddGradeParameters(String subjectName, double note, double weight) {
        validateNotEmpty(subjectName, "Fachname");
        validateGrade(note);
        validateWeight(weight);
    }
    
    /**
     * Validates all parameters for creating a subject
     * @param subjectName the name of the subject
     */
    public void validateCreateSubjectParameters(String subjectName, int credits) {
        validateNotEmpty(subjectName, "Fachname");
        validateCredits(credits);
    }

    /**
     * Checks if a string is null or empty
     * @param value the string to check
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}