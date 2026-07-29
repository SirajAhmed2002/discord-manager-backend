package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataScraperTest{

    @Mock
    private WebDriverFactory mockWebDriverFactory;

    @Mock
    private ChromeOptions mockChromeOptions;

    private MetadataScraper metadataScraper;

    private final List<String> departments = new ArrayList<>();
    private final List<String> semestersEngineering = new ArrayList<>();
    private final List<String> semestersArchitecture = new ArrayList<>();
    private final List<String> weeks = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(mockWebDriverFactory.getChromeOptions()).thenReturn(mockChromeOptions);
        metadataScraper = new MetadataScraper(mockWebDriverFactory);

        departments.add("(T) School of Engineering");
        departments.add("(A) Architektur, Gestaltung und Bauingenieurwesen");

        semestersEngineering.add("Frühlingssemester 2025");

        semestersArchitecture.add("Frühlingssemester 2025");

        weeks.add("24.03.2025 - 30.03.2025 (13)");
    }

    @Test
    void fetchDepartments_sollteNichtNullSein_wennScrapenErfolgreich() {
        WebElement mockDepartmentSelect = mock(WebElement.class);
        List<WebElement> optionsList = createMockOptions(departments);

        when(mockDepartmentSelect.findElements(By.tagName("option"))).thenReturn(optionsList);

        try (MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class);
             MockedConstruction<WebDriverWait> mockWait = mockConstruction(WebDriverWait.class,
                     (mock, context) -> when(mock.until(any())).thenReturn(mockDepartmentSelect))) {

            List<String> result = metadataScraper.fetchDepartments();

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(departments.size(), result.size());
            assertEquals(departments.get(0), result.get(0));
            assertEquals(departments.get(1), result.get(1));

            WebDriver driver = mockChromeDriver.constructed().get(0);
            verify(driver).get(anyString());
            verify(driver).quit();
        }
    }

    @Test
    void fetchDepartments_sollteLeereListeZurückgeben_wennScrapenFehlschlägt() {
        try (MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class);
             MockedConstruction<WebDriverWait> mockWait = mockConstruction(WebDriverWait.class,
                     (mock, context) -> when(mock.until(any())).thenThrow(new RuntimeException("Test-Fehler")))) {

            List<String> result = metadataScraper.fetchDepartments();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            WebDriver driver = mockChromeDriver.constructed().get(0);
            verify(driver).quit();
        }
    }

    @Test
    void fetchSemesters_sollteNichtNullSein_wennScrapenErfolgreich() throws InterruptedException {
        String department = "(T) School of Engineering";
        List<String> semesters = semestersEngineering;

        WebElement mockDepartmentSelect = mock(WebElement.class);
        WebElement mockSemesterSelect = mock(WebElement.class);

        List<WebElement> optionsList = createMockOptions(semesters);

        final Select[] selectMocks = new Select[2];

        try (MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class);
             MockedConstruction<WebDriverWait> mockWait = mockConstruction(WebDriverWait.class,
                     (mock, context) -> {
                         when(mock.until(any())).thenReturn(mockDepartmentSelect, mockSemesterSelect);
                     });
             MockedConstruction<Select> mockSelect = mockConstruction(Select.class,
                     (mock, context) -> {
                         int index = selectMocks[0] == null ? 0 : 1;
                         selectMocks[index] = mock;

                         if (index == 0) {
                             doNothing().when(mock).selectByVisibleText(anyString());
                         } else {
                             when(mock.getOptions()).thenReturn(optionsList);
                         }
                     })) {

            List<String> result = metadataScraper.fetchSemesters(department);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(semesters.size(), result.size());
            assertEquals(semesters.get(0), result.get(0));

            WebDriver driver = mockChromeDriver.constructed().get(0);
            verify(driver).get(anyString());
            verify(driver).quit();
        }
    }

    @Test
    void fetchSemesters_sollteLeereListeZurückgeben_wennScrapenFehlschlägt() {
        String department = "(T) School of Engineering";

        try (MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class);
             MockedConstruction<WebDriverWait> mockWait = mockConstruction(WebDriverWait.class,
                     (mock, context) -> when(mock.until(any())).thenThrow(new RuntimeException("Test-Fehler")))) {

            List<String> result = metadataScraper.fetchSemesters(department);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            WebDriver driver = mockChromeDriver.constructed().get(0);
            verify(driver).quit();
        }
    }

    @Test
    void fetchWeeks_sollteNichtNullSein_wennScrapenErfolgreich() throws InterruptedException {
        String department = "(T) School of Engineering";
        String semester = "Frühlingssemester 2025";

        WebElement mockDepartmentSelect = mock(WebElement.class);
        WebElement mockSemesterSelect = mock(WebElement.class);
        WebElement mockWeekSelect = mock(WebElement.class);

        List<WebElement> optionsList = createMockOptions(weeks);

        when(mockWeekSelect.findElements(By.tagName("option"))).thenReturn(optionsList);

        try (MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class);
             MockedConstruction<WebDriverWait> mockWait = mockConstruction(WebDriverWait.class,
                     (mock, context) -> {
                         when(mock.until(any())).thenReturn(mockDepartmentSelect, mockSemesterSelect, mockWeekSelect);
                     });
             MockedConstruction<Select> mockSelect = mockConstruction(Select.class)) {

            List<String> result = metadataScraper.fetchWeeks(department, semester);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(weeks.size(), result.size());
            assertEquals(weeks.get(0), result.get(0));

            WebDriver driver = mockChromeDriver.constructed().get(0);
            verify(driver).get(anyString());
            verify(driver).quit();
        }
    }

    @Test
    void fetchWeeks_sollteLeereListeZurückgeben_wennScrapenFehlschlägt() {
        String department = "(T) School of Engineering";
        String semester = "Frühlingssemester 2025";

        try (MockedConstruction<ChromeDriver> mockChromeDriver = mockConstruction(ChromeDriver.class);
             MockedConstruction<WebDriverWait> mockWait = mockConstruction(WebDriverWait.class,
                     (mock, context) -> when(mock.until(any())).thenThrow(new RuntimeException("Test-Fehler")))) {

            List<String> result = metadataScraper.fetchWeeks(department, semester);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            WebDriver driver = mockChromeDriver.constructed().get(0);
            verify(driver).quit();
        }
    }

    /**
     * Hilfsmethode zum Erstellen von Mock-WebElements
     */
    private List<WebElement> createMockOptions(List<String> optionTexts) {
        List<WebElement> options = new ArrayList<>();
        for (String text : optionTexts) {
            WebElement option = mock(WebElement.class);
            when(option.getText()).thenReturn(text);
            options.add(option);
        }
        return options;
    }
}