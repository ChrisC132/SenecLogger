import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleTask extends TimerTask {
    private final String username;
    private final String password;
    private final String sqlConnectionString;

    public ScheduleTask(String username, String password, String sqlConnectionString) {
        this.username = username;
        this.password = password;
        this.sqlConnectionString = sqlConnectionString;
    }

    @Override
    public void run() {
        System.setProperty("webdriver.gecko.driver", "geckodriver.exe");

        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setPageLoadStrategy(PageLoadStrategy.EAGER);

        WebDriver driver = new FirefoxDriver(firefoxOptions);
        WebDriverWait wait = new WebDriverWait(driver, 5 * 1000);
        try {
            driver.get("https://mein-senec.de/auth/login");
            driver.findElement(By.name("username")).sendKeys(username);
            driver.findElement(By.name("password")).sendKeys(password + Keys.ENTER);

            Pattern pattern = Pattern.compile("\\d{1,3},\\d\\d\\skW");
            if(wait.until(ExpectedConditions.textMatches(By.className("iesValueBox"), pattern))) {
                String data = driver.findElement(By.className("clear")).getText();

                Pattern fetchDataPattern = Pattern.compile("Momentanwert:\\s(?<ErzeugtMomentan>\\d{1,3},\\d\\d)\\skW\\sTageswert:\\s(?<ErzeugtTageswert>\\d{1,3},\\d\\d)\\skWh\\sAkku\\sFüllstand\\sMomentanwert:\\s(?<AkkuMomentan>\\d{1,3},\\d\\d)\\s%\\s*Mein\\sHausverbrauch\\sMomentanwert:\\s(?<HausverbrauchMomentan>\\d{1,3},\\d\\d)\\skW\\sTageswert:\\s(?<HausverbrauchTageswert>\\d{1,3},\\d\\d)\\skWh\\sAkku-Beladung\\sMomentanwert:\\s(?<BeladungMomentan>\\d{1,3},\\d\\d)\\skW\\sTageswert:\\s(?<BeladungTageswert>\\d{1,3},\\d\\d)\\skWh\\sNetzstrom-Bezug\\sMomentanwert:\\s(?<NetzBezugMomentan>\\d{1,3},\\d\\d)\\skW\\sTageswert:\\s(?<NetzBezugTageswert>\\d{1,3},\\d\\d)\\skWh\\sAkku-Entnahme\\sMomentanwert:\\s(?<AkkuEntnahmeMomentan>\\d{1,3},\\d\\d)\\skW\\sTageswert:\\s(?<AkkuEntnahmeTageswert>\\d{1,3},\\d\\d)\\skWh\\sNetzstrom-Einspeisung\\sMomentanwert:\\s(?<NetzEinspeisungMomentan>\\d{1,3},\\d\\d)\\skW\\sTageswert:\\s(?<NetzEinspeisungTageswert>\\d{1,3},\\d\\d)\\skWh");
                Matcher matcher = fetchDataPattern.matcher(data);

                if(matcher.find()) {
                    String insertSQL = "INSERT INTO PVAnalge.Senec(ErzeugtMom, ErzeugtTag, AkkuFuellstandMom, HausverbrauchMom, HausverbrauchTag, AkkuLadungMom, AkkuLadungTag, NetzbezugMom, NetzbezugTag, AkkuEntnahmeMom, AkkuEntnahmeTag, NetzEinspeisungMom, NetzEinspeisungTag, TimeStmp " +
                            ") VALUES (" +
                            matcher.group("ErzeugtMomentan").replace(',', '.') + "," +
                            matcher.group("ErzeugtTageswert").replace(',', '.') + "," +
                            matcher.group("AkkuMomentan").replace(',', '.') + "," +
                            matcher.group("HausverbrauchMomentan").replace(',', '.') + "," +
                            matcher.group("HausverbrauchTageswert").replace(',', '.') + "," +
                            matcher.group("BeladungMomentan").replace(',', '.') + "," +
                            matcher.group("BeladungTageswert").replace(',', '.') + "," +
                            matcher.group("NetzBezugMomentan").replace(',', '.') + "," +
                            matcher.group("NetzBezugTageswert").replace(',', '.') + "," +
                            matcher.group("AkkuEntnahmeMomentan").replace(',', '.') + "," +
                            matcher.group("AkkuEntnahmeTageswert").replace(',', '.') + "," +
                            matcher.group("NetzEinspeisungMomentan").replace(',', '.') + "," +
                            matcher.group("NetzEinspeisungTageswert").replace(',', '.') + "," +
                            "now(3))";

                    System.out.println(insertSQL);

                    Connection conn = DriverManager.getConnection(sqlConnectionString);

                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate(insertSQL);

                    stmt.close();
                    conn.close();
                }
            }
        } catch(Exception ex) {
            System.err.println(ex);
        } finally {
            driver.quit();
        }
    }
}
