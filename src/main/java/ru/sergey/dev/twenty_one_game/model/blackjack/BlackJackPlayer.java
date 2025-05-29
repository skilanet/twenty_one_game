package ru.sergey.dev.twenty_one_game.model.blackjack;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import ru.sergey.dev.twenty_one_game.model.cards.Card;
import ru.sergey.dev.twenty_one_game.model.cards.Deck;

public class BlackJackPlayer extends BlackJack {
    private final Label scoreLabel;

    public BlackJackPlayer(HBox cardsContainer, Deck deck, Label scoreLabel) {
        super(cardsContainer, deck);
        this.scoreLabel = scoreLabel;
    }

    @Override
    public void addCard(Card card, boolean isWithShirt) {
        super.addCard(card, isWithShirt);
        updateScoreLabel();
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Текущее количество очков: " + score);
    }

    public void getCard() {
        if (isActive) {
            Card card = deck.getRandomCard();
            addCard(card, false);
        }
    }
}
