package tech.ada.api.service;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.ada.api.dto.AnswerDto;
import tech.ada.api.dto.GameDto;
import tech.ada.api.dto.MovieDto;
import tech.ada.api.dto.RankingDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.exception.NotFoundException;
import tech.ada.api.model.Game;
import tech.ada.api.model.Movie;
import tech.ada.api.model.Round;
import tech.ada.api.model.User;
import tech.ada.api.repository.GameRepository;
import tech.ada.api.repository.MovieRepository;
import tech.ada.api.repository.RoundRepository;
import tech.ada.api.repository.UserRepository;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.impl.GameServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @InjectMocks
    GameServiceImpl gameService;

    @Mock
    GameRepository gameRepository;
    @Mock
    RoundRepository roundRepository;
    @Mock
    MovieRepository movieRepository;
    @Mock
    UserRepository userRepository;

    User user;
    Game game;
    Game savedGame;
    Movie movie1;
    Movie movie2;
    List<Movie> movies;
    Round round;
    Round savedRound;
    GameDto gameDto;
    AnswerDto answerDto;
    List<RankingDto> ranking;
    String username;

    @BeforeEach
    void setUp() {
        username = "jogador1";
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        user = new User();
        user.setUsername(username);
        game = populateGame();
        savedGame = populateGame();
        savedGame.setId(1L);
        movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Sonic The Hedgehog");
        movie2 = new Movie();
        movie2.setId(2L);
        movie2.setTitle("Sonic The Hedgehog 2");
        movies = new ArrayList<>();
        movies.add(movie1);
        movies.add(movie2);
        round = populateRound(game);
        savedRound = populateRound(savedGame);
        savedRound.setId(1L);
        gameDto = new GameDto(savedGame);
        gameDto.setRoundId(1L);
        gameDto.setMovie1(new MovieDto(movie1));
        gameDto.setMovie2(new MovieDto(movie2));
        answerDto = new AnswerDto();
        answerDto.setGameId(1L);
        answerDto.setUsername(username);
        answerDto.setRoundId(1L);
        answerDto.setAnswerMovieId(2L);
        ranking = new ArrayList<>();
        RankingDto player = new RankingDto();
        player.setUsername("jogador1");
        player.setTotalScore(0L);
        ranking.add(player);
    }

    @DisplayName("Testando a criacao de uma nova partida")
    @Test
    void whenGameStarted() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findByUser(user)).thenReturn(Optional.of(Collections.emptyList()));
        when(gameRepository.save(game)).thenReturn(savedGame);
        when(movieRepository.findFirstByOrderByIdAsc()).thenReturn(movie1);
        when(movieRepository.findTopByOrderByIdDesc()).thenReturn(movie2);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie1));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie2));
        when(roundRepository.save(round)).thenReturn(savedRound);

        GenericResponse genericResponse = gameService.startGame();

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Inicializando uma nova partida!", gameDto);

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Quando o usuario atual nao e encontrado na base de dados durante a inicializacao da partida")
    @Test
    void whenGameNotStartedBecauseCurrentUserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> gameService.startGame());
    }

    @DisplayName("Testando a inicializacao de uma nova rodada")
    @Test
    void whenNewRoundBegins() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findByUser(user)).thenReturn(Optional.of(Collections.emptyList()));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(savedGame));
        when(movieRepository.findFirstByOrderByIdAsc()).thenReturn(movie1);
        when(movieRepository.findTopByOrderByIdDesc()).thenReturn(movie2);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie1));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(movie2));
        when(roundRepository.save(round)).thenReturn(savedRound);

        GenericResponse genericResponse = gameService.newRound(1L, "jogador1");

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Inicializando uma nova rodada!", gameDto);

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Quando o usuario atual nao e encontrado na base de dados durante a inicializacao da rodada")
    @Test
    void whenNewRoundNotStartedBecauseCurrentUserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> gameService.newRound(1L, "jogador1"));
    }

    @DisplayName("Quando o usuario informado nao e o responsavel pela partida durante a inicializacao da rodada")
    @Test
    void whenUsernameIsDifferentThanCurrentUserGameOnNewRound() {
        User otherUser = new User();
        otherUser.setUsername("jogador2");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        Assertions.assertThrows(BadRequestException.class, () -> gameService.newRound(1L, "jogador1"));
    }

    @DisplayName("Quando a partida nao e encontrada durante a inicializacao da rodada")
    @Test
    void whenNewRoundNotStartedBecauseGameIsNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findByUser(user)).thenReturn(Optional.of(Collections.emptyList()));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> gameService.newRound(1L, "jogador1"));
    }

    @DisplayName("Quando a partida esta finalizada durante a inicializacao da rodada")
    @Test
    void whenNewRoundNotStartedBecauseGameIsFinished() {
        Game anotherGame = new Game();
        anotherGame.setFinished(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findByUser(user)).thenReturn(Optional.of(List.of(anotherGame)));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(anotherGame));

        Assertions.assertThrows(BadRequestException.class, () -> gameService.newRound(1L, "jogador1"));
    }

    @DisplayName("Testando a o envio da resposta")
    @Test
    void whenSendAnAnswer() {
        movie1.setScore(100.0);
        movie2.setScore(200.0);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(savedGame));
        when(roundRepository.findById(1L)).thenReturn(Optional.of(savedRound));
        when(gameRepository.save(savedGame)).thenReturn(savedGame);

        GenericResponse genericResponse = gameService.answer(answerDto);

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Resposta correta! Pontuação total: 1");

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Quando o usuario atual nao e encontrado na base de dados durante o registro de uma resposta")
    @Test
    void whenAnswerNotRegisteredBecauseCurrentUserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> gameService.answer(answerDto));
    }

    @DisplayName("Quando o usuario informado nao e o responsavel pela partida durante o registro de uma resposta")
    @Test
    void whenUsernameIsDifferentThanCurrentUserGameOnAnswer() {
        User otherUser = new User();
        otherUser.setUsername("jogador2");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        Assertions.assertThrows(BadRequestException.class, () -> gameService.answer(answerDto));
    }

    @DisplayName("Quando a partida nao e encontrada durante o registro de uma resposta")
    @Test
    void whenAnswerNotRegisteredBecauseGameIsNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> gameService.answer(answerDto));
    }

    @DisplayName("Quando a partida esta finalizada durante o registro de uma resposta")
    @Test
    void whenAnswerNotRegisteredBecauseGameIsFinished() {
        Game anotherGame = new Game();
        anotherGame.setFinished(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(anotherGame));

        Assertions.assertThrows(BadRequestException.class, () -> gameService.answer(answerDto));
    }

    @DisplayName("Quando a rodada nao e encontrada durante o registro de uma resposta")
    @Test
    void whenAnswerNotRegisteredBecauseRoundIsNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(savedGame));
        when(roundRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> gameService.answer(answerDto));
    }

    @DisplayName("Quando a rodada esta finalizada durante o registro de uma resposta")
    @Test
    void whenAnswerNotRegisteredBecauseRoundIsFinished() {
        Round anotherRound = new Round();
        anotherRound.setCorrect(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(savedGame));
        when(roundRepository.findById(1L)).thenReturn(Optional.of(anotherRound));
        Assertions.assertThrows(BadRequestException.class, () -> gameService.answer(answerDto));
    }

    @DisplayName("Testando a finalizacao de uma partida")
    @Test
    void whenGameIsFinished() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(savedGame));
        when(gameRepository.save(savedGame)).thenReturn(savedGame);

        GenericResponse genericResponse = gameService.stopGame(1L, "jogador1");

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Partida finalizada!");

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Quando o usuario atual nao e encontrado na base de dados durante a finalizacao da partida")
    @Test
    void whenGameNotFinishedBecauseCurrentUserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> gameService.stopGame(1L, "jogador1"));
    }

    @DisplayName("Quando o usuario informado nao e o responsavel pela partida durante a finalizacao da partida")
    @Test
    void whenUsernameIsDifferentThanCurrentUserGameOnFinishGame() {
        User otherUser = new User();
        otherUser.setUsername("jogador2");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(otherUser));
        Assertions.assertThrows(BadRequestException.class, () -> gameService.stopGame(1L, "jogador1"));
    }

    @DisplayName("Quando a partida nao e encontrada durante a finalizacao da partida")
    @Test
    void whenGameNotFinishedBecauseGameIsNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> gameService.stopGame(1L, "jogador1"));
    }

    @DisplayName("Quando a partida ja esta finalizada durante a finalizacao da partida")
    @Test
    void whenGameNotFinishedBecauseGameIsAlreadyFinished() {
        Game anotherGame = new Game();
        anotherGame.setFinished(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(anotherGame));

        Assertions.assertThrows(BadRequestException.class, () -> gameService.stopGame(1L, "jogador1"));
    }

    @DisplayName("Testando a exibicao do ranking")
    @Test
    void whenShowingRanking() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(gameRepository.findByUser(user)).thenReturn(Optional.of(List.of(savedGame)));

        GenericResponse genericResponse = gameService.getRanking();

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Ranking exibido com sucesso!", ranking);

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    private Game populateGame() {
        Game game = new Game();
        game.setUser(user);
        game.setFinished(false);
        game.setRounds(new ArrayList<>());
        game.setTotalErrors(0L);
        game.setTotalScore(0L);
        return game;
    }

    private Round populateRound(Game game) {
        Round round = new Round();
        round.setGame(game);
        round.setCorrect(null);
        round.setMovies(movies);
        return round;
    }

}
