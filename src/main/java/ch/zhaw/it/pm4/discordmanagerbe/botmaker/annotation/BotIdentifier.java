package ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for identifying and categorizing Discord bot implementations.
 * Used to mark bot classes with their specific type and category for automatic registration and management.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BotIdentifier {

    /**
     * The slash command bot type this implementation provides.
     *
     * @return the slash command bot type, defaults to NONE
     */
    SlashCommandBotType slashCommand() default SlashCommandBotType.NONE;

    /**
     * The server bot type this implementation provides.
     *
     * @return the server bot type, defaults to NONE
     */
    ServerBotType server() default ServerBotType.NONE;

    /**
     * The primary category this bot belongs to for organizational purposes.
     *
     * @return the bot category (required)
     */
    BotCategory category();

    /**
     * Enumeration of bot categories for organizational and functional classification.
     */
    enum BotCategory {

        /**
         * Bot provides slash command functionality.
         */
        SLASH_COMMAND,

        /**
         * Bot provides server management functionality.
         */
        SERVER
    }
}

