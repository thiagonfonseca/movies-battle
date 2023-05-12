package tech.ada.api.selenium.login;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import tech.ada.api.selenium.PageObject;

public class LoginPage extends PageObject {

    private static final String URL_LOGIN = "http://localhost:4200/login";

    public LoginPage() {
        super(null);
        this.browser.navigate().to(URL_LOGIN);
    }

    private void preencherFormularioDeLogin(String username, String password) {
        browser.findElement(By.id("username")).sendKeys(username);
        browser.findElement(By.id("password")).sendKeys(password);
    }

    public void efetuarLogin(String username, String password) {
        this.preencherFormularioDeLogin(username, password);
        browser.findElement(By.id("login-button")).click();
    }

    public String getNomeUsuarioLogado() {
        try {
            return browser.findElement(By.id("usuario-logado")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public boolean isPaginaAtual() {
        return browser.getCurrentUrl().contains(URL_LOGIN);
    }

    public boolean isMensagemDeLoginInvalidoVisivel() {
        return browser.getPageSource().contains("Usuário e/ou senha inválidos!");
    }

}
