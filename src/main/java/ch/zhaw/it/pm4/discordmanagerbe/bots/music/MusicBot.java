package ch.zhaw.it.pm4.discordmanagerbe.bots.music;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractSlashCommandJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * MusicBot is a Discord bot that provides music playback functionality.
 * It allows users to play, pause, resume, skip tracks, manage queues, and control volume.
 * The bot uses Lavaplayer for audio handling and JDA for Discord interactions.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SLASH_COMMAND,
        slashCommand = SlashCommandBotType.MUSIC)
@Component
public class MusicBot extends AbstractSlashCommandJdaBot{

    private static final Logger logger = LoggerFactory.getLogger(MusicBot.class);

    // Slash Commands
    private static final String COMMAND_PLAY = "play";
    private static final String COMMAND_PAUSE = "pause";
    private static final String COMMAND_RESUME = "resume";
    private static final String COMMAND_SKIP = "skip";
    private static final String COMMAND_QUEUE = "queue";
    private static final String COMMAND_CLEAR = "clear";
    private static final String COMMAND_LEAVE = "leave";
    private static final String COMMAND_VOLUME = "volume";
    private static final String COMMAND_NOW_PLAYING = "nowplaying";

    // Colors
    private static final Color COLOR_SUCCESS = Color.GREEN;
    private static final Color COLOR_ERROR = Color.RED;
    private static final Color COLOR_WARNING = Color.YELLOW;
    private static final Color COLOR_INFO = Color.BLUE;
    private static final Color COLOR_LOADING = Color.GRAY;

    private final MusicManager musicManager;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Constructs a MusicBot instance with the provided JDA and command services.
     * @param jdaBean              the JDA instance for Discord API operations
     * @param slashCommandService  the service for handling slash commands
     * @param slashCommandListener the service for listening to slash command events
     */
    @Autowired
    public MusicBot(JDA jdaBean, JdaSlashCommandService slashCommandService,
                    JdaEventListenerService slashCommandListener){
        super(jdaBean, slashCommandService, slashCommandListener);
        this.musicManager = new MusicManager();
        setBotType(this.getClass().getAnnotation(BotIdentifier.class).slashCommand());
        setDescription("Musik Bot, der Musik von YouTube und anderen Quellen abspielen kann.");
    }

    /**
     * Initializes the bot by setting up commands and interaction handlers.
     */
    @Override
    protected void setupCommands(){
        // Play command
        SlashCommandData playCommand = createCommand(COMMAND_PLAY, "Spielt Musik von einer URL")
                .addOption(OptionType.STRING, "url", "URL des Videos oder der Playlist", true);
        registerCommand(COMMAND_PLAY, "Spielt Musik", playCommand, this::handlePlay);

        // Pause command
        SlashCommandData pauseCommand = createCommand(COMMAND_PAUSE, "Pausiert die aktuelle Wiedergabe");
        registerCommand(COMMAND_PAUSE, "Pausiert Musik", pauseCommand, this::handlePause);

        // Resume command
        SlashCommandData resumeCommand = createCommand(COMMAND_RESUME, "Setzt die pausierte Wiedergabe fort");
        registerCommand(COMMAND_RESUME, "Setzt Musik fort", resumeCommand, this::handleResume);

        // Skip command
        SlashCommandData skipCommand = createCommand(COMMAND_SKIP, "Überspringt den aktuellen Song");
        registerCommand(COMMAND_SKIP, "Überspringt Song", skipCommand, this::handleSkip);

        // Queue command
        SlashCommandData queueCommand = createCommand(COMMAND_QUEUE, "Zeigt die aktuelle Warteschlange an");
        registerCommand(COMMAND_QUEUE, "Zeigt Warteschlange", queueCommand, this::handleQueue);

        // Clear command
        SlashCommandData clearCommand = createCommand(COMMAND_CLEAR, "Leert die Warteschlange");
        registerCommand(COMMAND_CLEAR, "Leert Warteschlange", clearCommand, this::handleClear);

        // Leave command
        SlashCommandData leaveCommand = createCommand(COMMAND_LEAVE, "Bot verlässt den Sprachkanal");
        registerCommand(COMMAND_LEAVE, "Verlässt Kanal", leaveCommand, this::handleLeave);

        // Volume command
        SlashCommandData volumeCommand = createCommand(COMMAND_VOLUME, "Stellt die Lautstärke ein")
                .addOption(OptionType.INTEGER, "level", "Lautstärke (0-100)", true);
        registerCommand(COMMAND_VOLUME, "Stellt Lautstärke ein", volumeCommand, this::handleVolume);

        // Now Playing command
        SlashCommandData nowPlayingCommand = createCommand(COMMAND_NOW_PLAYING, "Zeigt den aktuell spielenden Song");
        registerCommand(COMMAND_NOW_PLAYING, "Zeigt aktuellen Song", nowPlayingCommand, this::handleNowPlaying);
    }

    /**
     * Registers button interaction handlers.
     */
    @Override
    protected void registerButtonInteractionHandlers(){
        // No button interactions for music bot currently
        // Could add controls like play/pause/skip buttons in embeds
    }

    /**
     * Registers string select interaction handlers.
     * Currently not used, but can be extended for playlist selection or volume presets.
     */
    @Override
    protected void registerStringInteractionHandlers(){
        // No string select interactions for music bot currently
        // Could add playlist selection or volume presets
    }

    /**
     * Registers modal interaction handlers.
     * Currently not used, but can be extended for user input forms.
     */
    @Override
    protected void registerModalInteractionHandlers(){
        // No modal interactions for music bot currently
    }

    /**
     * Handles the play command interaction.
     * Validates the URL, checks voice state, connects to voice channel, and plays the track.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handlePlay(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        String url = event.getOption("url").getAsString();

        if(! url.startsWith("http")){
            MessageEmbed embed = createEmbed("🌐 Ungültige URL",
                    "Bitte gib eine gültige HTTP/HTTPS URL ein.", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        Member member = event.getMember();
        if(member==null){
            MessageEmbed embed = createEmbed("❌ Fehler",
                    "Konnte Mitglied nicht identifizieren.", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if(voiceState==null || ! voiceState.inAudioChannel()){
            MessageEmbed embed = createEmbed("❌ Fehler",
                    "Du musst in einem Sprachkanal sein!", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        Guild guild = event.getGuild();
        if(guild==null){
            MessageEmbed embed = createEmbed("❌ Fehler",
                    "Dieser Befehl funktioniert nur auf Servern.", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        long guildId = guild.getIdLong();
        AudioManager audioManager = guild.getAudioManager();

        // Connect to voice channel
        audioManager.openAudioConnection(voiceState.getChannel());
        audioManager.setSendingHandler(musicManager.getSendHandler(guildId));

        // Show loading message
        MessageEmbed loadingEmbed = createEmbed("⏳ Lade Musik",
                "Versuche URL zu laden: " + url, COLOR_LOADING);
        event.getHook().sendMessageEmbeds(loadingEmbed).queue();

        // Load and play asynchronously
        CompletableFuture.runAsync(() -> {
            musicManager.loadAndPlay(guildId, url);
        }, executorService).thenRun(() -> {
            MessageEmbed successEmbed = createEmbed("🎵 Musik wird abgespielt",
                    "Erfolgreich geladen: " + url, COLOR_SUCCESS);
            event.getHook().editOriginalEmbeds(successEmbed).queue();
        }).exceptionally(throwable -> {
            logger.error("Error loading music", throwable);
            MessageEmbed errorEmbed = createEmbed("❌ Fehler beim Laden",
                    "Konnte die URL nicht laden: " + throwable.getMessage(), COLOR_ERROR);
            event.getHook().editOriginalEmbeds(errorEmbed).queue();
            return null;
        });
    }

    /**
     * Handles the pause command interaction.
     * Pauses the currently playing track if it is not already paused.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handlePause(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        long guildId = event.getGuild().getIdLong();
        boolean wasPaused = musicManager.getPlayer(guildId).isPaused();

        if(wasPaused){
            MessageEmbed embed = createEmbed("⏸️ Bereits pausiert",
                    "Die Musik ist bereits pausiert.", COLOR_WARNING);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        musicManager.getPlayer(guildId).setPaused(true);

        MessageEmbed embed = createEmbed("⏸️ Musik pausiert",
                "Die Musik wurde pausiert.", COLOR_WARNING);
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    /**
     * Handles the resume command interaction.
     * Resumes the currently paused track if it is paused.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handleResume(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        long guildId = event.getGuild().getIdLong();
        boolean wasPaused = musicManager.getPlayer(guildId).isPaused();

        if(! wasPaused){
            MessageEmbed embed = createEmbed("▶️ Bereits am Spielen",
                    "Die Musik spielt bereits.", COLOR_SUCCESS);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        musicManager.getPlayer(guildId).setPaused(false);

        MessageEmbed embed = createEmbed("▶️ Musik fortgesetzt",
                "Die Musik wird fortgesetzt.", COLOR_SUCCESS);
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    /**
     * Handles the skip command interaction.
     * Skips the currently playing track and plays the next one in the queue if available.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handleSkip(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        long guildId = event.getGuild().getIdLong();
        AudioTrack currentTrack = musicManager.getPlayer(guildId).getPlayingTrack();

        if(currentTrack==null){
            MessageEmbed embed = createEmbed("❌ Nichts zu überspringen",
                    "Es wird gerade keine Musik abgespielt.", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        String trackTitle = currentTrack.getInfo().title;

        // Check if there's a next track
        boolean hasNext = ! musicManager.getTrackScheduler(guildId).getQueue().isEmpty();

        musicManager.getTrackScheduler(guildId).nextTrack();

        String description = hasNext
                ? "Übersprungen: " + truncateString(trackTitle, 50)
                : "Letzter Song übersprungen: " + truncateString(trackTitle, 50);

        MessageEmbed embed = createEmbed("⏭️ Song übersprungen", description, COLOR_INFO);
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    /**
     * Handles the queue command interaction.
     * Displays the current track and the next 10 tracks in the queue.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handleQueue(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        long guildId = event.getGuild().getIdLong();
        StringBuilder queueString = new StringBuilder();

        AudioTrack currentTrack = musicManager.getPlayer(guildId).getPlayingTrack();
        if(currentTrack!=null){
            queueString.append("**🎵 Aktuell:** ")
                    .append(truncateString(currentTrack.getInfo().title, 50))
                    .append(" [")
                    .append(formatDuration(currentTrack.getDuration()))
                    .append("]\n\n");
        }

        queueString.append("**📋 Warteschlange:**\n");

        int trackNumber = 1;
        int totalDuration = 0;
        for(AudioTrack track : musicManager.getTrackScheduler(guildId).getQueue()){
            if(trackNumber > 10){
                int remaining = musicManager.getTrackScheduler(guildId).getQueue().size() - 10;
                queueString.append("\n*... und ").append(remaining).append(" weitere Tracks*");
                break;
            }

            queueString.append(trackNumber).append(". ")
                    .append(truncateString(track.getInfo().title, 40))
                    .append(" [")
                    .append(formatDuration(track.getDuration()))
                    .append("]\n");

            totalDuration += track.getDuration();
            trackNumber++;
        }

        if(trackNumber==1){
            queueString.append("*Die Warteschlange ist leer.*");
        } else{
            queueString.append("\n**⏱️ Gesamtdauer:** ").append(formatDuration(totalDuration));
        }

        MessageEmbed embed = createEmbed("📋 Musikwarteschlange",
                queueString.toString(), COLOR_INFO);
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    /**
     * Handles the clear command interaction.
     * Stops the current track and clears the queue.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handleClear(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        long guildId = event.getGuild().getIdLong();
        int queueSize = musicManager.getTrackScheduler(guildId).getQueue().size();
        AudioTrack currentTrack = musicManager.getPlayer(guildId).getPlayingTrack();

        musicManager.getPlayer(guildId).stopTrack();
        musicManager.getTrackScheduler(guildId).clearQueue();

        String description = currentTrack!=null
                ? "Aktueller Song gestoppt und " + queueSize + " Tracks aus der Warteschlange entfernt."
                : queueSize + " Tracks wurden aus der Warteschlange entfernt.";

        MessageEmbed embed = createEmbed("🗑️ Warteschlange geleert", description, COLOR_INFO);
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    /**
     * Handles the leave command interaction.
     * Disconnects the bot from the voice channel and clears the queue.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handleLeave(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        Guild guild = event.getGuild();
        AudioManager audioManager = guild.getAudioManager();

        if(audioManager.isConnected()){
            // Clear music for this guild
            long guildId = guild.getIdLong();
            AudioTrack currentTrack = musicManager.getPlayer(guildId).getPlayingTrack();
            int queueSize = musicManager.getTrackScheduler(guildId).getQueue().size();

            musicManager.getPlayer(guildId).stopTrack();
            musicManager.getTrackScheduler(guildId).clearQueue();

            audioManager.closeAudioConnection();

            String description = "Bot hat den Sprachkanal verlassen.";
            if(currentTrack!=null || queueSize > 0){
                description += "\n" + (queueSize + (currentTrack!=null ? 1 : 0)) + " Songs wurden entfernt.";
            }

            MessageEmbed embed = createEmbed("👋 Bot verlässt den Kanal", description, COLOR_INFO);
            event.getHook().sendMessageEmbeds(embed).queue();
        } else{
            MessageEmbed embed = createEmbed("❌ Fehler",
                    "Bot ist in keinem Sprachkanal.", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

    /**
     * Handles the volume command interaction.
     * Sets the volume of the audio player to the specified level (0-100).
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handleVolume(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        int volume = event.getOption("level").getAsInt();

        if(volume < 0 || volume > 100){
            MessageEmbed embed = createEmbed("❌ Ungültige Lautstärke",
                    "Die Lautstärke muss zwischen 0 und 100 liegen.", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        int oldVolume = musicManager.getPlayer(guildId).getVolume();
        musicManager.getPlayer(guildId).setVolume(volume);

        String volumeBar = createVolumeBar(volume);
        String change = "";
        if(oldVolume!=volume){
            change = " (vorher: " + oldVolume + "%)";
        }

        MessageEmbed embed = createEmbed("🔊 Lautstärke eingestellt",
                "Lautstärke: " + volume + "%" + change + "\n" + volumeBar, COLOR_SUCCESS);
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    /**
     * Handles the now playing command interaction.
     * Displays the currently playing track, its status, and progress.
     * @param event the SlashCommandInteractionEvent containing the command data
     */
    private void handleNowPlaying(SlashCommandInteractionEvent event){
        event.deferReply().queue();

        if(! checkGuildAndHook(event)) return;

        long guildId = event.getGuild().getIdLong();

        CompletableFuture.supplyAsync(() -> {
            AudioTrack currentTrack = musicManager.getPlayer(guildId).getPlayingTrack();

            if(currentTrack==null){
                return null;
            }

            boolean isPaused = musicManager.getPlayer(guildId).isPaused();
            long position = currentTrack.getPosition();
            long duration = currentTrack.getDuration();

            return new NowPlayingInfo(currentTrack, isPaused, position, duration);
        }, executorService).thenAccept(info -> {
            if(info==null){
                MessageEmbed embed = createEmbed("🎵 Nichts spielt",
                        "Es wird gerade keine Musik abgespielt.", COLOR_INFO);
                event.getHook().sendMessageEmbeds(embed).queue();
                return;
            }

            String progressBar = createProgressBar(info.position, info.duration);
            String status = info.isPaused ? "⏸️ Pausiert" : "▶️ Spielt";

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("🎵 Aktueller Song")
                    .setDescription("**" + info.track.getInfo().title + "**")
                    .addField("Künstler", info.track.getInfo().author, true)
                    .addField("Dauer", formatDuration(info.duration), true)
                    .addField("Status", status, true)
                    .addField("Fortschritt",
                            formatDuration(info.position) + " / " + formatDuration(info.duration) + "\n" + progressBar,
                            false)
                    .setColor(info.isPaused ? COLOR_WARNING : COLOR_SUCCESS);

            if(info.track.getInfo().uri!=null){
                embedBuilder.addField("Link", "[Zur Quelle](" + info.track.getInfo().uri + ")", false);
            }

            // Add queue info
            int queueSize = musicManager.getTrackScheduler(guildId).getQueue().size();
            if(queueSize > 0){
                embedBuilder.addField("Warteschlange", queueSize + " Song(s) in der Warteschlange", false);
            }

            event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
        }).exceptionally(throwable -> {
            logger.error("Error getting now playing info", throwable);
            MessageEmbed errorEmbed = createEmbed("❌ Fehler",
                    "Konnte aktuelle Wiedergabe nicht abrufen.", COLOR_ERROR);
            event.getHook().sendMessageEmbeds(errorEmbed).queue();
            return null;
        });
    }

    // Helper methods

    /**
     * Checks if the command is executed in a guild (server).
     * Replies with an error message if not.
     * @param event the SlashCommandInteractionEvent to check
     * @return true if the command is from a guild, false otherwise
     */
    private boolean checkGuildAndReply(SlashCommandInteractionEvent event){
        if(! event.isFromGuild()){
            event.reply("Dieser Befehl funktioniert nur auf Servern.")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }

    /**
     * Checks if the command is executed in a guild (server) and has a valid hook.
     * Replies with an error message if not.
     * @param event the SlashCommandInteractionEvent to check
     * @return true if the command is from a guild and has a valid hook, false otherwise
     */
    private boolean checkGuildAndHook(SlashCommandInteractionEvent event){
        if(! event.isFromGuild()){
            event.getHook().sendMessage("Dieser Befehl funktioniert nur auf Servern.")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }


    /**
     * Creates an embed message with the specified title, description, and color.
     * This is a helper method to standardize embed creation across the bot.
     * @param title       the title of the embed
     * @param description the description of the embed
     * @param color       the color of the embed
     */
    private MessageEmbed createEmbed(String title, String description, Color color){
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .build();
    }

    /**
     * Formats a duration in milliseconds to a human-readable string.
     * If the duration is Long.MAX_VALUE, it returns "LIVE".
     * @param milliseconds the duration in milliseconds
     * @return formatted duration string
     */
    private String formatDuration(long milliseconds){
        if(milliseconds==Long.MAX_VALUE){
            return "LIVE";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if(hours > 0){
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else{
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    /**
     * Truncates a string to a maximum length, adding "..." if it exceeds the limit.
     * @param str        the string to truncate
     * @param maxLength  the maximum length of the string
     * @return truncated string
     */
    private String truncateString(String str, int maxLength){
        if(str.length() <= maxLength){
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Creates a progress bar string based on the current position and total duration.
     * The bar is 20 characters long, with filled, unfilled, and current position indicators.
     * @param position the current position in milliseconds
     * @param duration the total duration in milliseconds
     * @return a string representation of the progress bar
     */
    private String createProgressBar(long position, long duration){
        int barLength = 20;
        int filledLength = (int) ((position / (double) duration) * barLength);

        StringBuilder bar = new StringBuilder();
        for(int i = 0; i < barLength; i++){
            if(i==filledLength){
                bar.append("🔘");
            } else if(i < filledLength){
                bar.append("▬");
            } else{
                bar.append("—");
            }
        }

        return bar.toString();
    }

    /**
     * Creates a volume bar string based on the current volume level.
     * The bar is 10 characters long, with filled and unfilled indicators.
     * @param volume the current volume level (0-100)
     * @return a string representation of the volume bar
     */
    private String createVolumeBar(int volume){
        int barLength = 10;
        int filledLength = (volume / 10);

        StringBuilder bar = new StringBuilder();
        for(int i = 0; i < barLength; i++){
            if(i < filledLength){
                bar.append("🔊");
            } else{
                bar.append("🔈");
            }
        }

        return bar.toString();
    }

    /**
     * Unregisters all commands and cleans up resources.
     * This method is called when the bot is shutting down or being reloaded.
     */
    @Override
    public void unregisterCommands(){
        // Shutdown executor service
        executorService.shutdown();
        try{
            if(! executorService.awaitTermination(5, TimeUnit.SECONDS)){
                executorService.shutdownNow();
            }
        } catch(InterruptedException e){
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Stop all audio players
        musicManager.shutdown();

        super.unregisterCommands();
    }

    // Helper data classes
    /**
     * Holds information about the currently playing track.
     * Contains the track, its paused state, current position, and duration.
     */
    private static class NowPlayingInfo{
        final AudioTrack track;
        final boolean isPaused;
        final long position;
        final long duration;

        /**
         * Constructs a NowPlayingInfo object with the given track, paused state, position, and duration.
         * @param track     the currently playing audio track
         * @param isPaused  whether the track is paused
         * @param position  the current playback position in milliseconds
         * @param duration  the total duration of the track in milliseconds
         */
        NowPlayingInfo(AudioTrack track, boolean isPaused, long position, long duration){
            this.track = track;
            this.isPaused = isPaused;
            this.position = position;
            this.duration = duration;
        }
    }
}