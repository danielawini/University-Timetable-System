package com.university.models;

public class Schedule {private int scheduleId;
    private Course course;
    private Lecturer lecturer;
    private Room room;
    private String timeSlot;
    private String dayOfWeek;

    public Schedule() {}

    public Schedule(int scheduleId, Course course, Lecturer lecturer, Room room, String timeSlot, String dayOfWeek) {
        this.scheduleId = scheduleId;
        this.course = course;
        this.lecturer = lecturer;
        this.room = room;
        this.timeSlot = timeSlot;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Lecturer getLecturer() { return lecturer; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
}
