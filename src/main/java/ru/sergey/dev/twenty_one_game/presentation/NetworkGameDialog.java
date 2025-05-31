package ru.sergey.dev.twenty_one_game.presentation;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

public class NetworkGameDialog {
    private final Stage dialogStage;
    private final TextField nameField;
    private final Button connectButton;
    private final Button cancelButton;
    private final Label statusLabel;
    private final Label charCountLabel;
    private final ProgressIndicator progressIndicator;

    private String playerName;
    private boolean cancelled = false;
    private Consumer<String> connectAction;
    private Runnable onCancel;

    public NetworkGameDialog(Stage owner) {
        dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle("Подключение к сетевой игре");
        dialogStage.setResizable(false);

        // Создаем элементы
        Label titleLabel = new Label("Подключение к сетевой игре");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #71ea71;");

        Label promptLabel = new Label("Введите ваше имя:");
        promptLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #71ea71;");

        nameField = new TextField();
        nameField.setPromptText("Имя игрока");
        nameField.setMaxWidth(400);
        nameField.setStyle("-fx-font-size: 14px; -fx-background-color: rgba(255, 255, 255, 0.9);");

        charCountLabel = new Label("0/255");
        charCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #71ea71;");

        // Прогресс индикатор и статус
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setVisible(false);

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #71ea71;");
        statusLabel.setVisible(false);

        // Кнопки
        connectButton = new Button("Подключиться");
        connectButton.setDisable(true);
        connectButton.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5;"
        );

        cancelButton = new Button("Отмена");
        cancelButton.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-background-color: #f44336; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5;"
        );

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(cancelButton, connectButton);

        // Контейнер для прогресса
        VBox progressBox = new VBox(10);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.getChildren().addAll(progressIndicator, statusLabel);

        // Главный контейнер
        VBox mainContainer = new VBox(15);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: #006400;");
        mainContainer.getChildren().addAll(
                titleLabel,
                promptLabel,
                nameField,
                charCountLabel,
                progressBox,
                buttonBox
        );

        // Обработчики
        setupHandlers();

        // Создаем сцену
        Scene scene = new Scene(mainContainer, 500, 350);
        dialogStage.setScene(scene);
    }

    private void setupHandlers() {
        // Ограничение на 255 символов
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 255) {
                nameField.setText(oldValue);
            } else {
                charCountLabel.setText(newValue.length() + "/255");
                connectButton.setDisable(newValue.trim().isEmpty());
            }
        });

        connectButton.setOnAction(e -> {
            playerName = nameField.getText().trim();
            showConnecting();
            // Запускаем подключение в отдельном потоке
            if (connectAction != null) {
                connectAction.accept(playerName);
            }
        });

        cancelButton.setOnAction(e -> {
            cancelled = true;
            if (onCancel != null) {
                onCancel.run();
            }
            dialogStage.close();
        });

        dialogStage.setOnCloseRequest(e -> {
            cancelled = true;
            if (onCancel != null) {
                onCancel.run();
            }
        });
    }

    private void showConnecting() {
        // Скрываем поля ввода
        nameField.setDisable(true);
        connectButton.setDisable(true);

        // Показываем прогресс
        progressIndicator.setVisible(true);
        statusLabel.setVisible(true);
        updateStatus("Подключение к серверу...");
    }

    public void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    public void showError(String error) {
        Platform.runLater(() -> {
            progressIndicator.setVisible(false);
            statusLabel.setText(error);
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff0000;");

            // Возвращаем возможность ввода
            nameField.setDisable(false);
            connectButton.setDisable(false);
        });
    }

    public void show() {
        dialogStage.show();
    }

    public void setOnConnect(Consumer<String> action) {
        this.connectAction = action;
    }

    public void setOnCancel(Runnable action) {
        this.onCancel = action;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void close() {
        Platform.runLater(dialogStage::close);
    }
}