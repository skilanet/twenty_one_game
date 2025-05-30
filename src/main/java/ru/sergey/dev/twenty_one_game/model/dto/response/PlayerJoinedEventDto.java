package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;
import ru.sergey.dev.twenty_one_game.model.dto.PlayerDto;

public class PlayerJoinedEventDto extends GameEventDto {
    @SerializedName("player")
    private PlayerDto player;

    @SerializedName("gameId")
    private String gameId;

    public PlayerJoinedEventDto() {
        this.type = "PlayerJoined";
    }

    public PlayerDto getPlayer() {
        return player;
    }

    public String getGameId() {
        return gameId;
    }
}