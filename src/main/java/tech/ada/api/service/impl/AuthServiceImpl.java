package tech.ada.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.ada.api.dto.LoginDto;
import tech.ada.api.dto.UserDto;
import tech.ada.api.exception.BadRequestException;
import tech.ada.api.model.Role;
import tech.ada.api.model.User;
import tech.ada.api.repository.RoleRepository;
import tech.ada.api.repository.UserRepository;
import tech.ada.api.response.AuthResponse;
import tech.ada.api.response.GenericResponse;
import tech.ada.api.security.JwtTokenProvider;
import tech.ada.api.service.AuthService;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                           RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        log.info("Usuario autenticado com sucesso!");
        return new AuthResponse(token);
    }

    @Override
    public GenericResponse register(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername()))
            throw new BadRequestException("Usuario ja registrado!");

        User user = new User();
        user.setName(userDto.getName());
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName("ROLE_USER").get();
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
        String msgSuccess = "Usuario registrado com sucesso!";
        log.info(msgSuccess);
        return new GenericResponse(HttpStatus.CREATED.value(), msgSuccess);
    }

}
