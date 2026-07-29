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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleJdaBotButtonHandlerTest {

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
    private ButtonInteractionEvent buttonEvent;

    @Mock
    private User user;

    @Mock
    private InteractionHook interactionHook;

    @Mock
    private MessageEditCallbackAction messageEditCallbackAction;

    @Mock
    private WebhookMessageCreateAction webhookMessageCreateAction;

    @Mock
    private WebhookMessageEditAction webhookMessageEditAction;

    @Mock
    private RestAction<Void> restAction;

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
        lenient().when(buttonEvent.getUser()).thenReturn(user);
        lenient().when(buttonEvent.getHook()).thenReturn(interactionHook);
        lenient().when(buttonEvent.deferEdit()).thenReturn(messageEditCallbackAction);
        lenient().doNothing().when(messageEditCallbackAction).queue();

        lenient().when(interactionHook.editOriginalEmbeds(any(MessageEmbed.class))).thenReturn(webhookMessageEditAction);
        lenient().when(interactionHook.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(webhookMessageCreateAction);
        lenient().when(interactionHook.deleteOriginal()).thenReturn(restAction);

        lenient().when(webhookMessageCreateAction.addComponents(any(ActionRow.class))).thenReturn(webhookMessageCreateAction);
        lenient().when(webhookMessageEditAction.setComponents(any(ActionRow.class))).thenReturn(webhookMessageEditAction);
        lenient().when(webhookMessageEditAction.setComponents(anyCollection())).thenReturn(webhookMessageEditAction);
        lenient().when(webhookMessageEditAction.setComponents(any(LayoutComponent[].class))).thenReturn(webhookMessageEditAction);
        lenient().when(webhookMessageEditAction.setFiles(any(FileUpload.class))).thenReturn(webhookMessageEditAction);

        lenient().doNothing().when(webhookMessageCreateAction).queue();
        lenient().doNothing().when(webhookMessageEditAction).queue();
        lenient().doNothing().when(restAction).queue();
    }

    @Test
    void testHandleCancelButton() throws Exception {
        Map<String, Map<String, String>> userSelections = getUserSelections();
        userSelections.computeIfAbsent(TEST_USER_ID, k -> new java.util.HashMap<>())
                .put(CustomId.USERNAME.getId(), TEST_USERNAME);

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleCancelButton", ButtonInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, buttonEvent);

        verify(buttonEvent).deferEdit();
        verify(interactionHook).deleteOriginal();

        assertNull(userSelections.get(TEST_USER_ID));
    }

    @Test
    void testHandleConfirmButton() throws Exception {
        Map<String, Map<String, String>> userSelections = getUserSelections();
        userSelections.computeIfAbsent(TEST_USER_ID, k -> new java.util.HashMap<>())
                .put(CustomId.USERNAME.getId(), TEST_USERNAME);

        when(metadataScraper.fetchDepartments()).thenReturn(Arrays.asList("Engineering", "Management"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleConfirmButton", ButtonInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, buttonEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(buttonEvent).deferEdit();
        verify(metadataScraper).fetchDepartments();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook, atLeastOnce()).editOriginalEmbeds(embedCaptor.capture());

        boolean hasDepartmentEmbed = embedCaptor.getAllValues().stream()
                .anyMatch(embed -> "🎓 Department auswählen".equals(embed.getTitle()));
        assertTrue(hasDepartmentEmbed, "Es sollte ein Department-Auswahl-Embed gesendet werden");
    }

    @Test
    void testHandleRestartButton() throws Exception {
        Map<String, Map<String, String>> userSelections = getUserSelections();
        userSelections.computeIfAbsent(TEST_USER_ID, k -> new java.util.HashMap<>())
                .put(CustomId.USERNAME.getId(), TEST_USERNAME);

        when(metadataScraper.fetchDepartments()).thenReturn(Arrays.asList("Engineering", "Management"));

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleRestartButton", ButtonInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, buttonEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(buttonEvent).deferEdit();
        verify(metadataScraper).fetchDepartments();
    }

    @Test
    void testHandleSubmitButton_WithCompleteSelections() throws Exception {
        Map<String, Map<String, String>> userSelections = getUserSelections();
        Map<String, String> selections = userSelections.computeIfAbsent(TEST_USER_ID, k -> new java.util.HashMap<>());
        selections.put(CustomId.USERNAME.getId(), TEST_USERNAME);
        selections.put(CustomId.DEPARTMENT.getId(), "Engineering");
        selections.put(CustomId.SEMESTER.getId(), "Semester 1");
        selections.put(CustomId.WEEK.getId(), "Week 1");

        String mockHtml = "<h1>Stundenplan für " + TEST_USERNAME + "</h1><p><b>Test User Info</b></p>";
        byte[] mockScreenshot = new byte[]{1, 2, 3, 4, 5};

        when(scheduleScraper.fetchScheduleHtml(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockHtml);
        when(screenshotGenerator.createScreenshot(anyString())).thenReturn(mockScreenshot);

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleSubmitButton", ButtonInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, buttonEvent);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        verify(buttonEvent).deferEdit();
        verify(scheduleScraper).fetchScheduleHtml(TEST_USERNAME, "Engineering", "Semester 1", "Week 1");
        verify(screenshotGenerator).createScreenshot(mockHtml);
    }

    @Test
    void testHandleSubmitButton_WithIncompleteSelections() throws Exception {
        Map<String, Map<String, String>> userSelections = getUserSelections();
        userSelections.computeIfAbsent(TEST_USER_ID, k -> new java.util.HashMap<>())
                .put(CustomId.USERNAME.getId(), TEST_USERNAME);

        Method method = ScheduleJdaBot.class.getDeclaredMethod("handleSubmitButton", ButtonInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, buttonEvent);

        verify(buttonEvent).deferEdit();

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).editOriginalEmbeds(embedCaptor.capture());

        MessageEmbed capturedEmbed = embedCaptor.getValue();
        assertEquals("❌ Fehler", capturedEmbed.getTitle());
        assertTrue(capturedEmbed.getDescription().contains("vollständige Auswahl"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> getUserSelections() throws Exception {
        Field field = ScheduleJdaBot.class.getDeclaredField("userSelections");
        field.setAccessible(true);
        return (Map<String, Map<String, String>>) field.get(scheduleJdaBot);
    }
}