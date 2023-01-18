package tech.ada.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import tech.ada.api.dto.AnswerDto;
import tech.ada.api.dto.GameDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.exception.NotFoundException;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.impl.GameServiceImpl;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    private static final String API_URL = "/api/v1/game";

    private MockMvc mockMvc;

    @InjectMocks
    GameController gameController;

    @Mock
    GameServiceImpl gameService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @DisplayName("Testando a criacao de uma nova partida")
    @Test
    void whenGameStarted() throws Exception {
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Inicializando uma nova partida!", new GameDto());

        when(gameService.startGame()).thenReturn(genericResponse);

        mockMvc.perform(get(API_URL + "/startGame")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Inicializando uma nova partida!")));
    }

    @DisplayName("Quando o usuario atual nao e encontrado na base de dados")
    @Test
    void whenGameNotStartedBecauseCurrentUserNotFound() throws Exception {

        doThrow(NotFoundException.class).when(gameService).startGame();

        mockMvc.perform(get(API_URL + "/startGame")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Testando a inicializacao de uma nova rodada")
    @Test
    void whenNewRoundBegins() throws Exception {
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Inicializando uma nova rodada!", new GameDto());

        when(gameService.newRound(1L, "jogador1")).thenReturn(genericResponse);

        mockMvc.perform(get(API_URL + "/newRound?gameId=1&username=jogador1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Inicializando uma nova rodada!")));
    }

    @DisplayName("Quando o usuario informado nao e o responsavel pela partida")
    @Test
    void whenUsernameIsDifferentThanCurrentUserGameOnNewRound() throws Exception {

        doThrow(BadRequestException.class).when(gameService).newRound(1L, "jogador1");

        mockMvc.perform(get(API_URL + "/newRound?gameId=1&username=jogador1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Quando o jogo nao e encontrado na base de dados durante uma nova rodada")
    @Test
    void whenNewRoundNotStartedBecauseGameNotFound() throws Exception {

        doThrow(NotFoundException.class).when(gameService).newRound(1L, "jogador1");

        mockMvc.perform(get(API_URL + "/newRound?gameId=1&username=jogador1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Testando a o envio da resposta")
    @Test
    void whenSendAnAnswer() throws Exception {
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Resposta correta! Pontuação total: 1");

        when(gameService.answer(new AnswerDto())).thenReturn(genericResponse);

        mockMvc.perform(post(API_URL + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @DisplayName("Quando a rodada nao e encontrada na base de dados durante o registro de resposta")
    @Test
    void whenAnswerNotRegisteredBecauseRoundNotFound() throws Exception {

        doThrow(NotFoundException.class).when(gameService).answer(new AnswerDto());

        mockMvc.perform(post(API_URL + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Quando a rodada esta finalizada ao registrar uma resposta")
    @Test
    void whenAnswerNotRegisteredBecauseRoundIsFinished() throws Exception {

        doThrow(BadRequestException.class).when(gameService).answer(new AnswerDto());

        mockMvc.perform(post(API_URL + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Testando a finalizacao de uma partida")
    @Test
    void whenGameIsFinished() throws Exception {
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Partida finalizada!");

        when(gameService.stopGame(1L, "jogador1")).thenReturn(genericResponse);

        mockMvc.perform(get(API_URL + "/stopGame?gameId=1&username=jogador1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Partida finalizada!")));
    }

    @DisplayName("Quando o jogo nao e encontrado na base de dados durante a finalizacao da partida")
    @Test
    void whenGameNotFinishedBecauseGameNotFound() throws Exception {

        doThrow(NotFoundException.class).when(gameService).stopGame(1L, "jogador1");

        mockMvc.perform(get(API_URL + "/stopGame?gameId=1&username=jogador1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Quando o jogo ja esta finalizado ao tentar finalizar a partida")
    @Test
    void whenGameNotFinishedBecauseGameIsAlreadyFinished() throws Exception {

        doThrow(BadRequestException.class).when(gameService).stopGame(1L, "jogador1");

        mockMvc.perform(get(API_URL + "/stopGame?gameId=1&username=jogador1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Testando a exibicao do ranking")
    @Test
    void whenShowingRanking() throws Exception {
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Ranking exibido com sucesso!");

        when(gameService.getRanking()).thenReturn(genericResponse);

        mockMvc.perform(get(API_URL + "/ranking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Ranking exibido com sucesso!")));
    }

}
