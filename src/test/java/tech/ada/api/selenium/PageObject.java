package tech.ada.api.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class PageObject {

    protected WebDriver browser;

    public PageObject(WebDriver browser) {
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        if (browser != null) {
            this.browser = browser;
        } else {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            this.browser = new ChromeDriver(options);
        }
    }

    public void fechar() {
        this.browser.quit();
    }

}
