package ru.sergey.dev.twenty_one_game.model.dto;

import java.util.List;

public class GameStateDto {
    private List<PlayerDto> players;
    private String currentPlayerId;
    private boolean gameOver;
    private String message;

    public GameStateDto() {
    }

    public List<PlayerDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDto> players) {
        this.players = players;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Удобные методы
    public PlayerDto getCurrentPlayer() {
        if (currentPlayerId == null || players == null) {
            return null;
        }
        return players.stream()
                .filter(p -> currentPlayerId.equals(p.getId()))
                .findFirst()
                .orElse(null);
    }

    public PlayerDto getPlayerById(String playerId) {
        if (playerId == null || players == null) {
            return null;
        }
        return players.stream()
                .filter(p -> playerId.equals(p.getId()))
                .findFirst()
                .orElse(null);
    }
}