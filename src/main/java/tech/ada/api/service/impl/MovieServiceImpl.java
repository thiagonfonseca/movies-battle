package tech.ada.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tech.ada.api.dto.MovieDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.exception.NotFoundException;
import tech.ada.api.model.Movie;
import tech.ada.api.repository.MovieRepository;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.response.OMDBResponse;
import tech.ada.api.service.MovieService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieServiceImpl implements MovieService {

    @Value("${app.omdb-apikey}")
    private String apikey;

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    public MovieServiceImpl(MovieRepository movieRepository, RestTemplate restTemplate) {
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public long countMovies() {
        return movieRepository.count();
    }

    @Override
    public GenericResponse getMovies() {
        log.info("Enviando lista de filmes!");
        return new GenericResponse(HttpStatus.OK.value(),
                "Lista de filmes enviada com sucesso",
                movieRepository.findAll().stream().map(MovieDto::new).collect(Collectors.toList()));
    }

    @Override
    public GenericResponse getById(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> {
            String msg = "Filme com %s nao encontrado".formatted(id);
            log.error(msg);
            throw new NotFoundException(msg);
        });
        return new GenericResponse(HttpStatus.OK.value(),
                "Filme encontrado com sucesso", new MovieDto(movie));
    }

    @Override
    public GenericResponse getByTitle(String title) {
        log.info("Enviando lista de filmes por título!");
        return new GenericResponse(HttpStatus.OK.value(),
                "Filmes enviados com sucesso",
                movieRepository.findByTitle(title).stream().map(MovieDto::new).collect(Collectors.toList()));
    }

    @Override
    public GenericResponse save(MovieDto dto) {
        ResponseEntity<String> response;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String url = "https://www.omdbapi.com/?apikey=" + apikey + "&t=" + dto.getTitle();
        HttpStatus httpStatus = HttpStatus.CREATED;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
            OMDBResponse omdbResponse = mapper.readValue(response.getBody(), OMDBResponse.class);
            Movie movie = new Movie();
            if (dto.getId() != null) {
                Optional<Movie> optMovie = movieRepository.findById(dto.getId());
                if (optMovie.isPresent()) {
                    httpStatus = HttpStatus.OK;
                    movie = optMovie.get();
                }
            }
            movie.setTitle(omdbResponse.getTitle());
            movie.setRating(omdbResponse.getRating() != null && !omdbResponse.getRating().isEmpty() ?
                    Float.parseFloat(omdbResponse.getRating()) : 0);
            String strVote = omdbResponse.getVotes().replace(",", "");
            movie.setVotes(!strVote.isEmpty() ?
                    Long.parseLong(strVote) : 0L);
            movie.setScore(((new BigDecimal(movie.getVotes())
                    .multiply(BigDecimal.valueOf(movie.getRating())))).setScale(2, RoundingMode.HALF_UP)
                    .doubleValue());
            movieRepository.save(movie);
            String msg = "Filme %s registrado/atualizado com sucesso!".formatted(movie.getTitle());
            log.info(msg);
            return new GenericResponse(httpStatus.value(), msg);
        } catch (HttpClientErrorException | IOException e) {
            throw new BadRequestException("Ocorreu um erro ao registrar/atualizar o filme! " + e.getMessage());
        }
    }

    @Scheduled(cron = "${app.cron.daily}")
    public void updateAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            MovieDto movieDto = new MovieDto(movie);
            this.save(movieDto);
        }
    }

}
