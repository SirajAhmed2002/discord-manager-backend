package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler;

import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory.EmbedFactory;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service.NotenrechnerService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.DiscordEventUtils;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.SummaryDataConverter;
import ch.zhaw.it.pm4.discordmanagerbe.dto.AverageDisplayData;
import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.SemesterSummary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AverageCommandHandlerTest {

    @Mock
    private NotenrechnerService notenrechnerService;

    @Mock
    private EmbedFactory embedFactory;

    @Mock
    private DiscordEventUtils eventUtils;

    @Mock
    private SummaryDataConverter summaryConverter;

    @Mock
    private SlashCommandInteractionEvent event;

    @InjectMocks
    private AverageCommandHandler averageCommandHandler;

    @Test
    void handleShowAverage_WithData_CallsCorrectMethods() {
        DiscordIdsDTO discordIds = DiscordIdsDTO.builder()
                .serverId("server1").channelId("channel1").userId("user1").build();

        Map<String, Object> summary = new HashMap<>();
        summary.put("overallAverage", 4.5);

        List<SemesterSummary> semesters = List.of(
                new SemesterSummary("HS2023", 4.5, List.of())
        );

        when(eventUtils.extractDiscordIds(event)).thenReturn(discordIds);
        when(eventUtils.getOptionalString(event, "semester")).thenReturn(null);
        when(notenrechnerService.getSummary("server1", "channel1", "user1")).thenReturn(summary);
        when(summaryConverter.convertToSemesterSummaries(summary)).thenReturn(semesters);
        when(embedFactory.createOverallAverageEmbed(any())).thenReturn(new EmbedBuilder());

        lenient().when(event.getHook()).thenReturn(mock(net.dv8tion.jda.api.interactions.InteractionHook.class));
        lenient().when(event.getHook().sendMessageEmbeds(any())).thenReturn(mock(WebhookMessageCreateAction.class));

        averageCommandHandler.handleShowAverage(event);

        verify(notenrechnerService).getSummary("server1", "channel1", "user1");
        verify(summaryConverter).convertToSemesterSummaries(summary);
        verify(embedFactory).createOverallAverageEmbed(any(AverageDisplayData.class));
    }

    @Test
    void handleShowAverage_NoData_CallsServiceCorrectly() {
        DiscordIdsDTO discordIds = DiscordIdsDTO.builder()
                .serverId("server1").channelId("channel1").userId("user1").build();

        Map<String, Object> emptySummary = new HashMap<>();
        emptySummary.put("overallAverage", 0.0);

        when(eventUtils.extractDiscordIds(event)).thenReturn(discordIds);
        when(eventUtils.getOptionalString(event, "semester")).thenReturn(null);
        when(notenrechnerService.getSummary("server1", "channel1", "user1")).thenReturn(emptySummary);

        lenient().when(event.getHook()).thenReturn(mock(net.dv8tion.jda.api.interactions.InteractionHook.class));
        lenient().when(event.getHook().sendMessage(anyString())).thenReturn(mock(WebhookMessageCreateAction.class));

        averageCommandHandler.handleShowAverage(event);

        verify(notenrechnerService).getSummary("server1", "channel1", "user1");
    }
}