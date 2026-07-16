package com.university.models;

public class Lecturer {private int lecturerId;
    private String lecturerName;
    private String department;

    public Lecturer() {}

    public Lecturer(int lecturerId, String lecturerName, String department) {
        this.lecturerId = lecturerId;
        this.lecturerName = lecturerName;
        this.department = department;
    }

    // Getters and Setters
    public int getLecturerId() { return lecturerId; }
    public void setLecturerId(int lecturerId) { this.lecturerId = lecturerId; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}

