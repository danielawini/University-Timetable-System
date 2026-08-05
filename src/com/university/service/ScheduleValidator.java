package com.university.service;

import java.sql.*;

public class ScheduleValidator {
    private Connection connection;

    public ScheduleValidator(Connection connection) {
        this.connection = connection;
    }

    public String validateAssignment(int lecturerId, int courseId, String dayOfWeek, String timeSlot) throws SQLException {
        // 1. Rule 3: A lecturer cannot teach two different classes at the exact same time slot
        String timeQuery = "SELECT COUNT(*) FROM schedules WHERE lecturer_id = ? AND day_of_week = ? AND time_slot = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(timeQuery)) {
            pstmt.setInt(1, lecturerId);
            pstmt.setString(2, dayOfWeek);
            pstmt.setString(3, timeSlot);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "Rule Violation: This lecturer is already scheduled for another class at this exact day and time!";
            }
        }

        // Fetch target course details (Department and Level)
        String courseQuery = "SELECT department, level FROM courses WHERE course_id = ?";
        String targetDept = "";
        int targetLevel = 0;
        try (PreparedStatement pstmt = connection.prepareStatement(courseQuery)) {
            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                targetDept = rs.getString("department");
                targetLevel = rs.getInt("level");
            }
        }

        // 2. Rule 4: A lecturer cannot teach across two or more different departments
        String deptCheckQuery = "SELECT DISTINCT c.department FROM schedules s JOIN courses c ON s.course_id = c.course_id WHERE s.lecturer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deptCheckQuery)) {
            pstmt.setInt(1, lecturerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String assignedDept = rs.getString("department");
                if (assignedDept != null && !assignedDept.equalsIgnoreCase(targetDept)) {
                    return "Rule Violation: A lecturer cannot teach across multiple departments! This lecturer is already teaching in: " + assignedDept;
                }
            }
        }

        // 3. Rule 5: A lecturer can teach multiple levels, but ONLY within their department
        // (Since Rule 4 already guarantees they belong to the same department, multi-level teaching within that department is permitted).

        return null; // Null means all rules passed successfully!
    }
}



