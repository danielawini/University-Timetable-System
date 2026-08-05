package com.university.dao;

import com.university.models.Room;
import com.university.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {
    private Connection connection;

    public RoomDAO() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public RoomDAO(Connection connection) {
        this.connection = connection;
    }

    public void addRoom(String roomName, int capacity) throws SQLException {
        ensureConnection();
        String query = "INSERT INTO rooms (room_name, capacity) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, roomName);
            stmt.setInt(2, capacity);
            stmt.executeUpdate();
        }
    }

    public int addRoomAndReturnId(Room room) throws SQLException {
        ensureConnection();
        String query = "INSERT INTO rooms (room_name, capacity) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, room.getRoomName());
            stmt.setInt(2, room.getCapacity());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<String> getAllRooms() throws SQLException {
        ensureConnection();
        List<String> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();
            String pkColumn = metaData.getColumnName(1);

            while (rs.next()) {
                String id = rs.getString(pkColumn);
                String name = getColumnValueSafely(rs, "room_name", "name", "title");
                String capacity = getColumnValueSafely(rs, "capacity", "cap", "size");

                rooms.add(id + ": " + name + " (Capacity: " + capacity + ")");
            }
        }
        return rooms;
    }

    public void deleteRoom(String roomId) throws SQLException {
        ensureConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            try {
                stmt.executeUpdate("DELETE FROM schedule WHERE room_id = '" + roomId + "'");
            } catch (SQLException ignored) {}

            String pkColumn = "id";
            try (ResultSet rs = stmt.executeQuery("SHOW KEYS FROM rooms WHERE Key_name = 'PRIMARY'")) {
                if (rs.next()) {
                    pkColumn = rs.getString("Column_name");
                }
            } catch (Exception ignored) {}

            stmt.executeUpdate("DELETE FROM rooms WHERE " + pkColumn + " = '" + roomId + "'");
            stmt.executeUpdate("ALTER TABLE rooms AUTO_INCREMENT = 1");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            try (Statement cleanup = connection.createStatement()) {
                cleanup.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            }
            throw e;
        }
    }

    public void deleteRoom(int roomId) throws SQLException {
        deleteRoom(String.valueOf(roomId));
    }

    private String getColumnValueSafely(ResultSet rs, String... possibleNames) {
        for (String name : possibleNames) {
            try {
                return rs.getString(name);
            } catch (SQLException ignored) {}
        }
        return "N/A";
    }

    private void ensureConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            this.connection = DatabaseConnection.getConnection();
        }
    }
}