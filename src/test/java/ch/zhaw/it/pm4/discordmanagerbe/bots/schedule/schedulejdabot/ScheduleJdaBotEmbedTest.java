package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.schedulejdabot;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.MetadataScraper;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScheduleJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScheduleScraper;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScreenshotGenerator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Color;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ScheduleJdaBotEmbedTest{

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
    private StringSelectInteractionEvent stringSelectEvent;

    @Mock
    private InteractionHook interactionHook;

    @Mock
    private WebhookMessageEditAction webhookMessageEditAction;

    private ScheduleJdaBot scheduleJdaBot;

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

        lenient().when(buttonEvent.getHook()).thenReturn(interactionHook);
        lenient().when(stringSelectEvent.getHook()).thenReturn(interactionHook);
        lenient().when(interactionHook.editOriginalEmbeds(any(MessageEmbed.class))).thenReturn(webhookMessageEditAction);
        lenient().when(webhookMessageEditAction.setComponents()).thenReturn(webhookMessageEditAction);
        lenient().doNothing().when(webhookMessageEditAction).queue();
    }

    @Test
    void testCreateEmbed() throws Exception {
        Method method = ScheduleJdaBot.class.getDeclaredMethod("createEmbed", String.class, String.class, Color.class);
        method.setAccessible(true);
        MessageEmbed embed = (MessageEmbed) method.invoke(scheduleJdaBot, "Test Title", "Test Description", Color.BLUE);

        assertEquals("Test Title", embed.getTitle());
        assertEquals("Test Description", embed.getDescription());
        assertEquals(Color.BLUE.getRGB(), embed.getColor().getRGB());
    }

    @Test
    void testCreateLoadingEmbed() throws Exception {
        Method method = ScheduleJdaBot.class.getDeclaredMethod("createLoadingEmbed");
        method.setAccessible(true);
        MessageEmbed embed = (MessageEmbed) method.invoke(scheduleJdaBot);

        assertEquals("⏳ Verarbeitung", embed.getTitle());
        assertEquals("Der Bot denkt nach...", embed.getDescription());
        assertEquals(Color.GRAY.getRGB(), embed.getColor().getRGB());
    }

    @Test
    void testCreateErrorEmbed() throws Exception {
        String errorMessage = "Test error message";

        Method method = ScheduleJdaBot.class.getDeclaredMethod("createErrorEmbed", String.class);
        method.setAccessible(true);
        MessageEmbed embed = (MessageEmbed) method.invoke(scheduleJdaBot, errorMessage);

        assertEquals("❌ Fehler", embed.getTitle());
        assertEquals(errorMessage, embed.getDescription());
        assertEquals(Color.RED.getRGB(), embed.getColor().getRGB());
    }

    @Test
    void testExtractUserInfoWithMalformedHtml() throws Exception {
        String malformedHtml = "<div>Kein p-Tag vorhanden</div>";

        Method method = ScheduleJdaBot.class.getDeclaredMethod("extractUserInfo", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(scheduleJdaBot, malformedHtml);

        assertEquals("", result);
    }

    @Test
    void testExtractUsername() throws Exception {
        String htmlWithUsername = "<h1>Stundenplan für " + TEST_USERNAME + "</h1>";

        Method method = ScheduleJdaBot.class.getDeclaredMethod("extractUsername", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(scheduleJdaBot, htmlWithUsername);

        assertEquals(TEST_USERNAME, result);
    }

    @Test
    void testExtractUsernameWithMalformedHtml() throws Exception {
        String malformedHtml = "<div>Kein h1-Tag vorhanden</div>";

        Method method = ScheduleJdaBot.class.getDeclaredMethod("extractUsername", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(scheduleJdaBot, malformedHtml);

        assertEquals("", result);
    }

    @Test
    void testShowLoadingEmbedWithButtonEvent() throws Exception {
        Method method = ScheduleJdaBot.class.getDeclaredMethod("showLoadingEmbed", ButtonInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, buttonEvent);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).editOriginalEmbeds(embedCaptor.capture());

        MessageEmbed loadingEmbed = embedCaptor.getValue();
        assertEquals("⏳ Verarbeitung", loadingEmbed.getTitle());
        assertEquals("Der Bot denkt nach...", loadingEmbed.getDescription());
        assertEquals(Color.GRAY.getRGB(), loadingEmbed.getColor().getRGB());
    }

    @Test
    void testShowLoadingEmbedWithStringSelectEvent() throws Exception {
        Method method = ScheduleJdaBot.class.getDeclaredMethod("showLoadingEmbed", StringSelectInteractionEvent.class);
        method.setAccessible(true);
        method.invoke(scheduleJdaBot, stringSelectEvent);

        ArgumentCaptor<MessageEmbed> embedCaptor = ArgumentCaptor.forClass(MessageEmbed.class);
        verify(interactionHook).editOriginalEmbeds(embedCaptor.capture());

        MessageEmbed loadingEmbed = embedCaptor.getValue();
        assertEquals("⏳ Verarbeitung", loadingEmbed.getTitle());
    }
}
