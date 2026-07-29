package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.GradeRepository;
import ch.zhaw.it.pm4.discordmanagerbe.data.repositories.SubjectRepository;
import ch.zhaw.it.pm4.discordmanagerbe.dto.AddGradeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotenrechnerServiceTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GradeValidationService validationService;

    @InjectMocks
    private NotenrechnerService notenrechnerService;

    private Subject testSubject;
    private Grade testGrade;
    private AddGradeRequest testRequest;

    @BeforeEach
    void setUp() {
        testSubject = new Subject("Mathematik", 6, "HS2023", "server1", "channel1", "user1");
        testSubject.setId(1L);

        testGrade = new Grade(4.5, 0.5, "Test Grade", testSubject);
        testGrade.setId(1L);

        testRequest = AddGradeRequest.builder()
                .serverId("server1")
                .channelId("channel1")
                .userId("user1")
                .subjectName("Mathematik")
                .semester("HS2023")
                .note(4.5)
                .weight(0.5)
                .description("Test Grade")
                .build();
    }

    @Test
    void createSubject_Success() {
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "Mathematik", "HS2023"))
                .thenReturn(Optional.empty());
        when(subjectRepository.save(any(Subject.class))).thenReturn(testSubject);

        Subject result = notenrechnerService.createSubject(
                "server1", "channel1", "user1", "Mathematik", 6, "HS2023");

        assertNotNull(result);
        assertEquals("Mathematik", result.getName());
        assertEquals(6, result.getCredits());
        verify(validationService).validateCreateSubjectParameters("Mathematik", 6);
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void createSubject_AlreadyExists_ThrowsException() {
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "Mathematik", "HS2023"))
                .thenReturn(Optional.of(testSubject));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                notenrechnerService.createSubject("server1", "channel1", "user1", "Mathematik", 6, "HS2023"));

        assertTrue(exception.getMessage().contains("existiert bereits"));
        verify(subjectRepository, never()).save(any());
    }

    @Test
    void addGrade_WithRequest_Success() {
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "Mathematik", "HS2023"))
                .thenReturn(Optional.of(testSubject));
        when(validationService.normalizeGrade(4.5)).thenReturn(4.5);
        when(validationService.normalizeWeight(0.5)).thenReturn(0.5);
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);

        Grade result = notenrechnerService.addGrade(testRequest);

        assertNotNull(result);
        verify(validationService).normalizeGrade(4.5);
        verify(validationService).normalizeWeight(0.5);
        verify(validationService).validateAddGradeParameters(eq("Mathematik"), eq(4.5), eq(0.5));
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    void removeSubject_Success() {
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "Mathematik", "HS2023"))
                .thenReturn(Optional.of(testSubject));

        boolean result = notenrechnerService.removeSubject(
                "server1", "channel1", "user1", "Mathematik", "HS2023");

        assertTrue(result);
        verify(gradeRepository).deleteBySubject(testSubject);
        verify(subjectRepository).delete(testSubject);
    }

    @Test
    void removeSubject_NotFound_ReturnsFalse() {
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "Mathematik", "HS2023"))
                .thenReturn(Optional.empty());

        boolean result = notenrechnerService.removeSubject(
                "server1", "channel1", "user1", "Mathematik", "HS2023");

        assertFalse(result);
        verify(gradeRepository, never()).deleteBySubject(any());
        verify(subjectRepository, never()).delete(any());
    }

    @Test
    void removeAllGradesFromSubject_Success() {
        testSubject.addGrade(testGrade);
        testSubject.addGrade(new Grade(5.0, 0.3, "Second Grade", testSubject));

        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "Mathematik", "HS2023"))
                .thenReturn(Optional.of(testSubject));

        int result = notenrechnerService.removeAllGradesFromSubject(
                "server1", "channel1", "user1", "Mathematik", "HS2023");

        assertEquals(2, result);
        verify(gradeRepository).deleteBySubject(testSubject);
        assertTrue(testSubject.getGrades().isEmpty());
    }

    @Test
    void removeSemester_Success() {
        List<Subject> subjects = Arrays.asList(testSubject, new Subject("Physik", 4, "HS2023", "server1", "channel1", "user1"));
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndSemester(
                "server1", "channel1", "user1", "HS2023"))
                .thenReturn(subjects);

        int result = notenrechnerService.removeSemester("server1", "channel1", "user1", "HS2023");

        assertEquals(2, result);
        verify(gradeRepository, times(2)).deleteBySubject(any(Subject.class));
        verify(subjectRepository).deleteAll(subjects);
    }

    @Test
    void calculateSubjectAverage_Success() {
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "Mathematik", "HS2023"))
                .thenReturn(Optional.of(testSubject));

        double result = notenrechnerService.calculateSubjectAverage(
                "server1", "channel1", "user1", "Mathematik", "HS2023");

        assertEquals(testSubject.calculateAverage(), result);
    }

    @Test
    void getSummary_Success() {
        List<Subject> subjects = Collections.singletonList(testSubject);
        List<String> semesters = List.of("HS2023");

        when(subjectRepository.findByServerIdAndChannelIdAndUserId("server1", "channel1", "user1"))
                .thenReturn(subjects);
        when(subjectRepository.findSemestersByServerIdAndChannelIdAndUserId("server1", "channel1", "user1"))
                .thenReturn(semesters);

        Map<String, Object> result = notenrechnerService.getSummary("server1", "channel1", "user1");

        assertNotNull(result);
        assertTrue(result.containsKey("semesters"));
        assertTrue(result.containsKey("overallAverage"));

        @SuppressWarnings("unchecked")
        Map<String, Object> semestersMap = (Map<String, Object>) result.get("semesters");
        assertTrue(semestersMap.containsKey("HS2023"));
    }

    @Test
    void findSubject_NotFound_ThrowsException() {
        when(subjectRepository.findByServerIdAndChannelIdAndUserIdAndNameAndSemester(
                "server1", "channel1", "user1", "NotFound", "HS2023"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                notenrechnerService.calculateSubjectAverage("server1", "channel1", "user1", "NotFound", "HS2023"));

        assertTrue(exception.getMessage().contains("nicht gefunden"));
    }
}