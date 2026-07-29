package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler;

import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory.EmbedFactory;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service.NotenrechnerService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.DiscordEventUtils;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveCommandHandlerTest {

    @Mock
    private NotenrechnerService notenrechnerService;

    @Mock
    private EmbedFactory embedFactory;

    @Mock
    private DiscordEventUtils eventUtils;

    @Mock
    private SlashCommandInteractionEvent event;

    @InjectMocks
    private RemoveCommandHandler removeCommandHandler;

    private DiscordIdsDTO testDiscordIds;

    @BeforeEach
    void setUp() {
        testDiscordIds = DiscordIdsDTO.builder()
                .serverId("server1")
                .channelId("channel1")
                .userId("user1")
                .build();

        when(eventUtils.extractDiscordIds(event)).thenReturn(testDiscordIds);

        when(event.getHook()).thenReturn(mock(net.dv8tion.jda.api.interactions.InteractionHook.class));
        lenient().when(event.getHook().sendMessage(anyString())).thenReturn(mock(WebhookMessageCreateAction.class));
        lenient().when(event.getHook().sendMessageEmbeds(any())).thenReturn(mock(WebhookMessageCreateAction.class));
    }

    @Test
    void handleRemoveSubject_Success() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.removeSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(embedFactory.createSubjectRemovedEmbed(anyString(), anyString(), anyBoolean()))
                .thenReturn(new EmbedBuilder());

        removeCommandHandler.handleRemoveSubject(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).removeSubject("server1", "channel1", "user1", "Mathematik", "HS2023");
        verify(embedFactory).createSubjectRemovedEmbed("Mathematik", "HS2023", true);
    }

    @Test
    void handleRemoveSubject_NotFound_CompletesSuccessfully() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("NotFound");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.removeSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(embedFactory.createSubjectRemovedEmbed(anyString(), anyString(), anyBoolean()))
                .thenReturn(new EmbedBuilder());

        assertDoesNotThrow(() -> removeCommandHandler.handleRemoveSubject(event));
        
        verify(notenrechnerService).removeSubject("server1", "channel1", "user1", "NotFound", "HS2023");
        verify(embedFactory).createSubjectRemovedEmbed("NotFound", "HS2023", false);
    }

    @Test
    void handleRemoveGrades_Success() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.removeAllGradesFromSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(3);
        when(embedFactory.createGradesRemovedEmbed(anyString(), anyString(), anyInt()))
                .thenReturn(new EmbedBuilder());

        removeCommandHandler.handleRemoveGrades(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).removeAllGradesFromSubject("server1", "channel1", "user1", "Mathematik", "HS2023");
        verify(embedFactory).createGradesRemovedEmbed("Mathematik", "HS2023", 3);
    }

    @Test
    void handleRemoveGrades_NoGrades_CompletesSuccessfully() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.removeAllGradesFromSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(0);
        when(embedFactory.createGradesRemovedEmbed(anyString(), anyString(), anyInt()))
                .thenReturn(new EmbedBuilder());

        assertDoesNotThrow(() -> removeCommandHandler.handleRemoveGrades(event));
        
        verify(notenrechnerService).removeAllGradesFromSubject("server1", "channel1", "user1", "Mathematik", "HS2023");
    }

    @Test
    void handleRemoveSemester_Success() {
        when(eventUtils.getRequiredString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.removeSemester(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(5);
        when(embedFactory.createSemesterRemovedEmbed(anyString(), anyInt()))
                .thenReturn(new EmbedBuilder());

        removeCommandHandler.handleRemoveSemester(event);

        verify(eventUtils).extractDiscordIds(event);
        verify(notenrechnerService).removeSemester("server1", "channel1", "user1", "HS2023");
        verify(embedFactory).createSemesterRemovedEmbed("HS2023", 5);
    }

    @Test
    void handleRemoveSemester_EmptyName_CompletesWithValidation() {
        when(eventUtils.getRequiredString(event, "semester")).thenReturn("   ");

        assertDoesNotThrow(() -> removeCommandHandler.handleRemoveSemester(event));

        verify(eventUtils).extractDiscordIds(event);
    }

    @Test
    void handleRemoveSubject_ServiceException_HandlesGracefully() {
        when(eventUtils.getRequiredString(event, "fach")).thenReturn("Mathematik");
        when(eventUtils.getOptionalString(event, "semester")).thenReturn("HS2023");
        when(notenrechnerService.removeSubject(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> removeCommandHandler.handleRemoveSubject(event));
        
        verify(eventUtils).sendErrorMessage(eq(event), eq("Löschen des Fachs"), any(RuntimeException.class));
    }
}