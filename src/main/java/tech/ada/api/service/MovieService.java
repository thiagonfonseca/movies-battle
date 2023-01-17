package tech.ada.api.service;

import tech.ada.api.dto.MovieDto;
import tech.ada.api.response.GenericResponse;

public interface MovieService {
    long countMovies();
    GenericResponse getMovies();
    GenericResponse getById(Long id);
    GenericResponse getByTitle(String title);
    GenericResponse save(MovieDto dto);
    void updateAllMovies();
}
