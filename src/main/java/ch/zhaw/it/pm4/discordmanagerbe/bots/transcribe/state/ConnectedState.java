package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the state when the bot is connected to a voice channel but not locked or recording.
 * Handles commands related to this state and transitions to other states.
 */
public class ConnectedState extends AbstractBotState {

    /** the logger instance for this class */
    private static final Logger logger = LoggerFactory.getLogger(ConnectedState.class);

    /**
     * Constructs a new ConnectedState with the specified bot.
     * @param bot The TranscribeBot instance
     */
    public ConnectedState(TranscribeBot bot) {
        super(bot);
    }

    /**
     * Informs the user that the bot is already connected to a voice channel.
     * @param event The slash command event
     */
    @Override
    public void joinVoiceChannel(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.ALREADY_CONNECTED)).queue();
    }

    /**
     * Handles leaving the voice channel and transitions to DisconnectedState.
     * @param event The slash command event
     */
    @Override
    public void leaveVoiceChannel(SlashCommandInteractionEvent event) {
        AudioManager audioManager = bot.getActiveAudioManager();
        if (audioManager != null && audioManager.isConnected()) {
            logger.info("Leaving voice channel");

            audioManager.closeAudioConnection();
            bot.setActiveAudioManager(null);
            bot.setState(bot.getDisconnectedState());

            event.getHook().sendMessage(BotMessages.get(MessageKey.LEAVE_SUCCESS)).queue();
            logger.info("Successfully left voice channel");
        }
    }

    /**
     * Locks the voice channel, requests recording permission, and transitions directly to PendingApprovalState.
     * @param event The slash command event
     */
    @Override
    public void lockVoiceChannel(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        VoiceChannel voiceChannel = getAndValidateMemberVoiceChannel(member, event);
        if (voiceChannel == null) {
            return;
        }

        lockChannel(voiceChannel);
        requestRecordingPermission(event, voiceChannel);
        bot.setState(bot.getPendingApprovalState());
    }

    /**
     * Informs the user that they need to lock the channel before starting recording.
     * @param event The slash command event
     */
    @Override
    public void startRecording(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        VoiceChannel channel = getAndValidateMemberVoiceChannel(member, event);
        if (channel == null) {
            return;
        }
        event.getHook().sendMessage(BotMessages.get(MessageKey.LOCK_FIRST)).queue();
    }
}