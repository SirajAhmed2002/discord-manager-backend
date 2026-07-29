package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor;

import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.transcriptionprocessor.transcriptionservice.OpenAITranscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAITranscriptionServiceTest {

    private OpenAITranscriptionService transcriptionService;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private final String apiUrl = "https://test-api.openai.com/v1/audio/transcriptions";
    private final String apiKey = "test-api-key";
    private final String transcriptionModel = "test-model";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        transcriptionService = new OpenAITranscriptionService(
                httpClient,
                apiUrl,
                apiKey,
                transcriptionModel
        );
    }

    @Test
    void transcribeSingleFile_Success() throws Exception {
        // Arrange
        File audioFile = createTempAudioFile();
        String expectedTranscription = "This is a test transcription";

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(
                new ObjectMapper().writeValueAsString(Map.of("text", expectedTranscription))
        );
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act
        String actualTranscription = transcriptionService.transcribeSingleFile(audioFile);

        // Assert
        assertEquals(expectedTranscription, actualTranscription);
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void transcribeSingleFile_ApiError() throws Exception {
        // Arrange
        File audioFile = createTempAudioFile();

        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("{\"error\":\"Bad request\"}");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act & Assert
        Exception exception = assertThrows(IOException.class, () -> {
            transcriptionService.transcribeSingleFile(audioFile);
        });

        assertTrue(exception.getMessage().contains("OpenAI API error: 400"));
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void transcribeSingleFile_HttpClientThrowsException() throws Exception {
        // Arrange
        File audioFile = createTempAudioFile();

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection error"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            transcriptionService.transcribeSingleFile(audioFile);
        });

        assertEquals("Connection error", exception.getMessage());
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void transcribeSingleFile_MalformedResponseBody() throws Exception {
        // Arrange
        File audioFile = createTempAudioFile();

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("Not a valid JSON");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act & Assert
        assertThrows(IOException.class, () -> {
            transcriptionService.transcribeSingleFile(audioFile);
        });
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void transcribeSingleFile_FileNotFound() throws IOException, InterruptedException {
        // Arrange
        File nonExistentFile = new File("non-existent-file.wav");

        // Act & Assert
        assertThrows(IOException.class, () -> {
            transcriptionService.transcribeSingleFile(nonExistentFile);
        });

        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void transcribeSingleFile_ResponseMissingText() throws Exception {
        // Arrange
        File audioFile = createTempAudioFile();

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(
                new ObjectMapper().writeValueAsString(Map.of("other_field", "value"))
        );
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // Act
        String actualTranscription = transcriptionService.transcribeSingleFile(audioFile);

        // Assert
        assertNull(actualTranscription);
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    private File createTempAudioFile() throws IOException {
        Path audioFilePath = tempDir.resolve("test-audio.wav");
        byte[] dummyAudioContent = new byte[1024];
        Files.write(audioFilePath, dummyAudioContent);
        return audioFilePath.toFile();
    }
}