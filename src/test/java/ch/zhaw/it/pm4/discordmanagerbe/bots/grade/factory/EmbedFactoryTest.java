package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory;

import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import ch.zhaw.it.pm4.discordmanagerbe.dto.*;
import net.dv8tion.jda.api.EmbedBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class EmbedFactoryTest {

    private EmbedFactory embedFactory;
    private Subject testSubject;
    private Grade testGrade;
    private AddGradeRequest testRequest;

    @BeforeEach
    void setUp() {
        embedFactory = new EmbedFactory();
        
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
    void createSubjectCreatedEmbed_WithSemester_ContainsSemesterField() {
        EmbedBuilder result = embedFactory.createSubjectCreatedEmbed(testSubject, "HS2023");

        assertNotNull(result);
        assertEquals("Fach erstellt", result.build().getTitle());
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Semester".equals(field.getName()) && "HS2023".equals(field.getValue())));
    }

    @Test
    void createSubjectCreatedEmbed_WithoutSemester_NoSemesterField() {
        EmbedBuilder result = embedFactory.createSubjectCreatedEmbed(testSubject, null);

        assertNotNull(result);
        assertEquals("Fach erstellt", result.build().getTitle());
        assertFalse(result.build().getFields().stream()
                .anyMatch(field -> "Semester".equals(field.getName())));
    }

    @Test
    void createGradeAddedEmbed_AllFields_ContainsAllInformation() {
        EmbedBuilder result = embedFactory.createGradeAddedEmbed(testGrade, testRequest, 4.5);

        assertNotNull(result);
        assertEquals("Note hinzugefügt", result.build().getTitle());
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Fach".equals(field.getName()) && "Mathematik".equals(field.getValue())));
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Note".equals(field.getName()) && "4.5".equals(field.getValue())));
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Gewichtung".equals(field.getName()) && "50%".equals(field.getValue())));
    }

    @Test
    void createSubjectsDisplayEmbed_WithSubjects_ShowsSubjectInfo() {
        List<Subject> subjects = Collections.singletonList(testSubject);
        SubjectDisplayData displayData = new SubjectDisplayData(subjects, 4.5, "HS2023");

        EmbedBuilder result = embedFactory.createSubjectsDisplayEmbed(displayData);

        assertNotNull(result);
        assertEquals("Deine Fächer für HS2023", result.build().getTitle());
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Gesamtdurchschnitt".equals(field.getName())));
    }

    @Test
    void createGradesDisplayEmbed_WithGrades_ShowsGradesWithNumbers() {
        testGrade.getSubject().addGrade(testGrade);
        List<Grade> grades = Collections.singletonList(testGrade);
        GradeDisplayData displayData = new GradeDisplayData(grades, 4.5, "Mathematik", "HS2023");

        EmbedBuilder result = embedFactory.createGradesDisplayEmbed(displayData);

        assertNotNull(result);
        assertEquals("Noten für Mathematik (HS2023)", result.build().getTitle());
        System.out.println(result.build().getDescription());
        assertTrue(Objects.requireNonNull(result.build().getDescription()).contains("Aktueller Durchschnitt: 4.5"));
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> {
                    assertNotNull(field.getName());
                    return field.getName().contains("#1 - Note:");
                }));
    }

    @Test
    void createSubjectRemovedEmbed_Success_ShowsSuccessMessage() {
        EmbedBuilder result = embedFactory.createSubjectRemovedEmbed("Mathematik", "HS2023", true);

        assertNotNull(result);
        assertEquals("Fach gelöscht", result.build().getTitle());
        assertTrue(Objects.requireNonNull(result.build().getDescription()).contains("erfolgreich gelöscht"));
    }

    @Test
    void createSubjectRemovedEmbed_NotFound_ShowsErrorMessage() {
        EmbedBuilder result = embedFactory.createSubjectRemovedEmbed("Mathematik", "HS2023", false);

        assertNotNull(result);
        assertEquals("Fach nicht gefunden", result.build().getTitle());
        assertTrue(Objects.requireNonNull(result.build().getDescription()).contains("wurde nicht gefunden"));
    }

    @Test
    void createGradesRemovedEmbed_Success_ShowsRemovedCount() {
        EmbedBuilder result = embedFactory.createGradesRemovedEmbed("Mathematik", "HS2023", 3);

        assertNotNull(result);
        assertEquals("Noten gelöscht", result.build().getTitle());
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Gelöschte Noten".equals(field.getName()) && "3".equals(field.getValue())));
    }

    @Test
    void createGradesRemovedEmbed_NoGrades_ShowsInfoMessage() {
        EmbedBuilder result = embedFactory.createGradesRemovedEmbed("Mathematik", "HS2023", 0);

        assertNotNull(result);
        assertEquals("Keine Noten vorhanden", result.build().getTitle());
        assertTrue(Objects.requireNonNull(result.build().getDescription()).contains("hat keine Noten zum Löschen"));
    }

    @Test
    void createSemesterRemovedEmbed_Success_ShowsRemovedCount() {
        EmbedBuilder result = embedFactory.createSemesterRemovedEmbed("HS2023", 5);

        assertNotNull(result);
        assertEquals("Semester gelöscht", result.build().getTitle());
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Gelöschte Fächer".equals(field.getName()) && "5".equals(field.getValue())));
    }

    @Test
    void createSemestersDisplayEmbed_WithSemesters_ShowsList() {
        List<String> semesters = Arrays.asList("HS2023", "FS2024");

        EmbedBuilder result = embedFactory.createSemestersDisplayEmbed(semesters);

        assertNotNull(result);
        assertEquals("Deine Semester", result.build().getTitle());
        assertTrue(Objects.requireNonNull(result.build().getDescription()).contains("• HS2023"));
        assertTrue(Objects.requireNonNull(result.build().getDescription()).contains("• FS2024"));
    }

    @Test
    void createSemesterAverageEmbed_WithSummary_ShowsDetails() {
        List<SubjectSummary> subjects = List.of(
                new SubjectSummary("Mathematik", 4.5, 6, 3)
        );
        SemesterSummary summary = new SemesterSummary("HS2023", 4.5, subjects);

        EmbedBuilder result = embedFactory.createSemesterAverageEmbed(summary);

        assertNotNull(result);
        assertEquals("Durchschnitt für HS2023", result.build().getTitle());
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Semesterdurchschnitt".equals(field.getName())));
    }

    @Test
    void createOverallAverageEmbed_WithData_ShowsAllSemesters() {
        List<SubjectSummary> subjects = List.of(
                new SubjectSummary("Mathematik", 4.5, 6, 3)
        );
        List<SemesterSummary> semesters = List.of(
                new SemesterSummary("HS2023", 4.5, subjects)
        );
        AverageDisplayData displayData = new AverageDisplayData(4.5, semesters, null);

        EmbedBuilder result = embedFactory.createOverallAverageEmbed(displayData);

        assertNotNull(result);
        assertEquals("Deine Notendurchschnitte", result.build().getTitle());
        assertTrue(result.build().getFields().stream()
                .anyMatch(field -> "Gesamtdurchschnitt".equals(field.getName())));
    }
}