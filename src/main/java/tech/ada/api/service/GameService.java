package tech.ada.api.service;

import tech.ada.api.dto.AnswerDto;
import tech.ada.api.response.GenericResponse;

public interface GameService {
    GenericResponse startGame();
    GenericResponse newRound(Long gameId, String username);
    GenericResponse answer(AnswerDto answerDto);
    GenericResponse stopGame(Long gameId, String username);
}
