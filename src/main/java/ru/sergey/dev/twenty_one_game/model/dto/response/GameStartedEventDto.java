package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;
import ru.sergey.dev.twenty_one_game.model.dto.GameStateDto;

public class GameStartedEventDto extends GameEventDto {
    @SerializedName("gameState")
    private GameStateDto gameState;

    public GameStartedEventDto() {
        this.type = "GameStarted";
    }

    public GameStateDto getGameState() {
        return gameState;
    }
}