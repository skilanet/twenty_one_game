package ru.sergey.dev.twenty_one_game.network.functional_interfaces;

import ru.sergey.dev.twenty_one_game.model.dto.response.GameEvent;

@FunctionalInterface
public interface GameEventHandler {
    void handleEvent(GameEvent event);
}
