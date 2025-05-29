package ru.sergey.dev.twenty_one_game.model.dto.requests;

public abstract class PlayerCommand {
    String type;

    public PlayerCommand(String type) {
        this.type = type;
    }
}
