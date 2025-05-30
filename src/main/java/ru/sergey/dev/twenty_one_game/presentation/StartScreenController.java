package ru.sergey.dev.twenty_one_game.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.sergey.dev.twenty_one_game.model.mediaplayer.MediaPlayerController;
import ru.sergey.dev.twenty_one_game.ui.Size;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class StartScreenController implements Initializable {

    public VBox vboxRoot;
    public CheckBox checkboxSwitchOnMusic;
    @FXML
    private Button startButton;

    @FXML
    private Button networkButton;

    @FXML
    private Button exitButton;

    @FXML
    private void startGame(ActionEvent ignoredEvent) {
        try {
            setupAndPlayMusic();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/game-screen.fxml"));
            Parent gameRoot = loader.load();

            TwentyOneGameController controller = loader.getController();
            controller.setNetworkMode(false);

            Stage stage = (Stage) startButton.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot, Size.WIDTH, Size.HEIGHT);
            stage.setScene(gameScene);
            stage.setTitle("Игра '21'");
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            showError("Ошибка при загрузке игрового экрана");
        }
    }

    @FXML
    private void startNetworkGame(ActionEvent ignoredEvent) {
        NetworkGameDialog dialog = new NetworkGameDialog((Stage) networkButton.getScene().getWindow());

        dialog.setOnConnect(playerName -> {
            System.out.println("Начинаем подключение для игрока: " + playerName);
            setupAndPlayMusic();

            // Создаем игровой экран в UI потоке
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/game-screen.fxml"));
                Parent gameRoot = loader.load();

                TwentyOneGameController controller = loader.getController();
                controller.setNetworkMode(true);

                // Переключаем сцену
                Stage stage = (Stage) networkButton.getScene().getWindow();
                Scene gameScene = new Scene(gameRoot, Size.WIDTH, Size.HEIGHT);
                stage.setScene(gameScene);
                stage.setTitle("Игра '21' - Сетевая игра");
                stage.show();

                // Начинаем подключение после переключения сцены
                controller.connectToNetwork(playerName, dialog);

            } catch (IOException e) {
                e.printStackTrace();
                dialog.close();
                showError("Ошибка при загрузке игрового экрана: " + e.getMessage());
            }
        });

        dialog.setOnCancel(() -> {
            System.out.println("Подключение отменено");
        });

        dialog.show();
    }

    @FXML
    private void showRules(ActionEvent ignoredEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Правила игры");
        alert.setHeaderText("Правила игры '21'");
        alert.setContentText("""
                Цель игры - набрать очки, максимально близкие к 21, но не превышающие это число.
                
                - Вы и дилер получаете карты
                - Карты от 2 до 10 стоят соответственно их номиналу
                - Валет, Дама и Король стоят по 10 очков
                - Туз может стоить 1 или 11 очков
                - Если вы набрали больше 21 очка - вы проиграли
                - Если у вас больше очков чем у дилера (но не больше 21), вы выиграли
                
                В сетевой игре:
                - Играют два игрока друг против друга
                - Ходы делаются по очереди
                - Карты противника скрыты до конца игры""");
        alert.showAndWait();
    }

    @FXML
    private void exitGame(ActionEvent ignoredEvent) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupAndPlayMusic() {
        if (checkboxSwitchOnMusic.isSelected()) {
            MediaPlayerController.initialize(this.getClass());
        } else if (MediaPlayerController.isMediaPlayerNotNull()) {
            MediaPlayerController.stop();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Size.updateSizeProperties(vboxRoot);
        if (MediaPlayerController.isMediaPlayerNotNull()) {
            MediaPlayerController.stop();
        }
    }
}