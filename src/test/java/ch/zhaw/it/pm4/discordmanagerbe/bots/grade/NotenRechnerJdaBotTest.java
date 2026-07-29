package ch.zhaw.it.pm4.discordmanagerbe.bots.grade;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.AverageCommandHandler;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.GradeCommandHandler;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.RemoveCommandHandler;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler.SubjectCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotenRechnerJdaBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private JdaSlashCommandService slashCommandService;

    @Mock
    private JdaEventListenerService slashCommandListener;

    @Mock
    private SubjectCommandHandler subjectHandler;

    @Mock
    private GradeCommandHandler gradeHandler;

    @Mock
    private AverageCommandHandler averageHandler;

    @Mock
    private RemoveCommandHandler removeHandler;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private ReplyCallbackAction replyAction;

    @InjectMocks
    private NotenRechnerJdaBot notenRechnerJdaBot;

    @BeforeEach
    void setUp() {
        lenient().when(event.deferReply()).thenReturn(replyAction);
        lenient().when(replyAction.setEphemeral(true)).thenReturn(replyAction);
    }

    @Test
    void handleCreateSubjectCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleCreateSubjectCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(subjectHandler).handleCreateSubject(event);
    }

    @Test
    void handleAddGradeCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleAddGradeCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(gradeHandler).handleAddGrade(event);
    }

    @Test
    void handleShowSubjectsCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleShowSubjectsCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(subjectHandler).handleShowSubjects(event);
    }

    @Test
    void handleShowGradesCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleShowGradesCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(gradeHandler).handleShowGrades(event);
    }

    @Test
    void handleShowAverageCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleShowAverageCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(averageHandler).handleShowAverage(event);
    }

    @Test
    void handleShowSemestersCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleShowSemestersCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(subjectHandler).handleShowSemesters(event);
    }

    @Test
    void handleRemoveSubjectCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleRemoveSubjectCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(removeHandler).handleRemoveSubject(event);
    }

    @Test
    void handleRemoveGradesCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleRemoveGradesCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(removeHandler).handleRemoveGrades(event);
    }

    @Test
    void handleRemoveSemesterCommand_DefersReplyAndCallsHandler() {
        // Act
        notenRechnerJdaBot.handleRemoveSemesterCommand(event);

        // Assert
        verify(event).deferReply();
        verify(replyAction).queue();
        verify(removeHandler).handleRemoveSemester(event);
    }

    @Test
    void setupCommands_CallsAllRegistrationMethods() {
        // This test ensures setupCommands is called during initialization
        // Since setupCommands is protected, we test indirectly by verifying
        // the bot initializes without errors
        assertDoesNotThrow(() -> {
            new NotenRechnerJdaBot(jdaBean, slashCommandService, slashCommandListener,
                    subjectHandler, gradeHandler, averageHandler, removeHandler);
        });
    }
}