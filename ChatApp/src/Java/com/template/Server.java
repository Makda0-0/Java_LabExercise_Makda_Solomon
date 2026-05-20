package com.template;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Application {
    private static final int HISTORY_LIMIT = 30;// messages memory can hold

    public static final String SERVER_NAME = "server";
    public static final CopyOnWriteArrayList<ClientHandler> activeClients = new CopyOnWriteArrayList<>();// for thread tracking

    private static final CopyOnWriteArrayList<String> sessionHistory = new CopyOnWriteArrayList<>();// to store recent messages

    private final int port = 1234;
    private ServerSocket serverSocket;
    private TextArea logArea;
    private TextField serverMessageField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Server");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #101418;");

        VBox headerBar = new VBox(4);
        headerBar.setPadding(new Insets(16));
        headerBar.setStyle("-fx-background-color: #1#d378e6-fx-border-color: #2dd4bf; -fx-border-width: 0 0 1 0;");

        Label serverTitle = new Label("Server");
        serverTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;");

        Label serverStatus = new Label("Listening on port " + port);
        serverStatus.setStyle("-fx-text-fill: #4fd3ff; -fx-font-size: 12px;");

        headerBar.getChildren().addAll(serverTitle, serverStatus);
        root.setTop(headerBar);

        logArea = new TextArea();
        logArea.setEditable(false);//read only
        logArea.setWrapText(true);
        logArea.setStyle("-fx-control-inner-background: #101418; -fx-text-fill: #d8e2ea; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-padding: 10;");
        root.setCenter(logArea);

        HBox controlBar = new HBox(10);
        controlBar.setPadding(new Insets(15));
        controlBar.setStyle("-fx-background-color: #18212b;");

        serverMessageField = new TextField();
        serverMessageField.setPromptText("Type a message...");
        HBox.setHgrow(serverMessageField, Priority.ALWAYS);
        serverMessageField.setStyle("-fx-background-color: #101418; -fx-text-fill: #f2f2f2; -fx-prompt-text-fill: #79aab9; -fx-background-radius: 6; -fx-padding: 10;");

        Button broadcastBtn = new Button("Broadcast");
        broadcastBtn.setStyle("-fx-background-color: #08deff; -fx-text-fill: #101418; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 18; -fx-cursor: hand;");

        controlBar.getChildren().addAll(serverMessageField, broadcastBtn);
        root.setBottom(controlBar);

        broadcastBtn.setOnAction(e -> dispatchServerBroadcast());
        serverMessageField.setOnAction(e -> dispatchServerBroadcast());

        Scene scene = new Scene(root, 620, 460);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> cleanupServer());
        primaryStage.show();

        startNetworkEngine();//starts server
    }
//new thread for server ops
    private void startNetworkEngine() {
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                logEvent("server: online on port " + port);
                logEvent("server: waiting for clients...");

                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    logEvent("server: connection from " + clientSocket.getRemoteSocketAddress());

                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread clientThread = new Thread(handler);
                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            } catch (java.net.BindException e) {
                logEvent("server error: port " + port + " is already in use.");
            } catch (IOException e) {
                if (serverSocket == null || !serverSocket.isClosed()) {
                    logEvent("server error: " + e.getMessage());
                }
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void dispatchServerBroadcast() {
        String serverText = serverMessageField.getText().trim();
        if (!serverText.isEmpty()) {
            broadcastChatMessage(SERVER_NAME, serverText);
            logEvent(SERVER_NAME + ": " + serverText);
            serverMessageField.clear();
        }
    }

    public static void broadcastChatMessage(String sender, String message) {
        rememberMessage(sender, message);
        broadcastMessage(sender + ": " + message);
    }

    public static void rememberMessage(String sender, String message) {
        sessionHistory.add(sender + ": " + message);
        while (sessionHistory.size() > HISTORY_LIMIT) {
            sessionHistory.remove(0);
        }
        Database.saveMessage(sender, message);
    }

    public static List<String> getRecentChatHistory() {
        List<String> databaseHistory = Database.getChatHistory();
        if (!databaseHistory.isEmpty()) {
            return databaseHistory;
        }
        return new ArrayList<>(sessionHistory);
    }

    public static void broadcastMessage(String message) {
        for (ClientHandler client : activeClients) {
            client.sendMessage(message);
        }
    }

    public void logEvent(String eventText) {
        Platform.runLater(() -> logArea.appendText(eventText + System.lineSeparator()));
    }

    private void cleanupServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            System.out.println("server: socket resources freed cleanly.");
            Platform.exit();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
