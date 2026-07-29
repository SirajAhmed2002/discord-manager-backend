package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.handlers;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.error.InteractionErrorHandler;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.model.JdaSlashCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlashCommandDelegationHandlerImplTest {

    @Mock
    private InteractionErrorHandler errorHandler;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private ReplyCallbackAction replyAction;

    @Mock
    private WebhookMessageCreateAction<Message> webhookAction;

    private SlashCommandDelegationHandlerImpl handler;
    private Consumer<SlashCommandInteractionEvent> commandHandler1;
    private Consumer<SlashCommandInteractionEvent> commandHandler2;
    private Consumer<SlashCommandInteractionEvent> commandHandler3;

    @Mock
    private SlashCommandData commandData1;

    @Mock
    private SlashCommandData commandData2;

    @Mock
    private SlashCommandData commandData3;

    @BeforeEach
    void setUp() {
        handler = new SlashCommandDelegationHandlerImpl(errorHandler);
        commandHandler1 = mock(Consumer.class);
        commandHandler2 = mock(Consumer.class);
        commandHandler3 = mock(Consumer.class);
    }

    @Test
    void testRegisterCommands_NewCommands() {
        // Prepare test data
        JdaSlashCommand command1 = new JdaSlashCommand("test1", "Test command 1", commandData1, commandHandler1, "testBot");
        JdaSlashCommand command2 = new JdaSlashCommand("test2", "Test command 2", commandData2, commandHandler2, "testBot");
        List<JdaSlashCommand> commands = Arrays.asList(command1, command2);

        // Execute the method
        handler.registerCommands(commands);

        // Verify the handlers were registered
        assertEquals(2, handler.getHandlerCount());
        assertTrue(handler.isCommandRegistered("test1"));
        assertTrue(handler.isCommandRegistered("test2"));

        // Get the list of command names and sort it to ensure consistent order
        List<String> registeredNames = handler.getRegisteredCommandNames();
        Collections.sort(registeredNames);

        // Compare with expected sorted list
        List<String> expectedNames = Arrays.asList("test1", "test2");
        Collections.sort(expectedNames);

        assertEquals(expectedNames, registeredNames);
    }

    @Test
    void testRegisterCommands_UpdateExistingCommands() {
        // Register initial commands
        JdaSlashCommand initialCommand = new JdaSlashCommand("test1", "Test command 1", commandData1, commandHandler1, "testBot");
        handler.registerCommands(Collections.singletonList(initialCommand));
        assertEquals(1, handler.getHandlerCount());

        // Prepare update test data
        JdaSlashCommand updatedCommand = new JdaSlashCommand("test1", "Updated test command 1", commandData1, commandHandler2, "testBot");
        JdaSlashCommand newCommand = new JdaSlashCommand("test2", "Test command 2", commandData2, commandHandler3, "testBot");
        List<JdaSlashCommand> commands = Arrays.asList(updatedCommand, newCommand);

        // Execute the method
        handler.registerCommands(commands);

        // Verify the handlers were properly updated
        assertEquals(2, handler.getHandlerCount());
        assertTrue(handler.isCommandRegistered("test1"));
        assertTrue(handler.isCommandRegistered("test2"));

        // Verify the handler was updated by triggering it
        when(event.getName()).thenReturn("test1");
        handler.handleInteraction(event);
        verify(commandHandler2).accept(event);
        verify(commandHandler1, never()).accept(event);
    }

    @Test
    void testRegisterCommands_EmptyList() {
        // Execute the method with empty list
        handler.registerCommands(Collections.emptyList());

        // Verify no handlers were registered
        assertEquals(0, handler.getHandlerCount());
        assertTrue(handler.getRegisteredCommandNames().isEmpty());
    }

    @Test
    void testRemoveCommands_ExistingCommands() {
        // Register initial commands
        JdaSlashCommand command1 = new JdaSlashCommand("test1", "Test command 1", commandData1, commandHandler1, "testBot");
        JdaSlashCommand command2 = new JdaSlashCommand("test2", "Test command 2", commandData2, commandHandler2, "testBot");
        JdaSlashCommand command3 = new JdaSlashCommand("test3", "Test command 3", commandData3, commandHandler3, "testBot");
        handler.registerCommands(Arrays.asList(command1, command2, command3));
        assertEquals(3, handler.getHandlerCount());

        // Prepare remove test data
        List<JdaSlashCommand> commandsToRemove = Arrays.asList(command1, command3);

        // Execute the method
        handler.removeCommands(commandsToRemove);

        // Verify the handlers were removed
        assertEquals(1, handler.getHandlerCount());
        assertFalse(handler.isCommandRegistered("test1"));
        assertTrue(handler.isCommandRegistered("test2"));
        assertFalse(handler.isCommandRegistered("test3"));
    }

    @Test
    void testRemoveCommands_NonExistingCommands() {
        // Register initial commands
        JdaSlashCommand command1 = new JdaSlashCommand("test1", "Test command 1", commandData1, commandHandler1, "testBot");
        handler.registerCommands(Collections.singletonList(command1));
        assertEquals(1, handler.getHandlerCount());

        // Prepare remove test data with non-existing command
        JdaSlashCommand command2 = new JdaSlashCommand("test2", "Test command 2", commandData2, commandHandler2, "testBot");
        List<JdaSlashCommand> commandsToRemove = Collections.singletonList(command2);

        // Execute the method
        handler.removeCommands(commandsToRemove);

        // Verify nothing was removed
        assertEquals(1, handler.getHandlerCount());
        assertTrue(handler.isCommandRegistered("test1"));
    }

    @Test
    void testRemoveCommands_EmptyList() {
        // Register initial commands
        JdaSlashCommand command1 = new JdaSlashCommand("test1", "Test command 1", commandData1, commandHandler1, "testBot");
        handler.registerCommands(Collections.singletonList(command1));
        assertEquals(1, handler.getHandlerCount());

        // Execute the method with empty list
        handler.removeCommands(Collections.emptyList());

        // Verify nothing was removed
        assertEquals(1, handler.getHandlerCount());
        assertTrue(handler.isCommandRegistered("test1"));
    }

    @Test
    void testRemoveCommands_MixedExistingAndNonExisting() {
        // Register initial commands
        JdaSlashCommand command1 = new JdaSlashCommand("test1", "Test command 1", commandData1, commandHandler1, "testBot");
        JdaSlashCommand command2 = new JdaSlashCommand("test2", "Test command 2", commandData2, commandHandler2, "testBot");
        handler.registerCommands(Arrays.asList(command1, command2));
        assertEquals(2, handler.getHandlerCount());

        // Prepare remove test data with mix of existing and non-existing commands
        JdaSlashCommand command3 = new JdaSlashCommand("test3", "Test command 3", commandData3, commandHandler3, "testBot");
        List<JdaSlashCommand> commandsToRemove = Arrays.asList(command1, command3);

        // Execute the method
        handler.removeCommands(commandsToRemove);

        // Verify only existing command was removed
        assertEquals(1, handler.getHandlerCount());
        assertFalse(handler.isCommandRegistered("test1"));
        assertTrue(handler.isCommandRegistered("test2"));
    }

    @Test
    void testHandleNoHandlerFound_NotAcknowledged() {
        // Setup event mock
        when(event.getName()).thenReturn("unknownCommand");
        when(event.isAcknowledged()).thenReturn(false);
        when(event.reply(anyString())).thenReturn(replyAction);
        when(replyAction.setEphemeral(anyBoolean())).thenReturn(replyAction);

        // Execute the method by triggering handleInteraction
        handler.handleInteraction(event);

        // Verify the proper ephemeral message was sent
        verify(event).reply("Unknown command: unknownCommand");
        verify(replyAction).setEphemeral(true);
        verify(replyAction).queue();
    }

    @Test
    void testHandleNoHandlerFound_AlreadyAcknowledged() {
        // Setup event mock
        when(event.getName()).thenReturn("unknownCommand");
        when(event.isAcknowledged()).thenReturn(true);

        // Execute the method by triggering handleInteraction
        handler.handleInteraction(event);

        // Verify no reply was sent
        verify(event, never()).reply(anyString());
    }

    @Test
    void testSynchronizedMethods() throws InterruptedException {
        // This test verifies that the synchronized methods work correctly in a multithreaded environment

        // First register all commands to ensure they exist
        List<JdaSlashCommand> commands = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            // Create mock SlashCommandData for each command
            SlashCommandData mockData = mock(SlashCommandData.class);
            commands.add(new JdaSlashCommand("command" + i, "Test command " + i, mockData, e -> {}, "testBot"));
        }

        // Register all commands first
        handler.registerCommands(commands);
        assertEquals(100, handler.getHandlerCount(), "All 100 commands should be registered initially");

        // Create a CountDownLatch to synchronize thread start
        CountDownLatch startLatch = new CountDownLatch(1);

        // Create threads that will register and remove commands concurrently
        Thread updateThread = new Thread(() -> {
            try {
                startLatch.await(); // Wait for the signal to start

                // Update first 50 commands (should replace existing ones)
                for (int i = 0; i < 5; i++) {
                    List<JdaSlashCommand> updatedCommands = new ArrayList<>();
                    for (int j = i * 10; j < (i + 1) * 10; j++) {
                        SlashCommandData mockData = mock(SlashCommandData.class);
                        updatedCommands.add(new JdaSlashCommand("command" + j, "Updated command " + j, mockData, e -> {}, "testBot"));
                    }
                    handler.registerCommands(updatedCommands);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread removeThread = new Thread(() -> {
            try {
                startLatch.await(); // Wait for the signal to start

                // Remove last 50 commands
                for (int i = 5; i < 10; i++) {
                    handler.removeCommands(commands.subList(i * 10, (i + 1) * 10));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start threads
        updateThread.start();
        removeThread.start();

        // Signal both threads to start simultaneously
        startLatch.countDown();

        // Wait for threads to complete
        updateThread.join();
        removeThread.join();

        // Verify the final state is consistent
        // We expect 50 commands to be registered (first 50 updated, last 50 removed)
        assertEquals(50, handler.getHandlerCount(), "Should have 50 commands after concurrent operations");

        // Verify first 50 commands still exist
        for (int i = 0; i < 50; i++) {
            assertTrue(handler.isCommandRegistered("command" + i), "Command " + i + " should still be registered");
        }

        // Verify last 50 commands were removed
        for (int i = 50; i < 100; i++) {
            assertFalse(handler.isCommandRegistered("command" + i), "Command " + i + " should have been removed");
        }
    }
}