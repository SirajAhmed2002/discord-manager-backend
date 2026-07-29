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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Handles average calculation and display commands for the grade calculator bot
 */
@Component
public class AverageCommandHandler {

    /**
     * Logger for AverageCommandHandler
     */
    private final NotenrechnerService notenrechnerService;

    /**
     * Factory for creating Discord embeds
     */
    private final EmbedFactory embedFactory;

    /**
     * Utility class for handling Discord events
     */
    private final DiscordEventUtils eventUtils;

    /**
     * Converter for summary data to display format
     */
    private final SummaryDataConverter summaryConverter;

    /**
     * Constructor for AverageCommandHandler
     * @param notenrechnerService service for grade calculations
     * @param embedFactory factory for creating embeds
     * @param eventUtils utility for Discord event handling
     * @param summaryConverter converter for summary data
     */
    public AverageCommandHandler(NotenrechnerService notenrechnerService, 
                                EmbedFactory embedFactory,
                                DiscordEventUtils eventUtils,
                                SummaryDataConverter summaryConverter) {
        this.notenrechnerService = notenrechnerService;
        this.embedFactory = embedFactory;
        this.eventUtils = eventUtils;
        this.summaryConverter = summaryConverter;
    }

    /**
     * Handles the show average command
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleShowAverage(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            String semester = eventUtils.getOptionalString(event, OPT_SEMESTER);
            
            AverageDisplayData displayData = prepareAverageDisplay(discordIds, semester);
            
            if (displayData.isEmpty()) {
                event.getHook().sendMessage(MSG_NO_GRADES_ENTERED).setEphemeral(true).queue();
                return;
            }
            
            EmbedBuilder embed = createAverageEmbed(displayData);
            
            if (embed != null) {
                event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            } else {
                sendNoDataMessage(event, semester);
            }
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Anzeigen des Durchschnitts", e);
        }
    }

    /**
     * Prepares the average display data based on the provided Discord IDs and semester
     * @param discordIds the Discord IDs containing server, channel, and user IDs
     * @param semester the semester for which to calculate the average, or null for overall average
     * @return AverageDisplayData containing the overall average and semester summaries
     */
    private AverageDisplayData prepareAverageDisplay(DiscordIdsDTO discordIds, String semester) {
        Map<String, Object> summary = notenrechnerService.getSummary(
            discordIds.getServerId(), discordIds.getChannelId(), discordIds.getUserId());
        
        double overallAverage = (Double) summary.get("overallAverage");
        
        if (overallAverage == 0.0) {
            return AverageDisplayData.empty();
        }
        
        List<SemesterSummary> semesters = summaryConverter.convertToSemesterSummaries(summary);
        
        return new AverageDisplayData(overallAverage, semesters, semester);
    }

    /**
     * Creates an embed for displaying the average based on the provided display data
     * @param displayData the AverageDisplayData containing overall average and semester summaries
     * @return EmbedBuilder for the average display, or null if no data is available
     */
    private EmbedBuilder createAverageEmbed(AverageDisplayData displayData) {
        if (hasSemester(displayData.getRequestedSemester())) {
            return createSemesterAverageEmbed(displayData);
        } else {
            return createOverallAverageEmbed(displayData);
        }
    }

    /**
     * Creates an embed for displaying the average for a specific semester
     * @param displayData the AverageDisplayData containing semester summaries
     * @return EmbedBuilder for the semester average, or null if the semester is not found
     */
    private EmbedBuilder createSemesterAverageEmbed(AverageDisplayData displayData) {
        String semester = displayData.getRequestedSemester();
        
        SemesterSummary semesterSummary = displayData.getSemesters().stream()
            .filter(s -> semester.equals(s.getName()))
            .findFirst()
            .orElse(null);
            
        if (semesterSummary == null) {
            return null;
        }
        
        return embedFactory.createSemesterAverageEmbed(semesterSummary);
    }

    /**
     * Creates an embed for displaying the overall average
     * @param displayData the AverageDisplayData containing the overall average
     * @return EmbedBuilder for the overall average
     */
    private EmbedBuilder createOverallAverageEmbed(AverageDisplayData displayData) {
        return embedFactory.createOverallAverageEmbed(displayData);
    }

    /**
     * Sends a message indicating that no data is available for the specified semester
     * @param event the SlashCommandInteractionEvent to send the message in
     * @param semester the semester for which no data is available
     */
    private void sendNoDataMessage(SlashCommandInteractionEvent event, String semester) {
        String message = String.format(MSG_NO_DATA_FOR_SEMESTER, semester);
        event.getHook().sendMessage(message).setEphemeral(true).queue();
    }

    /**
     * Checks if the provided semester string is valid (not null and not empty)
     * @param semester the semester string to check
     * @return true if the semester is valid, false otherwise
     */
    private boolean hasSemester(String semester) {
        return semester != null && !semester.trim().isEmpty();
    }
}