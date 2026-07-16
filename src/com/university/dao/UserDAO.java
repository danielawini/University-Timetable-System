package com.university.dao;

import com.university.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Method to verify login credentials
    public boolean validateUser(String username, String password) {
        String query = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    // Here you would use BCrypt.checkpw(password, storedHash)
                    return true; // Placeholder for logic
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
