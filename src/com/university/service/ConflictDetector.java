package com.university.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConflictDetector {
    private final Connection connection;

    public ConflictDetector(Connection connection) {
        this.connection = connection;
    }

    public List<String> detectConflicts() {
        List<String> conflicts = new ArrayList<>();

        // SQL query comparing schedules to find double-bookings for the same room or lecturer at overlapping times.
        // Updated to use s1.schedule_id and s2.schedule_id to match your database schema.
        String query = "SELECT " +
                "  s1.schedule_id AS id1, " +
                "  c1.course_code AS course1, " +
                "  l1.lecturer_name AS lecturer1, " +
                "  r1.room_name AS room1, " +
                "  s2.schedule_id AS id2, " +
                "  c2.course_code AS course2, " +
                "  l2.lecturer_name AS lecturer2, " +
                "  r2.room_name AS room2, " +
                "  s1.day_of_week, " +
                "  s1.time_slot " +
                "FROM schedules s1 " +
                "JOIN schedules s2 ON s1.day_of_week = s2.day_of_week " +
                "  AND s1.time_slot = s2.time_slot " +
                "  AND s1.schedule_id < s2.schedule_id " + // Prevents duplicate reverse pairs and self-matching
                "JOIN courses c1 ON s1.course_id = c1.course_id " +
                "JOIN courses c2 ON s2.course_id = c2.course_id " +
                "JOIN lecturers l1 ON s1.lecturer_id = l1.lecturer_id " +
                "JOIN lecturers l2 ON s2.lecturer_id = l2.lecturer_id " +
                "JOIN rooms r1 ON s1.room_id = r1.room_id " +
                "JOIN rooms r2 ON s2.room_id = r2.room_id " +
                "WHERE s1.room_id = s2.room_id OR s1.lecturer_id = s2.lecturer_id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String day = rs.getString("day_of_week");
                String time = rs.getString("time_slot");
                String course1 = rs.getString("course1");
                String course2 = rs.getString("course2");
                String room1 = rs.getString("room1");
                String lecturer1 = rs.getString("lecturer1");

                String conflictMessage;
                if (rs.getInt("room1") == rs.getInt("room2") && rs.getInt("lecturer1") == rs.getInt("lecturer2")) {
                    conflictMessage = String.format("Conflict on %s (%s): Room '%s' and Lecturer '%s' are double-booked for courses [%s] and [%s].",
                            day, time, room1, lecturer1, course1, course2);
                } else if (rs.getInt("room1") == rs.getInt("room2")) {
                    conflictMessage = String.format("Room Conflict on %s (%s): Room '%s' is assigned to both [%s] and [%s].",
                            day, time, room1, course1, course2);
                } else {
                    conflictMessage = String.format("Lecturer Conflict on %s (%s): Lecturer '%s' is scheduled to teach both [%s] and [%s].",
                            day, time, lecturer1, course1, course2);
                }

                conflicts.add(conflictMessage);
            }

        } catch (SQLException ex) {
            conflicts.add("Error executing conflict detection audit: " + ex.getMessage());
        }

        return conflicts;
    }
}