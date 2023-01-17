package tech.ada.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.ada.api.dto.LoginDto;
import tech.ada.api.dto.UserDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.response.AuthResponse;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Realizar a autenticação do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário autenticado com sucesso",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Usuário e/ou senha inválidos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody LoginDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Endpoint para registrar um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = GenericResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Usuário já registrado", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<GenericResponse> registerUser(@RequestBody UserDto userDto) {
        try {
            GenericResponse genericResponse = authService.register(userDto);
            return new ResponseEntity<>(genericResponse, HttpStatus.valueOf(genericResponse.getStatus()));
        } catch (BadRequestException be) {
            return new ResponseEntity<>(new GenericResponse(HttpStatus.BAD_REQUEST.value(),
                    be.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
