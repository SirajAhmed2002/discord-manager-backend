package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the state when the bot is actively recording audio in a voice channel.
 * Handles commands related to this state and manages the recording session.
 */
public class RecordingState extends AbstractBotState {

    private static final Logger logger = LoggerFactory.getLogger(RecordingState.class);

    /**
     * Constructs a new RecordingState with the specified bot.
     * @param bot The TranscribeBot instance
     */
    public RecordingState(TranscribeBot bot) {
        super(bot);
    }

    /**
     * Stops the recording before leaving the voice channel.
     * @param event The slash command event
     */
    @Override
    public void leaveVoiceChannel(SlashCommandInteractionEvent event) {
        stopRecording(event);
        super.leaveVoiceChannel(event);
    }

    /**
     * Informs the user that recording is already active.
     * @param event The slash command event
     */
    @Override
    public void startRecording(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.RECORDING_ACTIVE)).queue();
    }

    /**
     * Stops the active recording, saves the files, and transitions to ConnectedState.
     * @param event The slash command event
     */
    @Override
    public void stopRecording(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        VoiceChannel channel = getAndValidateMemberVoiceChannel(member, event);
        if (channel == null) {
            return;
        }

        String channelId = channel.getId();

        if (!validateActiveRecording(channelId, event)) {
            return;
        }

        try {
            processStopRecording(channelId, event.getChannel());
            cleanupRecordingResources(channelId);
            bot.setState(bot.getPendingApprovalState());
            event.getHook().sendMessage(BotMessages.get(MessageKey.RECORDING_STOPPED)).queue();
        } catch (Exception e) {
            logger.error("Error stopping recording", e);
            event.getHook().sendMessage("Fehler beim Beenden der Aufnahme: " + e.getMessage())
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Validates that there is an active recording for the channel.
     * @param channelId The ID of the channel to check
     * @param event The slash command event
     * @return True if there is an active recording, false otherwise
     */
    private boolean validateActiveRecording(String channelId, SlashCommandInteractionEvent event) {
        TranscribeBot.RecordingSession session = bot.getActiveSessions().get(channelId);
        if (session == null) {
            event.getHook().sendMessage(BotMessages.get(MessageKey.NO_ACTIVE_RECORDING))
                    .setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    /**
     * Processes the stop recording request and returns the result.
     * @param channelId The ID of the channel
     * @param textChannel The text channel for output
     */
    private void processStopRecording(String channelId, MessageChannel textChannel) {
        TranscribeBot.RecordingSession session = bot.getActiveSessions().get(channelId);
        session.stopRecording(textChannel);
    }

    /**
     * Cleans up resources associated with the recording session.
     * @param channelId The ID of the channel
     */
    private void cleanupRecordingResources(String channelId) {
        bot.getActiveSessions().remove(channelId);
        removeChannelData(channelId);
    }

    /**
     * Removes all data associated with a specific channel.
     * @param channelId The ID of the channel to remove data for
     */
    private void removeChannelData(String channelId) {
        bot.getPendingAcceptances().remove(channelId);
        bot.getAcceptedUsers().remove(channelId);
    }
}