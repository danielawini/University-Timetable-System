package com.university.main;
import com.university.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class mainApp {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("Connection successful!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
    }
}
