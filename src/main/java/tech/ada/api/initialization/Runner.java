package tech.ada.api.initialization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import tech.ada.api.dto.MovieDto;
import tech.ada.api.model.Role;
import tech.ada.api.model.User;
import tech.ada.api.repository.RoleRepository;
import tech.ada.api.repository.UserRepository;
import tech.ada.api.service.MovieService;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class Runner implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final MovieService movieService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public Runner(PasswordEncoder passwordEncoder, MovieService movieService,
                  RoleRepository roleRepository, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.movieService = movieService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("ROLE_USER");
            Role savedRole = roleRepository.save(newRole);
            log.info("ROLE_USER adicionado com sucesso");
            return savedRole;
        });
        roles.add(role);

        long countUser = userRepository.count();
        if (countUser == 0) {
            User user = new User();
            user.setName("Jogador 1");
            user.setUsername("jogador1");
            user.setPassword(passwordEncoder.encode("player1"));
            user.setRoles(roles);
            userRepository.save(user);
            log.info("Jogador 1 adicionado com sucesso!");

            user = new User();
            user.setName("Jogador 2");
            user.setUsername("jogador2");
            user.setPassword(passwordEncoder.encode("player2"));
            user.setRoles(roles);
            userRepository.save(user);
            log.info("Jogador 2 adicionado com sucesso!");
        }
        long countMovies = movieService.countMovies();
        if (countMovies == 0) {
            try {
                Resource resource = new ClassPathResource("movies.txt");
                File file = resource.getFile();
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                for (String line : bufferedReader.lines().toList()) {
                    MovieDto movieDto = new MovieDto();
                    movieDto.setTitle(line);
                    movieService.save(movieDto);
                }
                log.info("Lista de filmes adicionada com sucesso!");
            } catch (IOException | HttpClientErrorException e) {
                log.error("Nao foi possivel carregar os dados de filmes! " + e.getMessage());
            }
        }
    }
}
