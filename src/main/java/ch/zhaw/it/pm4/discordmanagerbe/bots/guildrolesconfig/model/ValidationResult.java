package ch.zhaw.it.pm4.discordmanagerbe.bots.guildrolesconfig.model;

import net.dv8tion.jda.api.entities.Guild;

/**
 * Result of role sync validation.
 */
public class ValidationResult {

    /**
     * Indicates whether the validation was successful.
     */
    private final boolean valid;

    /**
     * Error message if validation failed.
     */
    private final String errorMessage;

    /**
     * The guild associated with the validation result.
     */
    private final Guild guild;

    /**
     * Private constructor to create a ValidationResult instance.
     * @param valid indicates whether the validation was successful
     * @param errorMessage provides additional information about the validation result
     * @param guild the guild associated with the validation result
     */
    private ValidationResult(boolean valid, String errorMessage, Guild guild) {
        this.valid = valid;
        this.errorMessage = errorMessage;
        this.guild = guild;
    }

    /**
     * Checks if the validation was successful.
     * @return true if validation was successful, false otherwise
     */
    public boolean isValid() { 
        return valid; 
    }

    /**
     * Gets the error message if validation failed.
     * @return the error message, or null if validation was successful
     */
    public String getErrorMessage() { 
        return errorMessage; 
    }

    /**
     * Gets the guild associated with the validation result.
     * @return the guild, or null if not applicable
     */
    public Guild getGuild() { 
        return guild; 
    }

    /**
     * Creates a successful ValidationResult.
     * @param guild the guild associated with the validation result
     * @return a new ValidationResult instance indicating success
     */
    public static ValidationResult success(Guild guild) {
        return new ValidationResult(true, null, guild);
    }

    /**
     * Creates a failed ValidationResult with an error message.
     * @param message the error message to include in the result
     * @return a new ValidationResult instance indicating failure
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message, null);
    }
}