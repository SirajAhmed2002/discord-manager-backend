package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

/**
 * Container class holding information about a JDA Discord bot instance.
 * Stores the bot's identifier, implementation class, and runtime instance.
 */
public class JdaBotEntry {

    /** Unique identifier for the bot */
    private final String identifier;

    /** Class type of the bot implementation. */
    private final Class<? extends AbstractJdaBot> botClass;

    /** Runtime instance of the bot. */
    private final Object botInstance;

    /**
     * Constructor for the BotEntry class.
     * @param identifier The identifier for the bot.
     * @param botClass The class of the bot.
     * @param botInstance The instance of the bot.
     */
    public JdaBotEntry(String identifier, Class<? extends AbstractJdaBot> botClass, Object botInstance) {
        this.identifier = identifier;
        this.botClass = botClass;
        this.botInstance = botInstance;
    }

    /**
     * Getter for the identifier.
     * @return The identifier of the bot.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Getter for the bot class.
     * @return The class of the bot.
     */
    public Class<? extends AbstractJdaBot> getBotClass() {
        return botClass;
    }

    /**
     * Getter for the bot instance.
     * @return The instance of the bot.
     */
    public Object getBotInstance() {
        return botInstance;
    }
}
