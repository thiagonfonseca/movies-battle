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
import tech.ada.api.dto.AnswerDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.exception.NotFoundException;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.GameService;

@RestController
@RequestMapping("/api/v1/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Endpoint para iniciar a partida", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uma nova partida é inicializada ou uma partida em andamento é continuada",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro ao iniciar a partida", content = @Content)
    })
    @GetMapping("/startGame")
    public ResponseEntity<GenericResponse> startGame() {
        try {
            GenericResponse genericResponse = gameService.startGame();
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (NotFoundException ne) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.NOT_FOUND.value(),
                    ne.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Endpoint para iniciar uma nova rodada", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uma nova rodada é inicializada ou uma rodada em andamento é continuada",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "400", description = "A partida não pertence a este usuário", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro ao iniciar a rodada", content = @Content)
    })
    @GetMapping("/newRound")
    public ResponseEntity<GenericResponse> newRound(@RequestParam("gameId") Long gameId,
                                            @RequestParam("username") String username) {
        try {
            GenericResponse genericResponse = gameService.newRound(gameId, username);
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (BadRequestException be) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.BAD_REQUEST.value(),
                    be.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException ne) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.NOT_FOUND.value(),
                    ne.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Endpoint que registra a resposta de uma rodada", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna se a resposta está certa ou errada",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Retorna que a partida não pertence ao usuário, ocorreu " +
                    "um erro durante a validação da resposta, partida já finalizada ou rodada já finalizada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário, Partida ou rodada não encontrados", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro ao registrar a resposta", content = @Content)
    })
    @PostMapping("/answer")
    public ResponseEntity<GenericResponse> answer(@RequestBody AnswerDto answerDto) {
        try {
            GenericResponse genericResponse = gameService.answer(answerDto);
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (BadRequestException be) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.BAD_REQUEST.value(),
                    be.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException ne) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.NOT_FOUND.value(),
                    ne.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Endpoint que finaliza a partida", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partida finalizada",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "400", description = "A partida não pertence a este usuário", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Partida não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro ao finalizar a partida", content = @Content)
    })
    @GetMapping("/stopGame")
    public ResponseEntity<GenericResponse> stopGame(@RequestParam("gameId") Long gameId,
                                                    @RequestParam("username") String username) {
        try {
            GenericResponse genericResponse = gameService.stopGame(gameId, username);
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (BadRequestException be) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.BAD_REQUEST.value(),
                    be.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (NotFoundException ne) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.NOT_FOUND.value(),
                    ne.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

}
