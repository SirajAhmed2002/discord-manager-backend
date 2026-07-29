package ch.zhaw.it.pm4.discordmanagerbe.data.entities;
import jakarta.persistence.*;

/**
 * Entity representing a student with Discord integration.
 * Stores student information including Discord ID, username, and email.
 */
@Entity(name = "Student")
@Table(name = "student",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "student_email_unique",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "student_discord_id_unique",
                        columnNames = "discord_id"
                )
        }
)
public class Student {

    /** Unique identifier for the student */
    @Id
    @SequenceGenerator(
            name = "student_sequence",
            sequenceName = "student_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "student_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    /** Unique Discord ID of the student */
    @Column(
            name = "discord_id",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String discordId;

    /** Discord username of the student */
    @Column(
            name = "username",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String username;

    /** Email address of the student */
    @Column(
            name = "email",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String email;

    /**
     * Creates a new student with specified details.
     *
     * @param discordId unique Discord ID
     * @param username Discord username
     * @param email student's email address
     */
    public Student(String discordId, String username, String email) {
        this.discordId = discordId;
        this.username = username;
        this.email = email;
    }

    /**
     * Default constructor.
     */
    public Student() {
    }

    /**
     * Gets the student ID.
     *
     * @return the student ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the student ID.
     *
     * @param id the student ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the Discord ID.
     *
     * @return the Discord ID
     */
    public String getDiscordId() {
        return discordId;
    }

    /**
     * Sets the Discord ID.
     *
     * @param discordId the Discord ID
     */
    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    /**
     * Gets the Discord username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the Discord username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", discordId='" + discordId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}