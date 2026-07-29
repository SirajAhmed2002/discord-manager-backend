package ch.zhaw.it.pm4.discordmanagerbe.data.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a grade with value, weight, and description.
 * Each grade belongs to a specific subject.
 */
@Entity
@Table(name = "grades")
public class Grade {

    /** Unique identifier for the grade */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numerical value of the grade */
    @Column(name = "value", nullable = false)
    private double value;

    /** Weight of the grade for average calculation */
    @Column(name = "weight", nullable = false)
    private double weight;

    /** Optional description of the grade */
    @Column(name = "description")
    private String description;

    /** Timestamp when the grade was created */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Subject this grade belongs to */
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Default constructor initializing creation timestamp.
     */
    public Grade() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a new grade with specified details.
     *
     * @param value numerical grade value
     * @param weight weight for average calculation
     * @param description optional grade description
     * @param subject subject this grade belongs to
     */
    public Grade(double value, double weight, String description, Subject subject) {
        this.value = value;
        this.weight = weight;
        this.description = description;
        this.subject = subject;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Gets the grade ID.
     *
     * @return the grade ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the grade ID.
     *
     * @param id the grade ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the numerical grade value.
     *
     * @return the grade value
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the numerical grade value.
     *
     * @param value the grade value
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Gets the grade weight.
     *
     * @return the grade weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets the grade weight.
     *
     * @param weight the grade weight
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Gets the grade description.
     *
     * @return the grade description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the grade description.
     *
     * @param description the grade description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the grade creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the subject this grade belongs to.
     *
     * @return the subject
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * Sets the subject this grade belongs to.
     *
     * @param subject the subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}