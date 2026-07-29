package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler;

import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory.EmbedFactory;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service.NotenrechnerService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.DiscordEventUtils;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.NumberParsingUtils;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import ch.zhaw.it.pm4.discordmanagerbe.dto.AddGradeRequest;
import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.GradeDisplayData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeCommandHandlerTest {

    @Mock
    private NotenrechnerService notenrechnerService;

    @Mock
    private EmbedFactory embedFactory;

    @Mock
    private DiscordEventUtils eventUtils;

    @Mock
    private NumberParsingUtils numberUtils;

    @Mock
    private SlashCommandInteractionEvent event;

    @InjectMocks
    private GradeCommandHandler gradeCommandHandler;

    private DiscordIdsDTO testDiscordIds;
    private Grade testGrade;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testDiscordIds = DiscordIdsDTO.builder()
                .serverId("server1")
                .channelId("channel1")
                .userId("user1")
                .build();

        testSubject = new Subject("Mathematik", 6, "HS2023", "server1", "channel1", "user1");
        testGrade = new Grade(4.5, 0.5, "Test Grade", testSubject);

        when(eventUtils.extractDiscordIds(event)).thenReturn(testDiscordIds);

        when(event.getHook()).thenReturn(mock(net.dv8tion.jda.api.interactions.InteractionHook.class));
        lenient().when(event.getHook().sendMessage(anyString())).thenReturn(mock(WebhookMessageCreateAction.class));
        lenient().when(event.getHook().sendMessageEmbeds(any())).thenReturn(mock(WebhookMessageCreateAction.class));
    }

    @Test
    void handleAddGrade_Success() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getRequiredString(event, "note")).thenReturn("4.5");
        when(eventUtils.getRequiredString(event, "gewichtung")).thenReturn("0.5");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(eventUtils.getOptionalString(event, "beschreibung")).thenReturn("Test");
        
        when(numberUtils.parseDouble("4.5")).thenReturn(4.5);
        when(numberUtils.parseDouble("0.5")).thenReturn(0.5);
        
        when(notenrechnerService.addGrade(any(AddGradeRequest.class))).thenReturn(testGrade);
        when(notenrechnerService.calculateSubjectAverage(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(4.5);
        
        when(embedFactory.createGradeAddedEmbed(any(Grade.class), any(AddGradeRequest.class), anyDouble()))
                .thenReturn(new EmbedBuilder());

        gradeCommandHandler.handleAddGrade(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(numberUtils).parseDouble("4.5");
        verify(numberUtils).parseDouble("0.5");
        verify(notenrechnerService).addGrade(any(AddGradeRequest.class));
        verify(notenrechnerService).calculateSubjectAverage(eq("server1"), eq("channel1"), eq("user1"), eq("Mathematik"), eq("HS2023"));
        verify(embedFactory).createGradeAddedEmbed(eq(testGrade), any(AddGradeRequest.class), eq(4.5));
    }

    @Test
    void handleAddGrade_NumberParsingException_SendsErrorMessage() {
        lenient().when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        lenient().when(eventUtils.getRequiredString(event, "note")).thenReturn("invalid");
        lenient().when(eventUtils.getRequiredString(event, "gewichtung")).thenReturn("0.5");
        lenient().when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        lenient().when(eventUtils.getOptionalString(event, "beschreibung")).thenReturn("Test");

        lenient().when(numberUtils.parseDouble("invalid")).thenThrow(new NumberFormatException("Invalid number"));

        gradeCommandHandler.handleAddGrade(event);

        verify(eventUtils).sendErrorMessage(eq(event), eq("Hinzufügen der Note"), any(NumberFormatException.class));
        verify(notenrechnerService, never()).addGrade(any());
    }

    @Test
    void handleShowGrades_WithGrades_Success() {
        List<Grade> grades = Collections.singletonList(testGrade);
        
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        
        when(notenrechnerService.getGradesForSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(grades);
        when(notenrechnerService.calculateSubjectAverage(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(4.5);
        
        when(embedFactory.createGradesDisplayEmbed(any(GradeDisplayData.class)))
                .thenReturn(new EmbedBuilder());

        gradeCommandHandler.handleShowGrades(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).getGradesForSubject("server1", "channel1", "user1", "Mathematik", "HS2023");
        verify(notenrechnerService).calculateSubjectAverage("server1", "channel1", "user1", "Mathematik", "HS2023");
        verify(embedFactory).createGradesDisplayEmbed(any(GradeDisplayData.class));
    }

    @Test
    void handleShowGrades_NoGrades_SendsMessage() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        
        when(notenrechnerService.getGradesForSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        gradeCommandHandler.handleShowGrades(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).getGradesForSubject("server1", "channel1", "user1", "Mathematik", "HS2023");
    }

    @Test
    void handleShowGrades_ServiceException_SendsErrorMessage() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        
        when(notenrechnerService.getGradesForSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Subject not found"));

        gradeCommandHandler.handleShowGrades(event);

        verify(eventUtils).sendErrorMessage(eq(event), eq("Anzeigen der Noten"), any(IllegalArgumentException.class));
    }
}