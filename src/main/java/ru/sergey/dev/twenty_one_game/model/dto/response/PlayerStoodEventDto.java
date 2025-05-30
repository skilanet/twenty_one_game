package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;

public class PlayerStoodEventDto extends GameEventDto {
    @SerializedName("playerId")
    private String playerId;

    public PlayerStoodEventDto() {
        this.type = "PlayerStood";
    }

    public String getPlayerId() {
        return playerId;
    }
}