package ch.zhaw.it.pm4.discordmanagerbe.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * Filter for JWT-based authentication.
 * Checks incoming requests for valid JWT tokens and authenticates users.
 * Public paths are allowed through without authentication.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Logger instance for logging events and debugging information. */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** Service for handling and validating JWT tokens. */
    private final JwtService jwtService;

    /** Utility for matching request paths against patterns. */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** List of public paths that don't require authentication. */
    private final List<String> publicPaths;

    /**
     * Constructor for the JwtAuthenticationFilter.
     *
     * @param jwtService Service for validating and processing JWT tokens
     */
    @Autowired
    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Value("${security.public-paths}") String publicPathsConfig) {
        this.jwtService = jwtService;
        this.publicPaths = Arrays.asList(publicPathsConfig.split(","));
    }

    /**
     * Main filter method called for each request.
     * Checks if the request needs authentication and performs authentication if necessary.
     *
     * @param request The incoming HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain for further processing
     * @throws ServletException For servlet-specific errors
     * @throws IOException For input/output errors
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);

        if (token != null && jwtService.validateToken(token)) {
            String username = jwtService.getUsernameFromToken(token);

            Claims claims = jwtService.parseClaims(token);
            Map<String, Object> detailsMap = new HashMap<>(claims);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            auth.setDetails(detailsMap);

            SecurityContextHolder.getContext().setAuthentication(auth);
            logger.debug("User authenticated: {}", username);
        } else if (token != null) {
            logger.debug("Invalid token provided");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the specified path is one of the public paths.
     *
     * @param requestPath The path to check
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicPath(String requestPath) {
        return publicPaths.stream().anyMatch(path -> pathMatcher.match(path, requestPath));
    }

    /**
     * Extracts the JWT token from the request.
     * Checks the Authorization header first, then the cookies.
     *
     * @param request The HTTP request
     * @return The extracted token or null if no token was found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}