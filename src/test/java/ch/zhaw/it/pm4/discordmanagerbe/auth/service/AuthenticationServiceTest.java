package ch.zhaw.it.pm4.discordmanagerbe.auth.service;

import ch.zhaw.it.pm4.discordmanagerbe.auth.jwt.JwtService;
import ch.zhaw.it.pm4.discordmanagerbe.auth.oauth.DiscordAuthService;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private DiscordAuthService discordAuthService;

    @Mock
    private JwtService jwtService;

    @Mock
    private StudentService studentService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(discordAuthService, jwtService, studentService);
    }

    @Test
    void authenticateWithDiscordCode_Success() throws IOException {
        // Arrange
        String code = "test-discord-code";
        String accessToken = "test-access-token";
        String username = "testUser";
        String userId = "12345";
        String email = "test@example.com";
        String jwtToken = "jwt-token-xyz";

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("id", userId);
        userInfo.put("email", email);

        Student mockStudent = new Student(); // Assume Student has a default constructor

        // Mock behavior
        when(discordAuthService.exchangeCodeForToken(code)).thenReturn(accessToken);
        when(discordAuthService.getUserInfo(accessToken)).thenReturn(userInfo);
        when(studentService.createStudentIfNotExists(userId, username, email)).thenReturn(mockStudent);

        Map<String, Object> expectedClaims = new HashMap<>();
        expectedClaims.put("discordId", userId);
        expectedClaims.put("source", "discord");

        when(jwtService.generateToken(eq(username), argThat(claims ->
                claims.containsKey("discordId") &&
                        claims.containsKey("source") &&
                        claims.get("discordId").equals(userId) &&
                        claims.get("source").equals("discord")
        ))).thenReturn(jwtToken);

        // Act
        Map<String, String> result = authenticationService.authenticateWithDiscordCode(code);

        // Assert
        assertNotNull(result);
        assertEquals(jwtToken, result.get("token"));
        assertEquals(username, result.get("username"));

        // Verify interactions
        verify(discordAuthService).exchangeCodeForToken(code);
        verify(discordAuthService).getUserInfo(accessToken);
        verify(studentService).createStudentIfNotExists(userId, username, email);
        verify(jwtService).generateToken(eq(username), any());
    }

    @Test
    void authenticateWithDiscordCode_ExchangeCodeFailed() {
        // Arrange
        String code = "invalid-code";

        // Mock behavior - simulate exception
        try {
            when(discordAuthService.exchangeCodeForToken(code)).thenThrow(new IOException("Exchange code failed"));

            // Act & Assert
            assertThrows(IOException.class, () -> authenticationService.authenticateWithDiscordCode(code));

            // Verify interactions
            verify(discordAuthService).exchangeCodeForToken(code);
            verifyNoInteractions(studentService, jwtService);
        } catch (IOException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void authenticateWithDiscordCode_GetUserInfoFailed() {
        // Arrange
        String code = "test-discord-code";
        String accessToken = "test-access-token";

        // Mock behavior
        try {
            when(discordAuthService.exchangeCodeForToken(code)).thenReturn(accessToken);
            when(discordAuthService.getUserInfo(accessToken)).thenThrow(new IOException("Get user info failed"));

            // Act & Assert
            assertThrows(IOException.class, () -> authenticationService.authenticateWithDiscordCode(code));

            // Verify interactions
            verify(discordAuthService).exchangeCodeForToken(code);
            verify(discordAuthService).getUserInfo(accessToken);
            verifyNoInteractions(studentService, jwtService);
        } catch (IOException e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void authenticateWithDiscordCode_MissingUserInfo() throws IOException {
        // Arrange
        String code = "test-discord-code";
        String accessToken = "test-access-token";

        Map<String, Object> userInfo = new HashMap<>();
        // Missing fields that should be there

        // Mock behavior
        when(discordAuthService.exchangeCodeForToken(code)).thenReturn(accessToken);
        when(discordAuthService.getUserInfo(accessToken)).thenReturn(userInfo);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> authenticationService.authenticateWithDiscordCode(code));

        // Verify interactions
        verify(discordAuthService).exchangeCodeForToken(code);
        verify(discordAuthService).getUserInfo(accessToken);
        verifyNoInteractions(jwtService);
    }
}