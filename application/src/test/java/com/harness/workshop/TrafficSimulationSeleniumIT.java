package com.harness.workshop;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Selenium UI test: logs in, clicks "Simulate users & generate traffic", and waits
 * until ≥350 impressions/events per on/off treatment are generated.
 *
 * Requires SPLIT_SDK_KEY and Chrome. Skipped when SDK key is unset.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrafficSimulationSeleniumIT {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        assumeTrue(
                System.getenv("SPLIT_SDK_KEY") != null && !System.getenv("SPLIT_SDK_KEY").isBlank(),
                "SPLIT_SDK_KEY required for FME simulation integration test");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofMinutes(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void simulateTrafficFromUi() {
        String base = "http://localhost:" + port;

        driver.get(base + "/");
        Select userSelect = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.name("userId"))));
        userSelect.selectByValue("u001");
        driver.findElement(By.cssSelector("button.hx-btn")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));

        driver.get(base + "/simulate");
        wait.until(ExpectedConditions.urlContains("/simulate"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btnSimulate")));

        driver.findElement(By.id("minPerTreatment")).clear();
        driver.findElement(By.id("minPerTreatment")).sendKeys("350");
        driver.findElement(By.id("btnSimulate")).click();

        wait.until(d -> {
            String text = driver.findElement(By.id("resultBox")).getText();
            return text.contains("\"status\" : \"COMPLETE\"") || text.contains("\"status\":\"COMPLETE\"");
        });

        String result = driver.findElement(By.id("resultBox")).getText();
        assertTrue(result.contains("COMPLETE"), "Simulation should complete with COMPLETE status: " + result);
    }
}
