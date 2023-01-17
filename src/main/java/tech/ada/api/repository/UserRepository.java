package tech.ada.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.ada.api.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
