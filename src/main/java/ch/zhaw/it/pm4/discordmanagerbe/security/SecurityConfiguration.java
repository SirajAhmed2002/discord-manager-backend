package ch.zhaw.it.pm4.discordmanagerbe.security;

import ch.zhaw.it.pm4.discordmanagerbe.auth.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for Spring Security settings.
 * Sets up security filters, CORS configuration, and authentication rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /** Comma-separated list of allowed origins for CORS. */
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /** Comma-separated list of allowed HTTP methods for CORS. */
    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    /** Comma-separated list of allowed HTTP headers for CORS. */
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    /**
     * Configures the security filter chain.
     * Sets up CSRF, CORS, authorization rules, and JWT authentication.
     *
     * @param http The HttpSecurity to modify
     * @param jwtAuthenticationFilter The JWT authentication filter to add
     * @return The built SecurityFilterChain
     * @throws Exception If an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/callback").permitAll()
                        .requestMatchers("/auth/discord-login").permitAll()
                        .requestMatchers("/auth/check").permitAll()
                        .requestMatchers("/auth/logout").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Creates a CORS configuration source based on application properties.
     *
     * @return The configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseCommaSeparatedList(allowedOrigins));
        configuration.setAllowedMethods(parseCommaSeparatedList(allowedMethods));

        if ("*".equals(allowedMethods)) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            configuration.setAllowedHeaders(parseCommaSeparatedList(allowedHeaders));
        }

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Utility method to parse comma-separated strings into lists.
     *
     * @param commaSeparatedList The comma-separated string to parse
     * @return A list of strings split by comma
     */
    private List<String> parseCommaSeparatedList(String commaSeparatedList) {
        return Arrays.asList(commaSeparatedList.split(","));
    }
}