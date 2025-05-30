package ru.sergey.dev.twenty_one_game.model.dto.requests;

import com.google.gson.annotations.SerializedName;

public class JoinCommandDto extends PlayerCommandDto {
    @SerializedName("playerName")
    private final String playerName;

    public JoinCommandDto(String playerName) {
        super("Join");
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}