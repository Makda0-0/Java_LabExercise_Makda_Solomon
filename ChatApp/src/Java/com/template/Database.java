package com.template;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/chatapp";
    private static final String USER = "admin";
    private static final String PASS = "1234";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Database Driver not found! Make sure the MySQL Connector JAR is added.");
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }


    public static void saveMessage(String username, String message) {
        String sql = "INSERT INTO chat_history (username, message) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL Error saving message: " + e.getMessage());
        }
    }


    public static List<String> getChatHistory() {
        List<String> history = new ArrayList<>();
        // Select the last 30 messages, ordered oldest to newest
        String sql = "SELECT username, message FROM (SELECT * FROM chat_history ORDER BY id DESC LIMIT 30) AS sub ORDER BY id ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String user = normalizeUsername(rs.getString("username"));
                String msg = rs.getString("message");
                history.add(user + ": " + msg);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error fetching history: " + e.getMessage());
        }
        return history;
    }

    private static String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "unknown";
        }

        if (username.equals("[SERVER]") || username.equalsIgnoreCase("SERVER_BOT")) {
            return "server";
        }

        return username;
    }
}
