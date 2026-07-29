package ch.zhaw.it.pm4.discordmanagerbe.auth.oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service handling Discord OAuth authentication flow including token exchange and user information retrieval.
 */
@Service
public class DiscordAuthService {

    /** Logger instance for this class. */
    private static final Logger logger = LoggerFactory.getLogger(DiscordAuthService.class);

    /** REST template for HTTP requests. */
    private final RestTemplate restTemplate;

    /** JSON object mapper for response parsing. */
    private final ObjectMapper objectMapper;

    /** Discord application client ID. */
    private final String clientId;

    /** Discord application client secret. */
    private final String clientSecret;

    /** OAuth redirect URI. */
    private final String redirectUri;

    /** Discord token exchange endpoint URL. */
    private final String discordTokenUrl;

    /** Discord user information endpoint URL. */
    private final String discordUserUrl;

    /**
     * Constructs DiscordAuthService with required dependencies and configuration.
     *
     * @param restTemplate REST template for HTTP requests
     * @param objectMapper JSON object mapper
     * @param clientId Discord application client ID
     * @param clientSecret Discord application client secret
     * @param redirectUri OAuth redirect URI
     * @param discordTokenUrl Discord token endpoint URL
     * @param discordUserUrl Discord user info endpoint URL
     */
    @Autowired
    public DiscordAuthService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${discord.client-id}") String clientId,
            @Value("${discord.client-secret}") String clientSecret,
            @Value("${discord.redirect-uri}") String redirectUri,
            @Value("${discord.auth-endpoints.token-url}") String discordTokenUrl,
            @Value("${discord.auth-endpoints.user-url}") String discordUserUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.discordTokenUrl = discordTokenUrl;
        this.discordUserUrl = discordUserUrl;
    }

    /**
     * Exchanges authorization code for Discord access token.
     *
     * @param code authorization code from OAuth flow
     * @return Discord access token
     * @throws IOException if parsing response fails
     * @throws RestClientException if Discord API call fails
     */
    public String exchangeCodeForToken(String code) throws IOException {
        logger.debug("Exchanging code for token with Discord");
        HttpEntity<MultiValueMap<String, String>> requestEntity = createTokenRequestEntity(code);
        ResponseEntity<String> tokenResponse = requestDiscordToken(requestEntity);
        String token = extractAccessToken(tokenResponse.getBody());
        logger.info("Successfully obtain Discord access token");
        return token;
    }

    /**
     * Retrieves user details from Discord API.
     *
     * @param accessToken Discord access token
     * @return user information from Discord
     * @throws IOException if parsing response fails
     * @throws RestClientException if Discord API call fails
     */
    public Map<String, Object> getUserInfo(String accessToken) throws IOException {
        logger.debug("Retrieving user info from Discord");
        HttpEntity<String> requestEntity = createUserInfoRequestEntity(accessToken);
        ResponseEntity<String> userInfoResponse = requestUserInfo(requestEntity);
        Map<String, Object> userInfo = parseUserInfo(userInfoResponse.getBody());
        logger.info("Successfully retrieved Discord user info for user: {}", userInfo.get("username"));
        return userInfo;
    }

    /**
     * Creates HTTP entity for token exchange request.
     *
     * @param code authorization code
     * @return HTTP entity with form data and headers
     */
    private HttpEntity<MultiValueMap<String, String>> createTokenRequestEntity(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("client_id", clientId);
        requestParams.add("client_secret", clientSecret);
        requestParams.add("grant_type", "authorization_code");
        requestParams.add("code", code);
        requestParams.add("redirect_uri", redirectUri);
        requestParams.add("scope", "identify email");

        return new HttpEntity<>(requestParams, headers);
    }

    /**
     * Creates HTTP entity for user info request.
     *
     * @param accessToken Discord access token
     * @return HTTP entity with authorization header
     */
    private HttpEntity<String> createUserInfoRequestEntity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(headers);
    }

    /**
     * Sends token exchange request to Discord.
     *
     * @param entity HTTP entity with request data
     * @return Discord API response
     * @throws RestClientException if request fails
     */
    private ResponseEntity<String> requestDiscordToken(HttpEntity<MultiValueMap<String, String>> entity) {
        try {
            return restTemplate.exchange(
                    discordTokenUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
        } catch (RestClientException e) {
            logger.error("Failed to exchange code for token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Sends user info request to Discord.
     *
     * @param entity HTTP entity with authorization header
     * @return Discord API response
     * @throws RestClientException if request fails
     */
    private ResponseEntity<String> requestUserInfo(HttpEntity<String> entity) {
        try {
            return restTemplate.exchange(
                    discordUserUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
        } catch (RestClientException e) {
            logger.error("Failed to get user info: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts access token from Discord response.
     *
     * @param responseBody JSON response from Discord
     * @return access token string
     * @throws IOException if JSON parsing fails
     */
    private String extractAccessToken(String responseBody) throws IOException {
        Map<String, String> tokenData = objectMapper.readValue(responseBody, new TypeReference<>() {});
        return tokenData.get("access_token");
    }

    /**
     * Parses user information from Discord response.
     *
     * @param responseBody JSON response from Discord
     * @return user information map
     * @throws IOException if JSON parsing fails
     */
    private Map<String, Object> parseUserInfo(String responseBody) throws IOException {
        return objectMapper.readValue(responseBody, new TypeReference<>() {});
    }
}