package ch.zhaw.it.pm4.discordmanagerbe.auth.service;

import ch.zhaw.it.pm4.discordmanagerbe.auth.jwt.JwtService;
import ch.zhaw.it.pm4.discordmanagerbe.auth.oauth.DiscordAuthService;
import ch.zhaw.it.pm4.discordmanagerbe.data.entities.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for user authentication operations.
 */
@Service
public class AuthenticationService {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    /** Service for handling Discord OAuth authentication. */
    private final DiscordAuthService discordAuthService;

    /** Service for managing and generating JWT tokens. */
    private final JwtService jwtService;

    /** Service for managing student-related operations. */
    private final StudentService studentService;

    /**
     * Constructs an instance of AuthenticationService with required dependencies.
     *
     * @param discordAuthService Service for handling Discord OAuth authentication
     * @param jwtService Service for managing and generating JWT tokens
     * @param studentService Service for managing student-related operations
     */
    @Autowired
    public AuthenticationService(
            DiscordAuthService discordAuthService,
            JwtService jwtService,
            StudentService studentService) {
        this.discordAuthService = discordAuthService;
        this.jwtService = jwtService;
        this.studentService = studentService;
    }

    /**
     * Processes Discord OAuth authentication.
     * @param code Discord authorization code
     * @return Authentication details with JWT token
     * @throws IOException If OAuth communication fails
     * @throws IllegalArgumentException If the provided code is null or empty
     * @throws IllegalStateException If required user information is missing from Discord response
     */
    public Map<String, String> authenticateWithDiscordCode(String code) throws IOException {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Discord authorization code cannot be null or empty");
        }

        String accessToken = discordAuthService.exchangeCodeForToken(code);

        Map<String, Object> userInfo = discordAuthService.getUserInfo(accessToken);

        if (!userInfo.containsKey("username") || !userInfo.containsKey("id") || !userInfo.containsKey("email")) {
            throw new IllegalStateException("Required user information missing from Discord response");
        }

        String username = (String) userInfo.get("username");
        String userId = (String) userInfo.get("id");
        String email = (String) userInfo.get("email");

        Student student = studentService.createStudentIfNotExists(userId, username, email);
        logger.info("Student with Discord ID: {} authenticated", userId);

        Map<String, Object> claims = createJwtClaims(userId);
        String jwtToken = jwtService.generateToken(username, claims);

        return createResponseMap(username, jwtToken);
    }

    /**
     * Creates JWT claims for Discord user.
     * @param userId Discord user ID
     * @return JWT claims
     */
    private Map<String, Object> createJwtClaims(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("discordId", userId);
        claims.put("source", "discord");
        return claims;
    }

    /**
     * Creates response map with authentication details.
     * @param username User's name
     * @param token JWT token
     * @return Response map
     */
    private Map<String, String> createResponseMap(String username, String token) {
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        return response;
    }
}