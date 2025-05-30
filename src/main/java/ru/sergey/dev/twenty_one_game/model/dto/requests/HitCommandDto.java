package ru.sergey.dev.twenty_one_game.model.dto.requests;

import com.google.gson.annotations.SerializedName;

public class HitCommandDto extends PlayerCommandDto {
    @SerializedName("playerId")
    private final String playerId;

    public HitCommandDto(String playerId) {
        super("Hit");
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }
}