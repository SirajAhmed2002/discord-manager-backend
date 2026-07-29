package ch.zhaw.it.pm4.discordmanagerbe.botmaker;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.ServerBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing JDA Discord bot instances.
 * Handles creation, initialization and retrieval of both slash command and server bots.
 */
@Service
public class JdaBotService {

    /**
     * Set containing all registered bot entries.
     */
    private Set<JdaBotEntry> botList;

    /**
     * Factory for creating JDA bot instances.
     */
    private final JdaBotFactory jdaBotFactory;

    /**
     * Constructs a JdaBotService with the specified bot factory.
     *
     * @param jdaBotFactory the factory used to create bot instances
     */
    public JdaBotService(JdaBotFactory jdaBotFactory) {
        this.jdaBotFactory = jdaBotFactory;
    }

    /**
     * Initializes the service by creating all required bot instances.
     */
    @PostConstruct
    private void initialize(){
        this.botList = new HashSet<>();

        initServerBots();
        initSlashCommandBots();
    }

    /**
     * Initializes all slash command bot types.
     */
    private void initSlashCommandBots(){
        instantiateSlashCommandBot(SlashCommandBotType.MUSIC); // MusicBot
        instantiateSlashCommandBot(SlashCommandBotType.TRANSCRIPTION); // NotenRechnerJdaBot, ScheduleJdaBot, etc.
        instantiateSlashCommandBot(SlashCommandBotType.TIMETABLE); // TranscribeBot, ToDoTrackerJdaBot
        instantiateSlashCommandBot(SlashCommandBotType.TODO);
        instantiateSlashCommandBot(SlashCommandBotType.GRADE_CALCULATOR);
    }

    /**
     * Initializes all server bot types.
     */
    private void initServerBots(){
        instantiateServerBot(ServerBotType.GUILD_CONFIG);
        instantiateServerBot(ServerBotType.GUILD_LIST);
        instantiateServerBot(ServerBotType.GUILD_INFO);
        instantiateServerBot(ServerBotType.GUILD_MEMBER_LIST);
        instantiateServerBot(ServerBotType.GUILD_INVITE_CREATE);
        instantiateServerBot(ServerBotType.GUILD_PERMISSION_LIST);
        instantiateServerBot(ServerBotType.GUILD_ROLES_CONFIG);
        instantiateServerBot(ServerBotType.GUILD_ROLES_LIST);
        instantiateServerBot(ServerBotType.GUILD_MEMBER_ROLES_CONFIG);
        instantiateServerBot(ServerBotType.GUILD_CHANNEL_ROLE_PERMISSION);
    }

    /**
     * Creates and registers a slash command bot of the specified type.
     *
     * @param botType the type of slash command bot to create
     */
    public void instantiateSlashCommandBot(SlashCommandBotType botType) {
        botList.add(jdaBotFactory.createSlashCommandBot(botType));
    }

    /**
     * Creates and registers a server bot of the specified type.
     *
     * @param botType the type of server bot to create
     */
    public void instantiateServerBot(ServerBotType botType) {
        botList.add(jdaBotFactory.createServerBot(botType));
    }

    /**
     * Retrieves a slash command bot by its type.
     *
     * @param botType the type of slash command bot to retrieve
     * @return an Optional containing the bot if found, empty otherwise
     */
    public Optional<JdaBotEntry> getBot(SlashCommandBotType botType) {
        return botList.stream()
                .filter(bot -> bot.getIdentifier().equals(botType.toString()))
                .findFirst();
    }

    /**
     * Retrieves a server bot by its type.
     *
     * @param botType the type of server bot to retrieve
     * @return an Optional containing the bot if found, empty otherwise
     */
    public Optional<JdaBotEntry> getBot(ServerBotType botType) {
        return botList.stream()
                .filter(bot -> bot.getIdentifier().equals(botType.toString()))
                .findFirst();
    }

    /**
     * Returns all registered slash command bots.
     *
     * @return a set containing all slash command bots
     */
    public Set<JdaBotEntry> getAllSlashCommandBots() {
        return botList.stream()
                .filter(bot -> isSlashCommandBot(bot))
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    /**
     * Returns all registered server bots.
     *
     * @return a set containing all server bots
     */
    public Set<JdaBotEntry> getAllServerBots() {
        return botList.stream()
                .filter(bot -> isServerBot(bot))
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    /**
     * Checks if a bot entry is a slash command bot.
     *
     * @param bot the bot entry to check
     * @return true if the bot is a slash command bot, false otherwise
     */
    private boolean isSlashCommandBot(JdaBotEntry bot) {
        try {
            SlashCommandBotType.valueOf(bot.getIdentifier());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks if a bot entry is a server bot.
     *
     * @param bot the bot entry to check
     * @return true if the bot is a server bot, false otherwise
     */
    private boolean isServerBot(JdaBotEntry bot) {
        try {
            ServerBotType.valueOf(bot.getIdentifier());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns all registered bots.
     *
     * @return a set containing all bot entries
     */
    public Set<JdaBotEntry> getAllBots() {
        return new HashSet<>(botList);
    }
}