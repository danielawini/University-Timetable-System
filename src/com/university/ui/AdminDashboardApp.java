package com.university.ui;

import com.university.dao.CourseDAO;
import com.university.dao.LecturerDAO;
import com.university.dao.RoomDAO;
import com.university.database.DatabaseConnection;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.List;

public class AdminDashboardApp extends Application {

    private ComboBox<String> sharedCourseCb;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Timetable System - Admin Dashboard");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f4f6f9;");

        // Top Navigation Bar
        HBox topNavBar = new HBox(15);
        topNavBar.setAlignment(Pos.CENTER_LEFT);
        topNavBar.setPadding(new Insets(15));
        topNavBar.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 2);");

        Button backBtn = new Button("⬅ Back to Login");
        styleRedButton(backBtn);
        backBtn.setOnAction(e -> {
            primaryStage.close();
            Stage loginStage = new Stage();
            new LoginApp().start(loginStage);
        });

        VBox titleTitles = new VBox(3);
        Label titleLabel = new Label("Administrator Control Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label subtitleLabel = new Label("Manage courses, rooms, lecturers, manual assignments, and system reports.");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        titleTitles.getChildren().addAll(titleLabel, subtitleLabel);

        topNavBar.getChildren().addAll(backBtn, titleTitles);

        // Tab Pane Container
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab courseTab = new Tab("Courses", createCourseManagementView());
        Tab roomTab = new Tab("Rooms", createRoomManagementView());
        Tab lecturerTab = new Tab("Lecturers & Contacts", createLecturerManagementView());
        Tab assignTab = new Tab("Manual Assignment", createManualAssignmentView());
        Tab timetableTab = new Tab("Timetable Viewer", createTimetableManagementView());
        Tab reportTab = new Tab("Reports & Export", createReportsView());

        tabPane.getTabs().addAll(courseTab, roomTab, lecturerTab, assignTab, timetableTab, reportTab);

        mainLayout.getChildren().addAll(topNavBar, tabPane);

        Scene scene = new Scene(mainLayout, 950, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createCourseManagementView() {
        VBox box = createCardLayout();
        Label label = new Label("Course Management Center");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        TextField courseCodeField = new TextField();
        courseCodeField.setPromptText("Enter Course Code (e.g., ICT401)");
        courseCodeField.setMaxWidth(400);

        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Enter Course Name (e.g., Advanced Networking)");
        courseNameField.setMaxWidth(400);

        TextField departmentField = new TextField();
        departmentField.setPromptText("Enter Department (e.g., ICT)");
        departmentField.setMaxWidth(400);

        TextField levelField = new TextField();
        levelField.setPromptText("Enter Year Level (e.g., 400)");
        levelField.setMaxWidth(400);

        TextField enrolledField = new TextField();
        enrolledField.setPromptText("Enter Enrolled Students (e.g., 60)");
        enrolledField.setMaxWidth(400);

        Button addBtn = new Button("Add Course");
        Button deleteBtn = new Button("Delete Course");
        Button refreshBtn = new Button("Refresh Courses");

        styleGreenButton(addBtn);
        styleRedButton(deleteBtn);
        styleBlueButton(refreshBtn);

        HBox btnBox = new HBox(10, addBtn, deleteBtn, refreshBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(150);

        loadCoursesIntoView(listView);

        addBtn.setOnAction(e -> {
            String courseCode = courseCodeField.getText().trim();
            String courseName = courseNameField.getText().trim();
            String department = departmentField.getText().trim();
            String levelText = levelField.getText().trim();
            String enrolledText = enrolledField.getText().trim();

            if (courseCode.isEmpty() || courseName.isEmpty() || department.isEmpty() || levelText.isEmpty() || enrolledText.isEmpty()) {
                showAlert("Input Error", "Please fill in all course fields.");
                return;
            }
            try {
                int level = Integer.parseInt(levelText);
                int enrolledStudents = Integer.parseInt(enrolledText);

                try (Connection conn = DatabaseConnection.getConnection()) {
                    CourseDAO dao = new CourseDAO(conn);
                    dao.addCourse(courseCode, courseName, department, level, enrolledStudents);
                    showAlert("Success", "Course added successfully! It is now available in the Manual Assignment tab.");

                    courseCodeField.clear();
                    courseNameField.clear();
                    departmentField.clear();
                    levelField.clear();
                    enrolledField.clear();
                    loadCoursesIntoView(listView);

                    if (sharedCourseCb != null) {
                        sharedCourseCb.setItems(FXCollections.observableArrayList(dao.getAllCourses()));
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Level and Enrolled Students must be valid numbers.");
            } catch (SQLException ex) {
                showAlert("Database Error", ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Error", "Please select a course from the list to remove.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Remove Course");
            alert.setContentText("Are you sure you want to remove this course?\n\n" + selected);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        int id = Integer.parseInt(selected.split(":")[0].trim());
                        try (Connection conn = DatabaseConnection.getConnection()) {
                            CourseDAO dao = new CourseDAO(conn);
                            dao.deleteCourse(id);
                            showAlert("Success", "Course removed successfully!");
                            loadCoursesIntoView(listView);
                            listView.getSelectionModel().clearSelection();

                            if (sharedCourseCb != null) {
                                sharedCourseCb.setItems(FXCollections.observableArrayList(dao.getAllCourses()));
                            }
                        }
                    } catch (Exception ex) {
                        showAlert("Error", "Could not delete course: " + ex.getMessage());
                    }
                }
            });
        });

        refreshBtn.setOnAction(e -> {
            loadCoursesIntoView(listView);
            listView.getSelectionModel().clearSelection();
        });

        box.getChildren().addAll(
                label, new Separator(),
                courseCodeField, courseNameField, departmentField, levelField, enrolledField,
                btnBox,
                new Label("Existing System Courses:"), listView
        );
        return box;
    }

    private void loadCoursesIntoView(ListView<String> listView) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            CourseDAO dao = new CourseDAO(conn);
            List<String> list = dao.getAllCourses();
            listView.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException ex) {
            showAlert("Database Error", "Could not load courses: " + ex.getMessage());
        }
    }

    private VBox createRoomManagementView() {
        VBox box = createCardLayout();
        Label label = new Label("Lecture Room & Facility Management");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        TextField roomNameField = new TextField();
        roomNameField.setPromptText("Enter Room Name (e.g., Hall A)");
        roomNameField.setMaxWidth(400);

        TextField capacityField = new TextField();
        capacityField.setPromptText("Enter Room Capacity (e.g., 100)");
        capacityField.setMaxWidth(400);

        Button addBtn = new Button("Register Room");
        Button deleteBtn = new Button("Remove Room");
        Button refreshBtn = new Button("Refresh Rooms");

        styleGreenButton(addBtn);
        styleRedButton(deleteBtn);
        styleBlueButton(refreshBtn);

        HBox btnBox = new HBox(10, addBtn, deleteBtn, refreshBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(180);

        loadRoomsIntoView(listView);

        addBtn.setOnAction(e -> {
            String roomName = roomNameField.getText().trim();
            String capText = capacityField.getText().trim();

            if (roomName.isEmpty() || capText.isEmpty()) {
                showAlert("Input Error", "Please enter both room name and capacity.");
                return;
            }

            try {
                int capacity = Integer.parseInt(capText);
                try (Connection conn = DatabaseConnection.getConnection()) {
                    RoomDAO dao = new RoomDAO(conn);
                    dao.addRoom(roomName, capacity);
                    showAlert("Success", "Room registered successfully!");
                    roomNameField.clear();
                    capacityField.clear();
                    loadRoomsIntoView(listView);
                }
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Capacity must be a valid number.");
            } catch (SQLException ex) {
                showAlert("Database Error", ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Error", "Please select a room from the list to remove.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Remove Room");
            alert.setContentText("Are you sure you want to remove this room?\n\n" + selected);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        int id = Integer.parseInt(selected.split(":")[0].trim());
                        try (Connection conn = DatabaseConnection.getConnection()) {
                            RoomDAO dao = new RoomDAO(conn);
                            dao.deleteRoom(id);
                            showAlert("Success", "Room removed successfully!");
                            roomNameField.clear();
                            capacityField.clear();
                            loadRoomsIntoView(listView);
                            listView.getSelectionModel().clearSelection();
                        }
                    } catch (Exception ex) {
                        showAlert("Error", "Could not delete room: " + ex.getMessage());
                    }
                }
            });
        });

        refreshBtn.setOnAction(e -> {
            loadRoomsIntoView(listView);
            listView.getSelectionModel().clearSelection();
        });

        box.getChildren().addAll(label, new Separator(), roomNameField, capacityField, btnBox, new Label("Registered Rooms Directory:"), listView);
        return box;
    }

    private void loadRoomsIntoView(ListView<String> listView) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            RoomDAO dao = new RoomDAO(conn);
            List<String> list = dao.getAllRooms();
            listView.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException ex) {
            showAlert("Database Error", "Could not load rooms: " + ex.getMessage());
        }
    }

    private VBox createLecturerManagementView() {
        VBox box = createCardLayout();
        Label label = new Label("Lecturer Directory, Contacts & Email Management");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        TableView<LecturerModel> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(200);

        TableColumn<LecturerModel, String> nameCol = new TableColumn<>("Lecturer Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<LecturerModel, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> data.getValue().departmentProperty());

        TableColumn<LecturerModel, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<LecturerModel, String> phoneCol = new TableColumn<>("Phone Contact");
        phoneCol.setCellValueFactory(data -> data.getValue().phoneProperty());

        table.getColumns().addAll(nameCol, deptCol, emailCol, phoneCol);

        ObservableList<LecturerModel> lecturerList = FXCollections.observableArrayList();
        loadLecturersFromDB(lecturerList);
        table.setItems(lecturerList);

        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(10, 0, 0, 0));

        TextField nameField = new TextField();
        nameField.setPromptText("Enter Lecturer Full Name");
        nameField.setMaxWidth(400);

        TextField deptField = new TextField();
        deptField.setPromptText("Enter Department (e.g., ICT)");
        deptField.setMaxWidth(400);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email Address (e.g., lecturer@uew.edu.gh)");
        emailField.setMaxWidth(400);

        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter Phone Number (e.g., +233 24 000 0000)");
        phoneField.setMaxWidth(400);

        Button addUpdateBtn = new Button("Add / Update Lecturer Contact");
        Button deleteBtn = new Button("Remove Lecturer");
        Button refreshBtn = new Button("Refresh Directory");

        styleGreenButton(addUpdateBtn);
        styleRedButton(deleteBtn);
        styleBlueButton(refreshBtn);

        HBox btnBox = new HBox(10, addUpdateBtn, deleteBtn, refreshBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        // Populate form fields when a table row is clicked
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                deptField.setText(newSelection.getDepartment());
                emailField.setText(newSelection.getEmail().equals("Not Provided") ? "" : newSelection.getEmail());
                phoneField.setText(newSelection.getPhone().equals("Not Provided") ? "" : newSelection.getPhone());
            }
        });

        addUpdateBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty()) {
                showAlert("Input Error", "Please enter the lecturer's name.");
                return;
            }

            saveOrUpdateLecturer(name, dept, email, phone);
            loadLecturersFromDB(lecturerList);
            nameField.clear();
            deptField.clear();
            emailField.clear();
            phoneField.clear();
            table.getSelectionModel().clearSelection();
            showAlert("Success", "Lecturer contact details saved/updated successfully!");
        });

        deleteBtn.setOnAction(e -> {
            LecturerModel selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Error", "Please select a lecturer from the table to remove.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Remove Lecturer");
            alert.setContentText("Are you sure you want to remove this lecturer and their contact info?\n\n" + selected.getName());

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM lecturers WHERE lecturer_name = ?")) {
                        pstmt.setString(1, selected.getName());
                        pstmt.executeUpdate();
                        showAlert("Success", "Lecturer removed successfully!");
                        loadLecturersFromDB(lecturerList);
                        nameField.clear();
                        deptField.clear();
                        emailField.clear();
                        phoneField.clear();
                        table.getSelectionModel().clearSelection();
                    } catch (SQLException ex) {
                        showAlert("Database Error", "Could not delete lecturer: " + ex.getMessage());
                    }
                }
            });
        });

        refreshBtn.setOnAction(e -> {
            loadLecturersFromDB(lecturerList);
            nameField.clear();
            deptField.clear();
            emailField.clear();
            phoneField.clear();
            table.getSelectionModel().clearSelection();
        });

        formBox.getChildren().addAll(
                new Label("Lecturer Details:"),
                nameField, deptField, emailField, phoneField,
                btnBox
        );

        box.getChildren().addAll(label, new Separator(), table, formBox);
        return box;
    }

    private void loadLecturersFromDB(ObservableList<LecturerModel> list) {
        list.clear();
        String query = "SELECT lecturer_name, department, email, phone FROM lecturers";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new LecturerModel(
                        rs.getString("lecturer_name"),
                        rs.getString("department"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveOrUpdateLecturer(String name, String dept, String email, String phone) {
        String checkQuery = "SELECT COUNT(*) FROM lecturers WHERE lecturer_name = ?";
        String updateQuery = "UPDATE lecturers SET department = ?, email = ?, phone = ? WHERE lecturer_name = ?";
        String insertQuery = "INSERT INTO lecturers (lecturer_name, department, email, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean exists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, name);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    exists = rs.getInt(1) > 0;
                }
            }

            if (exists) {
                try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                    ps.setString(1, dept.isEmpty() ? null : dept);
                    ps.setString(2, email.isEmpty() ? null : email);
                    ps.setString(3, phone.isEmpty() ? null : phone);
                    ps.setString(4, name);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                    ps.setString(1, name);
                    ps.setString(2, dept.isEmpty() ? null : dept);
                    ps.setString(3, email.isEmpty() ? null : email);
                    ps.setString(4, phone.isEmpty() ? null : phone);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createManualAssignmentView() {
        VBox box = createCardLayout();
        Label label = new Label("Manual Timetable Assignment Portal");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        ComboBox<String> courseCb = new ComboBox<>();
        courseCb.setPromptText("Select Course");
        courseCb.setMaxWidth(400);
        this.sharedCourseCb = courseCb;

        ComboBox<String> lecturerCb = new ComboBox<>();
        lecturerCb.setPromptText("Select Lecturer");
        lecturerCb.setMaxWidth(400);

        ComboBox<String> roomCb = new ComboBox<>();
        roomCb.setPromptText("Select Room");
        roomCb.setMaxWidth(400);

        ComboBox<String> dayCb = new ComboBox<>();
        dayCb.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        dayCb.setPromptText("Select Day of Week");
        dayCb.setMaxWidth(400);

        TextField timeField = new TextField();
        timeField.setPromptText("Enter Time Slot (e.g., 08:00 AM - 10:00 AM)");
        timeField.setMaxWidth(400);

        TextField dateField = new TextField();
        dateField.setPromptText("Enter Date (e.g., 2026-06-15)");
        dateField.setMaxWidth(400);

        Button assignBtn = new Button("Assign & Save to Timetable");
        Button refreshDropdownsBtn = new Button("🔄 Refresh Lists");

        styleGreenButton(assignBtn);
        styleBlueButton(refreshDropdownsBtn);

        HBox btnBox = new HBox(10, assignBtn, refreshDropdownsBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        Runnable loadDropdownData = () -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                CourseDAO cDao = new CourseDAO(conn);
                courseCb.setItems(FXCollections.observableArrayList(cDao.getAllCourses()));

                LecturerDAO lDao = new LecturerDAO(conn);
                lecturerCb.setItems(FXCollections.observableArrayList(lDao.getAllLecturers()));

                RoomDAO rDao = new RoomDAO(conn);
                roomCb.setItems(FXCollections.observableArrayList(rDao.getAllRooms()));
            } catch (SQLException ex) {
                showAlert("Database Error", "Failed to load dropdown records: " + ex.getMessage());
            }
        };

        loadDropdownData.run();
        refreshDropdownsBtn.setOnAction(e -> loadDropdownData.run());

        assignBtn.setOnAction(e -> {
            String selCourse = courseCb.getValue();
            String selLecturer = lecturerCb.getValue();
            String selRoom = roomCb.getValue();
            String selDay = dayCb.getValue();
            String timeText = timeField.getText().trim();
            String dateText = dateField.getText().trim();

            if (selCourse == null || selLecturer == null || selRoom == null || selDay == null || timeText.isEmpty()) {
                showAlert("Input Error", "Please select a course, lecturer, room, day, and enter a time slot.");
                return;
            }

            try {
                int courseId = Integer.parseInt(selCourse.split(":")[0].trim());
                int lecturerId = Integer.parseInt(selLecturer.split(":")[0].trim());
                int roomId = Integer.parseInt(selRoom.split(":")[0].trim());

                try (Connection conn = DatabaseConnection.getConnection()) {

                    // 1. Check Room Conflict
                    String roomCheckQuery = "SELECT COUNT(*) FROM schedules WHERE room_id = ? AND day_of_week = ? AND time_slot = ?";
                    try (PreparedStatement roomStmt = conn.prepareStatement(roomCheckQuery)) {
                        roomStmt.setInt(1, roomId);
                        roomStmt.setString(2, selDay);
                        roomStmt.setString(3, timeText);
                        try (ResultSet rs = roomStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                showAlert("Scheduling Conflict", "❌ Room Conflict: " + selRoom + " is already booked for another lecture on " + selDay + " at " + timeText + ".");
                                return;
                            }
                        }
                    }

                    // 2. Check Lecturer Conflict
                    String lecturerCheckQuery = "SELECT COUNT(*) FROM schedules WHERE lecturer_id = ? AND day_of_week = ? AND time_slot = ?";
                    try (PreparedStatement lecStmt = conn.prepareStatement(lecturerCheckQuery)) {
                        lecStmt.setInt(1, lecturerId);
                        lecStmt.setString(2, selDay);
                        lecStmt.setString(3, timeText);
                        try (ResultSet rs = lecStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                showAlert("Scheduling Conflict", "❌ Lecturer Conflict: " + selLecturer + " is already assigned to teach another class on " + selDay + " at " + timeText + ".");
                                return;
                            }
                        }
                    }

                    // 3. Confirm and Save
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirm Assignment");
                    confirmAlert.setHeaderText("Save Manual Schedule Entry");
                    confirmAlert.setContentText("Are you sure you want to assign this schedule?\n\nCourse: " + selCourse + "\nRoom: " + selRoom + "\nDay: " + selDay + " (" + timeText + ")");

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                String insertQuery = "INSERT INTO schedules (course_id, lecturer_id, room_id, day_of_week, time_slot, schedule_date) VALUES (?, ?, ?, ?, ?, ?)";
                                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                                    pstmt.setInt(1, courseId);
                                    pstmt.setInt(2, lecturerId);
                                    pstmt.setInt(3, roomId);
                                    pstmt.setString(4, selDay);
                                    pstmt.setString(5, timeText);
                                    pstmt.setString(6, dateText.isEmpty() ? null : dateText);
                                    pstmt.executeUpdate();

                                    showAlert("Success", "Course, Lecturer, Room, and Time assigned successfully!");
                                    timeField.clear();
                                    dateField.clear();
                                }
                            } catch (Exception ex) {
                                showAlert("Assignment Error", "Could not save manual assignment: " + ex.getMessage());
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                showAlert("Input Error", "Invalid ID selection format: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(
                label, new Separator(),
                new Label("Course:"), courseCb,
                new Label("Lecturer:"), lecturerCb,
                new Label("Room:"), roomCb,
                new Label("Day of Week:"), dayCb,
                new Label("Time Slot:"), timeField,
                new Label("Date (Optional):"), dateField,
                btnBox
        );
        return box;
    }

    private VBox createTimetableManagementView() {
        VBox box = createCardLayout();
        Label label = new Label("Department & Level Timetable Viewer");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> deptFilterCb = new ComboBox<>();
        deptFilterCb.setPromptText("Filter Department");
        deptFilterCb.getItems().add("All Departments");
        deptFilterCb.setValue("All Departments");
        deptFilterCb.setPrefWidth(160);

        ComboBox<String> levelFilterCb = new ComboBox<>();
        levelFilterCb.setPromptText("Filter Level");
        levelFilterCb.getItems().addAll("All Levels", "100", "200", "300", "400");
        levelFilterCb.setValue("All Levels");
        levelFilterCb.setPrefWidth(130);

        TextField searchCourseField = new TextField();
        searchCourseField.setPromptText("🔍 Search Course Code...");
        searchCourseField.setPrefWidth(200);

        filterBox.getChildren().addAll(deptFilterCb, levelFilterCb, searchCourseField);

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT department FROM courses WHERE department IS NOT NULL")) {
            while (rs.next()) {
                deptFilterCb.getItems().add(rs.getString("department"));
            }
        } catch (SQLException ignored) {}

        Button refreshBtn = new Button("🔄 Refresh");
        Button deleteSelectedBtn = new Button("🗑️ Delete Selected");
        Button clearTableBtn = new Button("🗑️ Clear All");

        styleBlueButton(refreshBtn);
        styleRedButton(deleteSelectedBtn);
        styleRedButton(clearTableBtn);

        HBox actionBtns = new HBox(10, refreshBtn, deleteSelectedBtn, clearTableBtn);
        actionBtns.setAlignment(Pos.CENTER_LEFT);

        TableView<ScheduleRecord> table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<ScheduleRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());
        idCol.setPrefWidth(45);

        TableColumn<ScheduleRecord, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> data.getValue().departmentProperty());
        deptCol.setPrefWidth(100);

        TableColumn<ScheduleRecord, String> levelCol = new TableColumn<>("Level");
        levelCol.setCellValueFactory(data -> data.getValue().levelProperty());
        levelCol.setPrefWidth(60);

        TableColumn<ScheduleRecord, String> courseCol = new TableColumn<>("Course Code");
        courseCol.setCellValueFactory(data -> data.getValue().courseCodeProperty());
        courseCol.setPrefWidth(110);

        TableColumn<ScheduleRecord, String> lecturerCol = new TableColumn<>("Lecturer");
        lecturerCol.setCellValueFactory(data -> data.getValue().lecturerProperty());
        lecturerCol.setPrefWidth(140);

        TableColumn<ScheduleRecord, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data -> data.getValue().roomProperty());
        roomCol.setPrefWidth(90);

        TableColumn<ScheduleRecord, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(data -> data.getValue().dayProperty());
        dayCol.setPrefWidth(95);

        TableColumn<ScheduleRecord, String> timeCol = new TableColumn<>("Time Slot");
        timeCol.setCellValueFactory(data -> data.getValue().timeProperty());
        timeCol.setPrefWidth(130);

        table.getColumns().addAll(idCol, deptCol, levelCol, courseCol, lecturerCol, roomCol, dayCol, timeCol);

        ObservableList<ScheduleRecord> masterData = FXCollections.observableArrayList();

        Runnable loadTimetable = () -> {
            masterData.clear();
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT s.schedule_id AS id, c.department, c.level, c.course_code, l.lecturer_name, r.room_name, s.day_of_week, s.time_slot, s.schedule_date " +
                                 "FROM schedules s " +
                                 "JOIN courses c ON s.course_id = c.course_id " +
                                 "JOIN lecturers l ON s.lecturer_id = l.lecturer_id " +
                                 "JOIN rooms r ON s.room_id = r.room_id")) {

                while (rs.next()) {
                    masterData.add(new ScheduleRecord(
                            String.valueOf(rs.getInt("id")),
                            rs.getString("department") != null ? rs.getString("department") : "N/A",
                            String.valueOf(rs.getInt("level")),
                            rs.getString("course_code"),
                            rs.getString("lecturer_name"),
                            rs.getString("room_name"),
                            rs.getString("day_of_week"),
                            rs.getString("time_slot"),
                            rs.getString("schedule_date") != null ? rs.getString("schedule_date") : "N/A"
                    ));
                }
            } catch (SQLException ex) {
                showAlert("Database Error", "Could not load timetable data: " + ex.getMessage());
            }
        };

        FilteredList<ScheduleRecord> filteredData = new FilteredList<>(masterData, p -> true);

        Runnable updateFilter = () -> {
            String selectedDept = deptFilterCb.getValue();
            String selectedLevel = levelFilterCb.getValue();
            String searchKeyword = searchCourseField.getText().toLowerCase().trim();

            filteredData.setPredicate(record -> {
                boolean matchesDept = (selectedDept == null || selectedDept.equals("All Departments") || record.getDepartment().equalsIgnoreCase(selectedDept));
                boolean matchesLevel = (selectedLevel == null || selectedLevel.equals("All Levels") || record.getLevel().equals(selectedLevel));
                boolean matchesSearch = (searchKeyword.isEmpty() || record.getCourseCode().toLowerCase().contains(searchKeyword));

                return matchesDept && matchesLevel && matchesSearch;
            });
        };

        deptFilterCb.setOnAction(e -> updateFilter.run());
        levelFilterCb.setOnAction(e -> updateFilter.run());
        searchCourseField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter.run());

        table.setItems(filteredData);
        loadTimetable.run();

        refreshBtn.setOnAction(e -> {
            loadTimetable.run();
            updateFilter.run();
        });

        deleteSelectedBtn.setOnAction(e -> {
            ScheduleRecord selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Selection Error", "Please select a schedule entry from the table to delete.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Schedule Entry");
            alert.setContentText("Are you sure you want to remove this timetable entry?\n\nCourse Code: " + selected.getCourseCode() + " (" + selected.getDay() + " " + selected.getTime() + ")");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM schedules WHERE schedule_id = ?")) {
                        pstmt.setInt(1, Integer.parseInt(selected.getId()));
                        pstmt.executeUpdate();
                        showAlert("Success", "Schedule entry deleted successfully!");
                        loadTimetable.run();
                        updateFilter.run();
                    } catch (SQLException ex) {
                        showAlert("Database Error", "Could not delete schedule entry: " + ex.getMessage());
                    }
                }
            });
        });

        clearTableBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Clear All");
            alert.setHeaderText("Clear Entire Timetable");
            alert.setContentText("Are you sure you want to remove all timetable schedule records from the system?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = DatabaseConnection.getConnection();
                         Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM schedules");
                        showAlert("Success", "All timetable records have been cleared!");
                        loadTimetable.run();
                        updateFilter.run();
                    } catch (SQLException ex) {
                        showAlert("Database Error", "Could not clear timetable: " + ex.getMessage());
                    }
                }
            });
        });

        box.getChildren().addAll(label, new Separator(), filterBox, table, actionBtns);
        return box;
    }

    private VBox createReportsView() {
        VBox box = createCardLayout();
        Label label = new Label("System Reports & Summary Center");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        TextArea reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setPrefHeight(250);
        reportArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        Button generateReportBtn = new Button("📊 Generate Full System Summary Report");
        styleBlueButton(generateReportBtn);

        generateReportBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("==================================================\n");
            sb.append("       UNIVERSITY TIMETABLE SYSTEM REPORT       \n");
            sb.append("==================================================\n\n");

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM courses")) {
                    if (rs.next()) sb.append("• Total Registered Courses: ").append(rs.getInt(1)).append("\n");
                }

                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms")) {
                    if (rs.next()) sb.append("• Total Registered Rooms: ").append(rs.getInt(1)).append("\n");
                }

                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lecturers")) {
                    if (rs.next()) sb.append("• Total Active Lecturers: ").append(rs.getInt(1)).append("\n");
                }

                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM schedules")) {
                    if (rs.next()) sb.append("• Total Scheduled Lecture Slots: ").append(rs.getInt(1)).append("\n");
                }

                sb.append("\n--------------------------------------------------\n");
                sb.append("DEPARTMENT BREAKDOWN:\n");
                sb.append("--------------------------------------------------\n");

                try (ResultSet rs = stmt.executeQuery("SELECT department, COUNT(*) as count FROM courses WHERE department IS NOT NULL GROUP BY department")) {
                    boolean hasDept = false;
                    while (rs.next()) {
                        hasDept = true;
                        sb.append(" - ").append(rs.getString("department")).append(": ").append(rs.getInt("count")).append(" courses\n");
                    }
                    if (!hasDept) {
                        sb.append(" No departmental data available.\n");
                    }
                }

                sb.append("\n==================================================\n");
                sb.append("Report generated successfully.\n");

            } catch (SQLException ex) {
                sb.append("Error generating report: ").append(ex.getMessage());
            }

            reportArea.setText(sb.toString());
        });

        generateReportBtn.fire();

        box.getChildren().addAll(label, new Separator(), reportArea, generateReportBtn);
        return box;
    }

    private VBox createCardLayout() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 2);");
        return box;
    }

    private void styleGreenButton(Button btn) {
        btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8px 15px; -fx-background-radius: 5px;");
    }

    private void styleRedButton(Button btn) {
        btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8px 15px; -fx-background-radius: 5px;");
    }

    private void styleBlueButton(Button btn) {
        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8px 15px; -fx-background-radius: 5px;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class LecturerModel {
        private final SimpleStringProperty name;
        private final SimpleStringProperty department;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;

        public LecturerModel(String name, String department, String email, String phone) {
            this.name = new SimpleStringProperty(name != null ? name : "");
            this.department = new SimpleStringProperty(department != null ? department : "N/A");
            this.email = new SimpleStringProperty(email != null && !email.isEmpty() ? email : "Not Provided");
            this.phone = new SimpleStringProperty(phone != null && !phone.isEmpty() ? phone : "Not Provided");
        }

        public String getName() { return name.get(); }
        public SimpleStringProperty nameProperty() { return name; }

        public String getDepartment() { return department.get(); }
        public SimpleStringProperty departmentProperty() { return department; }

        public String getEmail() { return email.get(); }
        public SimpleStringProperty emailProperty() { return email; }

        public String getPhone() { return phone.get(); }
        public SimpleStringProperty phoneProperty() { return phone; }
    }

    public static class ScheduleRecord {
        private final SimpleStringProperty id;
        private final SimpleStringProperty department;
        private final SimpleStringProperty level;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty lecturer;
        private final SimpleStringProperty room;
        private final SimpleStringProperty day;
        private final SimpleStringProperty time;
        private final SimpleStringProperty date;

        public ScheduleRecord(String id, String department, String level, String courseCode, String lecturer, String room, String day, String time, String date) {
            this.id = new SimpleStringProperty(id);
            this.department = new SimpleStringProperty(department);
            this.level = new SimpleStringProperty(level);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.lecturer = new SimpleStringProperty(lecturer);
            this.room = new SimpleStringProperty(room);
            this.day = new SimpleStringProperty(day);
            this.time = new SimpleStringProperty(time);
            this.date = new SimpleStringProperty(date);
        }

        public String getId() { return id.get(); }
        public SimpleStringProperty idProperty() { return id; }

        public String getDepartment() { return department.get(); }
        public SimpleStringProperty departmentProperty() { return department; }

        public String getLevel() { return level.get(); }
        public SimpleStringProperty levelProperty() { return level; }

        public String getCourseCode() { return courseCode.get(); }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }

        public String getLecturer() { return lecturer.get(); }
        public SimpleStringProperty lecturerProperty() { return lecturer; }

        public String getRoom() { return room.get(); }
        public SimpleStringProperty roomProperty() { return room; }

        public String getDay() { return day.get(); }
        public SimpleStringProperty dayProperty() { return day; }

        public String getTime() { return time.get(); }
        public SimpleStringProperty timeProperty() { return time; }

        public String getDate() { return date.get(); }
        public SimpleStringProperty dateProperty() { return date; }
    }
}