package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for JDA Discord bot setup.
 * Configures the JDA instance with required gateway intents and token.
 */
@Configuration
public class JdaBotConfiguration {

    /**
     * Discord bot token retrieved from application properties.
     */
    @Value("${token}")
    private String token;

    /**
     * Creates and configures a JDA instance as a Spring bean.
     * Enables necessary gateway intents for bot functionality including
     * guild moderation, member management, voice states, and message handling.
     *
     * @return configured JDA instance ready for bot operations
     */
    @Bean
    public JDA jdaBean(){
        return JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES
                )
                .build();
    }
}
