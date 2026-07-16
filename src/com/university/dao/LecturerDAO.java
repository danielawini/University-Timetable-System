package com.university.dao;

import com.university.models.Lecturer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LecturerDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/UniversityTimetableDB";
    private static final String USER = "root"; // Update if different
    private static final String PASSWORD = "Daniel1441@"; // Use your working password

    public void addLecturer(Lecturer lecturer) throws SQLException {
        String sql = "INSERT INTO Lecturers (lecturer_name, department) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lecturer.getLecturerName());
            stmt.setString(2, lecturer.getDepartment());
            stmt.executeUpdate();
        }
    }

    public List<Lecturer> getAllLecturers() throws SQLException {
        List<Lecturer> lecturers = new ArrayList<>();
        String sql = "SELECT * FROM Lecturers";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lecturers.add(new Lecturer(rs.getInt("lecturer_id"),
                        rs.getString("lecturer_name"),
                        rs.getString("department")));
            }
        }
        return lecturers;
    }
}
