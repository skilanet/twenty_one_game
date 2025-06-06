package ru.sergey.dev.twenty_one_game.presentation;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import ru.sergey.dev.twenty_one_game.model.dto.CardDto;
import ru.sergey.dev.twenty_one_game.model.dto.GameStateDto;
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
    private Runnable onGameStartCallback;
    private GameStateDto gameStateDto;

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
        opponentNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #71ea71;");
        opponentNameLabel.setVisible(false);
        AnchorPane.setTopAnchor(opponentNameLabel, 16.0); // Как в game-screen.fxml
        AnchorPane.setLeftAnchor(opponentNameLabel, 24.0); // Фиксированное значение

        gameStatusLabel = new Label("");
        gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #71ea71;");
        gameStatusLabel.setVisible(false);
        AnchorPane.setTopAnchor(gameStatusLabel, 16.0);
        AnchorPane.setRightAnchor(gameStatusLabel, 200.0);

        apRoot.getChildren().addAll(opponentNameLabel, gameStatusLabel);
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

    // Новый метод для установки callback на начало игры
    public void setOnGameStartCallback(Runnable callback) {
        this.onGameStartCallback = callback;
    }

    // Модифицированный метод connectToNetwork - убираем переключение сцены
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

            isGameStarted = true;

            // Находим противника
            for (PlayerDto player : gameState.getPlayers()) {
                if (!player.getId().equals(networkManager.getCurrentPlayer().getId())) {
                    opponentName = player.getName();
                    break;
                }
            }

            // Вызываем callback для переключения сцены
            if (onGameStartCallback != null) {
                onGameStartCallback.run();
            }

            this.gameStateDto = gameState;

            // Остальная логика инициализации игры будет выполнена после переключения сцены
            // через метод initializeGameUI()
        });

        networkManager.setOnCardDealt(cardDealt -> {
            if (cardDealt.isForCurrentPlayer()) {
                addCardToHand(hboxUserCards, cardDealt.card(), false);
                myScore += cardDealt.card().getPrice();
                updateScoreLabel(); // Вызываем здесь!
                if (myScore > 21) {
                    if (onButtonsStateChanged != null) {
                        onButtonsStateChanged.accept(true);
                    }
                }
            } else {
                addCardToHand(hboxOpponentCards, null, true);
            }
        });

        networkManager.setOnPlayerStood(playerId -> Platform.runLater(this::updateGameStatus));

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

        networkManager.setOnInfo(info -> {
            if (gameStatusLabel != null) {
                gameStatusLabel.setText(info);
            }
        });

        networkManager.setOnError(error -> {
            if (dialog != null && !dialog.isCancelled()) {
                dialog.showError("Ошибка: " + error);
            } else if (gameStatusLabel != null) {
                gameStatusLabel.setText("Ошибка: " + error);
                gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff0000;");
            }
        });

        networkManager.setOnConnectionChanged(status -> {
            if (!status.connected() && isGameStarted) {
                if (gameStatusLabel != null) {
                    gameStatusLabel.setText("Соединение потеряно");
                }
                if (onButtonsStateChanged != null) {
                    onButtonsStateChanged.accept(true);
                }
            }
        });

        networkManager.setOnOpponentCards(cards -> {
            hboxOpponentCards.getChildren().clear();
            for (CardDto card : cards.getCards()) {
                addCardToHand(hboxOpponentCards, card, false);
            }
        });
    }

    // Новый метод для инициализации UI после переключения сцены
    public void initializeGameUI() {
        if (opponentNameLabel != null) {
            opponentNameLabel.setText("Противник: " + opponentName);
            opponentNameLabel.setVisible(true);
        }

        if (gameStatusLabel != null) {
            gameStatusLabel.setVisible(true);
            updateGameStatus();
        }

        // Очищаем поле
        if (hboxUserCards != null) {
            hboxUserCards.getChildren().clear();
        }
        if (hboxOpponentCards != null) {
            hboxOpponentCards.getChildren().clear();
        }

        if (gameStateDto != null) {
            for (PlayerDto player : gameStateDto.getPlayers()) {
                if (player.getId().equals(networkManager.getCurrentPlayer().getId())) {
                    myScore = player.getScore();
                    updateScoreLabel();
                    for (CardDto card : player.getHand()) {
                        addCardToHand(hboxUserCards, card, false);
                    }
                } else {
                    for (int i = 0; i < player.getCards_in_hand(); i++) {
                        addCardToHand(hboxOpponentCards, null, true);
                    }
                }
            }
        }
    }

    private void updateScoreLabel() {
        totalScores.setText("Текущее количество очков: " + myScore);
        if (myScore > 21) {
            totalScores.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff0000;");
            gameStatusLabel.setText("Перебор!");
            gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff0000;");
        } else if (myScore == 21) {
            totalScores.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00ff00;");
        } else {
            totalScores.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #71ea71;");
        }
    }

    public record GameResult(boolean isPlayerWon, boolean isDraw, int playerScore, int opponentScore) { }
}