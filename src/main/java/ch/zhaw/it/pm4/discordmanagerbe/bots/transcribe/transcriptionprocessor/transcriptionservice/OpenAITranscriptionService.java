package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of TranscriptionService that uses OpenAI's audio transcription API.
 * Handles sending audio files to OpenAI and processing the transcription responses.
 */
@Service
public class OpenAITranscriptionService implements TranscriptionService {

    /**
     * Logger instance for logging events and debugging information.
     */
    private static final Logger logger = LoggerFactory.getLogger(OpenAITranscriptionService.class);

    /**
     * The URL of the OpenAI transcription API.
     */
    @Value("${openai.api.url:https://api.openai.com/v1/audio/transcriptions}")
    private String apiUrl;

    /**
     * The API key used for authenticating requests to the OpenAI API.
     */
    @Value("${openai.api.key}")
    private String apiKey;

    /**
     * The transcription model to use for processing audio files.
     */
    @Value("${openai.transcription.model:gpt-4o-transcribe}")
    private String transcriptionModel;

    /**
     * The HTTP client used for making requests to the OpenAI API.
     */
    private final HttpClient httpClient;

    /**
     * Default constructor that initializes a new HTTP client.
     */
    public OpenAITranscriptionService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Constructor with custom parameters for testing and configuration.
     *
     * @param httpClient The HTTP client to use for API requests
     * @param apiUrl The URL of the OpenAI transcription API
     * @param apiKey The API key for authentication
     * @param transcriptionModel The model to use for transcription
     */
    public OpenAITranscriptionService(HttpClient httpClient, String apiUrl, String apiKey, String transcriptionModel) {
        this.httpClient = httpClient;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.transcriptionModel = transcriptionModel;
    }

    /**
     * Transcribes multiple audio files concurrently using a thread pool.
     * Returns a map of sequence IDs to their transcribed text.
     *
     * @param sequenceFiles Map of sequence IDs to their corresponding audio files
     * @return Map of sequence IDs to their transcribed text
     */
    @Override
    public Map<Integer, String> transcribeSequenceFiles(Map<Integer, File> sequenceFiles) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(3, Runtime.getRuntime().availableProcessors()));

        Map<Integer, String> transcriptions = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = sequenceFiles.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    try {
                        String transcription = transcribeSingleFile(entry.getValue());
                        transcriptions.put(entry.getKey(), transcription);
                        logger.info("Transcribed sequence {}: {}", entry.getKey(), transcription);
                    } catch (Exception e) {
                        logger.error("Error transcribing sequence {}: {}", entry.getKey(), e.getMessage());
                        transcriptions.put(entry.getKey(), "[Transcription failed]");
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();
        return transcriptions;
    }

    /**
     * Transcribes a single audio file using the OpenAI API.
     *
     * @param audioFile The audio file to transcribe
     * @return The transcribed text
     * @throws Exception If an error occurs during the API request or response processing
     */
    @Override
    public String transcribeSingleFile(File audioFile) throws Exception {
        String boundary = "Boundary-" + UUID.randomUUID().toString().replace("-", "");
        byte[] requestBody = createMultipartBody(audioFile, boundary);

        HttpResponse<String> response = sendTranscriptionRequest(requestBody, boundary);

        return parseTranscriptionResponse(response);
    }

    /**
     * Creates a multipart form-data body for the API request.
     *
     * @param audioFile The audio file to include in the request
     * @param boundary The boundary string for multipart separation
     * @return Byte array containing the complete request body
     * @throws IOException If an error occurs while reading the file or creating the body
     */
    private byte[] createMultipartBody(File audioFile, String boundary) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeFormField(baos, boundary, "model", transcriptionModel);
        writeFileField(baos, boundary, "file", audioFile.getName(), "audio/wav",
                Files.readAllBytes(audioFile.toPath()));
        baos.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return baos.toByteArray();
    }

    /**
     * Writes a form field to the multipart request body.
     *
     * @param os The output stream to write to
     * @param boundary The boundary string for multipart separation
     * @param name The name of the form field
     * @param value The value of the form field
     * @throws IOException If an error occurs while writing to the output stream
     */
    private void writeFormField(OutputStream os, String boundary, String name, String value) throws IOException {
        String fieldHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n";
        os.write(fieldHeader.getBytes(StandardCharsets.UTF_8));
        os.write(value.getBytes(StandardCharsets.UTF_8));
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes a file field to the multipart request body.
     *
     * @param os The output stream to write to
     * @param boundary The boundary string for multipart separation
     * @param name The name of the form field
     * @param filename The name of the file
     * @param contentType The MIME type of the file
     * @param fileData The binary content of the file
     * @throws IOException If an error occurs while writing to the output stream
     */
    private void writeFileField(OutputStream os,
                                String boundary,
                                String name,
                                String filename,
                                String contentType,
                                byte[] fileData) throws IOException {
        String fileHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n";
        os.write(fileHeader.getBytes(StandardCharsets.UTF_8));
        os.write(fileData);
    }

    /**
     * Sends a transcription request to the OpenAI API.
     *
     * @param requestBody The prepared multipart request body
     * @param boundary The boundary string used in the multipart request
     * @return The HTTP response from the API
     * @throws IOException If an error occurs during the HTTP request
     * @throws InterruptedException If the HTTP request is interrupted
     */
    private HttpResponse<String> sendTranscriptionRequest(byte[] requestBody, String boundary)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.error("API call error: {} - {}", response.statusCode(), response.body());
        }

        return response;
    }

    /**
     * Parses the JSON response from the OpenAI API to extract the transcribed text.
     *
     * @param response The HTTP response from the API
     * @return The transcribed text
     * @throws IOException If an error occurs while parsing the response
     */
    private String parseTranscriptionResponse(HttpResponse<String> response) throws IOException {
        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 200) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
            return (String) responseMap.get("text");
        } else {
            throw new IOException("OpenAI API error: " + statusCode + " - " + responseBody);
        }
    }
}
