package com.template.Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends Application {

    private final int port = 1234;
    private final String host = "localhost";
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private String currentUsername;

    private Stage primaryStage;
    private VBox messageContainer;
    private ScrollPane scrollPane;
    private TextField messageField;

    // Track if the client has finished loading its initial history dump
    private boolean historyLoaded = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("CHAT APP");
        showPublicJoinScreen();
    }


    private void showPublicJoinScreen() {
        VBox joinLayout = new VBox(15);
        joinLayout.setPadding(new Insets(30));
        joinLayout.setAlignment(Pos.CENTER);
        joinLayout.setStyle("-fx-background-color: #1e1e24;");

        Label headerLabel = new Label("PUBLIC CHAT");
        headerLabel.setStyle("-fx-text-fill: #ca00ff; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Enter a Display Name");
        usernameInput.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-prompt-text-fill: #777; -fx-background-radius: 8; -fx-padding: 10;");

        Button joinBtn = new Button("Join Chatroom");
        joinBtn.setMaxWidth(Double.MAX_VALUE);
        joinBtn.setStyle("-fx-background-color: #fc00ff; -fx-text-fill: #1e1e24; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ff3366;");

        joinLayout.getChildren().addAll(headerLabel, usernameInput, joinBtn, statusLabel);

        joinBtn.setOnAction(e -> {
            String user = usernameInput.getText().trim();
            if (!user.isEmpty()) {
                connectToPublicChat(user, statusLabel);
            } else {
                statusLabel.setText("Please enter a username.");
            }
        });

        Scene joinScene = new Scene(joinLayout, 360, 400);
        primaryStage.setScene(joinScene);
        primaryStage.show();
    }

    private void connectToPublicChat(String user, Label statusLabel) {
        try {
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());//read data from server
            out = new DataOutputStream(socket.getOutputStream());//send data to server

            // Send username to the server immediately
            out.writeUTF(user);
            out.flush();

            this.currentUsername = user;

            // Go straight to the chat room layout
            showChatScreen();
            startIncomingMessageListener();

        } catch (IOException e) {
            statusLabel.setText("Error: Server is offline.");
        }
    }

    private void showChatScreen() {
        BorderPane chatRoot = new BorderPane();
        chatRoot.setStyle("-fx-background-color: #121214;");

        HBox headerBar = new HBox();
        headerBar.setPadding(new Insets(15));
        headerBar.setStyle("-fx-background-color: #1e1e24; -fx-border-color: #f100ff; -fx-border-width: 0 0 1 0;");
        Label roomTitle = new Label("Chat Space - Active User: " + currentUsername);
        roomTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        headerBar.getChildren().add(roomTitle);
        chatRoot.setTop(headerBar);

        messageContainer = new VBox(12);
        messageContainer.setPadding(new Insets(15));
        messageContainer.setStyle("-fx-background-color: #121214;");

        scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #121214; -fx-background-color: #121214; -fx-border-color: transparent;");
        chatRoot.setCenter(scrollPane);

        HBox inputBar = new HBox(10);
        inputBar.setPadding(new Insets(15));
        inputBar.setStyle("-fx-background-color: #1e1e24;");

        messageField = new TextField();
        messageField.setPromptText("Type a message here...");
        HBox.setHgrow(messageField, Priority.ALWAYS);
        messageField.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-prompt-text-fill: #666; -fx-background-radius: 20; -fx-padding: 10 15;");

        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #cc00ffeb; -fx-text-fill: #1e1e24; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;");

        inputBar.getChildren().addAll(messageField, sendBtn);
        chatRoot.setBottom(inputBar);

        sendBtn.setOnAction(e -> dispatchMessage());
        messageField.setOnAction(e -> dispatchMessage());

        Scene chatScene = new Scene(chatRoot, 500, 650);
        primaryStage.setScene(chatScene);
        primaryStage.setOnCloseRequest(e -> cleanup());
    }

    private void appendBubbleMessage(String text, boolean isCurrentUser) {
        Platform.runLater(() -> {
            Label msgLabel = new Label(text);
            msgLabel.setWrapText(true);
            msgLabel.setMaxWidth(320);

            HBox bubbleWrapper = new HBox();
            if (isCurrentUser) {
                msgLabel.setStyle("-fx-background-color: #e100ff; -fx-text-fill: #1e1e24; -fx-padding: 10 14; -fx-background-radius: 16 16 2 16;");
                bubbleWrapper.setAlignment(Pos.CENTER_RIGHT);
            } else {
                msgLabel.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: #e2e2e2; -fx-padding: 10 14; -fx-background-radius: 16 16 16 2;");
                bubbleWrapper.setAlignment(Pos.CENTER_LEFT);
            }

            bubbleWrapper.getChildren().add(msgLabel);
            messageContainer.getChildren().add(bubbleWrapper);
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }

    private void dispatchMessage() {
        String plainText = messageField.getText().trim();
        if (!plainText.isEmpty()) {
            try {
                out.writeUTF(plainText);
                out.flush();
                // Displays locally as clean plain text inside your right bubble
                appendBubbleMessage(plainText, true);
                messageField.clear();
            } catch (IOException e) {
                appendBubbleMessage("Error: Message failed to send.", false);
            }
        }
    }

    private void startIncomingMessageListener() {
        new Thread(() -> {
            try {
                // handshake signal

                out.writeUTF("READY_FOR_HISTORY");// client is ready for history
                out.flush();


                while (true) {
                    String rawIncoming = in.readUTF();

                    // Check if the server sent the history completion token
                    if (rawIncoming.equals("HISTORY_END")) {
                        historyLoaded = true;
                        continue;
                    }

                    // Process history messages differently to keep layout consistent
                    if (!historyLoaded) {
                        boolean wasMe = rawIncoming.startsWith(currentUsername + ":");
                        if (wasMe) {
                            // Strip username prefix off your own old messages to keep it on the right side
                            String cleanText = rawIncoming.substring((currentUsername + ":").length()).trim();
                            appendBubbleMessage(cleanText, true);
                        } else {
                            appendBubbleMessage(rawIncoming, false);
                        }
                    } else {
                        // Regular live messaging filter logic
                        if (!rawIncoming.startsWith(currentUsername + ":")) {
                            appendBubbleMessage(rawIncoming, false);
                        }
                    }
                }
            } catch (IOException e) {
                appendBubbleMessage("Disconnected from chat server.", false);
            }
        }).start();
    }

    private void cleanup() {
        try {
            if (out != null) out.writeUTF("exit");
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
