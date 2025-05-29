package ru.sergey.dev.twenty_one_game.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class TwentyOneGameApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlLocation = getClass().getResource("/layout/start-screen.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Не удалось найти FXML файл");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        stage.setTitle("Игра 21 очко");
        stage.setMinWidth(800);
        stage.setMinHeight(450);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}