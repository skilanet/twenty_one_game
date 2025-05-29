package ru.sergey.dev.twenty_one_game.model.blackjack;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import ru.sergey.dev.twenty_one_game.model.cards.Card;
import ru.sergey.dev.twenty_one_game.model.cards.Deck;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BlackJack {
    protected int score;
    protected final HBox cardsContainer;
    protected final Deck deck;
    protected final List<Card> cards;
    protected boolean isActive;

    protected BlackJack(HBox cardsContainer, Deck deck) {
        this.cardsContainer = cardsContainer;
        this.deck = deck;
        this.score = 0;
        this.cards = new ArrayList<>();
        this.isActive = true;
    }

    public void addCard(Card card, boolean isWithShirt) {
        cards.add(card);

        updateScore(card);

        ImageView cardImageView = new ImageView();
        String imagePath;
        if (isWithShirt) {
            imagePath = "/images/deck_green.png";
        } else  {
            imagePath = card.imagePath();
        }
        setupImage(cardImageView, imagePath, cardsContainer.getPrefHeight());
        cardsContainer.getChildren().add(cardImageView);

        checkBust();
    }

    protected void updateScore(Card card) {
        if (card.price() == 1) {
            if (21 - score >= 11) {
                score += 11;
            } else {
                score += 1;
            }
        } else {
            score += card.price();
        }
    }

    protected void checkBust() {
        if (score > 21) {
            isActive = false;
        }
    }

    protected void setupImage(ImageView imageView, String imagePath, double imageHeight) {
        imageView.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        imageView.setFitHeight(imageHeight);
        imageView.setPreserveRatio(true);
    }

    public int getScore() {
        return score;
    }

    public boolean isActive() {
        return !isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
