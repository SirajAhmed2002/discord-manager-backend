package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler;

import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory.EmbedFactory;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service.NotenrechnerService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.DiscordEventUtils;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectCommandHandlerTest {

    @Mock
    private NotenrechnerService notenrechnerService;

    @Mock
    private EmbedFactory embedFactory;

    @Mock
    private DiscordEventUtils eventUtils;

    @Mock
    private SlashCommandInteractionEvent event;

    @InjectMocks
    private SubjectCommandHandler subjectCommandHandler;

    private DiscordIdsDTO testDiscordIds;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testDiscordIds = DiscordIdsDTO.builder()
                .serverId("server1")
                .channelId("channel1")
                .userId("user1")
                .build();

        testSubject = new Subject("Mathematik", 6, "HS2023", "server1", "channel1", "user1");

        when(eventUtils.extractDiscordIds(event)).thenReturn(testDiscordIds);

        when(event.getHook()).thenReturn(mock(net.dv8tion.jda.api.interactions.InteractionHook.class));
        lenient().when(event.getHook().sendMessage(anyString())).thenReturn(mock(WebhookMessageCreateAction.class));
        lenient().when(event.getHook().sendMessageEmbeds(any())).thenReturn(mock(WebhookMessageCreateAction.class));
    }

    @Test
    void handleCreateSubject_Success() {
        when(eventUtils.getRequiredString(event, "name")).thenReturn("Mathematik");
        when(eventUtils.getRequiredInt(event, "credits")).thenReturn(6);
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.createSubject(anyString(), anyString(), anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(testSubject);
        when(embedFactory.createSubjectCreatedEmbed(any(Subject.class), anyString()))
                .thenReturn(new EmbedBuilder());

        subjectCommandHandler.handleCreateSubject(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).createSubject("server1", "channel1", "user1", "Mathematik", 6, "HS2023");
        verify(embedFactory).createSubjectCreatedEmbed(testSubject, "HS2023");
    }

    @Test
    void handleCreateSubject_Exception_HandlesGracefully() {
        when(eventUtils.getRequiredString(event, "name")).thenReturn("Mathematik");
        when(eventUtils.getRequiredInt(event, "credits")).thenReturn(6);
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.createSubject(anyString(), anyString(), anyString(), anyString(), anyInt(), anyString()))
                .thenThrow(new IllegalArgumentException("Subject already exists"));

        assertDoesNotThrow(() -> subjectCommandHandler.handleCreateSubject(event));
        
        verify(eventUtils).sendErrorMessage(eq(event), eq("Erstellen des Fachs"), any(IllegalArgumentException.class));
    }

    @Test
    void handleShowSubjects_WithSubjects_Success() {
        List<Subject> subjects = Collections.singletonList(testSubject);
        when(eventUtils.getOptionalString(event, "semester")).thenReturn(null);
        when(notenrechnerService.getSubjects(anyString(), anyString(), anyString())).thenReturn(subjects);
        when(notenrechnerService.calculateOverallAverage(anyString(), anyString(), anyString())).thenReturn(4.5);
        when(embedFactory.createSubjectsDisplayEmbed(any())).thenReturn(new EmbedBuilder());

        subjectCommandHandler.handleShowSubjects(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).getSubjects("server1", "channel1", "user1");
        verify(notenrechnerService).calculateOverallAverage("server1", "channel1", "user1");
        verify(embedFactory).createSubjectsDisplayEmbed(any());
    }

    @Test
    void handleShowSubjects_NoSubjects_CompletesSuccessfully() {
        when(eventUtils.getOptionalString(event, "semester")).thenReturn(null);
        when(notenrechnerService.getSubjects(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> subjectCommandHandler.handleShowSubjects(event));
        
        verify(notenrechnerService).getSubjects("server1", "channel1", "user1");
    }

    @Test
    void handleShowSemesters_Success() {
        List<String> semesters = Arrays.asList("HS2023", "FS2024");
        when(notenrechnerService.getSemesters(anyString(), anyString(), anyString())).thenReturn(semesters);
        when(embedFactory.createSemestersDisplayEmbed(any())).thenReturn(new EmbedBuilder());

        subjectCommandHandler.handleShowSemesters(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).getSemesters("server1", "channel1", "user1");
        verify(embedFactory).createSemestersDisplayEmbed(semesters);
    }

    @Test
    void handleShowSemesters_NoSemesters_CompletesSuccessfully() {
        when(notenrechnerService.getSemesters(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> subjectCommandHandler.handleShowSemesters(event));
        
        verify(notenrechnerService).getSemesters("server1", "channel1", "user1");
    }
}