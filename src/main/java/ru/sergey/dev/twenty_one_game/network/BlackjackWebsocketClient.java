package ru.sergey.dev.twenty_one_game.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.sergey.dev.twenty_one_game.model.dto.requests.JoinCommand;
import ru.sergey.dev.twenty_one_game.model.dto.requests.PlayerCommand;
import ru.sergey.dev.twenty_one_game.network.functional_interfaces.ConnectionHandler;
import ru.sergey.dev.twenty_one_game.network.functional_interfaces.ErrorHandler;
import ru.sergey.dev.twenty_one_game.network.functional_interfaces.GameEventHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

public class BlackjackWebsocketClient {
    private WebSocket webSocket;
    private final Gson gson = new Gson();
    private GameEventHandler gameEventHandler;
    private ConnectionHandler connectionHandler;
    private ErrorHandler errorHandler;

    public BlackjackWebsocketClient onGameEvent(GameEventHandler handler) {
        this.gameEventHandler = handler;
        return this;
    }

    public BlackjackWebsocketClient onConnectionHandler(ConnectionHandler handler) {
        this.connectionHandler = handler;
        return this;
    }

    public BlackjackWebsocketClient onErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    private void connectWebSocket() {
        Thread socketThread = new Thread(() -> {
            try (HttpClient client = HttpClient.newHttpClient();) {
                WebSocket.Builder builder = client.newWebSocketBuilder();

                webSocket = builder.buildAsync(new URI("ws://0.0.0.0:8080/game"), new WebsocketListener()).join();
            } catch (URISyntaxException e) {
                System.out.println("Incorrect URI");
            }
        });
        socketThread.setDaemon(true);
        socketThread.start();
    }

    private static class WebsocketListener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("Websocket opened");
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String message = data.toString();
            System.out.println("Message received: " + message);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("Websocket closed with status code: " + statusCode + " and reason: " + reason);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("Websocket error: " + error.getMessage());
        }
    }

    private void joinToGame() {
        sendCommand(new JoinCommand("Player 1"));
    }

    private void sendCommand(PlayerCommand command) {
        if (webSocket != null) {
            String json = gson.toJson(command);
            webSocket.sendText(json, true);
        }
    }

    private Gson createGson() {
        return GsonBuilder()
                .registerTypeAdapter(PlayerCommand.class, new PlayerCommandDeserializer())
                .create();
    }

    public static void main(String[] args) {
        BlackjackWebsocketClient client = new BlackjackWebsocketClient();
        client.connectWebSocket();
        System.out.println("Введите команду: 1 - войти в игру");
        Scanner scanner = new Scanner(System.in);
        int command = scanner.nextInt();
        while (command != 0) {
            switch (command) {
                case 1:
                    client.joinToGame();
                    break;
                default:
                    System.out.println("Ты еблан команда неверная!");
            }
            command = scanner.nextInt();
        }
    }
}