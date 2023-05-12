package tech.ada.api.selenium;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.ada.api.selenium.home.HomePage;
import tech.ada.api.selenium.login.LoginPage;

public class LoginTest {

    @Test
    public void deveriaEfetuarLoginComDadosValidos() throws InterruptedException {
        LoginPage paginaDeLogin = new LoginPage();
        paginaDeLogin.efetuarLogin("joao", "123456");
        Thread.sleep(1000);
        String nomeUsuarioLogado = paginaDeLogin.getNomeUsuarioLogado();
        Assertions.assertEquals("joao", nomeUsuarioLogado);
        Assertions.assertFalse(paginaDeLogin.isPaginaAtual());
        paginaDeLogin.fechar();
    }

    @Test
    public void naoDeveriaEfetuarLoginComDadosInvalidos() throws InterruptedException {
        LoginPage paginaDeLogin = new LoginPage();
        paginaDeLogin.efetuarLogin("invalido", "1233");
        Thread.sleep(1000);
        Assertions.assertNull(paginaDeLogin.getNomeUsuarioLogado());
        Assertions.assertTrue(paginaDeLogin.isPaginaAtual());
        Assertions.assertTrue(paginaDeLogin.isMensagemDeLoginInvalidoVisivel());
        paginaDeLogin.fechar();
    }

    @Test
    public void naoDeveriaAcessarUrlRestritaSemEstarLogado() throws InterruptedException {
        HomePage paginaHome = new HomePage();
        Thread.sleep(1000);
        Assertions.assertFalse(paginaHome.isPaginaAtual());
        Assertions.assertFalse(paginaHome.isUsuarioLogadoVisivel());

        paginaHome.fechar();
    }

}
