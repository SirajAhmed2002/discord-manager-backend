package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.AbstractSlashCommandJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.BotIdentifier;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.annotation.SlashCommandBotType;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This bot provides a timetable feature for ZHAW students.
 * It allows users to select their department, semester, and week to retrieve their schedule.
 * The bot uses slash commands and buttons for interaction.
 */
@BotIdentifier(category = BotIdentifier.BotCategory.SLASH_COMMAND,
        slashCommand = SlashCommandBotType.TIMETABLE)
@Component
public class ScheduleJdaBot extends AbstractSlashCommandJdaBot{

    private static final Logger logger = LoggerFactory.getLogger(ScheduleJdaBot.class);

    // Slash Command
    private static final String COMMAND_SCHEDULE = "stundenplan";
    private static final String OPTION_USERNAME = "username";

    // Colors
    private static final Color COLOR_SUCCESS = Color.GREEN;
    private static final Color COLOR_PRIMARY = Color.BLUE;
    private static final Color COLOR_ERROR = Color.RED;
    private static final Color COLOR_LOADING = Color.GRAY;

    private final ScheduleScraper scheduleScraper;
    private final MetadataScraper metadataScraper;
    private final ScreenshotGenerator screenshotGenerator;

    // Store user selections per user ID
    private final Map<String, Map<String, String>> userSelections = new ConcurrentHashMap<>();

    // ExecutorService for async operations
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Constructor for ScheduleJdaBot.
     * Initializes the bot with the necessary services and sets the bot type and description.
     * @param jdaBean              JDA instance for Discord API communication
     * @param slashCommandService  Service for handling slash commands
     * @param scheduleScraper      Service for scraping schedules
     * @param metadataScraper      Service for scraping metadata
     * @param screenshotGenerator  Service for generating screenshots
     * @param slashCommandListener Listener for slash command events
     */
    @Autowired
    public ScheduleJdaBot(JDA jdaBean, JdaSlashCommandService slashCommandService, ScheduleScraper scheduleScraper,
                          MetadataScraper metadataScraper, ScreenshotGenerator screenshotGenerator,
                          JdaEventListenerService slashCommandListener){
        super(jdaBean, slashCommandService, slashCommandListener);
        this.scheduleScraper = scheduleScraper;
        this.metadataScraper = metadataScraper;
        this.screenshotGenerator = screenshotGenerator;
        setBotType(this.getClass().getAnnotation(BotIdentifier.class).slashCommand());
        setDescription("Timetable Bot für ZHAW Studierende");
    }

    /**
     * Initializes the bot after construction.
     * This method is called after the bean is fully constructed and dependencies are injected.
     */
    @Override
    protected void setupCommands(){
        SlashCommandData stundenplanCommand = createCommand(COMMAND_SCHEDULE, "Zeigt deinen Stundenplan an")
                .addOption(OptionType.STRING, OPTION_USERNAME, "Dein ZHAW-Benutzername", true);

        registerCommand(COMMAND_SCHEDULE, "Erstellt einen Stundenplan", stundenplanCommand,
                this::handleStundenplanCommand);
    }

    /**
     * PostConstruct method to register interaction handlers.
     * This method is called after the bean is fully initialized.
     */
    @Override
    protected void registerButtonInteractionHandlers(){
        registerButtonInteractionHandler(CustomId.SUBMIT.getId(), this::handleSubmitButton);
        registerButtonInteractionHandler(CustomId.CANCEL.getId(), this::handleCancelButton);
        registerButtonInteractionHandler(CustomId.CONFIRM_PREFIX.getId(), this::handleConfirmButton);
        registerButtonInteractionHandler(CustomId.RESTART.getId(), this::handleRestartButton);
    }

    /**
     * Registers string interaction handlers for department, semester, and week selections.
     * This method is called to handle user selections in the UI.
     */
    @Override
    protected void registerStringInteractionHandlers(){
        registerStringInteractionHandler(CustomId.DEPARTMENT.getId(), this::handleDepartmentSelection);
        registerStringInteractionHandler(CustomId.SEMESTER.getId(), this::handleSemesterSelection);
        registerStringInteractionHandler(CustomId.WEEK.getId(), this::handleWeekSelection);
    }

    /**
     * Registers modal interaction handlers.
     * This method is empty as this bot does not use modals for interactions.
     */
    @Override
    protected void registerModalInteractionHandlers(){
        // No modal interactions for this bot
    }

    /**
     * Handles the slash command interaction for fetching the timetable.
     * It prompts the user to confirm their username and then proceeds with the selection process.
     * @param event The SlashCommandInteractionEvent containing the command details
     */
    private void handleStundenplanCommand(SlashCommandInteractionEvent event){
        event.deferReply().setEphemeral(true).queue();
        String userId = event.getUser().getId();
        String username = event.getOption(OPTION_USERNAME, "", OptionMapping::getAsString);

        if(username.isEmpty()){
            event.getHook().sendMessage("Bitte gib einen Benutzernamen an.")
                    .queue();
            return;
        }

        // Store username in user selections
        userSelections.computeIfAbsent(userId, k -> new HashMap<>())
                .put(CustomId.USERNAME.getId(), username);

        // Show confirmation embed
        MessageEmbed confirmEmbed = createEmbed(
                "❓ Bestätigung",
                "Bist du dir sicher, dass du den Stundenplan für **" + username + "** abrufen möchtest?",
                COLOR_PRIMARY
        );

        // Use hook to send embed with buttons
        event.getHook().sendMessageEmbeds(confirmEmbed)
                .addComponents(ActionRow.of(
                        Button.primary(CustomId.CONFIRM_PREFIX.getId() + userId, "Bestätigen"),
                        Button.danger(CustomId.CANCEL.getId(), "Abbrechen")
                ))
                .queue();
    }

    // Button interaction handlers

    /**
     * Handles the submit button interaction.
     * It checks if the user has made all necessary selections and fetches the schedule.
     * @param event The ButtonInteractionEvent containing the button interaction details
     */
    private void handleSubmitButton(ButtonInteractionEvent event){
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        Map<String, String> selections = userSelections.get(userId);

        if(selections!=null &&
                selections.containsKey(CustomId.USERNAME.getId()) &&
                selections.containsKey(CustomId.DEPARTMENT.getId()) &&
                selections.containsKey(CustomId.SEMESTER.getId()) &&
                selections.containsKey(CustomId.WEEK.getId())){
            fetchAndDisplaySchedule(event, selections);
        } else{
            MessageEmbed errorEmbed = createErrorEmbed("Bitte vollständige Auswahl treffen");
            event.getHook().editOriginalEmbeds(errorEmbed)
                    .setComponents()
                    .queue();

            // Delete after delay using the hook
            CompletableFuture.delayedExecutor(1, TimeUnit.MINUTES, executorService)
                    .execute(() -> event.getHook().deleteOriginal().queue());
        }
    }

    /**
     * Handles the cancel button interaction.
     * It clears the user's selections and deletes the original message.
     * @param event The ButtonInteractionEvent containing the button interaction details
     */
    private void handleCancelButton(ButtonInteractionEvent event){
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        userSelections.remove(userId);

        event.getHook().deleteOriginal().queue();
    }

    /**
     * Handles the confirm button interaction.
     * It shows the department selection menu after confirmation.
     * @param event The ButtonInteractionEvent containing the button interaction details
     */
    private void handleConfirmButton(ButtonInteractionEvent event){
        event.deferEdit().queue();
        showDepartmentSelection(event);
    }

    /**
     * Handles the restart button interaction.
     * It shows the department selection menu again to allow the user to start over.
     * @param event The ButtonInteractionEvent containing the button interaction details
     */
    private void handleRestartButton(ButtonInteractionEvent event){
        event.deferEdit().queue();
        showDepartmentSelection(event);
    }

    // String select interaction handlers

    /**
     * Handles the department selection from the dropdown menu.
     * It fetches the semesters for the selected department and shows the semester selection menu.
     * @param event The StringSelectInteractionEvent containing the selection details
     */
    private void handleDepartmentSelection(StringSelectInteractionEvent event){
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        String selectedValue = event.getValues().getFirst();

        // Store selection
        userSelections.computeIfAbsent(userId, k -> new HashMap<>())
                .put(CustomId.DEPARTMENT.getId(), selectedValue);

        showLoadingEmbed(event);

        CompletableFuture.supplyAsync(() -> metadataScraper.fetchSemesters(selectedValue), executorService)
                .thenAccept(semesters -> {
                    MessageEmbed embed = createEmbed(
                            "🎓 Semester auswählen",
                            "**Department: **" + selectedValue + "\n\nBitte wähle das Semester aus.",
                            COLOR_PRIMARY
                    );

                    StringSelectMenu selectMenu = createSelectMenu(
                            CustomId.SEMESTER.getId(),
                            "Wähle ein Semester",
                            semesters
                    );

                    event.getHook().editOriginalEmbeds(embed)
                            .setComponents(ActionRow.of(selectMenu))
                            .queue();
                })
                .exceptionally(throwable -> {
                    logger.error("Error fetching semesters", throwable);
                    MessageEmbed errorEmbed = createErrorEmbed("Fehler beim Laden der Semester");
                    event.getHook().editOriginalEmbeds(errorEmbed).queue();
                    return null;
                });
    }

    /**
     * Handles the semester selection from the dropdown menu.
     * It fetches the weeks for the selected department and semester, and shows the week selection menu.
     * @param event The StringSelectInteractionEvent containing the selection details
     */
    private void handleSemesterSelection(StringSelectInteractionEvent event){
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        String selectedValue = event.getValues().getFirst();

        // Store selection
        userSelections.computeIfAbsent(userId, k -> new HashMap<>())
                .put(CustomId.SEMESTER.getId(), selectedValue);

        showLoadingEmbed(event);

        String department = userSelections.get(userId).get(CustomId.DEPARTMENT.getId());

        CompletableFuture.supplyAsync(() -> metadataScraper.fetchWeeks(department, selectedValue), executorService)
                .thenAccept(weeks -> {
                    MessageEmbed embed = createEmbed(
                            "🎓 Woche auswählen",
                            "**Department: **" + department +
                                    "\n**Semester: **" + selectedValue +
                                    "\n\nBitte wähle die Woche aus.",
                            COLOR_PRIMARY
                    );

                    StringSelectMenu selectMenu = createSelectMenu(
                            CustomId.WEEK.getId(),
                            "Wähle eine Woche",
                            weeks
                    );

                    event.getHook().editOriginalEmbeds(embed)
                            .setComponents(ActionRow.of(selectMenu))
                            .queue();
                })
                .exceptionally(throwable -> {
                    logger.error("Error fetching weeks", throwable);
                    MessageEmbed errorEmbed = createErrorEmbed("Fehler beim Laden der Wochen");
                    event.getHook().editOriginalEmbeds(errorEmbed).queue();
                    return null;
                });
        logger.info("Finished fetching weeks for department: {} and semester: {}", department, selectedValue);
    }

    /**
     * Handles the week selection from the dropdown menu.
     * It confirms the user's selections and prompts them to retrieve their schedule.
     * @param event The StringSelectInteractionEvent containing the selection details
     */
    private void handleWeekSelection(StringSelectInteractionEvent event){
        event.deferEdit().queue();

        String userId = event.getUser().getId();
        String selectedValue = event.getValues().getFirst();

        // Store selection
        userSelections.computeIfAbsent(userId, k -> new HashMap<>())
                .put(CustomId.WEEK.getId(), selectedValue);

        showLoadingEmbed(event);

        Map<String, String> selections = userSelections.get(userId);

        MessageEmbed embed = createEmbed(
                "🎓 Stundenplan-Auswahl abgeschlossen",
                "**Department: **" + selections.get(CustomId.DEPARTMENT.getId()) +
                        "\n**Semester: **" + selections.get(CustomId.SEMESTER.getId()) +
                        "\n**Woche: **" + selectedValue +
                        "\n\nKlicke auf **'Stundenplan abrufen'** um fortzufahren.",
                COLOR_PRIMARY
        );

        event.getHook().editOriginalEmbeds(embed)
                .setComponents(ActionRow.of(
                        Button.primary(CustomId.SUBMIT.getId(), "Stundenplan abrufen"),
                        Button.secondary(CustomId.RESTART.getId(), "Neu auswählen"),
                        Button.danger(CustomId.CANCEL.getId(), "Abbrechen")
                ))
                .queue();
    }

    /**
     * Shows the department selection menu.
     * It fetches the list of departments and displays them in a dropdown menu.
     * @param event The ButtonInteractionEvent containing the button interaction details
     */
    private void showDepartmentSelection(ButtonInteractionEvent event){
        // Show loading embed first
        MessageEmbed loadingEmbed = createLoadingEmbed();
        event.getHook().editOriginalEmbeds(loadingEmbed)
                .setComponents()
                .queue();

        // Fetch departments asynchronously
        CompletableFuture.supplyAsync(metadataScraper::fetchDepartments, executorService)
                .thenAccept(departments -> {
                    MessageEmbed embed = createEmbed(
                            "🎓 Department auswählen",
                            "Bitte wähle das Department aus.",
                            COLOR_PRIMARY
                    );

                    StringSelectMenu selectMenu = createSelectMenu(
                            CustomId.DEPARTMENT.getId(),
                            "Wähle ein Department",
                            departments
                    );

                    event.getHook().editOriginalEmbeds(embed)
                            .setComponents(ActionRow.of(selectMenu))
                            .queue();
                })
                .exceptionally(throwable -> {
                    logger.error("Error fetching departments", throwable);
                    MessageEmbed errorEmbed = createErrorEmbed("Fehler beim Laden der Departments");
                    event.getHook().editOriginalEmbeds(errorEmbed).queue();
                    return null;
                });
    }

    /**
     * Fetches the schedule based on user selections and displays it.
     * It generates a screenshot of the schedule and sends it as an attachment.
     * @param event      The ButtonInteractionEvent containing the button interaction details
     * @param selections The map containing user selections for username, department, semester, and week
     */
    private void fetchAndDisplaySchedule(ButtonInteractionEvent event, Map<String, String> selections){
        showLoadingEmbed(event);

        CompletableFuture.supplyAsync(() -> {
                    try{
                        String htmlSchedule = scheduleScraper.fetchScheduleHtml(
                                selections.get(CustomId.USERNAME.getId()),
                                selections.get(CustomId.DEPARTMENT.getId()),
                                selections.get(CustomId.SEMESTER.getId()),
                                selections.get(CustomId.WEEK.getId())
                        );

                        byte[] screenshot = screenshotGenerator.createScreenshot(htmlSchedule);
                        String userInfo = extractUserInfo(htmlSchedule);
                        String username = extractUsername(htmlSchedule);

                        return new ScheduleResult(userInfo, username, screenshot);
                    } catch(Exception e){
                        logger.error("Error generating schedule", e);
                        return null;
                    }
                }, executorService)
                .thenAccept(result -> {
                    if(result==null || result.screenshot==null){
                        MessageEmbed errorEmbed = createErrorEmbed("Es ist ein Fehler beim Erstellen des Stundenplans aufgetreten.");
                        event.getHook().editOriginalEmbeds(errorEmbed).queue();
                        return;
                    }

                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("🎓 Stundenplan für " + result.username)
                            .setDescription("**" + result.userInfo + "**")
                            .setColor(COLOR_SUCCESS)
                            .setImage("attachment://stundenplan.png")
                            .build();

                    ByteArrayInputStream imageStream = new ByteArrayInputStream(result.screenshot);
                    FileUpload fileUpload = FileUpload.fromData(imageStream, "stundenplan.png");

                    event.getHook().editOriginalEmbeds(embed)
                            .setFiles(fileUpload)
                            .setComponents(ActionRow.of(
                                    Button.secondary(CustomId.RESTART.getId(), "Neuer Stundenplan"),
                                    Button.danger(CustomId.CANCEL.getId(), "Löschen")
                            ))
                            .queue();
                })
                .exceptionally(throwable -> {
                    logger.error("Error displaying schedule", throwable);
                    MessageEmbed errorEmbed = createErrorEmbed("Fehler beim Anzeigen des Stundenplans");
                    event.getHook().editOriginalEmbeds(errorEmbed).queue();
                    return null;
                });
    }

    // Helper methods

    /**
     * Shows a loading embed while processing user requests.
     * This method is used to indicate that the bot is working on the user's request.
     * @param event The ButtonInteractionEvent containing the button interaction details
     */
    private void showLoadingEmbed(ButtonInteractionEvent event){
        MessageEmbed loadingEmbed = createLoadingEmbed();
        event.getHook().editOriginalEmbeds(loadingEmbed)
                .setComponents()
                .queue();
    }

    /**
     * Shows a loading embed while processing user requests in string select interactions.
     * This method is used to indicate that the bot is working on the user's request.
     * @param event The StringSelectInteractionEvent containing the selection details
     */
    private void showLoadingEmbed(StringSelectInteractionEvent event){
        MessageEmbed loadingEmbed = createLoadingEmbed();
        event.getHook().editOriginalEmbeds(loadingEmbed)
                .setComponents()
                .queue();
    }

    /**
     * Creates a select menu for department, semester, or week selection.
     * This method builds a dropdown menu with the provided options.
     * @param selectId    The ID of the select menu
     * @param placeholder The placeholder text for the select menu
     * @param options     The list of options to display in the select menu
     * @return A StringSelectMenu object with the specified options
     */
    private StringSelectMenu createSelectMenu(String selectId, String placeholder, List<String> options){
        StringSelectMenu.Builder builder = StringSelectMenu.create(selectId)
                .setPlaceholder(placeholder)
                .setRequiredRange(1, 1);

        for(String option : options){
            builder.addOption(option, option);
        }

        return builder.build();
    }

    /**
     * Creates an embed message with the specified title, description, and color.
     * This method is used to create consistent embed messages throughout the bot.
     * @param title       The title of the embed
     * @param description The description of the embed
     * @param color       The color of the embed
     * @return A MessageEmbed object with the specified properties
     */
    private MessageEmbed createEmbed(String title, String description, Color color){
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .build();
    }

    /**
     * Creates a loading embed to indicate that the bot is processing a request.
     * This method is used to inform users that their request is being processed.
     * @return A MessageEmbed object with a loading message
     */
    private MessageEmbed createLoadingEmbed(){
        return createEmbed("⏳ Verarbeitung", "Der Bot denkt nach...", COLOR_LOADING);
    }

    /**
     * Creates an error embed to display error messages.
     * This method is used to inform users about errors that occur during processing.
     * @param errorMessage The error message to display
     * @return A MessageEmbed object with the error message
     */
    private MessageEmbed createErrorEmbed(String errorMessage){
        return createEmbed("❌ Fehler", errorMessage, COLOR_ERROR);
    }

    /**
     * Extracts user information from the HTML schedule.
     * This method parses the HTML to retrieve user-specific information.
     * @param htmlSchedule The HTML content of the schedule
     * @return A string containing user information formatted for Discord
     */
    private String extractUserInfo(String htmlSchedule){
        if(htmlSchedule.contains("<p>")){
            String p = htmlSchedule.split("<p>")[1].split("</p>")[0].trim();

            // HTML entities replacement
            p = p.replace("&nbsp;", " ")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");

            // HTML tags to Discord formatting
            p = p.replace("<b>", "**")
                    .replace("</b>", "**");

            return p;
        }
        return "";
    }

    /**
     * Extracts the username from the HTML schedule.
     * This method retrieves the username from the HTML content.
     * @param htmlSchedule The HTML content of the schedule
     * @return The username extracted from the HTML
     */
    private String extractUsername(String htmlSchedule){
        if(htmlSchedule.contains("<h1>Stundenplan für ")){
            return htmlSchedule.split("<h1>Stundenplan für ")[1].split("</h1>")[0].trim();
        }
        return "";
    }

    // Data class for schedule results
    /**
     * ScheduleResult is a data class that holds the results of a schedule request.
     * It contains user information, username, and the screenshot of the schedule.
     */
    private static class ScheduleResult{
        final String userInfo;
        final String username;
        final byte[] screenshot;

        /**
         * Constructor for ScheduleResult.
         * Initializes the userInfo, username, and screenshot fields.
         * @param userInfo   The user information extracted from the schedule HTML
         * @param username   The username of the user
         * @param screenshot The screenshot of the schedule as a byte array
         */
        ScheduleResult(String userInfo, String username, byte[] screenshot){
            this.userInfo = userInfo;
            this.username = username;
            this.screenshot = screenshot;
        }
    }

    /**
     * Unregisters the commands and cleans up resources.
     * This method is called when the bot is shutting down or no longer needed.
     */
    @Override
    public void unregisterCommands(){
        // Clean up executor service
        executorService.shutdown();
        try{
            if(! executorService.awaitTermination(5, TimeUnit.SECONDS)){
                executorService.shutdownNow();
            }
        } catch(InterruptedException e){
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        super.unregisterCommands();
    }
}