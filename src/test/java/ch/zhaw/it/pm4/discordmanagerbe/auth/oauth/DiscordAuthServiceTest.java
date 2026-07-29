package ch.zhaw.it.pm4.discordmanagerbe.auth.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordAuthServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DiscordAuthService discordAuthService;

    private final String clientId = "test-client-id";
    private final String clientSecret = "test-client-secret";
    private final String redirectUri = "http://localhost:8081/callback";
    private final String discordTokenUrl = "https://discord.com/api/oauth2/token";
    private final String discordUserUrl = "https://discord.com/api/users/@me";

    @BeforeEach
    void setUp() {
        discordAuthService = new DiscordAuthService(
                restTemplate,
                objectMapper,
                clientId,
                clientSecret,
                redirectUri,
                discordTokenUrl,
                discordUserUrl
        );
    }

    @Test
    void exchangeCodeForToken_Success() throws IOException {
        // Arrange
        String authCode = "test-auth-code";
        String responseBody = "{\"access_token\":\"test-access-token\",\"token_type\":\"Bearer\",\"expires_in\":604800}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("access_token", "test-access-token");

        when(restTemplate.exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class))).thenReturn(tokenData);

        // Act
        String accessToken = discordAuthService.exchangeCodeForToken(authCode);

        // Assert
        assertEquals("test-access-token", accessToken);
        verify(restTemplate, times(1)).exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
    }

    @Test
    void exchangeCodeForToken_RestClientException() throws JsonProcessingException {
        // Arrange
        String authCode = "invalid-auth-code";

        when(restTemplate.exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // Act & Assert
        assertThrows(RestClientException.class, () -> {
            discordAuthService.exchangeCodeForToken(authCode);
        });

        verify(restTemplate, times(1)).exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void exchangeCodeForToken_JsonParsingError() throws IOException {
        // Arrange
        String authCode = "test-auth-code";
        String responseBody = "{\"access_token\":\"test-access-token\",\"token_type\":\"Bearer\",\"expires_in\":604800}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            discordAuthService.exchangeCodeForToken(authCode);
        });

        verify(restTemplate, times(1)).exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
    }

    @Test
    void exchangeCodeForToken_MissingAccessToken() throws IOException {
        // Arrange
        String authCode = "test-auth-code";
        String responseBody = "{\"token_type\":\"Bearer\",\"expires_in\":604800}"; // Missing access_token

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Map<String, String> tokenData = new HashMap<>();
        // No access_token in the map

        when(restTemplate.exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class))).thenReturn(tokenData);

        // Act
        String accessToken = discordAuthService.exchangeCodeForToken(authCode);

        // Assert
        assertNull(accessToken);
        verify(restTemplate, times(1)).exchange(
                eq(discordTokenUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
    }

    @Test
    void getUserInfo_Success() throws IOException {
        // Arrange
        String accessToken = "test-access-token";
        String responseBody = "{\"id\":\"123456789\",\"username\":\"testuser\",\"avatar\":\"abc123\",\"email\":\"test@example.com\"}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", "123456789");
        userInfo.put("username", "testuser");
        userInfo.put("avatar", "abc123");
        userInfo.put("email", "test@example.com");

        when(restTemplate.exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class))).thenReturn(userInfo);

        // Act
        Map<String, Object> result = discordAuthService.getUserInfo(accessToken);

        // Assert
        assertNotNull(result);
        assertEquals("123456789", result.get("id"));
        assertEquals("testuser", result.get("username"));
        assertEquals("abc123", result.get("avatar"));
        assertEquals("test@example.com", result.get("email"));

        verify(restTemplate, times(1)).exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
    }

    @Test
    void getUserInfo_RestClientException() throws JsonProcessingException {
        // Arrange
        String accessToken = "invalid-access-token";

        when(restTemplate.exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // Act & Assert
        assertThrows(RestClientException.class, () -> {
            discordAuthService.getUserInfo(accessToken);
        });

        verify(restTemplate, times(1)).exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    @Test
    void getUserInfo_JsonParsingError() throws IOException {
        // Arrange
        String accessToken = "test-access-token";
        String responseBody = "{\"id\":\"123456789\",\"username\":\"testuser\",\"avatar\":\"abc123\",\"email\":\"test@example.com\"}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            discordAuthService.getUserInfo(accessToken);
        });

        verify(restTemplate, times(1)).exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
    }

    @Test
    void getUserInfo_EmptyResponse() throws IOException {
        // Arrange
        String accessToken = "test-access-token";
        String responseBody = "{}"; // Leere JSON-Antwort

        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        Map<String, Object> userInfo = new HashMap<>(); // Leere Map

        when(restTemplate.exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        when(objectMapper.readValue(eq(responseBody), any(TypeReference.class))).thenReturn(userInfo);

        // Act
        Map<String, Object> result = discordAuthService.getUserInfo(accessToken);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(restTemplate, times(1)).exchange(
                eq(discordUserUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        );
        verify(objectMapper, times(1)).readValue(eq(responseBody), any(TypeReference.class));
    }

}