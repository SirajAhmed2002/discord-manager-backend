package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionformatter;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.SequenceTimelineEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of the TranscriptionFormatter interface.
 * Formats transcription data into a Discord-friendly markdown format with timestamps and usernames.
 */
@Component
public class TranscriptionFormatterImpl implements TranscriptionFormatter {

    /**
     * Formats timeline entries and transcriptions into a readable markdown document.
     * Includes timestamps and usernames for each transcribed segment.
     * Filters out empty or failed transcriptions.
     *
     * @param timeline List of sequence timeline entries
     * @param transcriptions Map of sequence IDs to their transcribed text
     * @return A formatted markdown string of the conversation transcript
     */
    @Override
    public String formatTranscription(List<SequenceTimelineEntry> timeline, Map<Integer, String> transcriptions) {
        StringBuilder transcript = new StringBuilder();
        transcript.append("# Discord Conversation Transcript\n\n");

        timeline.stream()
                .map(entry -> {
                    String transcription = transcriptions.getOrDefault(entry.getSequenceId(), "").trim();
                    if (transcription.isEmpty() || "[Transcription failed]".equals(transcription)) {
                        return null;
                    }

                    String timestamp = formatTime(entry.getStartTime());
                    return "[" + timestamp + "] **" + entry.getUsername() + "**: " + transcription + "\n\n";
                })
                .filter(Objects::nonNull)
                .forEach(transcript::append);

        return transcript.toString();
    }

    /**
     * Formats a timestamp in milliseconds to a readable time format (HH:MM:SS).
     *
     * @param timeMs Time in milliseconds
     * @return Formatted time string
     */
    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%02d:%02d:%02d",
                hours,
                minutes % 60,
                seconds % 60);
    }
}
