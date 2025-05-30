package ru.sergey.dev.twenty_one_game.presentation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import ru.sergey.dev.twenty_one_game.model.blackjack.BlackJackAI;
import ru.sergey.dev.twenty_one_game.model.blackjack.BlackJackPlayer;
import ru.sergey.dev.twenty_one_game.model.cards.Deck;

import java.util.function.Consumer;

public class AITwentyOneGame {
    private static AITwentyOneGame instance;

    private Deck deck;
    private BlackJackPlayer blackJackPlayer;
    private BlackJackAI blackJackAI;
    private boolean gameEnded = false;

    // UI элементы
    private HBox hboxUserCards;
    private HBox hboxOpponentCards;
    private Label totalScores;

    // Callbacks
    private Consumer<Boolean> onButtonsStateChanged;
    private Consumer<GameResult> onGameResult;

    private AITwentyOneGame() {}

    public static AITwentyOneGame getInstance() {
        if (instance == null) {
            instance = new AITwentyOneGame();
        }
        return instance;
    }

    public void initialize(HBox hboxUserCards, HBox hboxOpponentCards, Label totalScores,
                           Consumer<Boolean> onButtonsStateChanged, Consumer<GameResult> onGameResult) {
        this.hboxUserCards = hboxUserCards;
        this.hboxOpponentCards = hboxOpponentCards;
        this.totalScores = totalScores;
        this.onButtonsStateChanged = onButtonsStateChanged;
        this.onGameResult = onGameResult;
    }

    public void startGame() {
        hboxUserCards.getChildren().clear();
        hboxOpponentCards.getChildren().clear();
        gameEnded = false;

        deck = new Deck();

        blackJackPlayer = new BlackJackPlayer(hboxUserCards, deck, totalScores);
        blackJackAI = new BlackJackAI(hboxOpponentCards, deck, 50);

        if (onButtonsStateChanged != null) {
            onButtonsStateChanged.accept(false);
        }

        dealInitialCards();
    }

    private void dealInitialCards() {
        blackJackPlayer.getCard();
        blackJackAI.addCard(deck.getRandomCard(), true);
    }

    public void hit() {
        if (!gameEnded) {
            blackJackPlayer.getCard();

            if (!blackJackPlayer.isActive()) {
                blackJackAI.revealCards();
                endGame(false, false);
            }
        }
    }

    public void stand() {
        if (!gameEnded) {
            blackJackPlayer.setActive(false);
            if (onButtonsStateChanged != null) {
                onButtonsStateChanged.accept(true);
            }
            aiTurn();
        }
    }

    private void aiTurn() {
        blackJackAI.collectAllCards();

        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(100), event -> {
            if (!blackJackAI.isRevealingCards() && blackJackAI.isActive()) {
                timeline.stop();
                Platform.runLater(this::determineWinner);
            }
        });

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void determineWinner() {
        int playerScore = blackJackPlayer.getScore();
        int aiScore = blackJackAI.getScore();

        boolean playerBust = playerScore > 21;
        boolean aiBust = aiScore > 21;

        if (playerBust) {
            endGame(false, false);
        } else if (aiBust) {
            endGame(true, false);
        } else if (playerScore > aiScore) {
            endGame(true, false);
        } else {
            endGame(false, aiScore == playerScore);
        }
    }

    private void endGame(boolean isPlayerWins, boolean isDraw) {
        gameEnded = true;
        if (onButtonsStateChanged != null) {
            onButtonsStateChanged.accept(true);
        }

        if (onGameResult != null) {
            onGameResult.accept(new GameResult(isPlayerWins, isDraw,
                    blackJackPlayer.getScore(), blackJackAI.getScore()));
        }
    }

    public void reset() {
        gameEnded = false;
        deck = null;
        blackJackPlayer = null;
        blackJackAI = null;
    }

    public record GameResult(boolean isPlayerWon, boolean isDraw, int playerScore, int aiScore) {
    }
}