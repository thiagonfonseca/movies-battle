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
import tech.ada.api.dto.MovieDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.exception.NotFoundException;
import tech.ada.api.model.Movie;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.impl.MovieServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {

    private static final String API_URL = "/api/v1/movie";

    private MockMvc mockMvc;

    @InjectMocks
    private MovieController movieController;
    @Mock
    private MovieServiceImpl movieService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController)
                .setViewResolvers((s, locale) -> new MappingJackson2JsonView())
                .build();
    }

    @DisplayName("Testando a exibicao de uma lista de filmes")
    @Test
    void whenAListOfMoviesIsDisplayed() throws Exception {
        Movie movie = new Movie();
        List<MovieDto> movies = new ArrayList<>();
        movies.add(new MovieDto(movie));
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Lista de filmes enviada com sucesso", movies);

        when(movieService.getMovies()).thenReturn(genericResponse);

        mockMvc.perform(get(API_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Lista de filmes enviada com sucesso")));
    }

    @DisplayName("Testando a exibicao de um filme pelo ID")
    @Test
    void whenAMovieByIdIsDisplayed() throws Exception {
        Movie movie = new Movie();
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Filme encontrado com sucesso", new MovieDto(movie));

        when(movieService.getById(1L)).thenReturn(genericResponse);

        mockMvc.perform(get(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Filme encontrado com sucesso")));
    }

    @DisplayName("Quando nao e encontrado o filme pelo ID")
    @Test
    void whenAMovieByIdIsNotFound() throws Exception {

        doThrow(NotFoundException.class).when(movieService).getById(1L);

        mockMvc.perform(get(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Testando a exibicao de um filme pelo titulo")
    @Test
    void whenAMovieByTitleIsDisplayed() throws Exception {
        Movie movie = new Movie();
        movie.setTitle("Sonic the Hedgehog");
        List<MovieDto> movies = new ArrayList<>();
        movies.add(new MovieDto(movie));
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Filmes enviados com sucesso", movies);

        when(movieService.getByTitle(movie.getTitle())).thenReturn(genericResponse);

        mockMvc.perform(get(API_URL + "/title/" + movie.getTitle())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Filmes enviados com sucesso")));
    }

    @DisplayName("Testando o registro de um filme")
    @Test
    void whenSaveAMovie() throws Exception {
        Movie movie = new Movie();
        movie.setTitle("Sonic the Hedgehog");
        GenericResponse genericResponse = new GenericResponse(HttpStatus.CREATED.value(),
                "Filme %s registrado/atualizado com sucesso!".formatted(movie.getTitle()));

        when(movieService.save(new MovieDto(movie))).thenReturn(genericResponse);

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": null, \"title\": \"Sonic the Hedgehog\"}"))
                .andExpect(status().isCreated());
    }

    @DisplayName("Quando o filme nao e registrado por causa de um erro durante a execucao")
    @Test
    void whenMovieIsNotRegisteredBecauseAnError() throws Exception {

        doThrow(BadRequestException.class).when(movieService).save(new MovieDto());

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Testando a atualizacao de um filme")
    @Test
    void whenUpdateAMovie() throws Exception {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Sonic the Hedgehog");
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Filme %s registrado/atualizado com sucesso!".formatted(movie.getTitle()));

        when(movieService.save(new MovieDto(movie))).thenReturn(genericResponse);

        mockMvc.perform(put(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"title\": \"Sonic the Hedgehog\"}"))
                .andExpect(status().isOk());
    }

    @DisplayName("Quando o filme nao e atualizado por causa de um erro durante a execucao")
    @Test
    void whenMovieIsNotUpdatedBecauseAnError() throws Exception {
        Movie movie = new Movie();
        movie.setId(1L);
        doThrow(BadRequestException.class).when(movieService).save(new MovieDto(movie));

        mockMvc.perform(put(API_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Testando a atualizacao de todos os filmes")
    @Test
    void whenUpdateAllMovies() throws Exception {
        GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(),
                "Filmes atualizados com sucesso!");
        doNothing().when(movieService).updateAllMovies();

        mockMvc.perform(put(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(genericResponse.toString()))
                .andExpect(status().isOk());
    }

    @DisplayName("Quando ocorre um erro durante a atualizacao dos filmes")
    @Test
    void whenAllMoviesNotUpdatedBecauseAnError() throws Exception {

        doThrow(BadRequestException.class).when(movieService).updateAllMovies();

        mockMvc.perform(put(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

}
