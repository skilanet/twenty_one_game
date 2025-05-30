package ru.sergey.dev.twenty_one_game.model.dto;

import java.util.List;

public class PlayerDto {
    private String id;
    private String name;
    private int score;
    private boolean isStanding;
    private List<CardDto> hand; // Список карт в руке
    private int handSize; // Размер руки (если hand недоступен)
    private int cards_in_hand; // Альтернативное поле для размера руки

    public PlayerDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isStanding() {
        return isStanding;
    }

    public void setStanding(boolean standing) {
        isStanding = standing;
    }

    public List<CardDto> getHand() {
        return hand;
    }

    public void setHand(List<CardDto> hand) {
        this.hand = hand;
    }

    public int getHandSize() {
        return handSize;
    }

    public void setHandSize(int handSize) {
        this.handSize = handSize;
    }

    public int getCards_in_hand() {
        return cards_in_hand;
    }

    public void setCards_in_hand(int cards_in_hand) {
        this.cards_in_hand = cards_in_hand;
    }

    // Удобный метод для получения размера руки
    public int getActualHandSize() {
        if (hand != null) {
            return hand.size();
        }
        if (cards_in_hand > 0) {
            return cards_in_hand;
        }
        return handSize;
    }
}