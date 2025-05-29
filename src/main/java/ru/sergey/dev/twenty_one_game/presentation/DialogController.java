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

    public void initData(boolean isPlayerWon, boolean isDraw, int playerScore, int dealerScore, Stage dialogStage) {
        this.dialogStage = dialogStage;

        if (isPlayerWon) {
            resultLabel.setText("Вы выиграли :)");
            resultLabel.setStyle("-fx-text-fill: #00ff00;"); // Зеленый цвет для победы
        } else if (!isDraw) {
            resultLabel.setText("Вы проиграли :(");
            resultLabel.setStyle("-fx-text-fill: #102502;"); // Красный цвет для проигрыша
        } else {
            resultLabel.setText("Ничья :0");
            resultLabel.setStyle("-fx-text-fill: #102502;");
        }

        scoreLabel.setText("Ваш счет: " + playerScore + " / Счет дилера: " + dealerScore);
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