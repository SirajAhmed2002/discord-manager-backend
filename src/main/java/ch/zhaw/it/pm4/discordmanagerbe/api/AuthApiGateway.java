package ch.zhaw.it.pm4.discordmanagerbe.api;

import ch.zhaw.it.pm4.discordmanagerbe.auth.service.AuthenticationService;
import ch.zhaw.it.pm4.discordmanagerbe.auth.jwt.JwtService;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Student;
import ch.zhaw.it.pm4.discordmanagerbe.dto.UserInfoDto;
import ch.zhaw.it.pm4.discordmanagerbe.auth.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller handling authentication-related endpoints for Discord OAuth integration.
 * Manages user login, logout, authentication status checks, and user information retrieval.
 */
@RestController
@RequestMapping("/auth")
public class AuthApiGateway {

    /** Logger instance for this class. */
    private static final Logger logger = LoggerFactory.getLogger(AuthApiGateway.class);

    /** Discord application client ID. */
    private final String clientId;

    /** OAuth redirect URI for Discord callbacks. */
    private final String redirectUri;

    /** OAuth scopes requested from Discord. */
    private final String oauthScopes;

    /** Frontend URL for post-authentication redirects. */
    private final String frontendRedirectUrl;

    /** Discord OAuth authorization URL. */
    private final String discordOauthUrl;

    /** Service handling authentication logic. */
    private final AuthenticationService authenticationService;

    /** Service for JWT token operations. */
    private final JwtService jwtService;

    /** Service for student data operations. */
    private final StudentService studentService;

    /**
     * Constructs AuthApiGateway with required services and configuration values.
     *
     * @param authenticationService service for authentication operations
     * @param clientId Discord application client ID
     * @param redirectUri OAuth redirect URI
     * @param oauthScopes OAuth scopes to request
     * @param frontendRedirectUrl frontend URL for redirects
     * @param discordOauthUrl Discord OAuth URL
     * @param jwtService JWT token service
     * @param studentService student data service
     */
    @Autowired
    public AuthApiGateway(
            AuthenticationService authenticationService,
            @Value("${discord.client-id}") String clientId,
            @Value("${discord.redirect-uri}") String redirectUri,
            @Value("${discord.oauth.scopes}") String oauthScopes,
            @Value("${frontend.redirect-url}") String frontendRedirectUrl,
            @Value("${discord.auth-endpoints.oauth-url}") String discordOauthUrl,
            JwtService jwtService,
            StudentService studentService) {
        this.authenticationService = authenticationService;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.oauthScopes = oauthScopes;
        this.frontendRedirectUrl = frontendRedirectUrl;
        this.discordOauthUrl = discordOauthUrl;
        this.jwtService = jwtService;
        this.studentService = studentService;
    }

    /**
     * Generates Discord OAuth login URL.
     *
     * @return response containing Discord login URL
     */
    @GetMapping("/discord-login")
    public ResponseEntity<String> getDiscordLoginUrl() {
        String redirectUri = getEncodedRedirectUri();
        String discordLoginUrl = buildDiscordLoginUrl(redirectUri);
        logger.debug("Generated Discord login URL with redirect to: {}", redirectUri);
        return ResponseEntity.ok(discordLoginUrl);
    }

    /**
     * Handles OAuth callback from Discord.
     * @param code Authorization code from Discord
     * @return Redirect to frontend with authentication cookie
     */
    @GetMapping("/callback")
    public ResponseEntity<String> handleDiscordCallback(@RequestParam String code) {
        try {
            logger.info("Received Discord callback with auth code");
            Map<String, String> authResponse = authenticationService.authenticateWithDiscordCode(code);
            String token = authResponse.get("token");
            String username = authResponse.get("username");
            logger.info("Authentication successful for user: {}", username);
            return createSuccessRedirectResponse(token);
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Authentication error: " + e.getMessage());
        }
    }

    /**
     * Verifies user authentication status.
     * @param token JWT token from cookie
     * @return Authentication status
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkAuthStatus(
            @CookieValue(name = "auth_token", required = false) String token) {

        Map<String, Boolean> response = new HashMap<>();
        boolean isAuthenticated = (token != null) && jwtService.validateToken(token);
        response.put("authenticated", isAuthenticated);
        logger.debug("Auth check: {}", isAuthenticated ? "authenticated" : "not authenticated");
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves current user information from database.
     * Spring Security garantiert, dass Authentication nicht null ist.
     * @param authentication Automatically injected by Spring Security
     * @return User data or not found response
     */
    @GetMapping("/user")
    public ResponseEntity<UserInfoDto> getCurrentUser(Authentication authentication) {
        String discordId = getDiscordIdFromAuth(authentication);

        Optional<Student> studentOpt = studentService.findStudentByDiscordId(discordId);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            UserInfoDto userInfo = new UserInfoDto(
                    student.getUsername(),
                    student.getDiscordId(),
                    student.getEmail()
            );
            logger.debug("Retrieved user info from database for Discord ID: {}", discordId);
            return ResponseEntity.ok(userInfo);
        } else {
            logger.warn("No student found for Discord ID: {}", discordId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Logs out the current user.
     * @param token JWT token from cookie
     * @return Logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(
            @CookieValue(name = "auth_token", required = false) String token) {

        if (token != null && !token.isEmpty()) {
            boolean invalidated = jwtService.invalidateToken(token);
            if (invalidated) {
                logger.info("User logged out successfully");
            } else {
                logger.warn("Failed to invalidate token during logout");
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createExpiredCookie().toString())
                .body("Logged out successfully");
    }

    /**
     * Extracts Discord ID from Spring Security authentication object.
     *
     * @param auth authentication object
     * @return Discord ID or empty string if not found
     */
    private String getDiscordIdFromAuth(Authentication auth) {
        if (auth == null) {
            return "";
        }

        Object details = auth.getDetails();
        if (details instanceof Map) {
            Object discordId = ((Map<?, ?>) details).get("discordId");
            return discordId != null ? discordId.toString() : "";
        }
        return "";
    }

    /**
     * URL-encodes the redirect URI for OAuth requests.
     *
     * @return encoded redirect URI
     */
    private String getEncodedRedirectUri() {
        return URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }

    /**
     * Constructs complete Discord OAuth URL with required parameters.
     *
     * @param redirectUri encoded redirect URI
     * @return complete Discord OAuth URL
     */
    private String buildDiscordLoginUrl(String redirectUri) {
        return discordOauthUrl
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(oauthScopes, StandardCharsets.UTF_8);
    }

    /**
     * Creates HTTP response with authentication cookie and redirect location.
     *
     * @param token JWT token to set in cookie
     * @return redirect response with authentication cookie
     */
    private ResponseEntity<String> createSuccessRedirectResponse(String token) {
        HttpCookie authCookie = createAuthCookie(token);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .header(HttpHeaders.LOCATION, frontendRedirectUrl)
                .build();
    }

    /**
     * Creates HTTP cookie containing authentication token.
     *
     * @param token JWT token value
     * @return configured HTTP cookie
     */
    private HttpCookie createAuthCookie(String token) {
        return ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .secure(false) // Allow HTTP
                .path("/")
                .maxAge(3600)
                .build();
    }

    /**
     * Creates expired HTTP cookie for logout operations.
     *
     * @return expired authentication cookie
     */
    private HttpCookie createExpiredCookie() {
        return ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false) // Allow HTTP
                .path("/")
                .maxAge(0)
                .build();
    }
}