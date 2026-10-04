package com.qa.salesforce.base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.qa.salesforce.exceptions.FrameworkException;
import com.qa.salesforce.utils.ConfigReader;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Owns the WebDriver lifecycle. One Chromium session per test method keeps tests
 * isolated; a {@link ThreadLocal} holder makes the setup safe to parallelise later.
 */
public abstract class BaseTest {

    protected static final ConfigReader CONFIG = ConfigReader.getInstance();

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    protected WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException("WebDriver is not initialised for this thread");
        }
        return driver;
    }

    @BeforeSuite(alwaysRun = true)
    public void suiteSetup() {
        System.out.println("[suite] Salesforce login suite starting");
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        try {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-notifications");
            options.addArguments("--remote-allow-origins=*");
            if (CONFIG.isHeadless()) {
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
            } else {
                options.addArguments("--start-maximized");
            }

            ChromeDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            DRIVER.set(driver);
        } catch (Exception e) {
            throw new FrameworkException("Failed to initialise the Chromium driver", e);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            System.err.println("[teardown] Error while quitting the driver: " + e.getMessage());
        } finally {
            DRIVER.remove();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void suiteTeardown() {
        System.out.println("[suite] Salesforce login suite finished");
    }
}
