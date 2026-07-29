package ch.zhaw.it.pm4.discordmanagerbe.bots.schedule;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Diese Klasse ist verantwortlich für das Scrapen des Stundenplans von der ZHAW Webseite.
 * Sie verwendet Selenium WebDriver, um die Webseite zu laden und den Stundenplan als HTML-String zu extrahieren.
 */
@Component
public class ScheduleScraper{

    private static final Logger logger = LoggerFactory.getLogger(ScheduleScraper.class);

    private final WebDriverFactory webDriverFactory;

    private final String websiteUrl = "https://stundenplan.zhaw.ch/";

    public ScheduleScraper(WebDriverFactory webDriverFactory) {
        this.webDriverFactory = webDriverFactory;
    }

    /**
     * Holt den Stundenplan als HTML-String für einen bestimmten Benutzer, Department, Semester und Woche.
     *
     * @param username   Der Benutzername des Studenten.
     * @param department Das Department des Studenten.
     * @param semester   Das Semester des Studenten.
     * @param week       Die Woche des Stundenplans (optional).
     * @return Der Stundenplan als HTML-String.
     */
    public String fetchScheduleHtml(String username, String department, String semester, String week){
        WebDriver driver = new ChromeDriver(webDriverFactory.getChromeOptions());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try{
            driver.get(websiteUrl);

            // Benutzername eingeben und suchen
            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='ctl00$SelectionContent$txtSearch']")));
            searchBox.sendKeys(username);
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[id='SelectionContent_iptSearch']")));
            searchButton.click();

            // Department auswählen
            WebElement departmentDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("select[name='ctl00$SelectionContent$selDepartment']")));
            Select departmentSelect = new Select(departmentDropdown);
            departmentSelect.selectByVisibleText(department);

            // Semester auswählen
            WebElement semesterDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("select[name='ctl00$SelectionContent$selPeriodVersion']")));
            Select semesterSelect = new Select(semesterDropdown);
            semesterSelect.selectByVisibleText(semester);

            // Woche auswählen (neu)
            if(week!=null && ! week.isEmpty()){
                WebElement weekDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("select[name='ctl00$SelectionContent$selWeek']")));
                Select weekSelect = new Select(weekDropdown);
                weekSelect.selectByVisibleText(week);
            }

            Thread.sleep(1000);

            try{
                // Benutzerinfo holen
                WebElement userInfo = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("span#MainContent_lblSelectedEntitiesNames")));
                String userInfoText = userInfo.getText().trim();

                // Username extrahieren (alles vor dem Komma)
                String usernameOnly = userInfoText.contains(",") ? userInfoText.split(",")[0].trim() : userInfoText;

                // Stundenplan-Tabelle finden
                WebElement scheduleTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("schedTable")));

                // HTML für Stundenplan generieren mit verbessertem CSS
                StringBuilder html = new StringBuilder();
                html.append("<!DOCTYPE html><html><head>");
                html.append("<meta charset='UTF-8'><style>");
                html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
                html.append("h1 { color: #2c3e50; }");
                html.append("p { margin-top: 5px; margin-bottom: 15px; }");
                html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
                html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
                html.append("th { background-color: #5865F2; color: white; }");
                html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
                html.append("td.lesson { background-color: #e3f2fd; }");
                html.append("td.time-slot { font-weight: bold; background-color: #f0f0f0; }");
                html.append("</style></head><body>");

                // Titel und Info-Zeile mit allen Details
                html.append("<h1>Stundenplan für ").append(usernameOnly).append("</h1>");
                html.append("<p>").append(userInfoText)
                        .append(" &nbsp;|&nbsp; <b>Semester:</b> ").append(semester)
                        .append(" &nbsp;|&nbsp; <b>Woche:</b> ").append(week)
                        .append("</p>");

                // Tabelle erstellen
                html.append("<table>");

                // Header-Zeile
                html.append("<tr>");
                List<WebElement> headers = scheduleTable.findElements(By.tagName("th"));
                for(WebElement header : headers){
                    html.append("<th>").append(header.getText()).append("</th>");
                }
                html.append("</tr>");

                // Zeilen mit Daten
                List<WebElement> rows = scheduleTable.findElements(By.tagName("tr"));
                for(int i = 1; i < rows.size(); i++){
                    html.append("<tr>");
                    List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));

                    for(WebElement cell : cells){
                        String cellClass = "";

                        // Prüfen, ob die Zelle ein rowspan-Attribut hat
                        String rowspan = cell.getAttribute("rowspan");
                        String rowspanAttr = (rowspan!=null && ! rowspan.isEmpty()) ?
                                " rowspan=\"" + rowspan + "\"" : "";

                        // Prüfen, ob die Zelle ein colspan-Attribut hat
                        String colspan = cell.getAttribute("colspan");
                        String colspanAttr = (colspan!=null && ! colspan.isEmpty()) ?
                                " colspan=\"" + colspan + "\"" : "";

                        // Spezielle Formatierung für Zeitslots in der ersten Spalte
                        if(Objects.requireNonNull(cell.getAttribute("class")).contains("schedTableSlotColumn")){
                            cellClass = " class='time-slot'";
                            html.append("<td").append(cellClass).append(rowspanAttr).append(colspanAttr).append(">")
                                    .append(cell.getText().trim())
                                    .append("</td>");
                        }
                        // Formatierung für Lektionen
                        else if(Objects.requireNonNull(cell.getAttribute("class")).contains("schedPersonal")){
                            cellClass = " class='lesson'";
                            html.append("<td").append(cellClass).append(rowspanAttr).append(colspanAttr).append(">");

                            // Fächer extrahieren
                            List<WebElement> lessons = cell.findElements(By.className("schedLessonList"));
                            for(WebElement lessonList : lessons){
                                List<WebElement> lessonDivs = lessonList.findElements(By.tagName("div"));
                                for(WebElement lessonDiv : lessonDivs){
                                    String lessonText = lessonDiv.getText().replace("\n", "<br>");
                                    html.append(lessonText).append("<br>");
                                }
                            }
                            html.append("</td>");
                        }
                        // Leere Zellen
                        else{
                            html.append("<td").append(rowspanAttr).append(colspanAttr).append("></td>");
                        }
                    }
                    html.append("</tr>");
                }

                html.append("</table></body></html>");
                return html.toString();

            } catch(Exception e){
                return "<html><body><h1>Fehler</h1><p>Keine Ergebnisse gefunden für: " + username +
                        " mit Department " + department + " und Semester " + semester + "</p></body></html>";
            }

        } catch(Exception e){
            return "<html><body><h1>Fehler</h1><p>Ein unerwarteter Fehler ist aufgetreten: " +
                    e.getMessage() + "</p></body></html>";
        } finally{
            driver.quit();
        }
    }



}