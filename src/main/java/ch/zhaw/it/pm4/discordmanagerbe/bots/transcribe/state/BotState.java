package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Interface defining the different states of the TranscribeBot and their associated actions.
 * Implements the State pattern for managing bot behavior.
 */
public interface BotState {

    /**
     * Handles joining a voice channel.
     * @param event The slash command event
     */
    void joinVoiceChannel(SlashCommandInteractionEvent event);

    /**
     * Handles leaving a voice channel.
     * @param event The slash command event
     */
    void leaveVoiceChannel(SlashCommandInteractionEvent event);

    /**
     * Handles locking a voice channel.
     * @param event The slash command event
     */
    void lockVoiceChannel(SlashCommandInteractionEvent event);

    /**
     * Handles unlocking a voice channel.
     * @param event The slash command event
     */
    void unlockVoiceChannel(SlashCommandInteractionEvent event);

    /**
     * Handles accepting a recording request.
     * @param event The slash command event
     */
    void handleAcceptRecording(SlashCommandInteractionEvent event);

    /**
     * Handles starting a recording.
     * @param event The slash command event
     */
    void startRecording(SlashCommandInteractionEvent event);

    /**
     * Handles stopping a recording.
     * @param event The slash command event
     */
    void stopRecording(SlashCommandInteractionEvent event);
}
