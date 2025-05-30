package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;

public class InfoEventDto extends GameEventDto {
    @SerializedName("message")
    private String message;

    public InfoEventDto() {
        this.type = "Info";
    }

    public String getMessage() {
        return message;
    }
}