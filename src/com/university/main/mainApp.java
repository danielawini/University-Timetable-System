package com.university.main;
import com.university.dao.UserDAO;
import com.university.database.DatabaseConnection;
import java.sql.Connection;
import com.university.dao.UserDAO;
import java.util.Scanner;
import com.university.dao.RoomDAO;
import com.university.models.Room;
import com.university.dao.LecturerDAO;
import com.university.models.Lecturer;
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
        // Add this test block:
        RoomDAO roomDAO = new RoomDAO();
        Room newRoom = new Room(0, "Lecture Hall A", 50, "Projector, AC");

        try {
            roomDAO.addRoom(newRoom);
            System.out.println("Room successfully added to the database!");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }

        // Test LecturerDAO
        LecturerDAO lecturerDAO = new LecturerDAO();
// Create a new lecturer object to test the insertion
        Lecturer newLecturer = new Lecturer(0, "Dr. John Smith", "Computer Science");

        try {
            // Attempt to save the lecturer to the database
            lecturerDAO.addLecturer(newLecturer);
            System.out.println("Lecturer successfully added to the database!");

            // Verify by printing the count of all lecturers
            System.out.println("Total Lecturers in system: " + lecturerDAO.getAllLecturers().size());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

}