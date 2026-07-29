package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse ist verantwortlich für das Scrapen von Metadaten von der ZHAW Stundenplan-Webseite.
 * Sie verwendet Selenium WebDriver, um die Webseite zu laden und die benötigten Informationen zu extrahieren.
 */
@Component
public class MetadataScraper {
    private static final Logger logger = LoggerFactory.getLogger(MetadataScraper.class);
    private final WebDriverFactory webDriverFactory;
    private final String websiteUrl = "https://stundenplan.zhaw.ch/";

    /**
     * Konstruktor für die MetadataScraper-Klasse.
     * Initialisiert den WebDriverFactory, der für die Erstellung von WebDriver-Instanzen verwendet wird.
     *
     * @param webDriverFactory Die Factory, die WebDriver-Instanzen erstellt.
     */
    public MetadataScraper(WebDriverFactory webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

    /**
     * Holt die Liste der Departments von der Webseite.
     *
     * @return Eine Liste von Strings, die die Departments repräsentieren.
     */
    public List<String> fetchDepartments(){
        List<String> departments = new ArrayList<>();
        WebDriver driver = new ChromeDriver(webDriverFactory.getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try{
            driver.get(websiteUrl);

            WebElement departmentSelect = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select[name='ctl00$SelectionContent$selDepartment']")));
            List<WebElement> departmentOptions = departmentSelect.findElements(By.tagName("option"));

            for(WebElement option : departmentOptions){
                departments.add(option.getText());
            }

        } catch(Exception e){
            logger.error("Failed to find department as select: {}", e.getMessage());
        } finally{
            driver.quit();
        }

        return departments;
    }

    /**
     * Holt die Liste der Semester für ein bestimmtes Department.
     *
     * @param department Das Department, für das die Semester abgerufen werden sollen.
     * @return Eine Liste von Strings, die die Semester repräsentieren.
     */
    public List<String> fetchSemesters(String department){
        List<String> semesters = new ArrayList<>();
        WebDriver driver = new ChromeDriver(webDriverFactory.getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try{
            driver.get(websiteUrl);

            // Department auswählen
            WebElement departmentSelect = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select[name='ctl00$SelectionContent$selDepartment']")));
            Select deptDropdown = new Select(departmentSelect);
            deptDropdown.selectByVisibleText(department);

            // Warten, bis die Seite das Department-Update verarbeitet hat
            Thread.sleep(1000);

            // Semester-Select finden und Optionen auslesen
            WebElement semesterSelect = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select[name='ctl00$SelectionContent$selPeriodVersion']")));
            Select semDropdown = new Select(semesterSelect);

            for(WebElement option : semDropdown.getOptions()){
                semesters.add(option.getText().trim());
            }

        } catch(Exception e){
            logger.error("Fehler beim Abrufen der Semester: {}", e.getMessage());
        } finally{
            driver.quit();
        }

        return semesters;
    }

    /**
     * Holt die Liste der Wochen für ein bestimmtes Department und Semester.
     *
     * @param department Das Department, für das die Wochen abgerufen werden sollen.
     * @param semester   Das Semester, für das die Wochen abgerufen werden sollen.
     * @return Eine Liste von Strings, die die Wochen repräsentieren.
     */
    public List<String> fetchWeeks(String department, String semester){
        List<String> weeks = new ArrayList<>();
        WebDriver driver = new ChromeDriver(webDriverFactory.getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try{
            driver.get(websiteUrl);

            // Department auswählen
            WebElement departmentSelect = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select[name='ctl00$SelectionContent$selDepartment']")));
            Select deptDropdown = new Select(departmentSelect);
            deptDropdown.selectByVisibleText(department);

            // Warten auf Aktualisierung
            Thread.sleep(1000);

            // Semester auswählen
            WebElement semesterSelect = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select[name='ctl00$SelectionContent$selPeriodVersion']")));
            Select semDropdown = new Select(semesterSelect);
            semDropdown.selectByVisibleText(semester);

            // Warten auf Aktualisierung
            Thread.sleep(1000);

            // Wochen erst jetzt laden, nachdem Department und Semester ausgewählt wurden
            WebElement weekSelect = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("select[name='ctl00$SelectionContent$selWeek']")));
            List<WebElement> weekOptions = weekSelect.findElements(By.tagName("option"));

            for(WebElement option : weekOptions){
                weeks.add(option.getText());
            }

        } catch(Exception e){
            logger.error("Fehler beim Abrufen der Wochen: {}", e.getMessage());
        } finally{
            driver.quit();
        }

        return weeks;
    }


}