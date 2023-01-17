package tech.ada.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ada.api.model.Game;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDto {

    private Long id;
    private Boolean finished;
    private UserDto userDto;
    private Long totalScore;
    private Long totalErrors;
    private Long roundId;
    private MovieDto movie1;
    private MovieDto movie2;

    public GameDto(Game game) {
        this.id = game.getId();
        this.finished = game.getFinished();
        this.userDto = new UserDto(game.getUser());
        this.totalScore = game.getTotalScore();
        this.totalErrors = game.getTotalErrors();
    }

}
