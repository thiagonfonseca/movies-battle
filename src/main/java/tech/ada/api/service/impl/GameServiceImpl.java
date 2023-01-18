package tech.ada.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
import tech.ada.api.security.SecurityUtils;
import tech.ada.api.service.GameService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Service
@Slf4j
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final RoundRepository roundRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    public GameServiceImpl(GameRepository gameRepository, RoundRepository roundRepository, MovieRepository movieRepository,
                           UserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.roundRepository = roundRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    @Override
    public GenericResponse startGame() {
        User user = verifyCurrentUser();
        List<Game> gamesPlayed = verifyGamesPlayed(user);
        String msg = "Inicializando uma nova partida!";
        if (!gamesPlayed.isEmpty()) {
            Game game = null;
            Round round = null;
            for (Game g : gamesPlayed) {
                if (!g.getFinished()) {
                    game = g;
                    List<Round> rounds = g.getRounds();
                    for (Round r : rounds) {
                        if (r.getCorrect() == null) {
                            round = r;
                            break;
                        }
                    }
                    msg = "Partida não finalizada! Continuando a partida!";
                    break;
                }
            }
            if (game == null) {
                game = populateGame(user);
            }
            if (round == null) {
                round = populateRound(gamesPlayed, game);
            }
            return new GenericResponse(HttpStatus.OK.value(), msg, populateGameDto(game, round));
        }
        Game game = populateGame(user);
        Round round = populateRound(gamesPlayed, game);
        log.info(msg);
        return new GenericResponse(HttpStatus.OK.value(), msg, populateGameDto(game, round));
    }

    @Override
    public GenericResponse newRound(Long gameId, String username) {
        User user = verifyCurrentUser();
        validateUser(user, username);
        List<Game> gamesPlayed = verifyGamesPlayed(user);
        Game game = getGame(gameId);
        verifyFinishedGame(game);
        String msg = "Inicializando uma nova rodada!";
        Round round = verifyUnfinishedRound(game);
        if (round != null)
            msg = "Rodada não finalizada! Continuando a rodada!";
        else
            round = populateRound(gamesPlayed, game);
        log.info(msg);
        return new GenericResponse(HttpStatus.OK.value(), msg, populateGameDto(game, round));
    }

    @Override
    public GenericResponse answer(AnswerDto answerDto) {
        User user = verifyCurrentUser();
        String msg;
        validateUser(user, answerDto.getUsername());
        Game game = getGame(answerDto.getGameId());
        verifyFinishedGame(game);
        Round round = getRound(answerDto.getRoundId());
        verifyFinishedRound(round);
        if (round.getMovies().isEmpty() || round.getMovies().size() == 1) {
            msg = "Ocorreu um erro durante a validação da resposta!";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        Movie movie1 = round.getMovies().get(0);
        Movie movie2 = round.getMovies().get(1);
        BigDecimal score1 = BigDecimal.valueOf(movie1.getScore());
        BigDecimal score2 = BigDecimal.valueOf(movie2.getScore());
        if (score1.compareTo(score2) > 0) {
            round.setCorrect(verifyAnswer(movie1.getId(),
                    answerDto.getAnswerMovieId()));
        } else if (score1.compareTo(score2) < 0) {
            round.setCorrect(verifyAnswer(movie2.getId(),
                    answerDto.getAnswerMovieId()));
        } else {
            boolean correct = verifyAnswer(movie1.getId(),
                    answerDto.getAnswerMovieId());
            if (!correct)
                correct = verifyAnswer(movie2.getId(),
                        answerDto.getAnswerMovieId());
            round.setCorrect(correct);
        }
        roundRepository.save(round);
        if (round.getCorrect()) {
            game.setTotalScore(game.getTotalScore() + 1);
            msg = "Resposta correta! Pontuação total: %s".formatted(game.getTotalScore());
        } else {
            game.setTotalErrors(game.getTotalErrors() + 1);
            long restantes = 3 - game.getTotalErrors();
            msg =  "Resposta errada! Número de tentativas restantes: %s".formatted(restantes);
        }
        if (game.getTotalErrors() == 3) {
            game.setFinished(true);
            msg = "Resposta errada! Game Over!";
        }
        gameRepository.save(game);
        log.info(msg);
        return new GenericResponse(HttpStatus.OK.value(), msg);
    }

    @Override
    public GenericResponse stopGame(Long gameId, String username) {
        User user = verifyCurrentUser();
        String msg;
        validateUser(user, username);
        Game game = getGame(gameId);
        verifyFinishedGame(game);
        game.setFinished(true);
        gameRepository.save(game);
        msg = "Partida finalizada!";
        log.info(msg);
        return new GenericResponse(HttpStatus.OK.value(), msg);
    }

    @Override
    public GenericResponse getRanking() {
        List<User> users = userRepository.findAll();
        List<RankingDto> ranking = new ArrayList<>();
        for (User user : users) {
            BigInteger score = BigInteger.ZERO;
            BigInteger errors = BigInteger.ZERO;
            List<Game> gamesPlayed = verifyGamesPlayed(user);
            for (Game game : gamesPlayed) {
                score = score.add(BigInteger.valueOf(game.getTotalScore()));
                errors = errors.add(BigInteger.valueOf(game.getTotalErrors()));
            }
            BigInteger porcentagem = score.multiply(BigInteger.valueOf(100));
            ranking.add(new RankingDto(user.getUsername(), user.getName(), porcentagem.longValue()));
        }
        ranking.sort((r1, r2) -> r2.getTotalScore().compareTo(r1.getTotalScore()));
        return new GenericResponse(HttpStatus.OK.value(), "Ranking exibido com sucesso!",
                ranking);
    }

    private List<Game> verifyGamesPlayed(User user) {
        return gameRepository.findByUser(user).orElseGet(ArrayList::new);
    }

    private Game populateGame(User user) {
        Game game = new Game();
        game.setUser(user);
        game.setFinished(false);
        game.setTotalScore(0L);
        game.setTotalErrors(0L);
        game.setRounds(new ArrayList<>());
        return gameRepository.save(game);
    }

    private Round populateRound(List<Game> gamesPlayed, Game game) {
        List<Round> rounds = new ArrayList<>();
        if (!gamesPlayed.isEmpty()) {
            for (Game g : gamesPlayed) {
                rounds.addAll(g.getRounds());
            }
        }
        List<Long[]> allRounds = new ArrayList<>();
        for (Round r : rounds) {
            Long[] arrayRound = new Long[2];
            arrayRound[0] = r.getMovies().get(0).getId();
            arrayRound[1] = r.getMovies().get(1).getId();
            allRounds.add(arrayRound);
        }
        boolean notFound = false;
        int id1;
        int id2;
        Round round = new Round();
        round.setGame(game);
        round.setMovies(new ArrayList<>());
        // Aqui o método recupera o último ID de Movie, para buscar dois IDs aleatórios entre os registrados
        Long firstId = movieRepository.findFirstByOrderByIdAsc().getId();
        Long lastId = movieRepository.findTopByOrderByIdDesc().getId();
        while (!notFound) {
            if (lastId - firstId == 1) {
                id1 = firstId.intValue();
                id2 = lastId.intValue();
            } else {
                id1 = (int) ((Math.random() * (lastId - firstId)) + firstId);
                if (id1 == 0)
                    continue;
                id2 = (int) ((Math.random() * (lastId - firstId)) + firstId);
            }
            if (id2 == 0 || id1 == id2)
                continue;
            boolean found = false;
            for (Long[] s : allRounds) {
                if ((id1 == s[0] && id2 == s[1]) || (id1 == s[1] && id2 == s[0])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Optional<Movie> optMovie1 = movieRepository.findById((long) id1);
                Optional<Movie> optMovie2 = movieRepository.findById((long) id2);
                if (optMovie1.isPresent() && optMovie2.isPresent()) {
                    round.getMovies().add(optMovie1.get());
                    round.getMovies().add(optMovie2.get());
                    notFound = true;
                }
            }
        }
        return roundRepository.save(round);
    }

    private GameDto populateGameDto(Game game, Round round) {
        GameDto gameDto = new GameDto(game);
        gameDto.setRoundId(round.getId());
        gameDto.setMovie1(new MovieDto(round.getMovies().get(0).getId(), round.getMovies().get(0).getTitle()));
        gameDto.setMovie2(new MovieDto(round.getMovies().get(1).getId(), round.getMovies().get(1).getTitle()));
        return gameDto;
    }

    private Round verifyUnfinishedRound(Game game) {
        List<Round> rounds = game.getRounds();
        for (Round r : rounds) {
            if (r.getCorrect() == null) {
                return r;
            }
        }
        return null;
    }

    private boolean verifyAnswer(Long idCorrect, Long idAnswer) {
        return Objects.equals(idCorrect, idAnswer);
    }

    private User verifyCurrentUser() {
        String currentUsername = SecurityUtils.getCurrentUserLogin();
        return userRepository.findByUsername(currentUsername).orElseThrow(() -> {
            String msg = "Usuario %s nao encontrado".formatted(currentUsername);
            log.error(msg);
            throw new NotFoundException(msg);
        });
    }

    private void validateUser(User user, String username) {
        if (!user.getUsername().equals(username)) {
            String msg = "Esta partida não pertence a este usuário";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    private Game getGame(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> {
            String msg = "Partida não encontrada!";
            log.error(msg);
            throw  new NotFoundException(msg);
        });
    }

    private Round getRound(Long roundId) {
        return roundRepository.findById(roundId).orElseThrow(() -> {
            String msg = "Rodada não encontrada!";
            log.error(msg);
            throw  new NotFoundException(msg);
        });
    }

    private void verifyFinishedGame(Game game) {
        if (game.getFinished()) {
            String msg = "Esta partida já está finalizada!";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    private void verifyFinishedRound(Round round) {
        if (round.getCorrect() != null) {
            String msg = "Esta rodada já está finalizada!";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

}
