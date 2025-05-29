package ru.sergey.dev.twenty_one_game.model.dto;

import ru.sergey.dev.twenty_one_game.model.blackjack.BlackJackPlayer;

public record PlayerDto(String id,
                        String name,
                        int score,
                        boolean isStanding) {
    public BlackJackPlayer toPlayer() {
        return new BlackJackPlayer(id, name, score, isStanding);
    }
}
