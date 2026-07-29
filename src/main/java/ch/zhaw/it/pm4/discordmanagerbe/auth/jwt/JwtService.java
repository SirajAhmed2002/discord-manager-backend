package ch.zhaw.it.pm4.discordmanagerbe.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for JWT token operations.
 */
@Service
public class JwtService {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    /** Secret key string used for signing and verifying JWT tokens. */
    private final String jwtSecretString;

    /** Expiration time for JWT tokens in milliseconds. */
    private final long jwtExpiration;

    /** Interval for cleaning up expired tokens from the blacklist in milliseconds. */
    private final long cleanupInterval;

    /** Secret key derived from the secret string for HMAC signing. */
    private SecretKey jwtSecretKey;

    /** Thread-safe map for storing blacklisted tokens and their expiration dates. */
    private final Map<String, Date> blacklistedTokens = Collections.synchronizedMap(new HashMap<>());

    /** Scheduler for periodically cleaning up expired tokens from the blacklist. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Initializes JWT service with configuration.
     */
    public JwtService(
            @Value("${jwt.secret}") String jwtSecretString,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${jwt.cleanup-interval}") long cleanupInterval) {
        this.jwtSecretString = jwtSecretString;
        this.jwtExpiration = jwtExpiration;
        this.cleanupInterval = cleanupInterval;
    }

    /**
     * Sets up JWT components and cleanup scheduler.
     */
    @PostConstruct
    private void init() {
        initializeSecretKey();
        scheduleTokenCleanup();
        logger.info("JWT Service initialized with cleanup interval of {} ms", cleanupInterval);
    }

    /**
     * Creates a new JWT token for the user.
     * @param username User identifier
     * @param additionalClaims Extra information to include in token
     * @return Signed JWT token
     */
    public String generateToken(String username, Map<String, Object> additionalClaims) {
        Date now = new Date();
        Date expiryDate = calculateExpiryDate(now);

        String token = Jwts.builder()
                .claims(additionalClaims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();

        logger.debug("Generated JWT token for user: {}, expires at: {}", username, expiryDate);
        return token;
    }

    /**
     * Extracts username from token.
     * @param token JWT token
     * @return Username from token
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Adds token to blacklist.
     * @param token JWT token to invalidate
     * @return true if token was invalidated, false otherwise
     */
    public boolean invalidateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiryDate = claims.getExpiration();
            blacklistedTokens.put(token, expiryDate);

            String username = claims.getSubject();
            logger.info("Token invalidated for user: {}", username);
            return true;
        } catch (Exception ex) {
            logger.warn("Failed to invalidate token: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Checks if token is valid.
     * @param token JWT token to validate
     * @return True if token is valid
     */
    public boolean validateToken(String token) {
        try {
            if (blacklistedTokens.containsKey(token)) {
                return false;
            }
            Claims claims = parseClaims(token);
            logger.debug("Token validated successfully for user: {}", claims.getSubject());
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.debug("Token validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extracts claims from token.
     *
     * @param token JWT token to parse
     * @return claims from the token
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build().parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Removes expired tokens from blacklist.
     */
    private void cleanUpExpiredTokens() {
        Date now = new Date();
        int initialSize = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));

        int removedCount = initialSize - blacklistedTokens.size();
        if (removedCount > 0) {
            logger.info("Cleanup completed: removed {} expired tokens from blacklist", removedCount);
        }
    }

    /**
     * Calculates token expiration date.
     *
     * @param issuedAt token issue date
     * @return calculated expiration date
     */
    private Date calculateExpiryDate(Date issuedAt) {
        return new Date(issuedAt.getTime() + jwtExpiration);
    }

    /**
     * Initializes HMAC secret key.
     */
    private void initializeSecretKey() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sets up periodic token cleanup.
     */
    private void scheduleTokenCleanup() {
        scheduler.scheduleAtFixedRate(this::cleanUpExpiredTokens, 1,
                TimeUnit.MICROSECONDS.toSeconds(cleanupInterval), TimeUnit.SECONDS);
    }

    /**
     * Shuts down the scheduler.
     */
    @PreDestroy
    private void cleanup() {
        scheduler.shutdown();
        logger.info("JWT Service cleanup scheduler shutdown");
    }
}