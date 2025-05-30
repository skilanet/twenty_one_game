package ru.sergey.dev.twenty_one_game.presentation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.sergey.dev.twenty_one_game.model.blackjack.BlackJackAI;
import ru.sergey.dev.twenty_one_game.model.blackjack.BlackJackPlayer;
import ru.sergey.dev.twenty_one_game.model.cards.Deck;
import ru.sergey.dev.twenty_one_game.model.dto.CardDto;
import ru.sergey.dev.twenty_one_game.model.dto.PlayerDto;
import ru.sergey.dev.twenty_one_game.model.mediaplayer.MediaPlayerController;
import ru.sergey.dev.twenty_one_game.model.network.NetworkGameManager;
import ru.sergey.dev.twenty_one_game.ui.Size;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class TwentyOneGameController implements Initializable {

    @FXML
    public HBox hboxOpponentCards;
    @FXML
    public HBox hboxUserCards;
    @FXML
    public Label totalScores;
    @FXML
    public ImageView imageDeck;
    public Button buttonTakeCard;
    public Button buttonFinish;
    public ImageView imageStopMusic;
    @FXML
    private AnchorPane apRoot;
    @FXML
    private HBox hboxDeckWithControllers;

    @FXML
    private Label opponentNameLabel;
    @FXML
    private Label gameStatusLabel;

    private Deck deck;
    private BlackJackPlayer blackJackPlayer;
    private BlackJackAI blackJackAI;
    private boolean gameEnded = false;

    // Network game fields
    private boolean isNetworkMode = false;
    private NetworkGameManager networkManager;
    private boolean isGameStarted = false;
    private int myScore = 0;
    private int opponentScore = 0;
    private String opponentName = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRootContainer();
        setupHBoxes();
        setupImage(imageDeck, hboxDeckWithControllers.getPrefHeight(), "/images/deck_green.png");
        if (MediaPlayerController.isMediaPlayerNotNull()) {
            setupImage(imageStopMusic, 32.0, "/images/stop_music.png");
        }

        // Добавляем метки для сетевой игры
        setupNetworkLabels();
    }

    private void setupNetworkLabels() {
        // Создаем метки для отображения имени противника и статуса игры
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

    public void setNetworkMode(boolean networkMode) {
        this.isNetworkMode = networkMode;
        if (!networkMode) {
            startGame();
        }
    }

    public void connectToNetwork(String playerName, NetworkGameDialog dialog) {
        System.out.println("TwentyOneGameController: начинаем подключение для " + playerName);

        networkManager = new NetworkGameManager();
        setupNetworkCallbacks(dialog);

        // Подключаемся в фоновом потоке
        CompletableFuture.runAsync(() -> {
            try {
                networkManager.connect(playerName)
                        .thenRun(() -> {
                            Platform.runLater(() -> dialog.updateStatus("Поиск игры..."));
                        })
                        .get(); // Ждем завершения подключения
            } catch (Exception e) {
                Platform.runLater(() -> {
                    dialog.showError("Ошибка подключения: " + e.getMessage());
                    // Возвращаемся в главное меню при ошибке
                    returnToMainMenu();
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

        // Остальные обработчики остаются без изменений...
        networkManager.setOnCardDealt(cardDealt -> {
            if (cardDealt.isForCurrentPlayer()) {
                addCardToHand(hboxUserCards, cardDealt.card(), false);
            } else {
                addCardToHand(hboxOpponentCards, null, true);
            }
        });

        networkManager.setOnPlayerStood(playerId -> {
            updateGameStatus();
        });

        networkManager.setOnGameOver(gameOver -> {
            gameEnded = true;
            changeButtonsDisabling(true);

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

            showGameEndDialog(gameOver.didIWin(), gameOver.isDraw(), myScore, opponentScore);
        });

        networkManager.setOnInfo(info -> {
            gameStatusLabel.setText(info);
        });

        networkManager.setOnError(error -> {
            gameStatusLabel.setText("Ошибка: " + error);
            gameStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff0000;");
        });

        networkManager.setOnConnectionChanged(status -> {
            if (!status.connected() && isGameStarted) {
                gameStatusLabel.setText("Соединение потеряно");
                changeButtonsDisabling(true);
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
        changeButtonsDisabling(!networkManager.isMyTurn());
    }

    private void addCardToHand(HBox container, CardDto card, boolean showBack) {
        ImageView cardImageView = new ImageView();
        String imagePath;

        if (showBack || card == null) {
            imagePath = "/images/deck_green.png";
        } else {
            imagePath = card.getImagePath();
        }

        setupImage(cardImageView, container.getPrefHeight(), imagePath);
        container.getChildren().add(cardImageView);
    }

    private void setupRootContainer() {
        apRoot.heightProperty().addListener((observable, oldValue, newValue) ->
                AnchorPane.setTopAnchor(hboxDeckWithControllers, (newValue.doubleValue() - hboxDeckWithControllers.getPrefHeight()) / 2));
        Size.updateSizeProperties(apRoot);
    }

    private void setupImage(ImageView imageView, double imageHeight, String resource) {
        imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(resource))));
        imageView.setFitHeight(imageHeight);
        imageView.setPreserveRatio(true);
    }

    private void setupHBoxes() {
        hboxDeckWithControllers.setPrefHeight(Size.DEFAULT_HBOX_HEIGHT);
        hboxOpponentCards.setPrefHeight(Size.DEFAULT_HBOX_HEIGHT);
        hboxUserCards.setPrefHeight(Size.DEFAULT_HBOX_HEIGHT);
    }

    private void startGame() {
        hboxUserCards.getChildren().clear();
        hboxOpponentCards.getChildren().clear();
        gameEnded = false;

        deck = new Deck();

        blackJackPlayer = new BlackJackPlayer(hboxUserCards, deck, totalScores);
        blackJackAI = new BlackJackAI(hboxOpponentCards, deck, 50);

        changeButtonsDisabling(false);
        dealInitialCards();
    }

    private void dealInitialCards() {
        blackJackPlayer.getCard();
        blackJackAI.addCard(deck.getRandomCard(), true);
    }

    @FXML
    private void onGetCardButtonClick(ActionEvent ignoredAction) {
        if (isNetworkMode) {
            if (!gameEnded && networkManager.isMyTurn()) {
                networkManager.hit();
            }
        } else {
            if (!gameEnded) {
                blackJackPlayer.getCard();

                if (blackJackPlayer.isActive()) {
                    blackJackAI.revealCards();
                    endGame(false, false);
                }
            }
        }
    }

    @FXML
    private void onStandButtonClick(ActionEvent ignoredAction) {
        if (isNetworkMode) {
            if (!gameEnded && networkManager.isMyTurn()) {
                networkManager.stand();
            }
        } else {
            if (!gameEnded) {
                blackJackPlayer.setActive(false);
                changeButtonsDisabling(true);
                aiTurn();
            }
        }
    }

    @FXML
    private void onDeckImageViewClicked(MouseEvent ignoredAction) {
        if (isNetworkMode) {
            if (!gameEnded && networkManager.isMyTurn()) {
                networkManager.hit();
            }
        } else {
            if (!gameEnded) {
                blackJackPlayer.getCard();

                if (blackJackPlayer.isActive()) {
                    endGame(false, false);
                    blackJackAI.revealCards();
                }
            }
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
        } else endGame(false, aiScore <= playerScore);
    }

    private void endGame(boolean isPlayerWins, boolean isDraw) {
        gameEnded = true;
        changeButtonsDisabling(true);
        showGameEndDialog(isPlayerWins, isDraw,
                isNetworkMode ? myScore : blackJackPlayer.getScore(),
                isNetworkMode ? opponentScore : blackJackAI.getScore());
    }

    private void changeButtonsDisabling(boolean isNeedToDisable) {
        buttonTakeCard.setDisable(isNeedToDisable);
        buttonFinish.setDisable(isNeedToDisable);
    }

    public void showGameEndDialog(boolean isPlayerWon, boolean isDraw, int playerScore, int dealerScore) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Игра завершена");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/game-dialog.fxml"));
            Parent dialogRoot = loader.load();

            DialogController dialogController = loader.getController();

            dialogController.initData(isPlayerWon, isDraw, playerScore, dealerScore, dialogStage);

            Scene dialogScene = new Scene(dialogRoot);
            dialogStage.setScene(dialogScene);
            dialogStage.setResizable(false);
            dialogStage.initOwner(apRoot.getScene().getWindow());
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            dialogStage.setOnCloseRequest(e -> {
                if (isNetworkMode && networkManager != null) {
                    networkManager.disconnect();
                }
            });

            dialogStage.showAndWait();

            if (dialogController.isPlayAgain()) {
                if (isNetworkMode) {
                    // В сетевой игре возвращаемся в главное меню
                    returnToMainMenu();
                } else {
                    startGame();
                }
            } else {
                returnToMainMenu();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void returnToMainMenu() {
        try {
            if (isNetworkMode && networkManager != null) {
                networkManager.disconnect();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/start-screen.fxml"));
            Parent startRoot = loader.load();

            Stage stage = (Stage) apRoot.getScene().getWindow();
            Scene startScene = new Scene(startRoot, Size.WIDTH, Size.HEIGHT);
            stage.setScene(startScene);
            stage.setTitle("21 - Главное меню");
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void onNewGameButtonClick(KeyEvent keyEvent) {
        if (!isNetworkMode) {
            boolean isModifierPressed = keyEvent.isMetaDown() || keyEvent.isControlDown();
            if (isModifierPressed && keyEvent.getCode() == KeyCode.R) {
                startGame();
            }
        }
    }

    public void onStopMusicImageClicked(MouseEvent ignoredMouseEvent) {
        if (MediaPlayerController.isMediaPlayerNotNull()) {
            if (MediaPlayerController.isPlaying()) {
                MediaPlayerController.pause();
            } else {
                MediaPlayerController.play();
            }
        }
    }
}