package ru.sergey.dev.twenty_one_game.model.network;

import javafx.application.Platform;
import ru.sergey.dev.twenty_one_game.model.dto.CardDto;
import ru.sergey.dev.twenty_one_game.model.dto.GameStateDto;
import ru.sergey.dev.twenty_one_game.model.dto.PlayerDto;
import ru.sergey.dev.twenty_one_game.model.dto.response.*;
import ru.sergey.dev.twenty_one_game.network.BlackjackWebSocketClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NetworkGameManager {
    private final BlackjackWebSocketClient client;
    private Consumer<PlayerDto> onPlayerJoined;
    private Consumer<GameStateDto> onGameStarted;
    private Consumer<NetworkCardDealt> onCardDealt;
    private Consumer<String> onPlayerStood;
    private Consumer<GameOverInfo> onGameOver;
    private Consumer<String> onInfo;
    private Consumer<String> onError;
    private Consumer<ConnectionStatus> onConnectionChanged;
    private Consumer<OpponentCardsEventDto> onOpponentCards;

    private PlayerDto currentPlayer;
    private boolean isMyTurn = false;

    public NetworkGameManager() {
        this.client = new BlackjackWebSocketClient().onGameEvent(event -> Platform.runLater(() -> handleGameEvent(event)))
                .onConnection((connected, message) -> Platform.runLater(() -> {
                    if (onConnectionChanged != null) {
                        onConnectionChanged.accept(new ConnectionStatus(connected, message));
                    }
                }))
                .onError((error, throwable) -> Platform.runLater(() -> {
                    if (onError != null) {
                        onError.accept(error);
                    }
                }));
    }

    private void handleGameEvent(GameEventDto event) {
        if (event instanceof PlayerJoinedEventDto joined) {
            currentPlayer = joined.getPlayer();
            if (onPlayerJoined != null) {
                onPlayerJoined.accept(currentPlayer);
            }
        } else if (event instanceof GameStartedEventDto started) {
            GameStateDto gameState = started.getGameState();

            // Находим противника
            for (PlayerDto player : gameState.getPlayers()) {
                if (!player.getId().equals(currentPlayer.getId())) {
                    break;
                }
            }

            isMyTurn = gameState.getCurrentPlayerId().equals(currentPlayer.getId());

            if (onGameStarted != null) {
                onGameStarted.accept(gameState);
            }
        } else if (event instanceof CardDealtEventDto cardDealt) {
            boolean isForMe = cardDealt.getPlayerId().equals(currentPlayer.getId());

            if (onCardDealt != null) {
                onCardDealt.accept(new NetworkCardDealt(
                        cardDealt.getPlayerId(),
                        cardDealt.getCard(),
                        isForMe
                ));
            }
        } else if (event instanceof PlayerStoodEventDto stood) {
            if (onPlayerStood != null) {
                isMyTurn = !stood.getPlayerId().equals(currentPlayer.getId());
                onPlayerStood.accept(stood.getPlayerId());
            }
        } else if (event instanceof GameOverEventDto gameOver) {

            if (onGameOver != null) {
                boolean didIWin = gameOver.getWinner() != null &&
                        gameOver.getWinner().equals(currentPlayer.getId());
                boolean isDraw = gameOver.getWinner() == null;

                onGameOver.accept(new GameOverInfo(
                        didIWin,
                        isDraw,
                        gameOver.getWinner(),
                        gameOver.getGameState()
                ));
            }
        } else if (event instanceof InfoEventDto info) {
            if (onInfo != null) {
                onInfo.accept(info.getMessage());
            }
        } else if (event instanceof ErrorEventDto error) {
            if (onError != null) {
                onError.accept(error.getMessage());
            }
        } else if (event instanceof AnotherPlayerTookCardEventDto) {
            if (onCardDealt != null) {
                onCardDealt.accept(new NetworkCardDealt(
                        "",
                        null,
                        false
                ));
            }
        } else if (event instanceof OpponentCardsEventDto opponentCardsEventDto) {
            if (onOpponentCards != null) {
                onOpponentCards.accept(opponentCardsEventDto);
            }

        }
    }

    public CompletableFuture<Void> connect(String playerName) {
        return client.connect()
                .thenRun(() -> client.joinGame(playerName))
                .exceptionally(throwable -> {
                    System.err.println("Ошибка при подключении: " + throwable.getMessage());
                    Platform.runLater(() -> {
                        if (onError != null) {
                            onError.accept("Не удалось подключиться к серверу. Проверьте соединение.");
                        }
                    });
                    throw new RuntimeException(throwable);
                });
    }

    public void hit() {
        if (isMyTurn) {
            client.hit();
        }
    }

    public void stand() {
        if (isMyTurn) {
            client.stand();
        }
    }

    public void disconnect() {
        client.disconnect();
    }


    public boolean isMyTurn() {
        return isMyTurn;
    }

    public PlayerDto getCurrentPlayer() {
        return currentPlayer;
    }


    // Setters for callbacks
    public void setOnPlayerJoined(Consumer<PlayerDto> onPlayerJoined) {
        this.onPlayerJoined = onPlayerJoined;
    }

    public void setOnGameStarted(Consumer<GameStateDto> onGameStarted) {
        this.onGameStarted = onGameStarted;
    }

    public void setOnCardDealt(Consumer<NetworkCardDealt> onCardDealt) {
        this.onCardDealt = onCardDealt;
    }

    public void setOnPlayerStood(Consumer<String> onPlayerStood) {
        this.onPlayerStood = onPlayerStood;
    }

    public void setOnGameOver(Consumer<GameOverInfo> onGameOver) {
        this.onGameOver = onGameOver;
    }

    public void setOnInfo(Consumer<String> onInfo) {
        this.onInfo = onInfo;
    }

    public void setOnError(Consumer<String> onError) {
        this.onError = onError;
    }

    public void setOnConnectionChanged(Consumer<ConnectionStatus> onConnectionChanged) {
        this.onConnectionChanged = onConnectionChanged;
    }

    public void setOnOpponentCards(Consumer<OpponentCardsEventDto> onOpponentCards) {
        this.onOpponentCards = onOpponentCards;
    }

    // Helper classes
    public record NetworkCardDealt(String playerId, CardDto card, boolean isForCurrentPlayer) {
    }

    public record GameOverInfo(boolean didIWin, boolean isDraw, String winner, GameStateDto finalState) {
    }

    public record ConnectionStatus(boolean connected, String message) {
    }
}