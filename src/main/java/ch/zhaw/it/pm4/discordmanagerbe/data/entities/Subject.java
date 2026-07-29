package ch.zhaw.it.pm4.discordmanagerbe.data.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an academic subject with grades and Discord context.
 * Manages subject information and calculates weighted grade averages.
 */
@Entity
@Table(name = "subjects",
        uniqueConstraints = {
                @UniqueConstraint(name = "subject_server_channel_user_name_semester_unique",
                        columnNames = {"server_id", "channel_id", "user_id", "name", "semester"})
        })
public class Subject {

    /** Unique identifier for the subject */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the subject */
    @Column(name = "name", nullable = false)
    private String name;

    /** Number of credits for this subject */
    @Column(name = "credits", nullable = false)
    private int credits;

    /** Semester when the subject is taken */
    @Column(name = "semester", nullable = true)
    private String semester;

    /** Discord server ID where subject is tracked */
    @Column(name = "server_id", nullable = false)
    private String serverId;

    /** Discord channel ID where subject is tracked */
    @Column(name = "channel_id", nullable = false)
    private String channelId;

    /** Discord user ID who owns this subject */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** List of grades associated with this subject */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Grade> grades = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Subject() {}

    /**
     * Creates a new subject with specified details.
     *
     * @param name subject name
     * @param credits number of credits
     * @param semester semester identifier
     * @param serverId Discord server ID
     * @param channelId Discord channel ID
     * @param userId Discord user ID
     */
    public Subject(String name, int credits, String semester, String serverId, String channelId, String userId) {
        this.name = name;
        this.credits = credits;
        this.semester = semester;
        this.serverId = serverId;
        this.channelId = channelId;
        this.userId = userId;
    }

    /**
     * Calculates the weighted average of all grades.
     * Returns 0 if no grades exist or total weight is 0.
     *
     * @return weighted average grade
     */
    public double calculateAverage() {
        if (grades.isEmpty()) {
            return 0;
        }

        double sumWeightedGrades = 0;
        double sumWeights = 0;

        for (Grade grade : grades) {
            sumWeightedGrades += grade.getValue() * grade.getWeight();
            sumWeights += grade.getWeight();
        }

        if (sumWeights == 0) {
            return 0;
        }

        return sumWeightedGrades / sumWeights;
    }

    /**
     * Gets the subject ID.
     *
     * @return the subject ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the subject ID.
     *
     * @param id the subject ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the subject name.
     *
     * @return the subject name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the subject name.
     *
     * @param name the subject name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the number of credits.
     *
     * @return the credits
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Sets the number of credits.
     *
     * @param credits the credits
     */
    public void setCredits(int credits) {
        this.credits = credits;
    }

    /**
     * Gets the semester identifier.
     *
     * @return the semester
     */
    public String getSemester() {
        return semester;
    }

    /**
     * Sets the semester identifier.
     *
     * @param semester the semester
     */
    public void setSemester(String semester) {
        this.semester = semester;
    }

    /**
     * Gets the Discord server ID.
     *
     * @return the server ID
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Sets the Discord server ID.
     *
     * @param serverId the server ID
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Gets the Discord channel ID.
     *
     * @return the channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Sets the Discord channel ID.
     *
     * @param channelId the channel ID
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * Gets the Discord user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the Discord user ID.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the list of grades.
     *
     * @return the grades list
     */
    public List<Grade> getGrades() {
        return grades;
    }

    /**
     * Sets the list of grades.
     *
     * @param grades the grades list
     */
    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }

    /**
     * Adds a grade to this subject and establishes bidirectional relationship.
     *
     * @param grade the grade to add
     */
    public void addGrade(Grade grade) {
        grades.add(grade);
        grade.setSubject(this);
    }

    /**
     * Removes a grade from this subject and clears bidirectional relationship.
     *
     * @param grade the grade to remove
     */
    public void removeGrade(Grade grade) {
        grades.remove(grade);
        grade.setSubject(null);
    }
}