package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.core;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractDelegationHandlerTest {

    @Mock
    private InteractionErrorHandler errorHandler;

    @Mock
    private Consumer<String> mockHandler1;

    @Mock
    private Consumer<String> mockHandler2;

    private TestDelegationHandler delegationHandler;

    private static class TestDelegationHandler extends AbstractDelegationHandler<String> {
        private String lastIdentifier;
        private String lastEvent;
        private Exception lastError;
        private boolean noHandlerFoundCalled = false;

        public TestDelegationHandler(InteractionErrorHandler errorHandler) {
            super(errorHandler);
        }

        @Override
        protected String extractIdentifier(String event) {
            return event;
        }

        @Override
        protected String getHandlerTypeName() {
            return "test";
        }

        @Override
        protected void handleError(String event, String identifier, Exception e) {
            this.lastEvent = event;
            this.lastIdentifier = identifier;
            this.lastError = e;
            errorHandler.handleInteractionError(event, identifier, e);
        }

        @Override
        protected void handleNoHandlerFound(String event, String identifier) {
            this.lastEvent = event;
            this.lastIdentifier = identifier;
            this.noHandlerFoundCalled = true;
        }

        // Getter
        public String getLastIdentifier() { return lastIdentifier; }
        public String getLastEvent() { return lastEvent; }
        public Exception getLastError() { return lastError; }
        public boolean wasNoHandlerFoundCalled() { return noHandlerFoundCalled; }
    }

    @BeforeEach
    void setUp() {
        delegationHandler = new TestDelegationHandler(errorHandler);
    }

    @Test
    void testRegisterHandler() {
        // When
        delegationHandler.registerHandler("test1", mockHandler1);

        // Then
        assertEquals(1, delegationHandler.getHandlerCount());
        assertTrue(delegationHandler.canHandle("test1"));
        assertFalse(delegationHandler.canHandle("nonexistent"));
    }

    @Test
    void testRegisterHandlers() {
        // Given
        Map<String, Consumer<String>> handlers = new HashMap<>();
        handlers.put("test1", mockHandler1);
        handlers.put("test2", mockHandler2);

        // When
        delegationHandler.registerHandlers(handlers);

        // Then
        assertEquals(2, delegationHandler.getHandlerCount());
        assertTrue(delegationHandler.canHandle("test1"));
        assertTrue(delegationHandler.canHandle("test2"));
    }

    @Test
    void testRemoveHandler() {
        // Given
        delegationHandler.registerHandler("test1", mockHandler1);
        delegationHandler.registerHandler("test2", mockHandler2);

        // When
        boolean removed = delegationHandler.removeHandler("test1");
        boolean notRemoved = delegationHandler.removeHandler("nonexistent");

        // Then
        assertTrue(removed);
        assertFalse(notRemoved);
        assertEquals(1, delegationHandler.getHandlerCount());
        assertFalse(delegationHandler.canHandle("test1"));
        assertTrue(delegationHandler.canHandle("test2"));
    }

    @Test
    void testHandleInteractionSuccess() {
        // Given
        delegationHandler.registerHandler("test-event", mockHandler1);

        // When
        delegationHandler.handleInteraction("test-event");

        // Then
        verify(mockHandler1).accept("test-event");
        assertNull(delegationHandler.getLastError());
    }

    @Test
    void testHandleInteractionWithException() {
        // Given
        RuntimeException exception = new RuntimeException("Test exception");
        Consumer<String> failingHandler = mock(Consumer.class);
        doThrow(exception).when(failingHandler).accept(anyString());

        delegationHandler.registerHandler("failing-event", failingHandler);

        // When
        delegationHandler.handleInteraction("failing-event");

        // Then
        verify(errorHandler).handleInteractionError("failing-event", "failing-event", exception);
        assertEquals("failing-event", delegationHandler.getLastEvent());
        assertEquals("failing-event", delegationHandler.getLastIdentifier());
        assertEquals(exception, delegationHandler.getLastError());
    }

    @Test
    void testHandleInteractionNoHandlerFound() {
        // When
        delegationHandler.handleInteraction("nonexistent-event");

        // Then
        assertTrue(delegationHandler.wasNoHandlerFoundCalled());
        assertEquals("nonexistent-event", delegationHandler.getLastEvent());
        assertEquals("nonexistent-event", delegationHandler.getLastIdentifier());
    }

    @Test
    void testCanHandle() {
        // Given
        delegationHandler.registerHandler("existing", mockHandler1);

        // Then
        assertTrue(delegationHandler.canHandle("existing"));
        assertFalse(delegationHandler.canHandle("nonexistent"));
    }

    @Test
    void testGetHandlerCount() {
        // Given
        assertEquals(0, delegationHandler.getHandlerCount());

        // When
        delegationHandler.registerHandler("test1", mockHandler1);
        delegationHandler.registerHandler("test2", mockHandler2);

        // Then
        assertEquals(2, delegationHandler.getHandlerCount());

        // When
        delegationHandler.removeHandler("test1");

        // Then
        assertEquals(1, delegationHandler.getHandlerCount());
    }

    @Test
    void testThreadSafety() {

        // Given
        delegationHandler.registerHandler("test", mockHandler1);

        // When
        assertDoesNotThrow(() -> {
            delegationHandler.registerHandler("test2", mockHandler2);
            delegationHandler.canHandle("test");
            delegationHandler.getHandlerCount();
            delegationHandler.removeHandler("test2");
        });

        // Then
        assertEquals(1, delegationHandler.getHandlerCount());
        assertTrue(delegationHandler.canHandle("test"));
    }

    @Test
    void testRegisterHandlerOverwrite() {
        // Given
        delegationHandler.registerHandler("test", mockHandler1);

        // When
        delegationHandler.registerHandler("test", mockHandler2);

        // Then
        assertEquals(1, delegationHandler.getHandlerCount());

        delegationHandler.handleInteraction("test");
        verify(mockHandler2).accept("test");
        verify(mockHandler1, never()).accept("test");
    }
}