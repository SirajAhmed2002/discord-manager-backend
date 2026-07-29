package ch.zhaw.it.pm4.discordmanagerbe.bots.grade.util;

import ch.zhaw.it.pm4.discordmanagerbe.dto.DiscordIdsDTO;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static ch.zhaw.it.pm4.discordmanagerbe.bots.grade.GradeBotConstants.*;

/**
 * Utility class for Discord event handling
 */
@Component
public class DiscordEventUtils {

    /**
     * Logger for DiscordEventUtils
     */
    private static final Logger log = LoggerFactory.getLogger(DiscordEventUtils.class);

    /**
     * Constructs a DiscordIdsDTO from the SlashCommandInteractionEvent
     * @param event the event containing Discord interaction data
     * @return DiscordIdsDTO with server, channel, and user IDs
     */
    public DiscordIdsDTO extractDiscordIds(SlashCommandInteractionEvent event) {
        return DiscordIdsDTO.builder()
            .serverId(event.getGuild() != null ? event.getGuild().getId() : UNKNOWN_SERVER)
            .channelId(event.getChannel().getId())
            .userId(event.getUser().getId())
            .build();
    }

    /**
     * Gets a required string option from the event, returning an empty string if not found
     * @param event the SlashCommandInteractionEvent
     * @param optionName the name of the required option
     * @return the value of the required string option
     */
    public String getRequiredString(SlashCommandInteractionEvent event, String optionName) {
        return event.getOption(optionName, EMPTY_STRING, OptionMapping::getAsString);
    }

    /**
     * Gets an optional string option from the event
     * @param event the SlashCommandInteractionEvent
     * @param optionName the name of the optional option
     */
    public String getOptionalString(SlashCommandInteractionEvent event, String optionName) {
        return event.getOption(optionName, null, OptionMapping::getAsString);
    }

    /**
     * Gets a required integer option from the event
     * @param event the SlashCommandInteractionEvent
     * @param optionName the name of the required option
     */
    public int getRequiredInt(SlashCommandInteractionEvent event, String optionName) {
        return event.getOption(optionName, 0, OptionMapping::getAsInt);
    }

    /**
     * Sends an error message to the user
     * @param event the SlashCommandInteractionEvent
     * @param operation the operation that caused the error
     * @param e the exception that was thrown
     */
    public void sendErrorMessage(SlashCommandInteractionEvent event, String operation, Exception e) {
        log.error("Error in {}: {}", operation, e.getMessage(), e);
        event.getHook().sendMessage("Fehler beim " + operation + ": " + e.getMessage())
            .setEphemeral(true).queue();
    }
}