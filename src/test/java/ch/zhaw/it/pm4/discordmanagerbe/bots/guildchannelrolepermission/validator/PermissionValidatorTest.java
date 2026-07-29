package ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.validator;

import ch.zhaw.it.pm4.discordmanagerbe.bots.guildchannelrolepermission.exception.PermissionConflictException;
import net.dv8tion.jda.api.Permission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PermissionValidatorTest {

    @InjectMocks
    private PermissionValidator permissionValidator;

    @Test
    void validateIds_ValidIds() {
        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> permissionValidator.validateIds("123", "456", "789"));
    }

    @Test
    void validateIds_NullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> permissionValidator.validateIds("123", null, "789")
        );
        assertEquals("ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void validateIds_EmptyId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> permissionValidator.validateIds("123", "", "789")
        );
        assertEquals("ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void validateIds_WhitespaceOnlyId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> permissionValidator.validateIds("123", "   ", "789")
        );
        assertEquals("ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void validateNoConflicts_NoConflicts() {
        // Arrange
        Set<Permission> allowed = Set.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL);
        Set<Permission> denied = Set.of(Permission.MESSAGE_MANAGE, Permission.ADMINISTRATOR);

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> permissionValidator.validateNoConflicts(allowed, denied));
    }

    @Test
    void validateNoConflicts_WithConflicts() {
        // Arrange
        Set<Permission> allowed = Set.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL);
        Set<Permission> denied = Set.of(Permission.MESSAGE_SEND, Permission.ADMINISTRATOR);

        // Act & Assert
        PermissionConflictException exception = assertThrows(
                PermissionConflictException.class,
                () -> permissionValidator.validateNoConflicts(allowed, denied)
        );
        assertTrue(exception.getMessage().contains("Conflicting permissions found"));
        assertTrue(exception.getMessage().contains("MESSAGE_SEND"));
    }

    @Test
    void validateNoConflicts_EmptySets() {
        // Arrange
        Set<Permission> allowed = Collections.emptySet();
        Set<Permission> denied = Collections.emptySet();

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> permissionValidator.validateNoConflicts(allowed, denied));
    }

    @Test
    void validateNoConflicts_MultipleConflicts() {
        // Arrange
        Set<Permission> allowed = Set.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL, Permission.ADMINISTRATOR);
        Set<Permission> denied = Set.of(Permission.MESSAGE_SEND, Permission.ADMINISTRATOR, Permission.KICK_MEMBERS);

        // Act & Assert
        PermissionConflictException exception = assertThrows(
                PermissionConflictException.class,
                () -> permissionValidator.validateNoConflicts(allowed, denied)
        );
        assertTrue(exception.getMessage().contains("MESSAGE_SEND"));
        assertTrue(exception.getMessage().contains("ADMINISTRATOR"));
    }
}