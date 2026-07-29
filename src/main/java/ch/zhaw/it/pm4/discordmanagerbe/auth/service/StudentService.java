package ch.zhaw.it.pm4.discordmanagerbe.auth.service;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Student;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing student data.
 */
@Service
public class StudentService {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    /** Repository for accessing and managing student data. */
    private final StudentRepository studentRepository;

    /**
     * Constructs an instance of StudentService with the required dependencies.
     *
     * @param studentRepository Repository for accessing and managing student data
     */
    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Creates or updates a student with Discord information.
     *
     * @param discordId Discord user ID
     * @param username Discord username
     * @param email User email
     * @return The existing or newly created student
     */
    public Student createStudentIfNotExists(String discordId, String username, String email) {
        Optional<Student> existingStudent = studentRepository.findByDiscordId(discordId);

        if (existingStudent.isPresent()) {
            logger.info("Student with Discord ID: {} already exists", discordId);
            return existingStudent.get();
        } else {
            logger.info("Creating new student with Discord ID: {}", discordId);
            Student newStudent = new Student(discordId, username, email);
            return studentRepository.save(newStudent);
        }
    }

    /**
     * Finds a student by Discord ID.
     *
     * @param discordId Discord user ID
     * @return Optional containing the student if found
     */
    public Optional<Student> findStudentByDiscordId(String discordId) {
        return studentRepository.findByDiscordId(discordId);
    }
}
