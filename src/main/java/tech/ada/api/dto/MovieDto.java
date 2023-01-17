package tech.ada.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ada.api.model.Movie;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {

    private Long id;
    private String title;
    private Float rating;
    private Long votes;
    private Double score;

    public MovieDto(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.rating = movie.getRating();
        this.votes = movie.getVotes();
        this.score = movie.getScore();
    }

    public MovieDto(Long id, String title) {
        this.id = id;
        this.title = title;
    }

}
