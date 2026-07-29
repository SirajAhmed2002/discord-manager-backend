package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionformatter;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SequenceTimelineEntry;

import java.util.List;
import java.util.Map;

/**
 * Interface for formatting transcription data.
 * Defines methods to convert timeline entries and transcription text into a formatted output.
 */
public interface TranscriptionFormatter {

    /**
     * Formats a collection of timeline entries and their corresponding transcriptions
     * into a readable text format.
     *
     * @param timeline List of sequence timeline entries
     * @param transcriptions Map of sequence IDs to their transcribed text
     * @return A formatted string representation of the transcription
     */
    String formatTranscription(List<SequenceTimelineEntry> timeline, Map<Integer, String> transcriptions);
}
