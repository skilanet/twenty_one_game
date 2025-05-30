package ru.sergey.dev.twenty_one_game.model.dto.requests;

import com.google.gson.annotations.SerializedName;

public class StartGameCommandDto extends PlayerCommandDto {
    @SerializedName("playerId")
    private final String playerId;

    public StartGameCommandDto(String playerId) {
        super("StartGame");
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }
}