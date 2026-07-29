package ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractSlashCommandJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.state.*;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.BotMessages;
import ch.zhaw.it.pm4.discordmanagerbe.bots.transcribe.utility.MessageKey;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Discord bot for voice channel transcription with state-based workflow.
 * Supports joining voice channels, recording audio with user consent, and managing permissions.
 * Uses the State pattern to handle different operational phases.
 */
@Component
@BotIdentifier(category = BotIdentifier.BotCategory.SLASH_COMMAND,
        slashCommand = SlashCommandBotType.TRANSCRIPTION)
public class TranscribeBot extends AbstractSlashCommandJdaBot {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(TranscribeBot.class);

    /** Current operational state of the bot */
    private BotState currentState;

    /** State when bot is not connected to any voice channel */
    private final DisconnectedState disconnectedState;

    /** State when bot is connected but not recording */
    private final ConnectedState connectedState;

    /** State when waiting for user recording consent */
    private final PendingApprovalState pendingApprovalState;

    /** State when actively recording audio */
    private final RecordingState recordingState;

    /** Maps channel IDs to sets of user IDs awaiting recording consent */
    private final Map<String, Set<String>> pendingAcceptances = new HashMap<>();

    /** Maps channel IDs to sets of user IDs who have given recording consent */
    private final Map<String, Set<String>> acceptedUsers = new HashMap<>();

    /** Maps channel IDs to active recording sessions */
    private final Map<String, RecordingSession> activeSessions = new HashMap<>();

    /** ID of the user who first used the bot, grants admin privileges */
    private String botOwnerId;

    /** Current audio manager for voice channel connection */
    private AudioManager activeAudioManager;

    /**
     * Constructor for the TranscribeBot.
     * Initializes the various states of the bot and sets the initial state to "disconnected".
     *
     * @param jdaBean The JDA instance used for interacting with the Discord API.
     * @param slashCommandService The service for registering and managing slash commands.
     * @param slashCommandListener The listener for slash command events.
     * @param applicationContext The Spring ApplicationContext used for initializing dependent beans.
     */
    @Autowired
    public TranscribeBot(JDA jdaBean,
                         JdaSlashCommandService slashCommandService,
                         JdaEventListenerService slashCommandListener,
                         ApplicationContext applicationContext) {
        super(jdaBean, slashCommandService, slashCommandListener);
        setBotType(this.getClass().getAnnotation(BotIdentifier.class).slashCommand());
        this.disconnectedState = new DisconnectedState(this);
        this.connectedState = new ConnectedState(this);
        this.pendingApprovalState = new PendingApprovalState(this, applicationContext);
        this.recordingState = new RecordingState(this);

        this.currentState = disconnectedState;
        setDescription("Transcribe Bot");
    }

    /**
     * setup all slash commands.
     */
    @Override
    public void setupCommands() {
        SlashCommandData createSubjectCommand = createCommand("join-channel", "Bot mit deinem aktuellen Voice-Channel verbinden");
        registerCommand(
                "join-channel", "Bot mit deinem aktuellen Voice-Channel verbinden",
                createSubjectCommand, this::handleJoinChannel);

        createSubjectCommand = createCommand("leave-channel", "Bot vom Voice-Channel trennen");
        registerCommand(
                "leave-channel", "Bot vom Voice-Channel trennen",
                createSubjectCommand, this::handleLeaveChannel);

        createSubjectCommand = createCommand("lock-channel", "Channel sperren (notwendig vor der Aufnahme)");
        registerCommand(
                "lock-channel", "Channel sperren (notwendig vor der Aufnahme)",
                createSubjectCommand, this::handleLockChannel);

        createSubjectCommand = createCommand("unlock-channel", "Channel entsperren");
        registerCommand(
                "unlock-channel", "Channel entsperren",
                createSubjectCommand, this::handleUnlockChannel);

        createSubjectCommand = createCommand("accept-recording", "Der Aufnahme zustimmen");
        registerCommand(
                "accept-recording", "Der Aufnahme zustimmen",
                createSubjectCommand, this::handleAcceptRecording);

        createSubjectCommand = createCommand("start-recording", "Aufnahme starten (Zustimmung aller Teilnehmer erforderlich)");
        registerCommand(
                "start-recording", "Aufnahme starten (Zustimmung aller Teilnehmer erforderlich)",
                createSubjectCommand, this::handleStartRecording);

        createSubjectCommand = createCommand("stop-recording", "Aufnahme beenden und Dateien speichern");
        registerCommand(
                "stop-recording", "Aufnahme beenden und Dateien speichern",
                createSubjectCommand, this::handleStopRecording);

        createSubjectCommand = createCommand("help", "Liste aller Befehle anzeigen");
        registerCommand(
                "help", "Liste aller Befehle anzeigen",
                createSubjectCommand, this::handleHelp
        );
    }

    /**
     * Registers handlers for button interactions.
     * This method is overridden but currently does not handle any button interactions.
     */
    @Override
    protected void registerButtonInteractionHandlers() {
        // No button interactions to handle
    }

    /**
     * Registers handlers for string interactions.
     * This method is overridden but currently does not handle any string interactions.
     */
    @Override
    protected void registerStringInteractionHandlers() {
        // No string interactions to handle
    }

    /**
     * Registers handlers for modal interactions.
     * This method is overridden but currently does not handle any modal interactions.
     */
    @Override
    protected void registerModalInteractionHandlers() {
        // No modal interactions to handle
    }

    /**
     * Handles the join-channel command, connecting the bot to the user's current voice channel.
     *
     * @param event The slash command event
     */
    private void handleJoinChannel(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> {
            Member member = event.getMember();
            if (botOwnerId == null) {
                botOwnerId = member.getId();
                logger.info("Bot owner set to: {} ({})", member.getEffectiveName(), botOwnerId);
            }

            if (checkPermission(event)) {
                currentState.joinVoiceChannel(event);
            }
        });
    }

    /**
     * Handles the leave-channel command, disconnecting the bot from the voice channel.
     *
     * @param event The slash command event
     */
    private void handleLeaveChannel(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> {
            if (checkPermission(event)) {
                currentState.leaveVoiceChannel(event);
            }
        });
    }

    /**
     * Handles the lock-channel command, preventing other users from joining the voice channel.
     *
     * @param event The slash command event
     */
    private void handleLockChannel(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> {
            if (checkPermission(event)) {
                currentState.lockVoiceChannel(event);
            }
        });
    }

    /**
     * Handles the unlock-channel command, allowing other users to join the voice channel.
     *
     * @param event The slash command event
     */
    private void handleUnlockChannel(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> {
            if (checkPermission(event)) {
                currentState.unlockVoiceChannel(event);
            }
        });
    }

    /**
     * Handles the accept-recording command, allowing users to give consent for recording.
     *
     * @param event The slash command event
     */
    private void handleAcceptRecording(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> currentState.handleAcceptRecording(event));
    }

    /**
     * Handles the start-recording command, initiating the audio recording process.
     *
     * @param event The slash command event
     */
    private void handleStartRecording(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> {
            if (checkPermission(event)) {
                currentState.startRecording(event);
            }
        });
    }

    /**
     * Handles the stop-recording command, ending the recording and saving the files.
     *
     * @param event The slash command event
     */
    private void handleStopRecording(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> {
            if (checkPermission(event)) {
                currentState.stopRecording(event);
            }
        });
    }

    /**
     * Handles the help command, displaying available commands to the user.
     *
     * @param event The slash command event
     */
    private void handleHelp(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        
        CompletableFuture.runAsync(() -> {
            sendHelpMessage(event);
        });
    }

    /**
     * Verifies if the user has permission to execute the command.
     * @param event The slash command event
     * @return True if the user has permission, false otherwise
     */
    private boolean checkPermission(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (botOwnerId != null && !member.getId().equals(botOwnerId)) {
            event.getHook().sendMessage("Nur der Bot-Owner kann diesen Befehl ausführen.")
                    .setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    /**
     * Sends a help message listing all available commands.
     * @param event The slash command event
     */
    private void sendHelpMessage(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage(BotMessages.get(MessageKey.HELP_MESSAGE)).queue();
    }

    /**
     * Changes the current state of the bot.
     * @param state The new state to set
     */
    public void setState(BotState state) {
        this.currentState = state;
        logger.info("TranscribeBot changed to state: {}", state.getClass().getSimpleName());
    }

    /**
     * Returns the state when the bot is not connected to any voice channel.
     * @return The disconnected state of the bot.
     */
    public DisconnectedState getDisconnectedState() {
        return disconnectedState;
    }

    /**
     * Returns the state when the bot is connected to a voice channel but not recording.
     * @return The connected state of the bot.
     */
    public ConnectedState getConnectedState() {
        return connectedState;
    }

    /**
     * Returns the state when the bot is waiting for user consent to start recording.
     * @return The pending approval state of the bot.
     */
    public PendingApprovalState getPendingApprovalState() {
        return pendingApprovalState;
    }

    /**
     * Returns the state when the bot is actively recording audio.
     * @return The recording state of the bot.
     */
    public RecordingState getRecordingState() {
        return recordingState;
    }

    /**
     * Returns a map of channel IDs to sets of user IDs awaiting recording consent.
     * @return A map of pending acceptances.
     */
    public Map<String, Set<String>> getPendingAcceptances() {
        return pendingAcceptances;
    }

    /**
     * Returns a map of channel IDs to sets of user IDs who have given recording consent.
     * @return A map of accepted users.
     */
    public Map<String, Set<String>> getAcceptedUsers() {
        return acceptedUsers;
    }

    /**
     * Returns a map of channel IDs to active recording sessions.
     * @return A map of active recording sessions.
     */
    public Map<String, RecordingSession> getActiveSessions() {
        return activeSessions;
    }

    /**
     * Returns the current audio manager for the bot's voice channel connection.
     * @return The active audio manager.
     */
    public AudioManager getActiveAudioManager() {
        return activeAudioManager;
    }

    /**
     * Sets the current audio manager for the bot's voice channel connection.
     * @param audioManager The audio manager to set.
     */
    public void setActiveAudioManager(AudioManager audioManager) {
        this.activeAudioManager = audioManager;
    }

    /**
     * Interface defining a recording session.
     */
    public interface RecordingSession {

        /**
         * Stops the recording and returns a result message.
         *
         * @param channel The message channel where results will be reported
         */
        void stopRecording(MessageChannel channel);
    }
}