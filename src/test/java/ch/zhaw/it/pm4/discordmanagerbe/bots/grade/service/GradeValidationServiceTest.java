package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GradeValidationServiceTest {

    private GradeValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new GradeValidationService();
    }

    @Test
    void validateGrade_ValidGrade_NoException() {
        assertDoesNotThrow(() -> validationService.validateGrade(4.5));
        assertDoesNotThrow(() -> validationService.validateGrade(1.0));
        assertDoesNotThrow(() -> validationService.validateGrade(6.0));
    }

    @Test
    void validateGrade_InvalidGrade_ThrowsException() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateGrade(0.5));
        assertTrue(exception1.getMessage().contains("Note muss zwischen"));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateGrade(6.5));
        assertTrue(exception2.getMessage().contains("Note muss zwischen"));
    }

    @Test
    void validateWeight_ValidWeight_NoException() {
        assertDoesNotThrow(() -> validationService.validateWeight(0.5));
        assertDoesNotThrow(() -> validationService.validateWeight(0.0));
        assertDoesNotThrow(() -> validationService.validateWeight(1.0));
    }

    @Test
    void validateWeight_InvalidWeight_ThrowsException() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateWeight(-0.1));
        assertTrue(exception1.getMessage().contains("Gewichtung muss zwischen"));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateWeight(1.5));
        assertTrue(exception2.getMessage().contains("Gewichtung muss zwischen"));
    }

    @Test
    void normalizeGrade_NormalGrade_ReturnsUnchanged() {
        double result = validationService.normalizeGrade(4.5);

        assertEquals(4.5, result);
    }

    @Test
    void normalizeGrade_LargeGrade_DividedByTen() {
        double result = validationService.normalizeGrade(45.0);

        assertEquals(4.5, result);
    }

    @Test
    void normalizeGrade_InvalidAfterNormalization_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                validationService.normalizeGrade(75.0)); // 7.5 after division
    }

    @Test
    void normalizeWeight_NormalWeight_ReturnsUnchanged() {
        double result = validationService.normalizeWeight(0.5);

        assertEquals(0.5, result);
    }

    @Test
    void normalizeWeight_PercentageWeight_DividedByHundred() {
        double result = validationService.normalizeWeight(50.0);

        assertEquals(0.5, result);
    }

    @Test
    void normalizeWeight_InvalidAfterNormalization_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                validationService.normalizeWeight(150.0)); // 1.5 after division
    }

    @Test
    void validateNotEmpty_ValidString_NoException() {
        assertDoesNotThrow(() -> validationService.validateNotEmpty("Valid String", "Field"));
        assertDoesNotThrow(() -> validationService.validateNotEmpty("  Valid  ", "Field"));
    }

    @Test
    void validateNotEmpty_EmptyString_ThrowsException() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateNotEmpty("", "TestField"));
        assertTrue(exception1.getMessage().contains("TestField darf nicht leer sein"));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateNotEmpty("   ", "TestField"));
        assertTrue(exception2.getMessage().contains("TestField darf nicht leer sein"));

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateNotEmpty(null, "TestField"));
        assertTrue(exception3.getMessage().contains("TestField darf nicht leer sein"));
    }

    @Test
    void validateCredits_ValidCredits_NoException() {
        assertDoesNotThrow(() -> validationService.validateCredits(1));
        assertDoesNotThrow(() -> validationService.validateCredits(6));
        assertDoesNotThrow(() -> validationService.validateCredits(10));
    }

    @Test
    void validateCredits_InvalidCredits_ThrowsException() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateCredits(0));
        assertTrue(exception1.getMessage().contains("Credits müssen größer als 0 sein"));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () ->
                validationService.validateCredits(-1));
        assertTrue(exception2.getMessage().contains("Credits müssen größer als 0 sein"));
    }

    @Test
    void validateAddGradeParameters_ValidParameters_NoException() {
        assertDoesNotThrow(() -> validationService.validateAddGradeParameters("Mathematik", 4.5, 0.5));
    }

    @Test
    void validateAddGradeParameters_InvalidSubjectName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                validationService.validateAddGradeParameters("", 4.5, 0.5));
    }

    @Test
    void validateCreateSubjectParameters_ValidParameters_NoException() {
        assertDoesNotThrow(() -> validationService.validateCreateSubjectParameters("Mathematik", 6));
    }

    @Test
    void validateCreateSubjectParameters_InvalidParameters_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                validationService.validateCreateSubjectParameters("", 6));

        assertThrows(IllegalArgumentException.class, () ->
                validationService.validateCreateSubjectParameters("Mathematik", 0));
    }
}