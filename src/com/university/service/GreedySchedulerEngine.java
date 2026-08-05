package com.university.service;

import com.university.dao.ScheduleDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GreedySchedulerEngine {

    private Connection connection;
    private ScheduleDAO scheduleDAO;

    public GreedySchedulerEngine(Connection connection) {
        this.connection = connection;
        this.scheduleDAO = new ScheduleDAO(connection);
    }

    public void generateTimetable() {
        System.out.println("Executing Greedy Algorithm to assign non-conflicting slots...");

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[] startTimes = {"08:00", "10:00", "13:00", "15:00"};
        String[] endTimes = {"10:00", "12:00", "15:00", "17:00"};

        try {
            String courseQuery = "SELECT course_id FROM courses";
            String roomQuery = "SELECT room_id FROM rooms";
            String lecturerQuery = "SELECT lecturer_id FROM lecturers";

            try (PreparedStatement psCourses = connection.prepareStatement(courseQuery);
                 ResultSet rsCourses = psCourses.executeQuery()) {

                while (rsCourses.next()) {
                    int courseId = rsCourses.getInt("course_id");
                    boolean scheduled = false;

                    // Iterate through available rooms
                    try (PreparedStatement psRooms = connection.prepareStatement(roomQuery);
                         ResultSet rsRooms = psRooms.executeQuery()) {

                        while (rsRooms.next() && !scheduled) {
                            int roomId = rsRooms.getInt("room_id");

                            // Iterate through available lecturers
                            try (PreparedStatement psLecturers = connection.prepareStatement(lecturerQuery);
                                 ResultSet rsLecturers = psLecturers.executeQuery()) {

                                while (rsLecturers.next() && !scheduled) {
                                    int lecturerId = rsLecturers.getInt("lecturer_id");

                                    // Iterate through time slots and days
                                    for (String day : days) {
                                        for (int i = 0; i < startTimes.length; i++) {
                                            String start = startTimes[i];
                                            String end = endTimes[i];

                                            if (scheduleDAO.isRoomAvailable(roomId, day, start, end) &&
                                                    scheduleDAO.isLecturerAvailable(lecturerId, day, start, end)) {

                                                scheduleDAO.addSchedule(courseId, lecturerId, roomId, day, start, end);
                                                System.out.println("Successfully scheduled Course ID: " + courseId +
                                                        " in Room ID: " + roomId + " on " + day + " (" + start + "-" + end + ")");

                                                scheduled = true;
                                                break;
                                            }
                                        }
                                        if (scheduled) break;
                                    }
                                }
                            }
                        }
                    }

                    if (!scheduled) {
                        System.out.println("Could not find a conflict-free slot for Course ID: " + courseId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error running scheduler algorithm: " + e.getMessage());
        }
    }
}