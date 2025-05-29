package ru.sergey.dev.twenty_one_game.model.dto.requests;


public class JoinCommand extends PlayerCommand{
    private final String playerName;

    public JoinCommand(String playerName){
        super("Join");
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}