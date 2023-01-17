package tech.ada.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.ada.api.model.Movie;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitle(String title);

    Movie findTopByOrderByIdDesc();

    Movie findFirstByOrderByIdAsc();

}
