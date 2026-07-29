package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

/**
 * Diese Klasse ist verantwortlich für die Konfiguration und Erstellung von WebDriver-Instanzen.
 * Sie stellt sicher, dass die WebDriver-Optionen für den Headless-Betrieb und andere Einstellungen korrekt gesetzt sind.
 */
@Component
public class WebDriverFactory {

    /**
     * Erstellt und konfiguriert die ChromeOptions für den WebDriver.
     * Diese Optionen sind für den Headless-Betrieb optimiert und enthalten wichtige Argumente,
     * um die Leistung und Stabilität zu verbessern.
     *
     * @return Ein konfiguriertes ChromeOptions-Objekt.
     */
    public ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1200");
        return options;
    }
}