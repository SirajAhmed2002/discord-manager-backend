package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.messagesender;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

/**
 * Interface for sending transcription messages to communication channels.
 * Defines a common contract for different message sending implementations.
 */
public interface MessageSender {

    /**
     * Sends a transcription text to a specified message channel.
     *
     * @param channel The channel to send the transcription to
     * @param transcription The transcription text to send
     */
    void sendTranscriptionToChannel(MessageChannel channel, String transcription);
}
