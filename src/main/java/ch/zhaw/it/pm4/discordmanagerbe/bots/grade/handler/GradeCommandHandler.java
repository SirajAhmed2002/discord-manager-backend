package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler;

import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory.EmbedFactory;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service.NotenrechnerService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.DiscordEventUtils;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.NumberParsingUtils;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Grade;
import ch.zhaw.it.pm4.discordmanagerbe.dto.AddGradeRequest;
import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.GradeDisplayData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Handles grade-related commands for the grade calculator bot
 */
@Component
public class GradeCommandHandler {

    /**
     * NotenrechnerService for grade calculations and data retrieval
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
     * Utility for parsing numbers from strings
     */
    private final NumberParsingUtils numberUtils;

    /**
     * Logger for GradeCommandHandler
     *
     * @param notenrechnerService service for grade calculations
     * @param embedFactory factory for creating embeds
     * @param eventUtils utility for Discord event handling
     * @param numberUtils utility for parsing numbers
     */
    public GradeCommandHandler(NotenrechnerService notenrechnerService, 
                              EmbedFactory embedFactory,
                              DiscordEventUtils eventUtils,
                              NumberParsingUtils numberUtils) {
        this.notenrechnerService = notenrechnerService;
        this.embedFactory = embedFactory;
        this.eventUtils = eventUtils;
        this.numberUtils = numberUtils;
    }

    /**
     * Handles the add grade command
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleAddGrade(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            AddGradeRequest request = buildAddGradeRequest(event, discordIds);
            
            Grade grade = notenrechnerService.addGrade(request);
            double average = calculateSubjectAverage(request);
            
            EmbedBuilder embed = embedFactory.createGradeAddedEmbed(grade, request, average);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Hinzufügen der Note", e);
        }
    }

    /**
     * Handles the show grades command
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleShowGrades(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            String subjectName = eventUtils.getRequiredString(event, OPT_FACH);
            String semester = eventUtils.getOptionalString(event, OPT_SEMESTER);

            GradeDisplayData displayData = prepareGradeDisplay(discordIds, subjectName, semester);
            
            if (displayData.isEmpty()) {
                sendNoGradesMessage(event, subjectName, semester);
                return;
            }

            EmbedBuilder embed = embedFactory.createGradesDisplayEmbed(displayData);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Anzeigen der Noten", e);
        }
    }

    /**
     * Builds an AddGradeRequest from the SlashCommandInteractionEvent
     * @param event the SlashCommandInteractionEvent containing the command data
     * @param discordIds the DiscordIdsDTO containing server, channel, and user IDs
     * @return AddGradeRequest with the necessary data for adding a grade
     */
    private AddGradeRequest buildAddGradeRequest(SlashCommandInteractionEvent event, DiscordIdsDTO discordIds) {
        String noteStr = eventUtils.getRequiredString(event, OPT_NOTE);
        String gewichtungStr = eventUtils.getRequiredString(event, OPT_GEWICHTUNG);
        
        double note = numberUtils.parseDouble(noteStr);
        double gewichtung = numberUtils.parseDouble(gewichtungStr);

        return AddGradeRequest.builder()
            .serverId(discordIds.getServerId())
            .channelId(discordIds.getChannelId())
            .userId(discordIds.getUserId())
            .subjectName(eventUtils.getRequiredString(event, OPT_FACH))
            .note(note)
            .weight(gewichtung)
            .semester(eventUtils.getOptionalString(event, OPT_SEMESTER))
            .description(eventUtils.getOptionalString(event, OPT_BESCHREIBUNG))
            .build();
    }

    /**
     * Calculates the average for a subject based on the provided request
     * @param request the AddGradeRequest containing the subject and user data
     * @return the calculated average for the subject
     */
    private double calculateSubjectAverage(AddGradeRequest request) {
        return notenrechnerService.calculateSubjectAverage(
            request.serverId(), 
            request.channelId(), 
            request.userId(),
            request.subjectName(), 
            request.semester()
        );
    }

    /**
     * Prepares the grade display data for a specific subject and semester
     * @param discordIds the Discord IDs containing server, channel, and user IDs
     * @param subjectName the name of the subject for which to retrieve grades
     * @param semester the semester for which to retrieve grades, or null for overall grades
     * @return GradeDisplayData containing the grades and average for the subject
     */
    private GradeDisplayData prepareGradeDisplay(DiscordIdsDTO discordIds, String subjectName, String semester) {
        List<Grade> grades = notenrechnerService.getGradesForSubject(
            discordIds.getServerId(), 
            discordIds.getChannelId(), 
            discordIds.getUserId(),
            subjectName, 
            semester
        );

        if (grades.isEmpty()) {
            return GradeDisplayData.empty();
        }

        double average = notenrechnerService.calculateSubjectAverage(
            discordIds.getServerId(), 
            discordIds.getChannelId(), 
            discordIds.getUserId(),
            subjectName, 
            semester
        );

        return new GradeDisplayData(grades, average, subjectName, semester);
    }

    /**
     * Sends a message indicating that no grades were found for the specified subject and semester
     * @param event the SlashCommandInteractionEvent containing the command data
     * @param subjectName the name of the subject for which no grades were found
     * @param semester the semester for which no grades were found, or null if not specified
     */
    private void sendNoGradesMessage(SlashCommandInteractionEvent event, String subjectName, String semester) {
        String message = buildNoGradesMessage(subjectName, semester);
        event.getHook().sendMessage(message).setEphemeral(true).queue();
    }

    /**
     * Builds a message indicating that no grades were found for the specified subject and semester
     * @param subjectName the name of the subject for which no grades were found
     * @param semester the semester for which no grades were found, or null if not specified
     * @return the constructed message string
     */
    private String buildNoGradesMessage(String subjectName, String semester) {
        StringBuilder message = new StringBuilder("Keine Noten für das Fach '")
            .append(subjectName).append("'");
            
        if (semester != null && !semester.trim().isEmpty()) {
            message.append(" im Semester ").append(semester);
        }
        
        return message.append(" gefunden.").toString();
    }
}