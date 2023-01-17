package tech.ada.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.ada.api.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
