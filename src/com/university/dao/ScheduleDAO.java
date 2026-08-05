package com.university.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    private Connection connection;

    public ScheduleDAO(Connection connection) {
        this.connection = connection;
    }

    // Method to add a new schedule
    public void addSchedule(int courseId, int lecturerId, int roomId, String day, String start, String end) throws SQLException {
        String sql = "INSERT INTO schedules (course_id, lecturer_id, room_id, day_of_week, start_time, end_time, time_slot) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, lecturerId);
            pstmt.setInt(3, roomId);
            pstmt.setString(4, day);
            pstmt.setString(5, start);
            pstmt.setString(6, end);
            pstmt.setString(7, start + "-" + end);
            pstmt.executeUpdate();
        }
    }

    // Checks if a room is available at the given day and time slot
    public boolean isRoomAvailable(int roomId, String day, String start, String end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schedules WHERE room_id = ? AND day_of_week = ? " +
                "AND ((start_time < ? AND end_time > ?) OR (start_time >= ? AND start_time < ?))";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, roomId);
            pstmt.setString(2, day);
            pstmt.setString(3, end);
            pstmt.setString(4, start);
            pstmt.setString(5, start);
            pstmt.setString(6, end);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // True if 0 conflicting records found
                }
            }
        }
        return true; // Default to true if no conflict rows exist
    }

    // Checks if a lecturer is available at the given day and time slot
    public boolean isLecturerAvailable(int lecturerId, String day, String start, String end) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schedules WHERE lecturer_id = ? AND day_of_week = ? " +
                "AND ((start_time < ? AND end_time > ?) OR (start_time >= ? AND start_time < ?))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, lecturerId);
            stmt.setString(2, day);
            stmt.setString(3, end);
            stmt.setString(4, start);
            stmt.setString(5, start);
            stmt.setString(6, end);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // True if 0 conflicting records found
                }
            }
        }
        return true; // Default to true if no conflict rows exist
    }

    // Fetch all schedules
    public List<String> getAllSchedules() throws SQLException {
        List<String> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String entry = "Course ID: " + rs.getInt("course_id") +
                        " | Room: " + rs.getInt("room_id") +
                        " | Day: " + rs.getString("day_of_week") +
                        " | Time: " + rs.getString("start_time") + "-" + rs.getString("end_time");
                schedules.add(entry);
            }
        }
        return schedules;
    }
}