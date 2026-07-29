package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util;

import ch.zhaw.it.pm4.discordmanagerbe.dto.SemesterSummary;
import ch.zhaw.it.pm4.discordmanagerbe.dto.SubjectSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SummaryDataConverterTest {

    private SummaryDataConverter summaryDataConverter;

    @BeforeEach
    void setUp() {
        summaryDataConverter = new SummaryDataConverter();
    }

    @Test
    void convertToSemesterSummaries_ValidData_ReturnsCorrectSummaries() {
        // Arrange
        Map<String, Object> summary = createTestSummaryData();

        // Act
        List<SemesterSummary> result = summaryDataConverter.convertToSemesterSummaries(summary);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        SemesterSummary hs2023 = result.stream()
                .filter(s -> "HS2023".equals(s.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(hs2023);
        assertEquals("HS2023", hs2023.getName());
        assertEquals(4.5, hs2023.getAverage());
        assertEquals(2, hs2023.getSubjects().size());

        SubjectSummary math = hs2023.getSubjects().stream()
                .filter(s -> "Mathematik".equals(s.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(math);
        assertEquals("Mathematik", math.getName());
        assertEquals(4.5, math.getAverage());
        assertEquals(6, math.getCredits());
        assertEquals(3, math.getGradeCount());
    }

    @Test
    void convertToSemesterSummaries_EmptyData_ReturnsEmptyList() {
        // Arrange
        Map<String, Object> summary = new HashMap<>();
        summary.put("semesters", new HashMap<String, Object>());

        // Act
        List<SemesterSummary> result = summaryDataConverter.convertToSemesterSummaries(summary);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Map<String, Object> createTestSummaryData() {
        Map<String, Object> summary = new HashMap<>();
        Map<String, Object> semestersMap = new HashMap<>();

        // HS2023 Semester
        Map<String, Object> hs2023 = new HashMap<>();
        hs2023.put("average", 4.5);
        
        Map<String, Object> hs2023Subjects = new HashMap<>();
        
        // Mathematik Subject
        Map<String, Object> math = new HashMap<>();
        math.put("average", 4.5);
        math.put("credits", 6);
        math.put("gradeCount", 3);
        hs2023Subjects.put("Mathematik", math);
        
        // Physik Subject
        Map<String, Object> physics = new HashMap<>();
        physics.put("average", 5.0);
        physics.put("credits", 4);
        physics.put("gradeCount", 2);
        hs2023Subjects.put("Physik", physics);
        
        hs2023.put("subjects", hs2023Subjects);
        semestersMap.put("HS2023", hs2023);

        // FS2024 Semester
        Map<String, Object> fs2024 = new HashMap<>();
        fs2024.put("average", 5.2);
        
        Map<String, Object> fs2024Subjects = new HashMap<>();
        
        Map<String, Object> chemistry = new HashMap<>();
        chemistry.put("average", 5.2);
        chemistry.put("credits", 5);
        chemistry.put("gradeCount", 1);
        fs2024Subjects.put("Chemie", chemistry);
        
        fs2024.put("subjects", fs2024Subjects);
        semestersMap.put("FS2024", fs2024);

        summary.put("semesters", semestersMap);
        return summary;
    }
}