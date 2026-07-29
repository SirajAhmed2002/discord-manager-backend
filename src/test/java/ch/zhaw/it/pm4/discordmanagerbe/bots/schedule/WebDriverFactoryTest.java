package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebDriverFactoryTest {

    @Test
    void testGetChromeOptions() {
        try (MockedConstruction<ChromeOptions> mocked = mockConstruction(ChromeOptions.class)) {
            WebDriverFactory factory = new WebDriverFactory();
            ChromeOptions options = factory.getChromeOptions();

            assertEquals(1, mocked.constructed().size());

            ChromeOptions mockedOptions = mocked.constructed().get(0);

            ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockedOptions, times(5)).addArguments(argumentCaptor.capture());

            List<String> capturedArguments = argumentCaptor.getAllValues();

            assertTrue(capturedArguments.contains("--headless"),
                    "ChromeOptions sollten '--headless' enthalten");
            assertTrue(capturedArguments.contains("--disable-gpu"),
                    "ChromeOptions sollten '--disable-gpu' enthalten");
            assertTrue(capturedArguments.contains("--no-sandbox"),
                    "ChromeOptions sollten '--no-sandbox' enthalten");
            assertTrue(capturedArguments.contains("--disable-dev-shm-usage"),
                    "ChromeOptions sollten '--disable-dev-shm-usage' enthalten");
            assertTrue(capturedArguments.contains("--window-size=1920,1200"),
                    "ChromeOptions sollten '--window-size=1920,1200' enthalten");

            assertEquals(5, capturedArguments.size(),
                    "ChromeOptions sollten genau 5 Argumente haben");
        }
    }

    @Test
    void testGetChromeOptionsAlternativ() {
        ChromeOptions mockOptions = mock(ChromeOptions.class);

        WebDriverFactory factory = new WebDriverFactory() {
            @Override
            public ChromeOptions getChromeOptions() {
                mockOptions.addArguments("--headless");
                mockOptions.addArguments("--disable-gpu");
                mockOptions.addArguments("--no-sandbox");
                mockOptions.addArguments("--disable-dev-shm-usage");
                mockOptions.addArguments("--window-size=1920,1150");
                return mockOptions;
            }
        };

        ChromeOptions options = factory.getChromeOptions();

        assertEquals(mockOptions, options);

        verify(mockOptions).addArguments("--headless");
        verify(mockOptions).addArguments("--disable-gpu");
        verify(mockOptions).addArguments("--no-sandbox");
        verify(mockOptions).addArguments("--disable-dev-shm-usage");
        verify(mockOptions).addArguments("--window-size=1920,1150");

        verify(mockOptions, times(5)).addArguments(anyString());
    }
}