package com.template;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Read the display name sent immediately by the public GUI client
            this.clientUsername = in.readUTF();

            System.out.println("SERVER LOG: " + clientUsername + " connected.");


            // Blocks here until the client background thread explicitly signals it is listening
            String handshake = in.readUTF();

            if (handshake.equals("READY_FOR_HISTORY")) {
                System.out.println("SERVER LOG: Sending history dump to " + clientUsername);
                java.util.List<String> history = Server.getRecentChatHistory();
                for (String oldMessage : history) {
                    sendMessage(oldMessage);
                }
                sendMessage("HISTORY_END"); // Alert client history download is complete
            }


            // Register this client after history is sent so new users see old chat first
            Server.activeClients.add(this);
            Server.broadcastMessage(clientUsername + " joined the room!");

            // Start the normal live chat loop
            while (true) {
                String clientMessage = in.readUTF();

                if (clientMessage.equalsIgnoreCase("exit")) {
                    System.out.println("SERVER LOG: " + clientUsername + " requested disconnect.");
                    break;
                }

                System.out.println("MESSAGE RECEIVED [" + clientUsername + "]: " + clientMessage);

                // Save and broadcast the message to everyone
                Server.broadcastChatMessage(clientUsername, clientMessage);

                // Check for bot response
                String botResponse = generateServerResponse(clientMessage);
                if (botResponse != null) {
                    Thread.sleep(1000);
                    Server.broadcastChatMessage("server", botResponse);
                }
            }

        } catch (IOException e) {
            System.err.println("Connection lost abruptly with " + clientUsername);
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
        } finally {
            // Clean up when client leaves
            Server.activeClients.remove(this);
            if (clientUsername != null) {
                Server.broadcastMessage("System: " + clientUsername + " left the room.");
            }

            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing streams: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to " + clientUsername + ": " + e.getMessage());
        }
    }
 //generated response from the server side
    private String generateServerResponse(String message) {
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello " + clientUsername + "! Welcome to the chat room.";
        } else if (lowerMessage.contains("help")) {
            return "I am an automated assistant. Type 'exit' to log off securely.";
        }
        return null;
    }
}
