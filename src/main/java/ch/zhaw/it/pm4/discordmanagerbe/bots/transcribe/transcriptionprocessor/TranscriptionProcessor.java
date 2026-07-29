package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.messagesender.MessageSender;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.timelinereader.TimelineReader;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionformatter.TranscriptionFormatter;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionservice.TranscriptionService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * Processes audio files from recorded voice channels and generates text transcriptions.
 */
@Component
public class TranscriptionProcessor {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(TranscriptionProcessor.class);

    /** Reader for parsing timeline data from files.*/
    private final TimelineReader timelineReader;

    /** Service for transcribing audio files into text. */
    private final TranscriptionService transcriptionService;

    /** Formatter for converting transcription data into a readable format.*/
    private final TranscriptionFormatter formatter;

    /**Sender for delivering transcription messages to communication channels.*/
    private final MessageSender messageSender;

    /**
     * Constructs a new TranscriptionProcessor with the required dependencies.
     *
     * @param timelineReader The reader for parsing timeline data
     * @param transcriptionService The service for transcribing audio files
     * @param formatter The formatter for transcription data
     * @param messageSender The sender for delivering transcription messages
     */
    @Autowired
    public TranscriptionProcessor(
            TimelineReader timelineReader,
            TranscriptionService transcriptionService,
            TranscriptionFormatter formatter,
            MessageSender messageSender) {
        this.timelineReader = timelineReader;
        this.transcriptionService = transcriptionService;
        this.formatter = formatter;
        this.messageSender = messageSender;
    }

    /**
     * Processes temporary audio files and timeline data to create a complete transcription.
     * @param timelineFile The CSV file containing sequence timeline information
     * @param sequenceFiles Map of sequence IDs to their audio files
     * @param textChannel The Discord text channel to send the transcription to
     * @return The complete transcription as a formatted string
     */
    public String transcribeTemporaryFiles(
            File timelineFile,
            Map<Integer, File> sequenceFiles,
            MessageChannel textChannel) {
        try {
            logger.info("Starting transcription process with temporary files");

            List<SequenceTimelineEntry> timeline = timelineReader.readTimelineFile(timelineFile);
            timeline.sort(Comparator.comparingLong(SequenceTimelineEntry::getStartTime));
            Map<Integer, String> transcriptions = transcriptionService.transcribeSequenceFiles(sequenceFiles);
            String transcription = formatter.formatTranscription(timeline, transcriptions);

            if (textChannel != null) {
                messageSender.sendTranscriptionToChannel(textChannel, transcription);
            }

            logger.info("Transcription completed successfully");
            return transcription;

        } catch (Exception e) {
            logger.error("Error during transcription process", e);
            return "Error creating transcription: " + e.getMessage();
        }
    }
}