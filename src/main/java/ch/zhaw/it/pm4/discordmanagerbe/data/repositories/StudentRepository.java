package ch.zhaw.it.pm4.discordmanagerbe.data.repositories;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Student entity operations.
 * Provides CRUD operations and custom queries for student management.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Finds a student by Discord ID.
     *
     * @param discordId Discord ID of the student
     * @return optional containing the student if found
     */
    Optional<Student> findByDiscordId(String discordId);

    /**
     * Finds a student by username.
     *
     * @param username Discord username of the student
     * @return optional containing the student if found
     */
    Optional<Student> findByUsername(String username);

    /**
     * Checks if a student exists by Discord ID.
     *
     * @param discordId Discord ID to check
     * @return true if student exists, false otherwise
     */
    boolean existsByDiscordId(String discordId);
}