package tech.ada.api.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class HelloWorldSelenium {

    @Test
    public void helloTest() {
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver browser = new ChromeDriver(options);
        browser.navigate().to("http://localhost:8080/login");
        browser.quit();
    }

}
