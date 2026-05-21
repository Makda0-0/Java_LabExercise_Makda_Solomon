package com.universty.University;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Server extends Remote {
    // Student Operations
    String addStudent(String id, String name, String dept, String section, String year) throws RemoteException;
    String getAllStudents() throws RemoteException;
    String updateStudent(String id, String name, String dept, String section, String year) throws RemoteException;
    String deleteStudent(String id) throws RemoteException;

    // Teacher Operations
    String addTeacher(String id, String name, String dept) throws RemoteException;
    String getAllTeachers() throws RemoteException;
    String updateTeacher(String id, String name, String dept) throws RemoteException;
    String deleteTeacher(String id) throws RemoteException;

    // Database Operations
    String show() throws RemoteException;
    String save() throws RemoteException;
    String deleteAll() throws RemoteException;
}

class ServerImpl extends UnicastRemoteObject implements Server {
    private static final Logger LOGGER = Logger.getLogger(ServerImpl.class.getName());
    private static final String URL = "jdbc:mysql://localhost:3306/school?useSSL=false";
    private static final String USER = "admin";
    private static final String PASSWORD = "1234";
    private static final String BACKUP_DIR = "backups/";

    public ServerImpl() throws RemoteException {
        super();//parent unicast constructor
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // student

    @Override
    public String addStudent(String id, String name, String dept, String section, String year) throws RemoteException {
        if (id.isEmpty() || name.isEmpty()) return "Error: ID and Name required";

        String sql = "INSERT INTO students (id, name, department, section, year) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            ps.setString(2, name);
            ps.setString(3, dept);
            ps.setString(4, section);
            ps.setInt(5, Integer.parseInt(year));
            ps.executeUpdate();
            return "Student " + id + " added";
        } catch (SQLException e) {
            return e.getMessage().contains("Duplicate") ? "✗ ID " + id + " exists" : "✗ DB error";
        } catch (NumberFormatException e) {
            return "ID and Year must be numbers";
        }
    }

    @Override
    public String getAllStudents() throws RemoteException {
        StringBuilder sb = new StringBuilder("\n========== STUDENTS ==========\n");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY id")) {
            int count = 0;
            while (rs.next()) {
                count++;
                sb.append(String.format("ID:%-4s | %-20s | %-12s | Sec:%-4s | Year:%s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("department"),
                        rs.getString("section"), rs.getInt("year")));
            }
            sb.append(count == 0 ? "No students found\n" : "\nTotal: " + count + " students\n");
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage());
        }
        return sb.toString();
    }

    @Override
    public String updateStudent(String id, String name, String dept, String section, String year) throws RemoteException {
        if (id.isEmpty()) return "Error: ID required";
        String sql = "UPDATE students SET name=?, department=?, section=?, year=? WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, dept);
            ps.setString(3, section);
            ps.setInt(4, Integer.parseInt(year));
            ps.setInt(5, Integer.parseInt(id));
            return ps.executeUpdate() > 0 ? "Student " + id + " updated" : "✗ ID " + id + " not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String deleteStudent(String id) throws RemoteException {
        if (id.isEmpty()) return "Error: ID required";
        String sql = "DELETE FROM students WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            return ps.executeUpdate() > 0 ? "Student " + id + " deleted" : "✗ ID " + id + " not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // teacher

    @Override
    public String addTeacher(String id, String name, String dept) throws RemoteException {
        if (id.isEmpty() || name.isEmpty()) return "Error: ID and Name required";
        String sql = "INSERT INTO teachers (id, name, department) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            ps.setString(2, name);
            ps.setString(3, dept);
            ps.executeUpdate();
            return "Teacher " + id + " added";
        } catch (SQLException e) {
            return e.getMessage().contains("Duplicate") ? "✗ ID " + id + " exists" : "DB error";
        } catch (NumberFormatException e) {
            return "✗ ID must be a number";
        }
    }

    @Override
    public String getAllTeachers() throws RemoteException {
        StringBuilder sb = new StringBuilder("\n========== TEACHERS ==========\n");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM teachers ORDER BY id")) {
            int count = 0;
            while (rs.next()) {
                count++;
                sb.append(String.format("ID:%-4s | %-25s | Dept: %s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("department")));
            }
            sb.append(count == 0 ? "No teachers found\n" : "\nTotal: " + count + " teachers\n");
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage());
        }
        return sb.toString();
    }

    @Override
    public String updateTeacher(String id, String name, String dept) throws RemoteException {
        if (id.isEmpty()) return "Error: ID required";
        String sql = "UPDATE teachers SET name=?, department=? WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, dept);
            ps.setInt(3, Integer.parseInt(id));
            return ps.executeUpdate() > 0 ? "Teacher " + id + " updated" : "ID " + id + " not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String deleteTeacher(String id) throws RemoteException {
        if (id.isEmpty()) return "Error: ID required";
        String sql = "DELETE FROM teachers WHERE id=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            return ps.executeUpdate() > 0 ? "Teacher " + id + " deleted" : "ID " + id + " not found";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    //Database

    @Override
    public String show() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== COMPLETE DATABASE ==========\n\n");

        // Students
        sb.append("--- STUDENTS ---\n");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students ORDER BY id")) {
            int count = 0;
            while (rs.next()) {
                count++;
                sb.append(String.format("ID:%-4s | %-20s | %-12s | Sec:%-4s | Year:%s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("department"),
                        rs.getString("section"), rs.getInt("year")));
            }
            sb.append(count == 0 ? "No students\n" : "Total students: " + count + "\n\n");
        } catch (SQLException e) {
            sb.append("Error loading students\n");
        }

        // Teachers
        sb.append("--- TEACHERS ---\n");
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM teachers ORDER BY id")) {
            int count = 0;
            while (rs.next()) {
                count++;
                sb.append(String.format("ID:%-4s | %-25s | Dept: %s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("department")));
            }
            sb.append(count == 0 ? "No teachers\n" : "Total teachers: " + count + "\n");
        } catch (SQLException e) {
            sb.append("Error loading teachers\n");
        }
        return sb.toString();
    }
//backup
    @Override
    public String save() throws RemoteException {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String backupFile = BACKUP_DIR + "backup_" + timestamp + ".sql";

        new java.io.File(BACKUP_DIR).mkdirs();

        try (java.io.FileWriter fw = new java.io.FileWriter(backupFile)) {
            fw.write("-- Database Backup: " + new java.util.Date() + "\n\n");
            fw.write("SET FOREIGN_KEY_CHECKS = 0;\n\n");

            // Backup students
            fw.write("DROP TABLE IF EXISTS students;\n");
            fw.write("CREATE TABLE students (id INT PRIMARY KEY, name VARCHAR(100), department VARCHAR(100), section VARCHAR(50), year INT);\n");
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {
                while (rs.next()) {
                    fw.write(String.format("INSERT INTO students VALUES (%d, '%s', '%s', '%s', %d);\n",
                            rs.getInt("id"), escape(rs.getString("name")), escape(rs.getString("department")),
                            escape(rs.getString("section")), rs.getInt("year")));
                }
            }

            // Backup teachers
            fw.write("\nDROP TABLE IF EXISTS teachers;\n");
            fw.write("CREATE TABLE teachers (id INT PRIMARY KEY, name VARCHAR(100), department VARCHAR(100));\n");
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM teachers")) {
                while (rs.next()) {
                    fw.write(String.format("INSERT INTO teachers VALUES (%d, '%s', '%s');\n",
                            rs.getInt("id"), escape(rs.getString("name")), escape(rs.getString("department"))));
                }
            }
            fw.write("\nSET FOREIGN_KEY_CHECKS = 1;\n");

            return "Backup saved: " + backupFile + " (" + new java.io.File(backupFile).length() + " bytes)";
        } catch (Exception e) {
            return "Backup failed: " + e.getMessage();
        }
    }

    @Override
    public String deleteAll() throws RemoteException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            int students = stmt.executeUpdate("DELETE FROM students");
            int teachers = stmt.executeUpdate("DELETE FROM teachers");
            stmt.execute("ALTER TABLE students AUTO_INCREMENT = 1");
            stmt.execute("ALTER TABLE teachers AUTO_INCREMENT = 1");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            return String.format("Database cleared!\n  Students deleted: %d\n  Teachers deleted: %d\n  Total: %d records removed",
                    students, teachers, students + teachers);
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String escape(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("'", "\\'");
    }
}