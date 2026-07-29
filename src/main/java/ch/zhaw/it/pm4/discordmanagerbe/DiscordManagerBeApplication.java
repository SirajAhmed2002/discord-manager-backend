package ch.zhaw.it.pm4.discordmanagerbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class of the application, serving as the entry point for the Spring Boot application.
 * This class enables task scheduling and starts the application.
 */
@EnableScheduling
@SpringBootApplication
public class DiscordManagerBeApplication{

    /**
     * Main method that starts the Spring Boot application.
     *
     * @param args Arguments passed when starting the application
     */
    public static void main(String[] args){
        SpringApplication.run(DiscordManagerBeApplication.class, args);
    }
}
