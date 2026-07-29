package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler;

import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory.EmbedFactory;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service.NotenrechnerService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.DiscordEventUtils;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Subject;
import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import ch.zhaw.it.pm4.discordmanagerbe.dto.SubjectDisplayData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Handles subject-related commands for the grade calculator bot
 */
@Component
public class SubjectCommandHandler {

    /**
     * Logger for SubjectCommandHandler
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
     * Constructor for SubjectCommandHandler
     * @param notenrechnerService service for grade calculations and subject management
     * @param embedFactory factory for creating embeds
     * @param eventUtils utility for Discord event handling
     */
    public SubjectCommandHandler(NotenrechnerService notenrechnerService, 
                                EmbedFactory embedFactory,
                                DiscordEventUtils eventUtils) {
        this.notenrechnerService = notenrechnerService;
        this.embedFactory = embedFactory;
        this.eventUtils = eventUtils;
    }

    /**
     * Handles the create subject command
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleCreateSubject(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            String subjectName = eventUtils.getRequiredString(event, OPT_NAME);
            int credits = eventUtils.getRequiredInt(event, OPT_CREDITS);
            String semester = eventUtils.getOptionalString(event, OPT_SEMESTER);

            Subject subject = createSubject(discordIds, subjectName, credits, semester);
            EmbedBuilder embed = embedFactory.createSubjectCreatedEmbed(subject, semester);
            
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Erstellen des Fachs", e);
        }
    }

    /**
     * Handles the show subjects command
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleShowSubjects(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            String semester = eventUtils.getOptionalString(event, OPT_SEMESTER);

            SubjectDisplayData displayData = prepareSubjectDisplay(discordIds, semester);
            
            if (displayData.isEmpty()) {
                sendNoSubjectsMessage(event, semester);
                return;
            }

            EmbedBuilder embed = embedFactory.createSubjectsDisplayEmbed(displayData);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Anzeigen der Fächer", e);
        }
    }

    /**
     * Handles the show semesters command
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleShowSemesters(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            List<String> semesters = notenrechnerService.getSemesters(
                discordIds.getServerId(), discordIds.getChannelId(), discordIds.getUserId());

            if (semesters.isEmpty()) {
                event.getHook().sendMessage(MSG_NO_SEMESTERS).setEphemeral(true).queue();
                return;
            }

            EmbedBuilder embed = embedFactory.createSemestersDisplayEmbed(semesters);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Anzeigen der Semester", e);
        }
    }

    /**
     * Creates a new subject with the provided details
     * @param discordIds the Discord IDs containing server, channel, and user IDs
     * @param subjectName the name of the subject to create
     * @param credits the number of credits for the subject
     * @param semester the semester in which the subject is taken, or null for no specific semester
     * @return the created Subject entity
     */
    private Subject createSubject(DiscordIdsDTO discordIds, String subjectName, int credits, String semester) {
        return notenrechnerService.createSubject(
            discordIds.getServerId(), 
            discordIds.getChannelId(), 
            discordIds.getUserId(),
            subjectName, 
            credits, 
            semester
        );
    }

    /**
     * Prepares the subject display data based on the provided Discord IDs and semester
     * @param discordIds the Discord IDs containing server, channel, and user IDs
     * @param semester the semester for which to retrieve subjects, or null for all subjects
     * @return SubjectDisplayData containing the subjects and their averages
     */
    private SubjectDisplayData prepareSubjectDisplay(DiscordIdsDTO discordIds, String semester) {
        if (hasSemester(semester)) {
            return prepareSubjectDisplayForSemester(discordIds, semester);
        } else {
            return prepareSubjectDisplayForAll(discordIds);
        }
    }

    /**
     * Prepares the subject display data for a specific semester
     * @param discordIds the Discord IDs containing server, channel, and user IDs
     * @param semester the semester for which to retrieve subjects
     * @return SubjectDisplayData containing the subjects and their average for the semester
     */
    private SubjectDisplayData prepareSubjectDisplayForSemester(DiscordIdsDTO discordIds, String semester) {
        List<Subject> subjects = notenrechnerService.getSubjectsForSemester(
            discordIds.getServerId(), discordIds.getChannelId(), discordIds.getUserId(), semester);
        double average = notenrechnerService.calculateSemesterAverage(
            discordIds.getServerId(), discordIds.getChannelId(), discordIds.getUserId(), semester);
            
        return new SubjectDisplayData(subjects, average, semester);
    }

    /**
     * Prepares the subject display data for all subjects without a specific semester
     * @param discordIds the Discord IDs containing server, channel, and user IDs
     * @return SubjectDisplayData containing all subjects and their overall average
     */
    private SubjectDisplayData prepareSubjectDisplayForAll(DiscordIdsDTO discordIds) {
        List<Subject> subjects = notenrechnerService.getSubjects(
            discordIds.getServerId(), discordIds.getChannelId(), discordIds.getUserId());
        double average = notenrechnerService.calculateOverallAverage(
            discordIds.getServerId(), discordIds.getChannelId(), discordIds.getUserId());
            
        return new SubjectDisplayData(subjects, average, null);
    }

    /**
     * Sends a message indicating that no subjects were found
     * @param event the SlashCommandInteractionEvent containing the command data
     * @param semester the semester for which no subjects were found, or null if not specified
     */
    private void sendNoSubjectsMessage(SlashCommandInteractionEvent event, String semester) {
        String message = hasSemester(semester) 
            ? String.format(MSG_NO_SUBJECTS_FOR_SEMESTER, semester)
            : String.format(MSG_NO_SUBJECTS_CREATED, CMD_CREATE_SUBJECT);
            
        event.getHook().sendMessage(message).setEphemeral(true).queue();
    }

    /**
     * Checks if a semester is specified
     * @param semester the semester to check
     * @return true if the semester is not null and not empty, false otherwise
     */
    private boolean hasSemester(String semester) {
        return semester != null && !semester.trim().isEmpty();
    }
}