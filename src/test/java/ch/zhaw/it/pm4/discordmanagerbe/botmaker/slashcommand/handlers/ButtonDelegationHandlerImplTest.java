package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ButtonDelegationHandlerImpl Tests")
class ButtonDelegationHandlerImplTest {

    @Mock
    private InteractionErrorHandler errorHandler;

    @Mock
    private Consumer<ButtonInteractionEvent> mockHandler1;

    @Mock
    private Consumer<ButtonInteractionEvent> mockHandler2;

    @Mock
    private Consumer<ButtonInteractionEvent> mockHandler3;

    private ButtonDelegationHandlerImpl buttonHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        buttonHandler = new ButtonDelegationHandlerImpl(errorHandler);
    }

    @Test
    @DisplayName("Should return null when no handlers are registered")
    void testFindHandler_NoHandlersRegistered() {
        // Given
        String customId = "test-button";

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(customId);

        // Then
        assertNull(result, "Should return null when no handlers are registered");
    }

    @Test
    @DisplayName("Should return exact match handler when available")
    void testFindHandler_ExactMatch() {
        // Given
        String customId = "exact-match-button";
        buttonHandler.registerHandler(customId, mockHandler1);
        buttonHandler.registerHandler("other-button", mockHandler2);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(customId);

        // Then
        assertNotNull(result, "Should find exact match handler");
        assertEquals(mockHandler1, result, "Should return the correct handler for exact match");
    }

    @Test
    @DisplayName("Should prefer exact match over prefix match")
    void testFindHandler_ExactMatchPreferredOverPrefix() {
        // Given
        String exactId = "button";
        String prefixId = "button-prefix";

        buttonHandler.registerHandler(prefixId, mockHandler1); // Prefix handler registered first
        buttonHandler.registerHandler(exactId, mockHandler2); // Exact handler registered second

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(exactId);

        // Then
        assertNotNull(result, "Should find handler");
        assertEquals(mockHandler2, result, "Should prefer exact match over prefix match");
    }

    @Test
    @DisplayName("Should return prefix match when no exact match exists")
    void testFindHandler_PrefixMatch() {
        // Given
        String prefixId = "confirm";
        String fullId = "confirm-user-123";

        buttonHandler.registerHandler(prefixId, mockHandler1);
        buttonHandler.registerHandler("other-button", mockHandler2);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(fullId);

        // Then
        assertNotNull(result, "Should find prefix match handler");
        assertEquals(mockHandler1, result, "Should return the handler that matches the prefix");
    }

    @Test
    @DisplayName("Should return first matching prefix when multiple prefixes match")
    void testFindHandler_MultiplePrefixMatches() {
        // Given
        String shortPrefix = "btn";
        String longPrefix = "btn-action";
        String customId = "btn-action-delete-123";

        buttonHandler.registerHandler(shortPrefix, mockHandler1);
        buttonHandler.registerHandler(longPrefix, mockHandler2);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(customId);

        // Then
        assertNotNull(result, "Should find a prefix match handler");
        // Note: The order depends on the iteration order of the handlers map
        // This test verifies that one of the matching handlers is returned
        assertTrue(result == mockHandler1 || result == mockHandler2,
                "Should return one of the matching prefix handlers");
    }

    @Test
    @DisplayName("Should return null when no exact or prefix match exists")
    void testFindHandler_NoMatch() {
        // Given
        buttonHandler.registerHandler("button-one", mockHandler1);
        buttonHandler.registerHandler("button-two", mockHandler2);
        String nonMatchingId = "completely-different-id";

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(nonMatchingId);

        // Then
        assertNull(result, "Should return null when no exact or prefix match exists");
    }

    @Test
    @DisplayName("Should handle empty string input")
    void testFindHandler_EmptyString() {
        // Given
        buttonHandler.registerHandler("button", mockHandler1);
        String emptyId = "";

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(emptyId);

        // Then
        assertNull(result, "Should return null for empty string input");
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void testFindHandler_NullInput() {
        // Given
        buttonHandler.registerHandler("button", mockHandler1);
        String nullId = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            buttonHandler.findHandler(nullId);
        }, "Should throw NullPointerException for null input");
    }

    @Test
    @DisplayName("Should work with special characters in identifiers")
    void testFindHandler_SpecialCharacters() {
        // Given
        String prefixWithSpecialChars = "btn_action-";
        String fullIdWithSpecialChars = "btn_action-user:123";

        buttonHandler.registerHandler(prefixWithSpecialChars, mockHandler1);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(fullIdWithSpecialChars);

        // Then
        assertNotNull(result, "Should handle special characters in identifiers");
        assertEquals(mockHandler1, result, "Should return correct handler for special characters");
    }

    @Test
    @DisplayName("Should not match when customId is shorter than registered prefix")
    void testFindHandler_CustomIdShorterThanPrefix() {
        // Given
        String longPrefix = "very-long-button-prefix";
        String shortCustomId = "short";

        buttonHandler.registerHandler(longPrefix, mockHandler1);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(shortCustomId);

        // Then
        assertNull(result, "Should not match when customId is shorter than registered prefix");
    }

    @Test
    @DisplayName("Should work with identical prefix and exact match identifiers")
    void testFindHandler_IdenticalPrefixAndExact() {
        // Given
        String identifier = "button-action";
        buttonHandler.registerHandler(identifier, mockHandler1);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(identifier);

        // Then
        assertNotNull(result, "Should find handler for identical prefix and exact match");
        assertEquals(mockHandler1, result, "Should return the registered handler");
    }

    @Test
    @DisplayName("Should handle case-sensitive matching")
    void testFindHandler_CaseSensitive() {
        // Given
        String lowerCasePrefix = "button";
        String upperCaseCustomId = "BUTTON-123";

        buttonHandler.registerHandler(lowerCasePrefix, mockHandler1);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(upperCaseCustomId);

        // Then
        assertNull(result, "Should be case-sensitive and not match different cases");
    }

    @Test
    @DisplayName("Should work with numeric identifiers")
    void testFindHandler_NumericIdentifiers() {
        // Given
        String numericPrefix = "123";
        String numericCustomId = "123456";

        buttonHandler.registerHandler(numericPrefix, mockHandler1);

        // When
        Consumer<ButtonInteractionEvent> result = buttonHandler.findHandler(numericCustomId);

        // Then
        assertNotNull(result, "Should work with numeric identifiers");
        assertEquals(mockHandler1, result, "Should return handler for numeric prefix match");
    }
}