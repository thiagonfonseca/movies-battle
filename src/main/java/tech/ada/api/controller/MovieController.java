package tech.ada.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.ada.api.dto.MovieDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.exception.NotFoundException;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.MovieService;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @Operation(summary = "Endpoint para registrar um novo filme", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filme registrado com sucesso",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ocorreu um erro ao registrar o filme", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content)
    })
    @PostMapping("")
    public ResponseEntity<GenericResponse> saveMovie(@RequestBody MovieDto movieDto) {
        try {
            GenericResponse genericResponse = movieService.save(movieDto);
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (BadRequestException be) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.BAD_REQUEST.value(),
                    be.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Endpoint para atualizar um filme", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme atualizado com sucesso",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ocorreu um erro ao atualizar o filme", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse> updateMovie(@RequestBody MovieDto movieDto, @PathVariable("id") Long id) {
        try {
            movieDto.setId(id);
            GenericResponse genericResponse = movieService.save(movieDto);
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (BadRequestException be) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.BAD_REQUEST.value(),
                    be.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Endpoint que retorna uma lista com os filmes registrados", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de filmes carregado",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<GenericResponse> getMovies() {
        GenericResponse genericResponse = movieService.getMovies();
        return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
    }

    @Operation(summary = "Endpoint que retorna um filme pelo ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme encontrado com sucesso",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> getMovieById(@PathVariable("id") Long id) {
        try {
            GenericResponse genericResponse = movieService.getById(id);
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (NotFoundException ne) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.NOT_FOUND.value(),
                    ne.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Endpoint que retorna filmes pelo Título", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filme(s) encontrado(s) com sucesso",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content)
    })
    @GetMapping("/title/{title}")
    public ResponseEntity<GenericResponse> getMovieByTitle(@PathVariable("title") String title) {
        GenericResponse genericResponse = movieService.getByTitle(title);
        return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
    }

    @Operation(summary = "Endpoint para atualizar todos os filmes", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filmes atualizados com sucesso",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ocorreu um erro ao atualizar os filmes", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content)
    })
    @PutMapping("")
    public ResponseEntity<GenericResponse> updateMovies() {
        try {
            movieService.updateAllMovies();
            GenericResponse genericResponse = new GenericResponse(HttpStatus.OK.value(), "Filmes atualizados com sucesso!");
            return new ResponseEntity<>(genericResponse, HttpStatus.OK);
        } catch (BadRequestException be) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.BAD_REQUEST.value(),
                    be.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
