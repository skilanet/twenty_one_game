package ru.sergey.dev.twenty_one_game.network.functional_interfaces;

@FunctionalInterface
public interface ConnectionHandler {
    void handle(boolean connected, String message);
}
