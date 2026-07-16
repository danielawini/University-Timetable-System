package com.university.models;

public class User {private int userId;
    private String username;
    private String passwordHash;
    private String role; // 'Scheduler', 'Lecturer', or 'Student'

    public User() {}

    public User(int userId, String username, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    }
