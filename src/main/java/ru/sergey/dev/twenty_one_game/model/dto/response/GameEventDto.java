package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;

public abstract class GameEventDto {
    @SerializedName("type")
    protected String type;

    public String getType() {
        return type;
    }
}