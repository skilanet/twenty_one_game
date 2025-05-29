package ru.sergey.dev.twenty_one_game.model.blackjack;

import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import ru.sergey.dev.twenty_one_game.model.cards.Card;
import ru.sergey.dev.twenty_one_game.model.cards.Deck;

import java.util.Objects;
import java.util.Random;

public class BlackJackAI extends BlackJack {

    private static final int SOFT_STOP = 17;
    private static final int ANIMATION_DURATION = 500;
    private static final int CARD_DELAY = 700;

    private final int aggressiveness;
    private final Random random;
    private boolean revealingCards = false;

    public BlackJackAI(HBox cardsContainer, Deck deck, int aggressiveness) {
        super(cardsContainer, deck);
        this.aggressiveness = Math.min(100, Math.max(0, aggressiveness));
        this.random = new Random();
    }

    public void collectAllCards() {
        if (revealingCards) return;
        collectNextCard(0);
    }

    private void collectNextCard(int step) {
        if (!isActive || !wantsCard()) {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> revealCards());
            pause.play();
            return;
        }

        Card card = deck.getRandomCard();
        addCard(card, true);

        PauseTransition pause = new PauseTransition(Duration.millis(CARD_DELAY));
        pause.setOnFinished(e -> collectNextCard(step + 1));
        pause.play();
    }

    public void revealCards() {
        if (revealingCards) return;
        revealingCards = true;

        revealCardSequentially(0);
    }

    private void revealCardSequentially(int index) {
        if (index >= cards.size()) {
            revealingCards = false;
            isActive = false;
            return;
        }

        Card card = cards.get(index);
        ImageView cardView = (ImageView) cardsContainer.getChildren().get(index);

        Image frontImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(card.imagePath())));

        flipCard(cardView, frontImage, () -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> revealCardSequentially(index + 1));
            pause.play();
        });
    }

    private void flipCard(ImageView cardImageView, Image frontImage, Runnable onFinished) {
        ScaleTransition hideCard = new ScaleTransition(Duration.millis((double) BlackJackAI.ANIMATION_DURATION / 2), cardImageView);
        hideCard.setFromX(1);
        hideCard.setToX(0);

        ScaleTransition showCard = new ScaleTransition(Duration.millis((double) BlackJackAI.ANIMATION_DURATION / 2), cardImageView);
        showCard.setFromX(0);
        showCard.setToX(1);

        hideCard.setOnFinished(event -> {
            cardImageView.setImage(frontImage);
            showCard.play();
        });

        showCard.setOnFinished(event -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });

        hideCard.play();
    }

    public boolean wantsCard() {
        if (score > 21) {
            return false;
        }

        if (score < SOFT_STOP) {
            return true;
        } else if (score == 21) {
            return false;
        } else {
            return makeStrategyDecision();
        }
    }

    private boolean makeStrategyDecision() {
        int riskFactor = 0;

        if (aggressiveness > 50) {
            riskFactor += (aggressiveness - 50) / 5;
        } else {
            riskFactor -= (50 - aggressiveness) / 10;
        }

        if (score <= 18) {
            riskFactor += (19 - score);
        }

        boolean hasAce = false;
        for (Card card : cards) {
            if (card.price() == 1) {
                hasAce = true;
                break;
            }
        }

        if (hasAce && score <= 21 && score - 10 >= 0) {
            riskFactor += 2;
        }

        int randomFactor = random.nextInt(10);

        int chanceToHit = Math.max(0, Math.min(100, riskFactor * 5 + randomFactor));

        if (random.nextInt(100) < 5) {
            return score < 19;
        }

        return random.nextInt(100) < chanceToHit;
    }

    public boolean isRevealingCards() {
        return revealingCards;
    }
}