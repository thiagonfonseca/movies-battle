package tech.ada.api.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import tech.ada.api.dto.MovieDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.exception.NotFoundException;
import tech.ada.api.model.Movie;
import tech.ada.api.repository.MovieRepository;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.impl.MovieServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @InjectMocks
    MovieServiceImpl movieService;

    @Mock
    MovieRepository movieRepository;
    @Mock
    RestTemplate restTemplate;

    static String username;

    @BeforeAll
    public static void setUp() {
        username = "jogador1";
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @DisplayName("Testando a exibicao de uma lista de filmes")
    @Test
    void whenAListOfMoviesIsDisplayed() {
        Movie movie = new Movie();
        List<MovieDto> movies = new ArrayList<>();
        movies.add(new MovieDto(movie));
        when(movieRepository.findAll()).thenReturn(List.of(movie));

        GenericResponse genericResponse = movieService.getMovies();

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Lista de filmes enviada com sucesso", movies);

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Testando a exibicao de um filme pelo ID")
    @Test
    void whenAMovieByIdIsDisplayed() {
        Movie movie = new Movie();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        GenericResponse genericResponse = movieService.getById(1L);

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Filme encontrado com sucesso", new MovieDto(movie));

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Testando a exibicao de um filme pelo ID")
    @Test
    void whenAMovieByIdIsNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> movieService.getById(1L));
    }

    @DisplayName("Testando a exibicao de um filme pelo titulo")
    @Test
    void whenAMovieByTitleIsDisplayed() {
        Movie movie = new Movie();
        movie.setTitle("Filme 1");
        List<MovieDto> movies = new ArrayList<>();
        movies.add(new MovieDto(movie));
        when(movieRepository.findByTitle(movie.getTitle())).thenReturn(List.of(movie));

        GenericResponse genericResponse = movieService.getByTitle(movie.getTitle());

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Filmes enviados com sucesso", movies);

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Testando o registro/atualizacao de um filme")
    @Test
    void whenSaveAMovie() {
        ReflectionTestUtils.setField(movieService, "apikey", "bc36f2da");
        Movie movie = new Movie();
        movie.setTitle("Sonic the Hedgehog");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String jsonReturn = "{\"Title\":\"Sonic the Hedgehog\",\"Year\":\"2020\",\"Rated\":\"PG\",\"Released\":\"14 Feb 2020\",\"Runtime\":\"99 min\",\"Genre\":\"Action, Adventure, Comedy\",\"Director\":\"Jeff Fowler\",\"Writer\":\"Pat Casey, Josh Miller\",\"Actors\":\"Ben Schwartz, James Marsden, Jim Carrey\",\"Plot\":\"After discovering a small, blue, fast hedgehog, a small-town police officer must help him defeat an evil genius who wants to do experiments on him.\",\"Language\":\"English, French, Persian\",\"Country\":\"United States, Japan, Canada\",\"Awards\":\"3 wins & 12 nominations\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BNTdmNmI4MzQtZTAzNS00MjhjLWEzOGQtZjI1NDNjZjk4N2JjXkEyXkFqcGdeQXVyMTM0NTUzNDIy._V1_SX300.jpg\",\"Ratings\":[{\"Source\":\"Internet Movie Database\",\"Value\":\"6.5/10\"},{\"Source\":\"Rotten Tomatoes\",\"Value\":\"63%\"},{\"Source\":\"Metacritic\",\"Value\":\"47/100\"}],\"Metascore\":\"47\",\"imdbRating\":\"6.5\",\"imdbVotes\":\"141,123\",\"imdbID\":\"tt3794354\",\"Type\":\"movie\",\"DVD\":\"31 Mar 2020\",\"BoxOffice\":\"$148,974,665\",\"Production\":\"N/A\",\"Website\":\"N/A\",\"Response\":\"True\"}";
        doReturn(ResponseEntity.ok(jsonReturn)).when(restTemplate)
                .exchange("https://www.omdbapi.com/?apikey=bc36f2da&t=Sonic the Hedgehog", HttpMethod.GET,
                new HttpEntity<>(httpHeaders), String.class);
        when(movieRepository.save(movie)).thenReturn(movie);

        GenericResponse genericResponse = movieService.save(new MovieDto(movie));

        GenericResponse expectedGenericResponse = new GenericResponse(HttpStatus.CREATED.value(),
                "Filme %s registrado/atualizado com sucesso!".formatted(movie.getTitle()));

        Assertions.assertEquals(genericResponse, expectedGenericResponse);
    }

    @DisplayName("Quando o filme nao e registrado por causa de um erro durante a execucao")
    @Test
    void whenMovieIsNotRegisteredBecauseAnError() {
        ReflectionTestUtils.setField(movieService, "apikey", "bc36f2da");
        Movie movie = new Movie();
        movie.setTitle("Sonic the Hedgehog");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        doReturn(ResponseEntity.ok(null)).when(restTemplate)
                .exchange("https://www.omdbapi.com/?apikey=bc36f2da&t=Sonic the Hedgehog", HttpMethod.GET,
                        new HttpEntity<>(httpHeaders), String.class);

        Assertions.assertThrows(BadRequestException.class, () -> movieService.save(new MovieDto(movie)));
    }

}
