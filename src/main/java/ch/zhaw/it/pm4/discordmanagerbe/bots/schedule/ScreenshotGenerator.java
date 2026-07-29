package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import ch.zhaw.it.pm4.discordmanagerbe.bots.schedule.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Diese Klasse ist verantwortlich für die Erstellung von Screenshots des Stundenplans.
 * Sie verwendet Selenium WebDriver, um den Stundenplan als HTML zu rendern und einen Screenshot zu erstellen.
 */
@Component
public class ScreenshotGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotGenerator.class);
    private final WebDriverFactory webDriverFactory;

    /**
     * Konstruktor für die ScreenshotGenerator-Klasse.
     * Initialisiert den WebDriverFactory, der für die Erstellung von WebDriver-Instanzen verwendet wird.
     *
     * @param webDriverFactory Die Factory, die WebDriver-Instanzen erstellt.
     */
    public ScreenshotGenerator(WebDriverFactory webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

    /**
     * Erstellt einen Screenshot des Stundenplans als Byte-Array.
     *
     * @param html Der HTML-String des Stundenplans.
     * @return Ein Byte-Array, das den Screenshot enthält.
     */
    public byte[] createScreenshot(String html){
        WebDriver driver = new ChromeDriver(webDriverFactory.getChromeOptions());

        try{
            // Temporäre HTML-Datei erstellen
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("stundenplan_", ".html");
            java.nio.file.Files.write(tempFile, html.getBytes());
            String filePath = tempFile.toUri().toString();

            // Die Datei im Browser öffnen
            driver.get(filePath);

            // Screenshot machen
            byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driver)
                    .getScreenshotAs(org.openqa.selenium.OutputType.BYTES);

            // Temporäre Datei löschen
            java.nio.file.Files.delete(tempFile);

            return screenshot;
        } catch(Exception e){
            logger.error("Failed to create screenshot: {}", e.getMessage());
            return null;
        } finally{
            driver.quit();
        }
    }
}