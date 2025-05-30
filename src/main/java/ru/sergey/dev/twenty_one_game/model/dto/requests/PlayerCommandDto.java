package ru.sergey.dev.twenty_one_game.model.dto.requests;

import com.google.gson.annotations.SerializedName;

public abstract class PlayerCommandDto {
    @SerializedName("type")
    protected String type;

    public PlayerCommandDto(String type) {
        this.type = type;
    }
}