package ru.sergey.dev.twenty_one_game.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DialogController {
    @FXML
    private Label resultLabel;

    @FXML
    private Label scoreLabel;

    private Stage dialogStage;
    private boolean playAgain = false;

    // Для обычной игры с ботом
    public void initData(boolean isPlayerWon, boolean isDraw, int playerScore, int dealerScore, Stage dialogStage) {
        this.dialogStage = dialogStage;

        if (isPlayerWon) {
            resultLabel.setText("Вы выиграли :)");
            resultLabel.setStyle("-fx-text-fill: #00ff00;"); // Зеленый цвет для победы
        } else if (!isDraw) {
            resultLabel.setText("Вы проиграли :(");
            resultLabel.setStyle("-fx-text-fill: #ff4444;"); // Красный цвет для проигрыша
        } else {
            resultLabel.setText("Ничья :0");
            resultLabel.setStyle("-fx-text-fill: #ffaa00;"); // Оранжевый для ничьи
        }

        scoreLabel.setText("Ваш счет: " + playerScore + " / Счет соперника: " + dealerScore);
    }

    @FXML
    private void playAgain(ActionEvent ignoredEvent) {
        playAgain = true;
        dialogStage.close();
    }

    @FXML
    private void goToMainMenu(ActionEvent ignoredEvent) {
        playAgain = false;
        dialogStage.close();
    }

    public boolean isPlayAgain() {
        return playAgain;
    }
}