package ch.zhaw.it.pm4.discordmanagerbe.auth.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Service Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "testSecretKeyMustBeAtLeast32BytesLong123456789";
    private static final long TEST_EXPIRATION = 3600000; // 1 hour in milliseconds
    private static final long TEST_CLEANUP_INTERVAL = 1800000; // 30 minutes in milliseconds
    private static final String DEFAULT_USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, TEST_EXPIRATION, TEST_CLEANUP_INTERVAL);
        // Initialize the service manually since @PostConstruct won't run in tests
        ReflectionTestUtils.invokeMethod(jwtService, "init");
    }

    // Helper method to create a token with default claims
    private String createDefaultToken(String username) {
        return jwtService.generateToken(username, new HashMap<>());
    }

    // Helper method to create a token with custom claims
    private String createTokenWithClaims(String username, Map<String, Object> claims) {
        return jwtService.generateToken(username, claims);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should create valid token with username")
        void shouldCreateTokenWithValidUsername() {
            // Act
            String token = createDefaultToken(DEFAULT_USERNAME);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
        }

        @Test
        @DisplayName("Should store correct username in token")
        void shouldContainCorrectUsername() {
            // Act
            String token = createDefaultToken(DEFAULT_USERNAME);
            String extractedUsername = jwtService.getUsernameFromToken(token);

            // Assert
            assertEquals(DEFAULT_USERNAME, extractedUsername);
        }

        @Test
        @DisplayName("Should store additional claims in token")
        void shouldContainAdditionalClaims() {
            // Arrange
            Map<String, Object> additionalClaims = new HashMap<>();
            additionalClaims.put("role", "ADMIN");
            additionalClaims.put("userId", 12345);

            // Act
            String token = createTokenWithClaims(DEFAULT_USERNAME, additionalClaims);
            Claims claims = jwtService.parseClaims(token);

            // Assert
            assertEquals("ADMIN", claims.get("role"));
            assertEquals(12345, claims.get("userId", Integer.class));
        }

        @Test
        @DisplayName("Should set expiry time according to configuration")
        void shouldSetCorrectExpiryTime() {
            // Arrange
            long beforeGenerationTime = System.currentTimeMillis();

            // Act
            String token = createDefaultToken(DEFAULT_USERNAME);
            Claims claims = jwtService.parseClaims(token);
            Date expiryDate = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();

            // Assert
            assertNotNull(expiryDate);
            assertNotNull(issuedAt);

            // Check that issued time is now (or very close to now)
            long tolerance = 1000;
            assertTrue(issuedAt.getTime() >= (beforeGenerationTime - tolerance));
            assertTrue(issuedAt.getTime() <= System.currentTimeMillis());

            // Check expiration time matches configuration (allow 100ms margin for test execution time)
            long expectedExpiryTime = issuedAt.getTime() + TEST_EXPIRATION;
            long timeDifference = Math.abs(expectedExpiryTime - expiryDate.getTime());
            assertTrue(timeDifference < 100, "Expiry time should match configuration");
        }

        @Test
        @DisplayName("Different usernames should create different tokens")
        void differentUsernamesShouldCreateDifferentTokens() {
            // Act
            String token1 = createDefaultToken("testUser1");
            String token2 = createDefaultToken("testUser2");

            // Assert
            assertNotEquals(token1, token2);
        }

        @Test
        @DisplayName("Different claims should create different tokens")
        void differentClaimsShouldCreateDifferentTokens() {
            // Arrange
            Map<String, Object> claims1 = new HashMap<>();
            claims1.put("role", "USER");

            Map<String, Object> claims2 = new HashMap<>();
            claims2.put("role", "ADMIN");

            // Act
            String token1 = createTokenWithClaims(DEFAULT_USERNAME, claims1);
            String token2 = createTokenWithClaims(DEFAULT_USERNAME, claims2);

            // Assert
            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("Username Extraction Tests")
    class UsernameExtractionTests {

        @Test
        @DisplayName("Should extract correct username")
        void shouldExtractCorrectUsername() {
            // Act
            String token = createDefaultToken(DEFAULT_USERNAME);
            String extractedUsername = jwtService.getUsernameFromToken(token);

            // Assert
            assertEquals(DEFAULT_USERNAME, extractedUsername);
        }

        @ParameterizedTest
        @ValueSource(strings = {"user@example.com", "user.name", "user-name", "user_name"})
        @DisplayName("Should handle special characters in username")
        void shouldHandleSpecialCharactersInUsername(String username) {
            // Act
            String token = createDefaultToken(username);
            String extractedUsername = jwtService.getUsernameFromToken(token);

            // Assert
            assertEquals(username, extractedUsername);
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.string";

            // Act & Assert
            assertThrows(Exception.class, () -> jwtService.getUsernameFromToken(invalidToken));
        }

        @Test
        @DisplayName("Should extract username even from invalidated token")
        void shouldExtractUsernameEvenFromInvalidatedToken() {
            // Arrange
            String token = createDefaultToken(DEFAULT_USERNAME);

            // Act
            jwtService.invalidateToken(token);
            String extractedUsername = jwtService.getUsernameFromToken(token);

            // Assert
            assertEquals(DEFAULT_USERNAME, extractedUsername);
            assertFalse(jwtService.validateToken(token), "Token should be invalid after invalidation");
        }
    }

    @Nested
    @DisplayName("Token Invalidation Tests")
    class TokenInvalidationTests {

        @Test
        @DisplayName("Should successfully invalidate valid token")
        void shouldInvalidateValidToken() {
            // Arrange
            String token = createDefaultToken(DEFAULT_USERNAME);

            // Act
            boolean result = jwtService.invalidateToken(token);

            // Assert
            assertTrue(result, "Token invalidation should return true for valid tokens");
            assertFalse(jwtService.validateToken(token), "Token should be invalid after invalidation");
        }

        @Test
        @DisplayName("Should return false when invalidating invalid token")
        void shouldReturnFalseForInvalidToken() {
            // Arrange
            String invalidToken = "invalid.token.string";

            // Act
            boolean result = jwtService.invalidateToken(invalidToken);

            // Assert
            assertFalse(result, "Token invalidation should return false for invalid tokens");
        }

        @Test
        @DisplayName("Token should remain invalid after multiple invalidations")
        void shouldRemainInvalidAfterMultipleInvalidations() {
            // Arrange
            String token = createDefaultToken(DEFAULT_USERNAME);

            // Act
            boolean firstInvalidation = jwtService.invalidateToken(token);
            boolean secondInvalidation = jwtService.invalidateToken(token);

            // Assert
            assertTrue(firstInvalidation, "First invalidation should return true");
            assertTrue(secondInvalidation, "Second invalidation should also return true");
            assertFalse(jwtService.validateToken(token), "Token should remain invalid after second invalidation");
        }

        @Test
        @DisplayName("Token should remain invalid after multiple validations")
        void shouldRemainInvalidAfterMultipleValidations() {
            // Arrange
            String token = createDefaultToken(DEFAULT_USERNAME);

            // Act
            jwtService.invalidateToken(token);
            boolean firstValidation = jwtService.validateToken(token);
            boolean secondValidation = jwtService.validateToken(token);

            // Assert
            assertFalse(firstValidation, "Token should be invalid after invalidation");
            assertFalse(secondValidation, "Token should remain invalid on subsequent validations");
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Valid token should be validated successfully")
        void shouldValidateValidToken() {
            // Arrange
            String token = createDefaultToken(DEFAULT_USERNAME);

            // Act
            boolean isValid = jwtService.validateToken(token);

            // Assert
            assertTrue(isValid, "Valid token should be validated successfully");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"invalid.token.string"})
        @DisplayName("Invalid tokens should fail validation")
        void invalidTokensShouldFailValidation(String invalidToken) {
            // Act
            boolean isValid = jwtService.validateToken(invalidToken);

            // Assert
            assertFalse(isValid, "Invalid token should fail validation");
        }

        @Test
        @DisplayName("Tampered token should fail validation")
        void tamperedTokenShouldFailValidation() {
            // Arrange
            String token = createDefaultToken(DEFAULT_USERNAME);

            // Tamper with the token by changing a character in the middle
            int middleIndex = token.length() / 2;
            String tamperedToken = token.substring(0, middleIndex) +
                    (token.charAt(middleIndex) == 'a' ? 'b' : 'a') +
                    token.substring(middleIndex + 1);

            // Act
            boolean isValid = jwtService.validateToken(tamperedToken);

            // Assert
            assertFalse(isValid, "Tampered token should fail validation");
        }

        @Test
        @DisplayName("Expired token should fail validation")
        void expiredTokenShouldFailValidation() throws Exception {
            // Arrange - create short-lived token
            long originalExpiration = TEST_EXPIRATION;

            // Set very short expiration
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000);
            ReflectionTestUtils.invokeMethod(jwtService, "init");

            String token = createDefaultToken(DEFAULT_USERNAME);

            // Verify token is valid initially
            assertTrue(jwtService.validateToken(token), "Token should be valid immediately after generation");

            // Act - wait for token to expire
            Thread.sleep(1000);

            // Assert
            assertFalse(jwtService.validateToken(token), "Token should be invalid after expiration");

            // Restore original configuration
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", originalExpiration);
            ReflectionTestUtils.invokeMethod(jwtService, "init");
        }

        @Test
        @DisplayName("Expired blacklisted tokens should be cleaned up")
        void shouldCleanUpExpiredTokens() throws Exception {
            // Arrange - create short-lived token
            long originalExpiration = TEST_EXPIRATION;

            // Set very short expiration
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", 100); // 100ms expiration
            ReflectionTestUtils.invokeMethod(jwtService, "init");

            String token = createDefaultToken(DEFAULT_USERNAME);
            jwtService.invalidateToken(token);

            // Assert token is invalid immediately after invalidation
            assertFalse(jwtService.validateToken(token), "Token should be invalid after invalidation");

            // Act - wait for token to expire and trigger cleanup
            Thread.sleep(200); // Wait for token to expire (100ms + buffer)
            ReflectionTestUtils.invokeMethod(jwtService, "cleanUpExpiredTokens");

            // Assert - token should still be invalid
            assertFalse(jwtService.validateToken(token), "Token should remain invalid after expiration");

            // Restore original configuration
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", originalExpiration);
            ReflectionTestUtils.invokeMethod(jwtService, "init");
        }

        @Test
        @DisplayName("Multiple tokens should be validated independently")
        void multipleTokensShouldBeValidatedIndependently() {
            // Arrange
            String token1 = createDefaultToken("testUser1");
            String token2 = createDefaultToken("testUser2");

            // Act
            jwtService.invalidateToken(token1);

            // Assert
            assertFalse(jwtService.validateToken(token1), "Invalidated token should not be valid");
            assertTrue(jwtService.validateToken(token2), "Non-invalidated token should remain valid");
        }
    }
}