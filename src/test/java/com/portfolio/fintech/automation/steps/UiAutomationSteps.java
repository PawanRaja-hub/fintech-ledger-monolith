package com.portfolio.fintech.automation.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class UiAutomationSteps {

    private final String baseUrl = System.getProperty("app.baseUrl", "http://127.0.0.1:8081");
    private WebDriver driver;
    private WebDriverWait wait;

    @Before("@ui")
    public void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("selenium.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @After("@ui")
    public void stopBrowser(Scenario scenario) throws IOException {
        if (driver == null) {
            return;
        }
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        scenario.attach(screenshot, "image/png", screenshotName(scenario));
        Path screenshotDir = Path.of("target", "selenium-screenshots");
        Files.createDirectories(screenshotDir);
        Files.write(screenshotDir.resolve(screenshotName(scenario) + ".png"), screenshot);
        driver.quit();
    }

    @Given("I open the fintech ledger web console")
    public void iOpenTheFintechLedgerWebConsole() {
        driver.get(baseUrl + "/");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("app-title")));
    }

    @When("I sign in as Platform Admin")
    public void iSignInAsPlatformAdmin() {
        signIn("admin@demo.local", "admin@demo.local signed in as ADMIN");
    }

    @When("I sign in as Alice Customer")
    public void iSignInAsAliceCustomer() {
        signIn("alice@demo.local", "alice@demo.local signed in as CUSTOMER");
    }

    @When("I open the Admin workspace")
    public void iOpenTheAdminWorkspace() {
        clickTab("Admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fund-form")));
    }

    @When("I fund the first wallet with {bigdecimal} from the web console")
    public void iFundTheFirstWalletWithFromTheWebConsole(java.math.BigDecimal amount) {
        wait.until(driver -> new Select(driver.findElement(By.id("fund-wallet"))).getOptions().size() > 0);
        WebElement amountInput = driver.findElement(By.id("fund-amount"));
        amountInput.clear();
        amountInput.sendKeys(amount.toPlainString());
        driver.findElement(By.cssSelector("#fund-form button[type='submit']")).click();
    }

    @Then("I should see the wallet funded message")
    public void iShouldSeeTheWalletFundedMessage() {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("fund-result"),
                "Wallet funded from system cash account"
        ));
    }

    @When("I open the Reconcile workspace")
    public void iOpenTheReconcileWorkspace() {
        clickTab("Reconcile");
        driver.findElement(By.id("refresh-reconcile")).click();
    }

    @Then("I should see reconciliation rows on the page")
    public void iShouldSeeReconciliationRowsOnThePage() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#reconcile-list .row"), 0));
        String text = driver.findElement(By.id("reconcile-list")).getText();
        assertThat(text).contains("MATCHED");
    }

    @Then("Bob should be available as a transfer recipient")
    public void bobShouldBeAvailableAsATransferRecipient() {
        wait.until(ExpectedConditions.textMatches(By.id("to-wallet"), Pattern.compile(".*bob@demo\\.local.*", Pattern.DOTALL)));
        assertThat(driver.findElement(By.id("to-wallet")).getText()).contains("bob@demo.local");
    }

    private void signIn(String email, String sessionText) {
        Select select = new Select(driver.findElement(By.id("demo-user")));
        select.selectByValue(email);
        driver.findElement(By.cssSelector("#login-form button[type='submit']")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("session-label"), sessionText));
    }

    private void clickTab(String tabName) {
        driver.findElement(By.xpath("//button[contains(@class,'tab') and normalize-space()='" + tabName + "']")).click();
    }

    private String screenshotName(Scenario scenario) {
        return scenario.getName()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
