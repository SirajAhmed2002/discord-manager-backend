package ch.zhaw.it.pm4.discordmanagerbe.auth.service;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Student;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student testStudent;
    private String testDiscordId;
    private String testUsername;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testDiscordId = "123456789";
        testUsername = "testuser";
        testEmail = "test@example.com";
        testStudent = new Student(testDiscordId, testUsername, testEmail);
    }

    @Test
    void createStudentIfNotExists_WhenStudentExists_ShouldReturnExistingStudent() {
        // Given
        when(studentRepository.findByDiscordId(testDiscordId))
                .thenReturn(Optional.of(testStudent));

        // When
        Student result = studentService.createStudentIfNotExists(testDiscordId, testUsername, testEmail);

        // Then
        assertNotNull(result);
        assertEquals(testStudent, result);
        assertEquals(testDiscordId, result.getDiscordId());
        assertEquals(testUsername, result.getUsername());
        assertEquals(testEmail, result.getEmail());

        verify(studentRepository, times(1)).findByDiscordId(testDiscordId);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createStudentIfNotExists_WhenStudentDoesNotExist_ShouldCreateAndReturnNewStudent() {
        // Given
        when(studentRepository.findByDiscordId(testDiscordId))
                .thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class)))
                .thenReturn(testStudent);

        // When
        Student result = studentService.createStudentIfNotExists(testDiscordId, testUsername, testEmail);

        // Then
        assertNotNull(result);
        assertEquals(testStudent, result);
        assertEquals(testDiscordId, result.getDiscordId());
        assertEquals(testUsername, result.getUsername());
        assertEquals(testEmail, result.getEmail());

        verify(studentRepository, times(1)).findByDiscordId(testDiscordId);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void createStudentIfNotExists_WhenStudentDoesNotExist_ShouldSaveStudentWithCorrectData() {
        // Given
        when(studentRepository.findByDiscordId(testDiscordId))
                .thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class)))
                .thenReturn(testStudent);

        // When
        studentService.createStudentIfNotExists(testDiscordId, testUsername, testEmail);

        // Then
        verify(studentRepository).save(argThat(student ->
                testDiscordId.equals(student.getDiscordId()) &&
                        testUsername.equals(student.getUsername()) &&
                        testEmail.equals(student.getEmail())
        ));
    }

    @Test
    void createStudentIfNotExists_WithNullDiscordId_ShouldHandleGracefully() {
        // Given
        String nullDiscordId = null;
        when(studentRepository.findByDiscordId(nullDiscordId))
                .thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class)))
                .thenReturn(new Student(nullDiscordId, testUsername, testEmail));

        // When
        Student result = studentService.createStudentIfNotExists(nullDiscordId, testUsername, testEmail);

        // Then
        assertNotNull(result);
        verify(studentRepository, times(1)).findByDiscordId(nullDiscordId);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void createStudentIfNotExists_WithEmptyStrings_ShouldCreateStudent() {
        // Given
        String emptyDiscordId = "";
        String emptyUsername = "";
        String emptyEmail = "";
        Student emptyStudent = new Student(emptyDiscordId, emptyUsername, emptyEmail);

        when(studentRepository.findByDiscordId(emptyDiscordId))
                .thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class)))
                .thenReturn(emptyStudent);

        // When
        Student result = studentService.createStudentIfNotExists(emptyDiscordId, emptyUsername, emptyEmail);

        // Then
        assertNotNull(result);
        assertEquals(emptyStudent, result);
        verify(studentRepository, times(1)).findByDiscordId(emptyDiscordId);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void findStudentByDiscordId_WhenStudentExists_ShouldReturnStudent() {
        // Given
        when(studentRepository.findByDiscordId(testDiscordId))
                .thenReturn(Optional.of(testStudent));

        // When
        Optional<Student> result = studentService.findStudentByDiscordId(testDiscordId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testStudent, result.get());
        assertEquals(testDiscordId, result.get().getDiscordId());
        verify(studentRepository, times(1)).findByDiscordId(testDiscordId);
    }

    @Test
    void findStudentByDiscordId_WhenStudentDoesNotExist_ShouldReturnEmptyOptional() {
        // Given
        when(studentRepository.findByDiscordId(testDiscordId))
                .thenReturn(Optional.empty());

        // When
        Optional<Student> result = studentService.findStudentByDiscordId(testDiscordId);

        // Then
        assertFalse(result.isPresent());
        verify(studentRepository, times(1)).findByDiscordId(testDiscordId);
    }

    @Test
    void findStudentByDiscordId_WithNullDiscordId_ShouldCallRepositoryWithNull() {
        // Given
        String nullDiscordId = null;
        when(studentRepository.findByDiscordId(nullDiscordId))
                .thenReturn(Optional.empty());

        // When
        Optional<Student> result = studentService.findStudentByDiscordId(nullDiscordId);

        // Then
        assertFalse(result.isPresent());
        verify(studentRepository, times(1)).findByDiscordId(nullDiscordId);
    }

    @Test
    void findStudentByDiscordId_WithEmptyString_ShouldCallRepositoryWithEmptyString() {
        // Given
        String emptyDiscordId = "";
        when(studentRepository.findByDiscordId(emptyDiscordId))
                .thenReturn(Optional.empty());

        // When
        Optional<Student> result = studentService.findStudentByDiscordId(emptyDiscordId);

        // Then
        assertFalse(result.isPresent());
        verify(studentRepository, times(1)).findByDiscordId(emptyDiscordId);
    }

    @Test
    void createStudentIfNotExists_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        when(studentRepository.findByDiscordId(testDiscordId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                studentService.createStudentIfNotExists(testDiscordId, testUsername, testEmail)
        );

        verify(studentRepository, times(1)).findByDiscordId(testDiscordId);
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void findStudentByDiscordId_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        when(studentRepository.findByDiscordId(testDiscordId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                studentService.findStudentByDiscordId(testDiscordId)
        );

        verify(studentRepository, times(1)).findByDiscordId(testDiscordId);
    }
}