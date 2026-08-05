package com.university.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    private Connection connection;

    public CourseDAO(Connection connection) {
        this.connection = connection;
    }

    public void addCourse(String courseCode, String courseName, String department, int level, int enrolledStudents) throws SQLException {
        String query = "INSERT INTO courses (course_code, course_name, department, level, enrolled_students) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, courseCode);
            pstmt.setString(2, courseName);
            pstmt.setString(3, department);
            pstmt.setInt(4, level);
            pstmt.setInt(5, enrolledStudents);
            pstmt.executeUpdate();
        }
    }

    public List<String> getAllCourses() throws SQLException {
        List<String> courses = new ArrayList<>();
        String query = "SELECT course_id, course_code, course_name, department, level, enrolled_students FROM courses";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("course_id");
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                String dept = rs.getString("department");
                int level = rs.getInt("level");
                int enrolled = rs.getInt("enrolled_students");

                courses.add(id + ": " + (code != null ? code : "") + " - " + (name != null ? name : "") +
                        " (" + (dept != null ? dept : "N/A") + " - L" + level + ", Enrolled: " + enrolled + ")");
            }
        }
        return courses;
    }

    public void deleteCourse(int courseId) throws SQLException {
        String query = "DELETE FROM courses WHERE course_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        }
    }
}