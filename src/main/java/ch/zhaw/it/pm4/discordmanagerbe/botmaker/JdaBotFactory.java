package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating JDA Discord bot instances.
 * Scans for bot implementations and provides type-safe creation methods.
 */
@Component
public class JdaBotFactory {

    /**
     * Spring application context for bean retrieval.
     */
    private final ApplicationContext context;

    /**
     * Map storing bot classes indexed by category and identifier.
     */
    private final Map<BotKey, Class<? extends AbstractJdaBot>> botMap = new HashMap<>();

    /**
     * Key record for identifying bots by category and identifier.
     *
     * @param category the bot category (SLASH_COMMAND or SERVER)
     * @param identifier the unique bot identifier
     */
    private record BotKey(BotIdentifier.BotCategory category, String identifier) {}

    /**
     * Constructs a JdaBotFactory with the specified application context.
     *
     * @param context the Spring application context
     */
    @Autowired
    public JdaBotFactory(@Lazy ApplicationContext context) {
        this.context = context;
    }

    /**
     * Initializes the factory by registering all available bot implementations.
     */
    @PostConstruct
    public void initialize() {
        registerBots();
    }

    /**
     * Scans the application context for bot implementations and registers them.
     */
    private void registerBots() {
        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(BotIdentifier.class);

        for (Object bean : annotatedBeans.values()) {
            Class<?> beanClass = bean.getClass();
            BotIdentifier annotation = beanClass.getAnnotation(BotIdentifier.class);

            if (annotation != null) {
                @SuppressWarnings("unchecked")
                Class<? extends AbstractJdaBot> botClazz = (Class<? extends AbstractJdaBot>) beanClass;

                String identifier = switch (annotation.category()) {
                    case SLASH_COMMAND -> annotation.slashCommand().toString();
                    case SERVER -> annotation.server().toString();
                };

                BotKey key = new BotKey(annotation.category(), identifier);
                botMap.put(key, botClazz);
            }
        }
    }

    /**
     * Creates a slash command bot instance of the specified type.
     *
     * @param botType the type of slash command bot to create
     * @return a JdaBotEntry containing the bot instance
     */
    public JdaBotEntry createSlashCommandBot(SlashCommandBotType botType) {
        return createBot(BotIdentifier.BotCategory.SLASH_COMMAND, botType.toString());
    }

    /**
     * Creates a server bot instance of the specified type.
     *
     * @param botType the type of server bot to create
     * @return a JdaBotEntry containing the bot instance
     */
    public JdaBotEntry createServerBot(ServerBotType botType) {
        return createBot(BotIdentifier.BotCategory.SERVER, botType.toString());
    }

    /**
     * Creates a bot instance for the given category and identifier.
     *
     * @param category the bot category
     * @param identifier the bot identifier
     * @return a JdaBotEntry containing the bot instance
     * @throws IllegalArgumentException if no bot is found for the given parameters
     */
    private JdaBotEntry createBot(BotIdentifier.BotCategory category, String identifier) {
        BotKey key = new BotKey(category, identifier);
        Class<? extends AbstractJdaBot> botClazz = botMap.get(key);

        if (botClazz == null) {
            throw new IllegalArgumentException("No bot found for category: " + category + ", identifier: " + identifier);
        }

        return new JdaBotEntry(identifier, botClazz, context.getBean(botClazz));
    }
}