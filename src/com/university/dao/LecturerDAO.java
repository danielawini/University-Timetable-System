package com.university.dao;

import com.university.models.Lecturer;
import com.university.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerDAO {
    private Connection connection;

    public LecturerDAO() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LecturerDAO(Connection connection) {
        this.connection = connection;
    }

    public void addLecturer(String lecturerName) throws SQLException {
        ensureConnection();
        try {
            String query = "INSERT INTO lecturers (lecturer_name) VALUES (?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, lecturerName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String fallbackQuery = "INSERT INTO lecturers (name) VALUES (?)";
            try (PreparedStatement stmt2 = connection.prepareStatement(fallbackQuery)) {
                stmt2.setString(1, lecturerName);
                stmt2.executeUpdate();
            }
        }
    }

    public void addLecturer(Lecturer lecturer) throws SQLException {
        ensureConnection();
        String name = (lecturer != null) ? lecturer.toString() : "";
        addLecturer(name);
    }

    public List<String> getAllLecturers() throws SQLException {
        ensureConnection();
        List<String> lecturers = new ArrayList<>();
        String query = "SELECT * FROM lecturers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            String pkColumn = metaData.getColumnName(1);

            while (rs.next()) {
                String id = rs.getString(pkColumn);
                String name = getColumnValueSafely(rs, "name", "lecturer_name", "fullname");
                lecturers.add(id + ": " + name);
            }
        }
        return lecturers;
    }

    public void deleteLecturer(String lecturerId) throws SQLException {
        ensureConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            try {
                stmt.executeUpdate("DELETE FROM schedule WHERE lecturer_id = '" + lecturerId + "'");
            } catch (SQLException ignored) {}

            String pkColumn = "id";
            try (ResultSet rs = stmt.executeQuery("SHOW KEYS FROM lecturers WHERE Key_name = 'PRIMARY'")) {
                if (rs.next()) {
                    pkColumn = rs.getString("Column_name");
                }
            } catch (Exception ignored) {}

            stmt.executeUpdate("DELETE FROM lecturers WHERE " + pkColumn + " = '" + lecturerId + "'");
            stmt.executeUpdate("ALTER TABLE lecturers AUTO_INCREMENT = 1");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            try (Statement cleanup = connection.createStatement()) {
                cleanup.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            }
            throw e;
        }
    }

    public void deleteLecturer(int lecturerId) throws SQLException {
        deleteLecturer(String.valueOf(lecturerId));
    }

    private String getColumnValueSafely(ResultSet rs, String... possibleNames) {
        for (String name : possibleNames) {
            try {
                return rs.getString(name);
            } catch (SQLException ignored) {}
        }
        return "Unknown";
    }

    private void ensureConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = DatabaseConnection.getConnection();
        }
    }
}