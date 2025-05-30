package ru.sergey.dev.twenty_one_game.model.dto.requests;

import com.google.gson.annotations.SerializedName;

public class StandCommandDto extends PlayerCommandDto {
    @SerializedName("playerId")
    private final String playerId;

    public StandCommandDto(String playerId) {
        super("Stand");
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }
}