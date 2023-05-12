package tech.ada.api.selenium.home;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class HomePage {

    private static final String URL_HOME = "http://localhost:4200/private/home";

    private WebDriver browser;

    public HomePage() {
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        this.browser = new ChromeDriver(options);
        this.browser.navigate().to(URL_HOME);
    }

    public boolean isPaginaAtual() {
        return browser.getCurrentUrl().contains(URL_HOME);
    }

    public boolean isUsuarioLogadoVisivel() {
        return browser.getPageSource().contains("Usuário Logado");
    }

    public void fechar() {
        this.browser.quit();
    }

}
