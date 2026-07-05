package com.university.main;
import com.university.dao.UserDAO;
import com.university.database.DatabaseConnection;
import java.sql.Connection;
import com.university.dao.UserDAO;
import java.util.Scanner;
import java.sql.SQLException;

public class mainApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();

        System.out.print("Enter Username: ");
        String inputUser = scanner.nextLine();

        System.out.print("Enter Password: ");
        String inputPass = scanner.nextLine();

        // Pass the inputs to your DAO
        boolean result = userDAO.validateUser(inputUser, inputPass);

        if (result) {
            System.out.println("Login Successful! Welcome, " + inputUser);
        } else {
            System.out.println("Login Failed. Invalid username or password.");
        }

        scanner.close();
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