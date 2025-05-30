package ru.sergey.dev.twenty_one_game.model.dto.response;

import com.google.gson.annotations.SerializedName;

public class ErrorEventDto extends GameEventDto {
    @SerializedName("message")
    private String message;

    public ErrorEventDto() {
        this.type = "Error";
    }

    public String getMessage() {
        return message;
    }
}