package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleScraperTest{

    private WebDriverFactory mockWebDriverFactory;
    private ScheduleScraper scheduleScraper;

    @BeforeEach
    void setUp(){
        mockWebDriverFactory = mock(WebDriverFactory.class);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Headless-Modus für Testumgebungen
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        when(mockWebDriverFactory.getChromeOptions()).thenReturn(options);

        scheduleScraper = new ScheduleScraper(mockWebDriverFactory);
    }

    @Test
    void testFetchScheduleHtml_WithInvalidUser_ReturnsErrorHtml(){
        String html = scheduleScraper.fetchScheduleHtml("Siraj Ahmed", "(T) School of Engineering", "Frühlingssemester 2025", "14.04.2025 - 20.04.2025 (16)");

        assertNotNull(html);
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Keine Ergebnisse gefunden für:"));
    }

    @Test
    void testFetchScheduleHtml_WithoutWeek_ShouldNotFail(){
        String html = scheduleScraper.fetchScheduleHtml("ahmedsir", "(T) School of Engineering", "Frühlingssemester 2025", null);
        assertNotNull(html);
        assertTrue(html.contains("<table"));
        assertTrue(html.contains("Stundenplan"));
    }

    @Test
    void testFetchScheduleHtml_SuccessCase(){
        String html = scheduleScraper.fetchScheduleHtml("ahmedsir", "(T) School of Engineering", "Frühlingssemester 2025", "14.04.2025 - 20.04.2025 (16)");
        assertNotNull(html);
        assertTrue(html.contains("<table"));
        assertTrue(html.contains("Stundenplan"));
    }

    @Test
    void testErrorFalseParameters(){
        String html = scheduleScraper.fetchScheduleHtml("ahmedsir", "(A) Architektur, Gestaltung und Bauingenieurwesen", "Frühlingssemester 2025", "(A) Architektur, Gestaltung und Bauingenieurwesen");
        assertNotNull(html);
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("Ein unerwarteter Fehler ist aufgetreten"));
    }
}
