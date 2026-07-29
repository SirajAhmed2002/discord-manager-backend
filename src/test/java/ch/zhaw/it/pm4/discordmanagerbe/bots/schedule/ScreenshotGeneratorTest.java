package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreenshotGeneratorTest {

    @Mock
    private WebDriverFactory mockWebDriverFactory;

    @Mock
    private ChromeOptions mockChromeOptions;

    private ScreenshotGenerator screenshotGenerator;

    @BeforeEach
    void setUp() {
        when(mockWebDriverFactory.getChromeOptions()).thenReturn(mockChromeOptions);
        screenshotGenerator = new ScreenshotGenerator(mockWebDriverFactory);
    }

    @Test
    void createScreenshot_shouldGenerateScreenshot_whenHtmlIsValid() throws Exception {
        String html = "<html><body><h1>Stundenplan Test</h1></body></html>";
        byte[] expectedScreenshot = "screenshot data".getBytes();
        Path mockPath = mock(Path.class);

        when(mockPath.toUri()).thenReturn(new java.net.URI("file:///tmp/test.html"));

        try (MockedStatic<Files> mockFiles = mockStatic(Files.class);
             MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class,
                     (mock, context) -> {
                         // ChromeDriver auch als TakesScreenshot mocken
                         when(((TakesScreenshot) mock).getScreenshotAs(OutputType.BYTES)).thenReturn(expectedScreenshot);
                     })) {

            mockFiles.when(() -> Files.createTempFile(anyString(), anyString())).thenReturn(mockPath);
            mockFiles.when(() -> Files.write(any(Path.class), any(byte[].class))).thenReturn(mockPath);
            mockFiles.when(() -> Files.delete(any(Path.class))).thenAnswer(invocation -> null);

            byte[] result = screenshotGenerator.createScreenshot(html);

            assertEquals(1, mockChromeDriver.constructed().size());
            WebDriver mockDriver = mockChromeDriver.constructed().get(0);

            verify(mockWebDriverFactory).getChromeOptions();

            mockFiles.verify(() -> Files.createTempFile(eq("stundenplan_"), eq(".html")));
            mockFiles.verify(() -> Files.write(eq(mockPath), any(byte[].class)));
            mockFiles.verify(() -> Files.delete(eq(mockPath)));

            verify(mockDriver).get(anyString());

            assertArrayEquals(expectedScreenshot, result);

            verify(mockDriver).quit();
        }
    }

    @Test
    void createScreenshot_shouldReturnNull_whenExceptionOccurs() throws Exception {
        String html = "<html><body><h1>Fehlerfall Test</h1></body></html>";

        try (MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class,
                (mock, context) -> {
                    // Simuliere einen Fehler beim Aufruf von get()
                    doThrow(new RuntimeException("Browser-Fehler")).when(mock).get(anyString());
                })) {

            byte[] result = screenshotGenerator.createScreenshot(html);

            assertNull(result);

            WebDriver mockDriver = mockChromeDriver.constructed().get(0);
            verify(mockDriver).quit();
        }
    }

    @Test
    void createScreenshot_shouldHandleFileOperationFailures() throws Exception {
        String html = "<html><body><h1>Dateisystem-Fehler Test</h1></body></html>";

        try (MockedStatic<Files> mockFiles = mockStatic(Files.class);
             MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class)) {

            mockFiles.when(() -> Files.createTempFile(anyString(), anyString()))
                    .thenThrow(new java.io.IOException("Datei konnte nicht erstellt werden"));

            byte[] result = screenshotGenerator.createScreenshot(html);

            assertNull(result);

            WebDriver mockDriver = mockChromeDriver.constructed().get(0);
            verify(mockDriver).quit();
        }
    }
}