package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionservice;

import java.io.File;
import java.util.Map;

/**
 * Interface for audio transcription services.
 * Defines methods to convert audio files to text.
 */
public interface TranscriptionService {

    /**
     * Transcribes multiple audio files in parallel.
     *
     * @param sequenceFiles Map of sequence IDs to their corresponding audio files
     * @return Map of sequence IDs to their transcribed text
     */
    Map<Integer, String> transcribeSequenceFiles(Map<Integer, File> sequenceFiles);

    /**
     * Transcribes a single audio file.
     *
     * @param audioFile The audio file to transcribe
     * @return The transcribed text
     * @throws Exception If an error occurs during transcription
     */
    String transcribeSingleFile(File audioFile) throws Exception;
}
