package com.university.dao;

import com.university.models.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO { // Ensure this opening brace exists

    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/UniversityTimetableDB";
    private static final String USER = "root";
    private static final String PASSWORD = "Daniel1441@";

    public void addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO Rooms (room_name, capacity, facilities) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomName());
            stmt.setInt(2, room.getCapacity());
            stmt.setString(3, room.getFacilities());
            stmt.executeUpdate();
        }
    }
    // Ensure every method is closed with a closing brace '}'

    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM Rooms";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(new Room(rs.getInt("room_id"), rs.getString("room_name"),
                        rs.getInt("capacity"), rs.getString("facilities")));
            }
        }
        return rooms;
    }
} // <--- CRITICAL: Make sure this final closing brace is present for the class