package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.schedulejdabot;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.CustomId;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.MetadataScraper;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScheduleJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScheduleScraper;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScreenshotGenerator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyCollection;

@ExtendWith(MockitoExtension.class)
class ScheduleJdaBotTest {

    @Mock
    private JDA jdaBean;

    @Mock
    private JdaSlashCommandService slashCommandService;

    @Mock
    private ScheduleScraper scheduleScraper;

    @Mock
    private MetadataScraper metadataScraper;

    @Mock
    private ScreenshotGenerator screenshotGenerator;

    @Mock
    private JdaEventListenerService slashCommandListener;

    @Mock
    private SlashCommandInteractionEvent slashCommandEvent;

    @Mock
    private ButtonInteractionEvent buttonEvent;

    @Mock
    private StringSelectInteractionEvent stringSelectEvent;

    @Mock
    private User user;

    @Mock
    private InteractionHook interactionHook;

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    @Mock
    private WebhookMessageCreateAction webhookMessageCreateAction;

    @Mock
    private WebhookMessageEditAction webhookMessageEditAction;

    @Mock
    private RestAction<Void> restAction;

    @Mock
    private MessageEditCallbackAction messageEditCallbackAction;

    private ScheduleJdaBot scheduleJdaBot;

    private static final String TEST_USER_ID = "123456789";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        scheduleJdaBot = new ScheduleJdaBot(
                jdaBean,
                slashCommandService,
                scheduleScraper,
                metadataScraper,
                screenshotGenerator,
                slashCommandListener
        );

        lenient().when(user.getId()).thenReturn(TEST_USER_ID);
        lenient().when(slashCommandEvent.getUser()).thenReturn(user);
        lenient().when(buttonEvent.getUser()).thenReturn(user);
        lenient().when(stringSelectEvent.getUser()).thenReturn(user);

        lenient().when(slashCommandEvent.getHook()).thenReturn(interactionHook);
        lenient().when(buttonEvent.getHook()).thenReturn(interactionHook);
        lenient().when(stringSelectEvent.getHook()).thenReturn(interactionHook);

        lenient().when(slashCommandEvent.deferReply()).thenReturn(replyCallbackAction);
        lenient().when(replyCallbackAction.setEphemeral(true)).thenReturn(replyCallbackAction);
        lenient().doNothing().when(replyCallbackAction).queue();

        lenient().when(buttonEvent.deferEdit()).thenReturn(messageEditCallbackAction);
        lenient().when(stringSelectEvent.deferEdit()).thenReturn(messageEditCallbackAction);
        lenient().doNothing().when(messageEditCallbackAction).queue();

        lenient().when(interactionHook.sendMessage(anyString())).thenReturn(webhookMessageCreateAction);
        lenient().when(interactionHook.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(webhookMessageCreateAction);
        lenient().when(interactionHook.editOriginalEmbeds(any(MessageEmbed.class))).thenReturn(webhookMessageEditAction);
        lenient().when(interactionHook.deleteOriginal()).thenReturn(restAction);

        lenient().when(webhookMessageCreateAction.addComponents(any(ActionRow.class))).thenReturn(webhookMessageCreateAction);
        lenient().doNothing().when(webhookMessageCreateAction).queue();
        lenient().when(webhookMessageEditAction.setComponents(any(ActionRow.class))).thenReturn(webhookMessageEditAction);
        lenient().when(webhookMessageEditAction.setComponents(any(LayoutComponent[].class))).thenReturn(webhookMessageEditAction);
        lenient().when(webhookMessageEditAction.setComponents(anyCollection())).thenReturn(webhookMessageEditAction);
        lenient().when(webhookMessageEditAction.setFiles(any(FileUpload.class))).thenReturn(webhookMessageEditAction);
        lenient().doNothing().when(webhookMessageEditAction).queue();
        lenient().doNothing().when(restAction).queue();
    }

    @Test
    void testHandleStundenplanCommand_WithValidUsername() throws Exception {
        when(slashCommandEvent.getOption(eq("username"), eq(""), any())).thenReturn(TEST_USERNAME);

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleStundenplanCommand", SlashCommandInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, slashCommandEvent);

        verify(slashCommandEvent).deferReply();
        verify(replyCallbackAction).setEphemeral(true);
        verify(interactionHook).sendMessageEmbeds(any(MessageEmbed.class));

        Map<String, Map<String, String>> userSelections = getUserSelections();
        assertNotNull(userSelections.get(TEST_USER_ID));
        assertEquals(TEST_USERNAME, userSelections.get(TEST_USER_ID).get(CustomId.USERNAME.getId()));
    }

    @Test
    void testHandleStundenplanCommand_WithEmptyUsername() throws Exception {
        when(slashCommandEvent.getOption(eq("username"), eq(""), any())).thenReturn("");

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleStundenplanCommand", SlashCommandInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, slashCommandEvent);

        verify(interactionHook).sendMessage("Bitte gib einen Benutzernamen an.");
    }

    @Test
    void testHandleDepartmentSelection() throws Exception {
        List<String> mockSemesters = Arrays.asList("Semester 1", "Semester 2", "Semester 3");
        when(metadataScraper.fetchSemesters(anyString())).thenReturn(mockSemesters);
        when(stringSelectEvent.getValues()).thenReturn(Arrays.asList("Engineering"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleDepartmentSelection", StringSelectInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, stringSelectEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(stringSelectEvent).deferEdit();
        verify(metadataScraper).fetchSemesters("Engineering");

        Map<String, Map<String, String>> userSelections = getUserSelections();
        assertEquals("Engineering", userSelections.get(TEST_USER_ID).get(CustomId.DEPARTMENT.getId()));
    }

    @Test
    void testHandleSemesterSelection() throws Exception {
        setupUserSelections();
        getUserSelections().get(TEST_USER_ID).put(CustomId.DEPARTMENT.getId(), "Engineering");

        List<String> mockWeeks = Arrays.asList("Week 1", "Week 2", "Week 3");
        when(metadataScraper.fetchWeeks(anyString(), anyString())).thenReturn(mockWeeks);
        when(stringSelectEvent.getValues()).thenReturn(Arrays.asList("Semester 1"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleSemesterSelection", StringSelectInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, stringSelectEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(stringSelectEvent).deferEdit();
        verify(metadataScraper).fetchWeeks("Engineering", "Semester 1");

        Map<String, Map<String, String>> userSelections = getUserSelections();
        assertEquals("Semester 1", userSelections.get(TEST_USER_ID).get(CustomId.SEMESTER.getId()));
    }

    @Test
    void testHandleWeekSelection() throws Exception {
        setupUserSelections();
        Map<String, String> selections = getUserSelections().get(TEST_USER_ID);
        selections.put(CustomId.DEPARTMENT.getId(), "Engineering");
        selections.put(CustomId.SEMESTER.getId(), "Semester 1");

        when(stringSelectEvent.getValues()).thenReturn(Arrays.asList("Week 1"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleWeekSelection", StringSelectInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, stringSelectEvent);

        verify(stringSelectEvent).deferEdit();

        Map<String, Map<String, String>> userSelections = getUserSelections();
        assertEquals("Week 1", userSelections.get(TEST_USER_ID).get(CustomId.WEEK.getId()));
    }

    @Test
    void testExtractUserInfo() throws Exception {
        String htmlWithUserInfo = "<p><b>John Doe</b>&nbsp;-&nbsp;Student&nbsp;ID:&nbsp;12345</p>";

        Method method = ScheduleJdaBot.class.getDeclaredMethod("extractUserInfo", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(scheduleJdaBot, htmlWithUserInfo);

        assertEquals("**John Doe** - Student ID: 12345", result);
    }

    @Test
    void testFetchAndDisplayScheduleWithError() throws Exception {
        setupCompleteUserSelections();

        when(scheduleScraper.fetchScheduleHtml(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Simulierter Fehler"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("fetchAndDisplaySchedule",
                ButtonInteractionEvent.class, Map.class);
        method.setAccessible(true);
        Map<String, String> selections = getUserSelections().get(TEST_USER_ID);
        method.invoke(scheduleJdaBot, buttonEvent, selections);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(scheduleScraper).fetchScheduleHtml(TEST_USERNAME, "Engineering", "Semester 1", "Week 1");

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, atLeastOnce()).editOriginalEmbeds(embedCaptor.capture());

        boolean hasErrorEmbed = embedCaptor.getAllValues().stream()
                .anyMatch(embed -> "❌ Fehler".equals(embed.getTitle()));
        assertTrue(hasErrorEmbed, "Es sollte ein Fehler-Embed gesendet werden");
    }

    @Test
    void testCreateSelectMenu() throws Exception {
        List<String> options = Arrays.asList("Option 1", "Option 2", "Option 3");
        String selectId = "test-select";
        String placeholder = "Test Placeholder";

        Method method = ScheduleJdaBot.class.getDeclaredMethod("createSelectMenu",
                String.class, String.class, List.class);
        method.setAccessible(true);
        StringSelectMenu menu = (StringSelectMenu) method.invoke(scheduleJdaBot, selectId, placeholder, options);

        assertEquals(selectId, menu.getId());
        assertEquals(placeholder, menu.getPlaceholder());
        assertEquals(3, menu.getOptions().size());
        assertEquals("Option 1", menu.getOptions().get(0).getLabel());
        assertEquals("Option 2", menu.getOptions().get(1).getLabel());
        assertEquals("Option 3", menu.getOptions().get(2).getLabel());
    }

    @Test
    void testHandleSemesterSelectionWithError() throws Exception {
        setupUserSelections();
        getUserSelections().get(TEST_USER_ID).put(CustomId.DEPARTMENT.getId(), "Engineering");
        when(stringSelectEvent.getValues()).thenReturn(Arrays.asList("Semester 1"));
        when(metadataScraper.fetchWeeks(anyString(), anyString()))
                .thenThrow(new RuntimeException("Fehler beim Laden der Wochen"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleSemesterSelection", StringSelectInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, stringSelectEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(stringSelectEvent).deferEdit();
        verify(metadataScraper).fetchWeeks("Engineering", "Semester 1");

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, atLeastOnce()).editOriginalEmbeds(embedCaptor.capture());

        boolean hasErrorEmbed = embedCaptor.getAllValues().stream()
                .anyMatch(embed -> "❌ Fehler".equals(embed.getTitle()) &&
                        embed.getDescription().contains("Fehler beim Laden der Wochen"));
        assertTrue(hasErrorEmbed, "Es sollte ein Fehler-Embed zum Laden der Wochen gesendet werden");
    }

    @Test
    void testShowDepartmentSelectionWithError() throws Exception {
        setupUserSelections();
        when(metadataScraper.fetchDepartments())
                .thenThrow(new RuntimeException("Fehler beim Laden der Departments"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("showDepartmentSelection", ButtonInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, buttonEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(metadataScraper).fetchDepartments();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, atLeastOnce()).editOriginalEmbeds(embedCaptor.capture());

        boolean hasErrorEmbed = embedCaptor.getAllValues().stream()
                .anyMatch(embed -> "❌ Fehler".equals(embed.getTitle()) &&
                        embed.getDescription().contains("Fehler beim Laden der Departments"));
        assertTrue(hasErrorEmbed, "Es sollte ein Fehler-Embed zum Laden der Departments gesendet werden");
    }

    @Test
    void testFetchAndDisplayScheduleWithRuntimeException() throws Exception {
        setupCompleteUserSelections();

        String mockHtml = "<h1>Stundenplan für " + TEST_USERNAME + "</h1><p><b>Test User Info</b></p>";
        when(scheduleScraper.fetchScheduleHtml(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockHtml);
        when(screenshotGenerator.createScreenshot(anyString()))
                .thenReturn(null); // Simuliere einen null-Rückgabewert, der den Fehlerfall auslöst

        Method method = ScheduleJdaBot.class.getDeclaredMethod("fetchAndDisplaySchedule",
                ButtonInteractionEvent.class, Map.class);
        method.setAccessible(true);
        Map<String, String> selections = getUserSelections().get(TEST_USER_ID);
        method.invoke(scheduleJdaBot, buttonEvent, selections);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(scheduleScraper).fetchScheduleHtml(TEST_USERNAME, "Engineering", "Semester 1", "Week 1");
        verify(screenshotGenerator).createScreenshot(mockHtml);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, atLeastOnce()).editOriginalEmbeds(embedCaptor.capture());

        boolean hasErrorEmbed = embedCaptor.getAllValues().stream()
                .anyMatch(embed -> "❌ Fehler".equals(embed.getTitle()) &&
                        embed.getDescription().contains("Es ist ein Fehler beim Erstellen des Stundenplans aufgetreten"));
        assertTrue(hasErrorEmbed, "Es sollte ein Fehler-Embed bei null-Screenshot gesendet werden");
    }

    @Test
    void testHandleDepartmentSelectionWithError() throws Exception {
        setupUserSelections();
        when(stringSelectEvent.getValues()).thenReturn(java.util.List.of("Engineering"));

        when(metadataScraper.fetchSemesters(anyString()))
                .thenThrow(new RuntimeException("Fehler beim Laden der Semester"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleDepartmentSelection", StringSelectInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, stringSelectEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(stringSelectEvent).deferEdit();
        verify(metadataScraper).fetchSemesters("Engineering");

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, atLeastOnce()).editOriginalEmbeds(embedCaptor.capture());

        boolean hasErrorEmbed = embedCaptor.getAllValues().stream()
                .anyMatch(embed -> "❌ Fehler".equals(embed.getTitle()) &&
                        embed.getDescription().contains("Fehler beim Laden der Semester"));

        assertTrue(hasErrorEmbed, "Es sollte ein Fehler-Embed zum Laden der Semester gesendet werden");
    }
    // Helper methods
    private void setupUserSelections() throws Exception {
        Map<String, Map<String, String>> userSelections = getUserSelections();
        userSelections.computeIfAbsent(TEST_USER_ID, k -> new java.util.HashMap<>())
                .put(CustomId.USERNAME.getId(), TEST_USERNAME);
    }

    private void setupCompleteUserSelections() throws Exception {
        Map<String, Map<String, String>> userSelections = getUserSelections();
        Map<String, String> selections = userSelections.computeIfAbsent(TEST_USER_ID, k -> new java.util.HashMap<>());
        selections.put(CustomId.USERNAME.getId(), TEST_USERNAME);
        selections.put(CustomId.DEPARTMENT.getId(), "Engineering");
        selections.put(CustomId.SEMESTER.getId(), "Semester 1");
        selections.put(CustomId.WEEK.getId(), "Week 1");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> getUserSelections() throws Exception {
        Field field = ScheduleJdaBot.class.getDeclaredField("userSelections");
        field.setAccessible(true);
        return (Map<String, Map<String, String>>) field.get(scheduleJdaBot);
    }
}