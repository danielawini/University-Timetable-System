package com.university.models;

public class Course {
    // 1. Define your fields
    private int id;
    private String courseCode;
    private String courseName;
    private String department;
    private int enrolledStudents;

    // 2. The Constructor (matches your mainApp call: id, code, name, dept, enrolled)
    public Course(int id, String courseCode, String courseName, String department, int enrolledStudents) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
        this.enrolledStudents = enrolledStudents;
    }

    // 3. Getter Methods (These fix the "cannot find symbol" errors in CourseDAO)
    public int getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDepartment() {
        return department;
    }

    public int getEnrolledStudents() {
        return enrolledStudents;
    }
}
