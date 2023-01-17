package tech.ada.api.service;

import tech.ada.api.dto.LoginDto;
import tech.ada.api.dto.UserDto;
import tech.ada.api.response.AuthResponse;
import tech.ada.api.response.GenericResponse;

public interface AuthService {
    AuthResponse login(LoginDto loginDto);
    GenericResponse register(UserDto userDto);
}
