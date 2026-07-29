package ch.zhaw.it.pm4.discordmanagerbe.botmaker.slashcommand.service;

import ch.zhaw.it.pm4.discordmanagerbe.service.JdaBotStatusCoordinatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Component responsible for initializing Discord slash commands after Spring Boot application startup.
 * Listens for ApplicationReadyEvent and triggers command initialization across all Discord servers.
 */
@Component
public class JdaSlashCommandsInitializer {

    /**
     * Logger instance for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(JdaSlashCommandsInitializer.class);

    /**
     * Service coordinator for managing bot status and command operations.
     */
    private final JdaBotStatusCoordinatorService botStatusCoordinator;

    /**
     * Constructs a new JdaSlashCommandsInitializer with the required dependencies.
     *
     * @param botStatusCoordinator the service coordinator for bot operations
     */
    @Autowired
    public JdaSlashCommandsInitializer(JdaBotStatusCoordinatorService botStatusCoordinator) {
        this.botStatusCoordinator = botStatusCoordinator;
    }

    /**
     * Event listener that initializes slash commands after the Spring application is fully started.
     * This method is automatically triggered when ApplicationReadyEvent is fired.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Initializing slash commands for all servers...");
        botStatusCoordinator.initializeAllCommands();
    }
}