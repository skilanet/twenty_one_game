package ru.sergey.dev.twenty_one_game.presentation;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import ru.sergey.dev.twenty_one_game.model.dto.CardDto;
import ru.sergey.dev.twenty_one_game.model.dto.PlayerDto;
import ru.sergey.dev.twenty_one_game.model.network.NetworkGameManager;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NetworkTwentyOneGame {
    private static NetworkTwentyOneGame instance;

    private NetworkGameManager networkManager;
    private boolean isGameStarted = false;
    private int myScore = 0;
    private int opponentScore = 0;
    private String opponentName = "";

    // UI элементы
    private Label opponentNameLabel;
    private Label gameStatusLabel;
    private HBox hboxUserCards;
    private HBox hboxOpponentCards;
    private Label totalScores;

    // Callbacks
    private Consumer<Boolean> onButtonsStateChanged;
    private Runnable onGameEnded;
    private Consumer<GameResult> onGameResult;

    private NetworkTwentyOneGame() {}

    public static NetworkTwentyOneGame getInstance() {
        if (instance == null) {
            instance = new NetworkTwentyOneGame();
        }
        return instance;
    }

    public void initialize(HBox hboxUserCards, HBox hboxOpponentCards, Label totalScores,
                           AnchorPane apRoot, Consumer<Boolean> onButtonsStateChanged,
                           Runnable onGameEnded, Consumer<GameResult> onGameResult) {
        this.hboxUserCards = hboxUserCards;
        this.hboxOpponentCards = hboxOpponentCards;
        this.totalScores = totalScores;
        this.onButtonsStateChanged = onButtonsStateChanged;
        this.onGameEnded = onGameEnded;
        this.onGameResult = onGameResult;

        setupNetworkLabels(apRoot);
    }

    private void setupNetworkLabels(AnchorPane apRoot) {
        opponentNameLabel = new Label("");
        opponentNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #71ea71;");
        opponentNameLabel.setVisible(false);
        AnchorPane.setTopAnchor(opponentNameLabel, 5.0);
        AnchorPane.setLeftAnchor(opponentNameLabel, hboxOpponentCards.getLayoutX());

        gameStatusLabel = new Label("");
        gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #71ea71;");
        gameStatusLabel.setVisible(false);
        AnchorPane.setTopAnchor(gameStatusLabel, 30.0);
        AnchorPane.setRightAnchor(gameStatusLabel, 10.0);

        apRoot.getChildren().addAll(opponentNameLabel, gameStatusLabel);
    }

    public void connectToNetwork(String playerName, NetworkGameDialog dialog) {
        System.out.println("NetworkTwentyOneGame: начинаем подключение для " + playerName);

        networkManager = new NetworkGameManager();
        setupNetworkCallbacks(dialog);

        CompletableFuture.runAsync(() -> {
            try {
                networkManager.connect(playerName)
                        .thenRun(() -> Platform.runLater(() -> dialog.updateStatus("Поиск игры...")))
                        .get();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    dialog.showError("Ошибка подключения: " + e.getMessage());
                    if (onGameEnded != null) {
                        onGameEnded.run();
                    }
                });
            }
        });
    }

    private void setupNetworkCallbacks(NetworkGameDialog dialog) {
        networkManager.setOnPlayerJoined(player -> dialog.updateStatus("Ожидание других игроков..."));

        networkManager.setOnGameStarted(gameState -> {
            dialog.close();
            isGameStarted = true;

            // Находим противника
            for (PlayerDto player : gameState.getPlayers()) {
                if (!player.getId().equals(networkManager.getCurrentPlayer().getId())) {
                    opponentName = player.getName();
                    opponentNameLabel.setText("Противник: " + opponentName);
                    opponentNameLabel.setVisible(true);
                    break;
                }
            }

            gameStatusLabel.setVisible(true);
            updateGameStatus();

            // Очищаем поле
            hboxUserCards.getChildren().clear();
            hboxOpponentCards.getChildren().clear();

            // Обновляем счет из состояния игры
            for (PlayerDto player : gameState.getPlayers()) {
                if (player.getId().equals(networkManager.getCurrentPlayer().getId())) {
                    myScore = player.getScore();
                    totalScores.setText("Текущее количество очков: " + myScore);

                    if (player.getHand() != null && !player.getHand().isEmpty()) {
                        for (CardDto card : player.getHand()) {
                            addCardToHand(hboxUserCards, card, false);
                        }
                    }
                } else {
                    opponentScore = player.getScore();

                    for (int i = 0; i < 2; i++) {
                        addCardToHand(hboxOpponentCards, null, true);
                    }
                }
            }
        });

        networkManager.setOnCardDealt(cardDealt -> {
            if (cardDealt.isForCurrentPlayer()) {
                addCardToHand(hboxUserCards, cardDealt.card(), false);
                myScore += cardDealt.card().getPrice();
                totalScores.setText("Текущее количество очков: " + myScore);
            } else {
                addCardToHand(hboxOpponentCards, null, true);
            }
        });

        networkManager.setOnPlayerStood(playerId -> updateGameStatus());

        networkManager.setOnGameOver(gameOver -> {
            isGameStarted = false;
            if (onButtonsStateChanged != null) {
                onButtonsStateChanged.accept(true);
            }

            // Показываем карты противника
            hboxOpponentCards.getChildren().clear();
            if (gameOver.finalState() != null) {
                for (PlayerDto player : gameOver.finalState().getPlayers()) {
                    if (!player.getId().equals(networkManager.getCurrentPlayer().getId())) {
                        for (CardDto card : player.getHand()) {
                            addCardToHand(hboxOpponentCards, card, false);
                        }
                        opponentScore = player.getScore();
                    } else {
                        myScore = player.getScore();
                    }
                }
            }

            if (onGameResult != null) {
                onGameResult.accept(new GameResult(gameOver.didIWin(), gameOver.isDraw(), myScore, opponentScore));
            }
        });

        networkManager.setOnInfo(info -> gameStatusLabel.setText(info));

        networkManager.setOnError(error -> {
            gameStatusLabel.setText("Ошибка: " + error);
            gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff0000;");
        });

        networkManager.setOnConnectionChanged(status -> {
            if (!status.connected() && isGameStarted) {
                gameStatusLabel.setText("Соединение потеряно");
                if (onButtonsStateChanged != null) {
                    onButtonsStateChanged.accept(true);
                }
            }
        });
    }

    private void updateGameStatus() {
        if (networkManager.isMyTurn()) {
            gameStatusLabel.setText("Ваш ход");
            gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00ff00;");
        } else {
            gameStatusLabel.setText("Ход противника");
            gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ffff00;");
        }

        if (onButtonsStateChanged != null) {
            onButtonsStateChanged.accept(!networkManager.isMyTurn());
        }
    }

    private void addCardToHand(HBox container, CardDto card, boolean showBack) {
        ImageView cardImageView = new ImageView();
        String imagePath;

        if (showBack || card == null) {
            imagePath = "/images/deck_green.png";
        } else {
            imagePath = card.getImagePath();
        }

        cardImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        cardImageView.setFitHeight(container.getPrefHeight());
        cardImageView.setPreserveRatio(true);
        container.getChildren().add(cardImageView);
    }

    public void hit() {
        if (isGameStarted && networkManager.isMyTurn()) {
            networkManager.hit();
        }
    }

    public void stand() {
        if (isGameStarted && networkManager.isMyTurn()) {
            networkManager.stand();
        }
    }

    public void disconnect() {
        if (networkManager != null) {
            networkManager.disconnect();
        }
        isGameStarted = false;
    }

    public void reset() {
        disconnect();
        myScore = 0;
        opponentScore = 0;
        opponentName = "";
        isGameStarted = false;

        if (opponentNameLabel != null) {
            opponentNameLabel.setVisible(false);
        }
        if (gameStatusLabel != null) {
            gameStatusLabel.setVisible(false);
        }
    }

    public record GameResult(boolean isPlayerWon, boolean isDraw, int playerScore, int opponentScore) { }
}