package ru.sergey.dev.twenty_one_game.network;

import com.google.gson.*;
import ru.sergey.dev.twenty_one_game.model.dto.requests.HitCommandDto;
import ru.sergey.dev.twenty_one_game.model.dto.requests.JoinCommandDto;
import ru.sergey.dev.twenty_one_game.model.dto.requests.PlayerCommandDto;
import ru.sergey.dev.twenty_one_game.model.dto.requests.StandCommandDto;
import ru.sergey.dev.twenty_one_game.model.dto.response.*;
import ru.sergey.dev.twenty_one_game.network.functional_interfaces.ConnectionHandler;
import ru.sergey.dev.twenty_one_game.network.functional_interfaces.ErrorHandler;
import ru.sergey.dev.twenty_one_game.network.functional_interfaces.GameEventHandler;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlackjackWebSocketClient {
    private WebSocket webSocket;
    private final Gson gson;
    private GameEventHandler gameEventHandler;
    private ConnectionHandler connectionHandler;
    private ErrorHandler errorHandler;
    private String currentPlayerId;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private HttpClient httpClient;

    public BlackjackWebSocketClient() {
        this.gson = createGson();
    }

    public BlackjackWebSocketClient onGameEvent(GameEventHandler handler) {
        this.gameEventHandler = handler;
        return this;
    }

    public BlackjackWebSocketClient onConnection(ConnectionHandler handler) {
        this.connectionHandler = handler;
        return this;
    }

    public BlackjackWebSocketClient onError(ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                httpClient = HttpClient.newHttpClient();
                WebSocket.Builder builder = httpClient.newWebSocketBuilder();

                CompletableFuture<WebSocket> wsFuture = builder.buildAsync(
                        new URI("ws://0.0.0.0:8888/game"),
                        new WebSocketListener()
                );

                webSocket = wsFuture.join();
                isConnected.set(true);

                if (connectionHandler != null) {
                    connectionHandler.handle(true, "Подключено к серверу");
                }
            } catch (URISyntaxException e) {
                System.err.println("Ошибка URI: " + e.getMessage());
                isConnected.set(false);
                if (errorHandler != null) {
                    errorHandler.handle("Неверный URI сервера", e);
                }
            } catch (Exception e) {
                System.err.println("Ошибка подключения: " + e.getMessage());
                isConnected.set(false);
                if (errorHandler != null) {
                    errorHandler.handle("Ошибка подключения к серверу", e);
                }
            }
        });
    }

    public void joinGame(String playerName) {
        sendCommand(new JoinCommandDto(playerName));
    }

    public void hit() {
        if (currentPlayerId != null) {
            sendCommand(new HitCommandDto(currentPlayerId));
        } else {
            System.err.println("Невозможно выполнить hit: currentPlayerId is null");
        }
    }

    public void stand() {
        if (currentPlayerId != null) {
            sendCommand(new StandCommandDto(currentPlayerId));
        } else {
            System.err.println("Невозможно выполнить stand: currentPlayerId is null");
        }
    }

    public void disconnect() {
        if (webSocket != null && isConnected.get()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Игрок покинул игру");
            isConnected.set(false);
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }

    private void sendCommand(PlayerCommandDto command) {
        if (webSocket != null && isConnected.get()) {
            try {
                String json = gson.toJson(command);
                System.out.println("Отправляем команду: " + json);
                webSocket.sendText(json, true);
            } catch (Exception e) {
                System.err.println("Ошибка отправки команды: " + e.getMessage());
                if (errorHandler != null) {
                    errorHandler.handle("Ошибка отправки команды", e);
                }
            }
        } else {
            System.err.println("WebSocket не подключен");
        }
    }

    private class WebSocketListener implements WebSocket.Listener {
        private final StringBuilder messageBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("WebSocket открыт");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            messageBuffer.append(data);

            if (last) {
                String message = messageBuffer.toString();
                messageBuffer.setLength(0);

                System.out.println("Получено сообщение: \n" + message);

                try {
                    GameEventDto event = gson.fromJson(message, GameEventDto.class);
                    handleGameEvent(event);
                } catch (JsonParseException e) {
                    System.err.println("Ошибка парсинга JSON: " + e.getMessage());
                    System.err.println("Сообщение: " + message);
                    if (errorHandler != null) {
                        errorHandler.handle("Ошибка парсинга JSON", e);
                    }
                } catch (Exception e) {
                    System.err.println("Общая ошибка обработки сообщения: " + e.getMessage());
                    if (errorHandler != null) {
                        errorHandler.handle("Ошибка обработки сообщения", e);
                    }
                }
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            isConnected.set(false);
            System.out.println("WebSocket закрыт: " + statusCode + " - " + reason);

            if (connectionHandler != null) {
                connectionHandler.handle(false, "Соединение закрыто: " + reason);
            }

            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            isConnected.set(false);
            System.err.println("WebSocket ошибка: " + error.getMessage());

            if (errorHandler != null) {
                errorHandler.handle("Ошибка WebSocket", error);
            }
        }
    }

    private void handleGameEvent(GameEventDto event) {
        if (event instanceof PlayerJoinedEventDto joined) {
            this.currentPlayerId = joined.getPlayer().getId();
        }
        if (gameEventHandler != null) {
            gameEventHandler.handleEvent(event);
        }
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(GameEventDto.class, new GameEventDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    private static class GameEventDeserializer implements JsonDeserializer<GameEventDto> {
        @Override
        public GameEventDto deserialize(JsonElement json, Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            if (!jsonObject.has("type")) {
                throw new JsonParseException("Отсутствует поле 'type' в JSON");
            }

            String type = jsonObject.get("type").getAsString();

            return switch (type) {
                case "PlayerJoined" -> context.deserialize(json, PlayerJoinedEventDto.class);
                case "GameStarted" -> context.deserialize(json, GameStartedEventDto.class);
                case "CardDealt" -> context.deserialize(json, CardDealtEventDto.class);
                case "PlayerStood" -> context.deserialize(json, PlayerStoodEventDto.class);
                case "GameOver" -> context.deserialize(json, GameOverEventDto.class);
                case "Error" -> context.deserialize(json, ErrorEventDto.class);
                case "Info" -> context.deserialize(json, InfoEventDto.class);
                case "AnotherPlayerTookCard" -> context.deserialize(json, AnotherPlayerTookCardEventDto.class);
                case "OpponentCards" -> context.deserialize(json, OpponentCardsEventDto.class);
                default -> {
                    System.err.println("Неизвестный тип события: " + type);
                    throw new JsonParseException("Неизвестный тип события: " + type);
                }
            };
        }
    }
}