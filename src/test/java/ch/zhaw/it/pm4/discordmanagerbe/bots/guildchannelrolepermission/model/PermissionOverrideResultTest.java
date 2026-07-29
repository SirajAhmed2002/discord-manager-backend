package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PermissionOverrideResultTest {

    @Test
    void success_WithoutData() {
        // Act
        PermissionOverrideResult<String> result = PermissionOverrideResult.success();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Operation completed successfully", result.getMessage());
        assertTrue(result.getData().isEmpty());
        assertTrue(result.getException().isEmpty());
    }

    @Test
    void success_WithData() {
        // Arrange
        String testData = "test data";

        // Act
        PermissionOverrideResult<String> result = PermissionOverrideResult.success(testData);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Operation completed successfully", result.getMessage());
        assertEquals(Optional.of(testData), result.getData());
        assertEquals(testData, result.getDataOrThrow());
        assertTrue(result.getException().isEmpty());
    }

    @Test
    void failure_WithMessage() {
        // Arrange
        String errorMessage = "Something went wrong";

        // Act
        PermissionOverrideResult<String> result = PermissionOverrideResult.failure(errorMessage);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals(errorMessage, result.getMessage());
        assertTrue(result.getData().isEmpty());
        assertTrue(result.getException().isEmpty());
    }

    @Test
    void failure_WithMessageAndException() {
        // Arrange
        String errorMessage = "Something went wrong";
        Exception exception = new RuntimeException("Root cause");

        // Act
        PermissionOverrideResult<String> result = PermissionOverrideResult.failure(errorMessage, exception);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals(errorMessage, result.getMessage());
        assertTrue(result.getData().isEmpty());
        assertEquals(Optional.of(exception), result.getException());
    }

    @Test
    void getDataOrThrow_SuccessWithData() {
        // Arrange
        String testData = "test data";
        PermissionOverrideResult<String> result = PermissionOverrideResult.success(testData);

        // Act & Assert
        assertEquals(testData, result.getDataOrThrow());
    }

    @Test
    void getDataOrThrow_SuccessWithoutData() {
        // Arrange
        PermissionOverrideResult<String> result = PermissionOverrideResult.success();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, result::getDataOrThrow);
        assertEquals("No data available", exception.getMessage());
    }

    @Test
    void getDataOrThrow_Failure() {
        // Arrange
        String errorMessage = "Operation failed";
        PermissionOverrideResult<String> result = PermissionOverrideResult.failure(errorMessage);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, result::getDataOrThrow);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void getDataOrThrow_FailureWithException() {
        // Arrange
        String errorMessage = "Operation failed";
        RuntimeException originalException = new RuntimeException("Root cause");
        PermissionOverrideResult<String> result = PermissionOverrideResult.failure(errorMessage, originalException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, result::getDataOrThrow);
        assertEquals(originalException, thrownException);
    }

    @Test
    void getDataOrThrow_FailureWithNonRuntimeException() {
        // Arrange
        String errorMessage = "Operation failed";
        Exception originalException = new Exception("Root cause");
        PermissionOverrideResult<String> result = PermissionOverrideResult.failure(errorMessage, originalException);

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, result::getDataOrThrow);
        assertEquals(errorMessage, thrownException.getMessage());
        assertEquals(originalException, thrownException.getCause());
    }
}