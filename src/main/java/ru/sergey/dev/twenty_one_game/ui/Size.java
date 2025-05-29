package ru.sergey.dev.twenty_one_game.ui;

import javafx.scene.layout.Pane;

public class Size {
    public static int WIDTH = 1080;
    public static int HEIGHT = 720;
    public static final int DEFAULT_HBOX_HEIGHT = 200;

    public static void updateSizeProperties(Pane pane) {
        pane.heightProperty().addListener((observable, oldValue, newValue) -> Size.HEIGHT = newValue.intValue());
        pane.widthProperty().addListener((observable, oldValue, newValue) -> Size.WIDTH = newValue.intValue());
    }
}
