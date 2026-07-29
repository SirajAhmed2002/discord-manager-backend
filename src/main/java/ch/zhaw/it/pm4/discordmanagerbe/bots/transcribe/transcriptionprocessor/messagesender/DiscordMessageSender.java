package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.messagesender;

import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Discord implementation of the MessageSender interface.
 * Handles sending transcription messages to Discord channels with automatic message splitting
 * for messages exceeding Discord's length limits.
 */
@Component
public class DiscordMessageSender implements MessageSender {

    /** the logger instance for this class */
    private static final Logger logger = LoggerFactory.getLogger(DiscordMessageSender.class);

    /** tha max. message length */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    /**
     * Sends a transcription to a Discord channel.
     * If the transcription exceeds Discord's message length limit, it will be split into multiple messages.
     *
     * @param channel The Discord channel to send the transcription to
     * @param transcription The transcription text to send
     */
    @Override
    public void sendTranscriptionToChannel(MessageChannel channel, String transcription) {
        if (channel == null) {
            logger.warn("Cannot send transcription: channel is null");
            return;
        }

        if (transcription.length() <= MAX_MESSAGE_LENGTH) {
            channel.sendMessage(transcription).queue(
                    success -> logger.info("Transcription sent to Discord channel successfully"),
                    error -> logger.error("Failed to send transcription to Discord channel: {}", error.getMessage())
            );
        } else {
            splitTranscription(transcription, MAX_MESSAGE_LENGTH)
                    .forEach(part -> channel.sendMessage(part).queue());
        }
    }

    /**
     * Splits a long transcription into multiple parts that fit within Discord's message length limit.
     * Attempts to split at paragraph breaks to maintain readability.
     *
     * @param transcription The transcription text to split
     * @param maxLength The maximum message length allowed
     * @return A list of message parts that can be sent individually
     */
     private List<String> splitTranscription(String transcription, int maxLength) {
        return List.of(Arrays.stream(transcription.split("\n\n"))
                .collect(StringBuilder::new,
                        (sb, line) -> {
                            if (sb.isEmpty() || sb.length() + line.length() + 2 <= maxLength) {
                                sb.append(line).append("\n\n");
                            } else {
                                sb.append("\u0000").append(line).append("\n\n"); // Nullzeichen als Trennmarker
                            }
                        },
                        (sb1, sb2) -> sb1.append(sb2.toString()))
                .toString()
                .split("\u0000")
                .clone());
    }
}
