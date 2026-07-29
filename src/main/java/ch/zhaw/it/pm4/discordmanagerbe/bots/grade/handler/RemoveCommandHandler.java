package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.handler;

import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.factory.EmbedFactory;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.service.NotenrechnerService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util.DiscordEventUtils;
import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Handles remove/delete commands for the grade calculator bot
 */
@Component
public class RemoveCommandHandler {

    /**
     * Logger for RemoveCommandHandler
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
     * Constructor for RemoveCommandHandler
     * @param notenrechnerService service for grade calculations and data retrieval
     * @param embedFactory factory for creating embeds
     * @param eventUtils utility for Discord event handling
     */
    public RemoveCommandHandler(NotenrechnerService notenrechnerService, 
                               EmbedFactory embedFactory,
                               DiscordEventUtils eventUtils) {
        this.notenrechnerService = notenrechnerService;
        this.embedFactory = embedFactory;
        this.eventUtils = eventUtils;
    }

    /**
     * Handles the remove subject command
     * Removes a subject and all its associated grades
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleRemoveSubject(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            String subjectName = eventUtils.getRequiredString(event, OPT_FACH);
            String semester = eventUtils.getOptionalString(event, OPT_SEMESTER);

            boolean removed = notenrechnerService.removeSubject(
                discordIds.getServerId(), 
                discordIds.getChannelId(), 
                discordIds.getUserId(),
                subjectName, 
                semester
            );

            EmbedBuilder embed = embedFactory.createSubjectRemovedEmbed(subjectName, semester, removed);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Löschen des Fachs", e);
        }
    }

    /**
     * Handles the remove grades command
     * Removes all grades from a specific subject
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleRemoveGrades(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            String subjectName = eventUtils.getRequiredString(event, OPT_FACH);
            String semester = eventUtils.getOptionalString(event, OPT_SEMESTER);

            int removedCount = notenrechnerService.removeAllGradesFromSubject(
                discordIds.getServerId(), 
                discordIds.getChannelId(), 
                discordIds.getUserId(),
                subjectName, 
                semester
            );

            EmbedBuilder embed = embedFactory.createGradesRemovedEmbed(
                subjectName, semester, removedCount);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Löschen der Noten", e);
        }
    }

    /**
     * Handles the remove semester command
     * Removes all subjects and grades in the specified semester
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    public void handleRemoveSemester(SlashCommandInteractionEvent event) {
        try {
            DiscordIdsDTO discordIds = eventUtils.extractDiscordIds(event);
            String semester = eventUtils.getRequiredString(event, OPT_SEMESTER);


            // Validate semester name
            if (semester.trim().isEmpty()) {
                event.getHook().sendMessage("❌ Semestername darf nicht leer sein!")
                      .setEphemeral(true).queue();
                return;
            }

            int removedCount = notenrechnerService.removeSemester(
                discordIds.getServerId(), 
                discordIds.getChannelId(), 
                discordIds.getUserId(),
                semester
            );

            EmbedBuilder embed = embedFactory.createSemesterRemovedEmbed(semester, removedCount);
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
            
        } catch (Exception e) {
            eventUtils.sendErrorMessage(event, "Löschen des Semesters", e);
        }
    }
}