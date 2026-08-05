package com.university.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Audit_logDAO {
    private Connection connection;

    public Audit_logDAO(Connection connection) {
        this.connection = connection;
    }

    public void logAction(String username, String action, String details) throws SQLException {
        // Make sure these column names match your NEW table structure
        String sql = "INSERT INTO audit_logs (username, action, details, timestamp) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        }
    }
}
