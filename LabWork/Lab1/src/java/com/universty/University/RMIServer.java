package com.universty.University;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RMIServer {
    private static final Logger LOGGER = Logger.getLogger(RMIServer.class.getName());

    // Database connection properties centralized on the server
    private static final String URL = "jdbc:mysql://localhost:3306/school?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "admin";
    private static final String PASSWORD = "1234";

    public static void main(String[] args) {
        try {
            //  Initialize Database and create missing tables automatically
            initializeDatabase();

            //  Start RMI Registry on default port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            //  Instantiate the unified Server implementation
            Server server = new ServerImpl();

            //  Bind the server instance to the registry
            registry.rebind("SchoolServer", server);

            System.out.println("\n RMI SCHOOL SERVER STARTED\n");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "CRITICAL: Failed to start RMI server", e);
        }
    }

    private static void initializeDatabase() {
        try {
            // Load MySQL JDBC Driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                //  creates students table if it doesn't already exist
                String createStudentTable = "CREATE TABLE IF NOT EXISTS students (" +
                        "id INT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "department VARCHAR(100), " +
                        "section VARCHAR(50), " +
                        "year INT)";
                stmt.execute(createStudentTable);

                // Establish Teachers Table
                String createTeacherTable = "CREATE TABLE IF NOT EXISTS teachers (" +
                        "id INT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "department VARCHAR(100))";
                stmt.execute(createTeacherTable);

                LOGGER.info("Database connection successful. ");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection failed!", e);
        }
    }
}