package tech.ada.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.ada.api.model.Game;
import tech.ada.api.model.User;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<List<Game>> findByUser(User user);

    @Modifying
    @Query("UPDATE Game g SET g.finished = TRUE where g.id = :id")
    void finishGame(@Param("id") Long id);

}
