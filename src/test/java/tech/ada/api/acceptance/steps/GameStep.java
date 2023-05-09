package tech.ada.api.acceptance.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import tech.ada.api.controller.GameController;
import tech.ada.api.dto.GameDto;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.impl.GameServiceImpl;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GameStep {

    private static final String API_URL = "/api/v1/game";

    private final GameServiceImpl gameService = Mockito.mock(GameServiceImpl.class);

    private final MockMvc mockMvc;

    private ResultActions resultActions;

    private GenericResponse genericResponse;

    public GameStep() {
        GameController gameController = new GameController(gameService);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @Dado("um usuario qualquer")
    public void dado_usuario_qualquer() {
    }

    @Quando("ele seleciona para criacao de uma nova partida")
    public void quando_seleciona_nova_partida() throws Exception {
        genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Inicializando uma nova partida!", new GameDto());
        when(gameService.startGame()).thenReturn(genericResponse);
        resultActions = mockMvc.perform(get(API_URL + "/startGame")
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Entao("uma nova partida eh iniciada")
    public void entao_uma_nova_partida_eh_iniciada() throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
    }


}
