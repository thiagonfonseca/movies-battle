package tech.ada.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.ada.api.model.Round;

public interface RoundRepository extends JpaRepository<Round, Long> {

}
