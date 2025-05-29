module ru.sergey.dev.twenty_one_game {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.media;
    requires javafx.graphics;
    requires javafx.base;
    requires java.net.http;
    requires com.google.gson;

    exports ru.sergey.dev.twenty_one_game.network.functional_interfaces;
    exports ru.sergey.dev.twenty_one_game.model.dto.response;


    exports ru.sergey.dev.twenty_one_game.presentation;
    opens ru.sergey.dev.twenty_one_game.presentation to javafx.fxml;
    exports ru.sergey.dev.twenty_one_game.ui;
    opens ru.sergey.dev.twenty_one_game.ui to javafx.fxml;
    exports ru.sergey.dev.twenty_one_game.model.blackjack;
    opens ru.sergey.dev.twenty_one_game.model.blackjack to javafx.fxml;
    exports ru.sergey.dev.twenty_one_game.model.cards;
    opens ru.sergey.dev.twenty_one_game.model.cards to javafx.fxml;
    exports ru.sergey.dev.twenty_one_game.model.mediaplayer;
    opens ru.sergey.dev.twenty_one_game.model.mediaplayer to javafx.fxml;
    exports ru.sergey.dev.twenty_one_game.network;
    opens ru.sergey.dev.twenty_one_game.network to javafx.fxml;
    exports ru.sergey.dev.twenty_one_game.model.dto.requests;
    opens ru.sergey.dev.twenty_one_game.model.dto.requests to com.google.gson;
}