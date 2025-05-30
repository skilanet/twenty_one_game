package ru.sergey.dev.twenty_one_game.presentation;

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

    private boolean isNetworkMode = false;
    private NetworkTwentyOneGame networkGame;
    private AITwentyOneGame aiGame;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRootContainer();
        setupHBoxes();
        setupImage(imageDeck, hboxDeckWithControllers.getPrefHeight(), "/images/deck_green.png");
        if (MediaPlayerController.isMediaPlayerNotNull()) {
            setupImage(imageStopMusic, 32.0, "/images/stop_music.png");
        }
        initializeGameModes();
    }

    private void initializeGameModes() {
        // Инициализируем сетевую игру
        networkGame = NetworkTwentyOneGame.getInstance();
        networkGame.initialize(
                hboxUserCards,
                hboxOpponentCards,
                totalScores,
                apRoot,
                this::changeButtonsDisabling,
                this::returnToMainMenu,
                this::handleGameResult
        );

        // Инициализируем игру с ИИ
        aiGame = AITwentyOneGame.getInstance();
        aiGame.initialize(
                hboxUserCards,
                hboxOpponentCards,
                totalScores,
                this::changeButtonsDisabling,
                this::handleGameResult
        );
    }

    private void handleGameResult(Object result) {
        if (result instanceof NetworkTwentyOneGame.GameResult(
                boolean isPlayerWon, boolean isDraw, int playerScore, int opponentScore
        )) {
            showGameEndDialog(isPlayerWon, isDraw,
                    playerScore, opponentScore);
        } else if (result instanceof AITwentyOneGame.GameResult(
                boolean isPlayerWon, boolean isDraw, int playerScore, int aiScore
        )) {
            showGameEndDialog(isPlayerWon, isDraw,
                    playerScore, aiScore);
        }
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

    @FXML
    private void onGetCardButtonClick(ActionEvent ignoredAction) {
        if (isNetworkMode) {
            networkGame.hit();
        } else {
            aiGame.hit();
        }
    }

    @FXML
    private void onStandButtonClick(ActionEvent ignoredAction) {
        if (isNetworkMode) {
            networkGame.stand();
        } else {
            aiGame.stand();
        }
    }

    @FXML
    private void onDeckImageViewClicked(MouseEvent ignoredAction) {
        if (isNetworkMode) {
            networkGame.hit();
        } else {
            aiGame.hit();
        }
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
                if (isNetworkMode) {
                    networkGame.disconnect();
                }
            });

            dialogStage.showAndWait();

            if (dialogController.isPlayAgain()) {
                if (isNetworkMode) {
                    // В сетевой игре возвращаемся в главное меню
                    returnToMainMenu();
                } else {
                    aiGame.startGame();
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
            // Очищаем состояние игр
            if (isNetworkMode) {
                networkGame.reset();
            } else {
                aiGame.reset();
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
                aiGame.startGame();
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

    public void setNetworkMode(boolean networkMode) {
        this.isNetworkMode = networkMode;
        if (!networkMode) {
            aiGame.startGame();
        } else {
            NetworkTwentyOneGame networkGame = NetworkTwentyOneGame.getInstance();
            networkGame.initialize(
                    hboxUserCards,
                    hboxOpponentCards,
                    totalScores,
                    apRoot,
                    this::changeButtonsDisabling,
                    this::returnToMainMenu,
                    this::handleGameResult
            );

            networkGame.initializeGameUI();
        }
    }
}