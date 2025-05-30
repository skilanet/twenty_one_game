package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;
import ru.sergey.dev.twenty_one_game.model.dto.GameStateDto;

public class GameOverEventDto extends GameEventDto {
    @SerializedName("winner")
    private String winner;

    @SerializedName("gameState")
    private GameStateDto gameState;

    public GameOverEventDto() {
        this.type = "GameOver";
    }

    public String getWinner() {
        return winner;
    }

    public GameStateDto getGameState() {
        return gameState;
    }
}