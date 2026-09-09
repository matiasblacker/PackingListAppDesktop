package com.logistics.packinglist.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CompletionStage;

public class WebSocketManager {

    private static WebSocketManager instance;
    private final Gson gson = new Gson();
    private final Map<String, Set<UpdateListener>> listeners = new ConcurrentHashMap<>();
    private WebSocket webSocket;
    private boolean isConnecting = false;
    private boolean userDisconnected = false;

    public interface UpdateListener {
        void onUpdate(String action);
    }

    public interface NotificationListener {
        void onNotification(String entity, String action, String user, String message, String id);
    }

    private final Set<NotificationListener> notificationListeners = new CopyOnWriteArraySet<>();

    private WebSocketManager() {}

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public synchronized void connect() {
        if (isConnecting) return;
        
        AuthService auth = AuthService.getInstance();
        if (!auth.isLoggedIn()) {
            System.out.println("Cannot connect to WebSocket: User is not logged in.");
            return;
        }

        String token = auth.getToken();
        String baseUrl = auth.getBaseUrl();
        String wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws-wms";

        System.out.println("Connecting to WebSocket: " + wsUrl);
        isConnecting = true;
        userDisconnected = false;

        HttpClient.newHttpClient().newWebSocketBuilder()
                .header("Authorization", "Bearer " + token)
                .buildAsync(URI.create(wsUrl), new WebSocketListenerImpl())
                .thenAccept(ws -> {
                    synchronized (this) {
                        this.webSocket = ws;
                        this.isConnecting = false;
                    }
                    System.out.println("WebSocket connection successfully established.");
                })
                .exceptionally(ex -> {
                    synchronized (this) {
                        this.isConnecting = false;
                    }
                    System.err.println("WebSocket connection failed: " + ex.getMessage());
                    Platform.runLater(this::showReconnectionAlert);
                    return null;
                });
    }

    public synchronized void disconnect() {
        userDisconnected = true;
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "User logging out")
                    .thenRun(() -> {
                        synchronized (this) {
                            webSocket = null;
                        }
                    });
        }
    }

    public void subscribe(String entity, UpdateListener listener) {
        listeners.computeIfAbsent(entity.toUpperCase(), k -> new CopyOnWriteArraySet<>()).add(listener);
    }

    public void unsubscribe(String entity, UpdateListener listener) {
        Set<UpdateListener> set = listeners.get(entity.toUpperCase());
        if (set != null) {
            set.remove(listener);
        }
    }

    public void subscribeNotifications(NotificationListener listener) {
        notificationListeners.add(listener);
    }

    public void unsubscribeNotifications(NotificationListener listener) {
        notificationListeners.remove(listener);
    }

    private void notifyListeners(String entity, String action) {
        Set<UpdateListener> set = listeners.get(entity.toUpperCase());
        if (set != null) {
            for (UpdateListener listener : set) {
                Platform.runLater(() -> {
                    try {
                        listener.onUpdate(action);
                    } catch (Exception e) {
                        System.err.println("Error notifying listener for entity " + entity + ": " + e.getMessage());
                    }
                });
            }
        }
    }

    private void showReconnectionAlert() {
        // Do not prompt if user manually disconnected or logged out
        if (userDisconnected || !AuthService.getInstance().isLoggedIn()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conexión perdida");
        alert.setHeaderText("Se perdió la conexión en tiempo real con el servidor.");
        alert.setContentText("¿Desea intentar reconectarse ahora mismo?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            connect();
        }
    }

    private class WebSocketListenerImpl implements WebSocket.Listener {

        private final StringBuilder textBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String completeMessage = textBuffer.toString();
                textBuffer.setLength(0);
                handleMessage(completeMessage);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            System.out.println("WebSocket closed: " + statusCode + " - " + reason);
            synchronized (WebSocketManager.this) {
                WebSocketManager.this.webSocket = null;
            }
            if (!userDisconnected) {
                Platform.runLater(WebSocketManager.this::showReconnectionAlert);
            }
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("WebSocket error: " + error.getMessage());
            synchronized (WebSocketManager.this) {
                WebSocketManager.this.webSocket = null;
            }
            if (!userDisconnected) {
                Platform.runLater(WebSocketManager.this::showReconnectionAlert);
            }
        }

        private void handleMessage(String messageText) {
            try {
                System.out.println("WS Message received: " + messageText);
                JsonObject json = gson.fromJson(messageText, JsonObject.class);
                if (json.has("entity") && json.has("action")) {
                    String entity = json.get("entity").getAsString();
                    String action = json.get("action").getAsString();
                    notifyListeners(entity, action);

                    // Extract user and message if present
                    String user = json.has("user") && !json.get("user").isJsonNull() ? json.get("user").getAsString() : null;
                    String msg = json.has("message") && !json.get("message").isJsonNull() ? json.get("message").getAsString() : null;
                    String id = json.has("id") && !json.get("id").isJsonNull() ? json.get("id").getAsString() : null;

                    if (user != null && msg != null) {
                        for (NotificationListener listener : notificationListeners) {
                            Platform.runLater(() -> {
                                try {
                                    listener.onNotification(entity, action, user, msg, id);
                                } catch (Exception e) {
                                    System.err.println("Error notifying notification listener: " + e.getMessage());
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse websocket message payload: " + e.getMessage());
            }
        }
    }
}
