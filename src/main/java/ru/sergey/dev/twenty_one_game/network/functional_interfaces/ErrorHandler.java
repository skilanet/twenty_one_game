package ru.sergey.dev.twenty_one_game.network.functional_interfaces;

@FunctionalInterface
public interface ErrorHandler {
    void handle(String error, Throwable throwable);
}
