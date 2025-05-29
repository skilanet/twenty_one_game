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
import ru.sergey.dev.twenty_one_game.model.mediaplayer.MediaPlayerController;
import ru.sergey.dev.twenty_one_game.ui.Size;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

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

    private Deck deck;
    private BlackJackPlayer blackJackPlayer;
    private BlackJackAI blackJackAI;
    private boolean gameEnded = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRootContainer();
        setupHBoxes();
        setupImage(imageDeck, hboxDeckWithControllers.getPrefHeight(), "/images/deck_green.png");
        if (MediaPlayerController.isMediaPlayerNotNull()) {
            setupImage(imageStopMusic, 32.0, "/images/stop_music.png");
        }
        startGame();
    }

    private void setupRootContainer() {
        apRoot.heightProperty().addListener((observable, oldValue, newValue) -> AnchorPane.setTopAnchor(hboxDeckWithControllers, (newValue.doubleValue() - hboxDeckWithControllers.getPrefHeight()) / 2));
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

        deck = new  Deck();

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
        if (!gameEnded) {
            blackJackPlayer.getCard();

            if (blackJackPlayer.isActive()) {
                blackJackAI.revealCards();
                endGame(false, false);
            }
        }
    }

    @FXML
    private void onStandButtonClick(ActionEvent ignoredAction) {
        if (!gameEnded) {
            blackJackPlayer.setActive(false);
            changeButtonsDisabling(true);
            aiTurn();
        }
    }

    @FXML
    private void onDeckImageViewClicked(MouseEvent ignoredAction) {
        if (!gameEnded) {
            blackJackPlayer.getCard();

            if (blackJackPlayer.isActive()) {
                endGame(false, false);
                blackJackAI.revealCards();
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
        showGameEndDialog(isPlayerWins, isDraw, blackJackPlayer.getScore(), blackJackAI.getScore());
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
            dialogStage.showAndWait();

            if (dialogController.isPlayAgain()) {
                startGame();
            } else {
                returnToMainMenu();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void returnToMainMenu() {
        try {
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
        boolean isModifierPressed = keyEvent.isMetaDown() || keyEvent.isControlDown();
        if (isModifierPressed && keyEvent.getCode() == KeyCode.R) {
            startGame();
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