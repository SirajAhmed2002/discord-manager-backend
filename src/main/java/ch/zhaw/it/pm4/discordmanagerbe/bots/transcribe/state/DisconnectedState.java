package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.TranscribeBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the state when the bot is not connected to any voice channel.
 * Handles connection requests and provides appropriate responses for unavailable actions.
 */
public class DisconnectedState extends AbstractBotState {

    private static final Logger logger = LoggerFactory.getLogger(DisconnectedState.class);

    /**
     * Constructs a new DisconnectedState with the specified bot.
     * @param bot The TranscribeBot instance
     */
    public DisconnectedState(TranscribeBot bot) {
        super(bot);
    }

    /**
     * Handles joining a voice channel when the bot is disconnected.
     * @param event The slash command event
     */
    @Override
    public void joinVoiceChannel(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        VoiceChannel channel = getAndValidateMemberVoiceChannel(member, event);
        if (channel == null) {
            return;
        }
        logger.info("Joining voice channel: {}", channel.getName());

        try {
            connectToVoiceChannel(channel);
            event.getHook().sendMessage(BotMessages.get(MessageKey.JOIN_SUCCESS)).queue();
        } catch (Exception e) {
            logger.error("Error joining voice channel", e);
            event.getHook().sendMessage(BotMessages.get(MessageKey.CONNECTION_ERROR))
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Informs the user that they need to join a channel first before locking.
     * @param event The slash command event
     */
    @Override
    public void lockVoiceChannel(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.JOIN_FIRST)).queue();
    }

    /**
     * Connects to a voice channel and updates the bot state.
     * @param channel The voice channel to connect to
     */
    private void connectToVoiceChannel(VoiceChannel channel) {
        AudioManager audioManager = channel.getGuild().getAudioManager();
        audioManager.openAudioConnection(channel);

        bot.setActiveAudioManager(audioManager);
        bot.setState(bot.getConnectedState());
    }
}