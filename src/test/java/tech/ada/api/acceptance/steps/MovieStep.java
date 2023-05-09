package tech.ada.api.acceptance.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import tech.ada.api.dto.MovieDto;
import tech.ada.api.model.Movie;
import tech.ada.api.repository.MovieRepository;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.impl.MovieServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class MovieStep {

    private final MovieRepository movieRepository = Mockito.mock(MovieRepository.class);
    private final MovieServiceImpl movieService;
    private List<MovieDto> movies;

    private Movie movie;

    private GenericResponse genericResponse;

    public MovieStep() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        this.movieService = new MovieServiceImpl(movieRepository, restTemplate);
    }

    @Dado("uma lista vazia de filmes")
    public void dado_uma_lista_vazia_de_filmes() {
        movies = new ArrayList<>();
        genericResponse = new GenericResponse();
    }

    @Quando("realiza uma busca de todos os filmes")
    public void realiza_uma_busca_de_todos_os_filmes() {
        Mockito.when(movieRepository.findAll()).thenReturn(List.of(new Movie()));
        genericResponse = movieService.getMovies();
    }

    @Entao("a lista de filmes eh exibida")
    public void lista_de_filmes_eh_exibida() {
        movies.add(new MovieDto(new Movie()));
        Assertions.assertEquals(new GenericResponse(HttpStatus.OK.value(),
                "Lista de filmes enviada com sucesso", movies), genericResponse);
    }

    @Dado("o ID {long} do filme")
    public void dado_filme(Long id) {
        System.out.println(id);
        movie = new Movie();
        movie.setId(id);
        genericResponse = new GenericResponse();
    }

    @Quando("realiza uma busca do filme pelo ID")
    public void realiza_busca_filme_id() {
        when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
        genericResponse = movieService.getById(movie.getId());
    }

    @Entao("o filme eh exibido")
    public void entao_filme_exibido() {
        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Filme encontrado com sucesso", new MovieDto(movie));

        Assertions.assertEquals(expectedGenericResponse, genericResponse);
    }

}
