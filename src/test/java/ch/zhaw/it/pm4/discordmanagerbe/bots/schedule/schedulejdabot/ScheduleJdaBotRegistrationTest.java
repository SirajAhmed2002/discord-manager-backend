package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.schedulejdabot;

import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaEventListenerService;
import ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service.JdaSlashCommandService;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.MetadataScraper;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScheduleJdaBot;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScheduleScraper;
import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.ScreenshotGenerator;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class ScheduleJdaBotRegistrationTest{

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

    private ScheduleJdaBot scheduleJdaBot;

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
    }

    @Test
    void testSetupCommands() throws Exception {
        Method setupCommandsMethod = ScheduleJdaBot.class.getDeclaredMethod("setupCommands");
        setupCommandsMethod.setAccessible(true);

        assertDoesNotThrow(() -> setupCommandsMethod.invoke(scheduleJdaBot));
    }

    @Test
    void testRegisterButtonInteractionHandlers() throws Exception {
        Method registerButtonInteractionHandlersMethod = ScheduleJdaBot.class.getDeclaredMethod("registerButtonInteractionHandlers");
        registerButtonInteractionHandlersMethod.setAccessible(true);

        assertDoesNotThrow(() -> registerButtonInteractionHandlersMethod.invoke(scheduleJdaBot));
    }

    @Test
    void testRegisterStringInteractionHandlers() throws Exception {
        Method registerStringInteractionHandlersMethod = ScheduleJdaBot.class.getDeclaredMethod("registerStringInteractionHandlers");
        registerStringInteractionHandlersMethod.setAccessible(true);

        assertDoesNotThrow(() -> registerStringInteractionHandlersMethod.invoke(scheduleJdaBot));
    }

    @Test
    void testRegisterModalInteractionHandlers() throws Exception {
        Method registerModalInteractionHandlersMethod = ScheduleJdaBot.class.getDeclaredMethod("registerModalInteractionHandlers");
        registerModalInteractionHandlersMethod.setAccessible(true);

        assertDoesNotThrow(() -> registerModalInteractionHandlersMethod.invoke(scheduleJdaBot));
    }

    @Test
    void testUnregisterCommands() {
        scheduleJdaBot.unregisterCommands();

        assertDoesNotThrow(() -> scheduleJdaBot.unregisterCommands());
    }

    @Test
    void testUnregisterCommandsWithInterrupt() throws Exception {
        Thread mainThread = Thread.currentThread();

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(50);
                mainThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        assertDoesNotThrow(() -> scheduleJdaBot.unregisterCommands());

        Thread.interrupted();
    }
}
