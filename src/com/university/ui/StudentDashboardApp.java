package com.university.ui;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDashboardApp extends Application {

    private final String studentDepartment;
    private final String studentLevel;

    // Default constructor for testing/direct launch
    public StudentDashboardApp() {
        this.studentDepartment = "ICT";
        this.studentLevel = "400";
    }

    // Parameterized constructor receiving student selections from login
    public StudentDashboardApp(String department, String level) {
        this.studentDepartment = department != null ? department : "ICT";
        this.studentLevel = level != null ? level : "400";
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Timetable System - Student Portal (" + studentDepartment + " Level " + studentLevel + ")");

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f4f6f9;");

        // Top Header / Navigation Bar
        HBox topNavBar = new HBox(15);
        topNavBar.setAlignment(Pos.CENTER_LEFT);
        topNavBar.setPadding(new Insets(15));
        topNavBar.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 2);");

        Button backBtn = new Button("⬅ Back to Login");
        styleRedButton(backBtn);
        backBtn.setPrefHeight(38);
        backBtn.setOnAction(e -> confirmAndReturnToLogin(primaryStage));

        VBox titleTitles = new VBox(3);
        Label titleLabel = new Label("Student Portal — " + studentDepartment + " (Level " + studentLevel + ")");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label subtitleLabel = new Label("Viewing your restricted departmental schedule and associated course lecturers.");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        titleTitles.getChildren().addAll(titleLabel, subtitleLabel);

        topNavBar.getChildren().addAll(backBtn, titleTitles);

        // Tab Pane Container for Modular Student Experience
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab timetableTab = new Tab("📅 My Timetable", createTimetableTabView(primaryStage));
        Tab lecturerTab = new Tab("👨‍🏫 My Attached Lecturers", createLecturerDirectoryView());

        tabPane.getTabs().addAll(timetableTab, lecturerTab);

        mainLayout.getChildren().addAll(topNavBar, tabPane);

        Scene scene = new Scene(mainLayout, 960, 680);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- Logout Confirmation Helper ---
    private void confirmAndReturnToLogin(Stage currentStage) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Logout");
        confirmAlert.setHeaderText("Are you sure you want to go back to login?");
        confirmAlert.setContentText("Your current session will be closed.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                currentStage.close();
                Stage loginStage = new Stage();
                new LoginApp().start(loginStage);
            }
        });
    }

    // --- Tab 1: My Strict Department & Level Timetable ---
    private VBox createTimetableTabView(Stage stage) {
        VBox box = createCardLayout();
        Label label = new Label("My Departmental Class Schedule");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        HBox actionControls = new HBox(12);
        actionControls.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search my courses by code or name...");
        searchField.setPrefHeight(38);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Refresh Schedule");
        styleBlueButton(refreshBtn);
        refreshBtn.setPrefHeight(38);
        HBox.setHgrow(refreshBtn, Priority.ALWAYS);

        Button exportBtn = new Button("📄 Export Schedule to CSV");
        styleGreenButton(exportBtn);
        exportBtn.setPrefHeight(38);
        HBox.setHgrow(exportBtn, Priority.ALWAYS);

        actionControls.getChildren().addAll(searchField, refreshBtn, exportBtn);

        TableView<StudentScheduleRecord> table = new TableView<>();
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<StudentScheduleRecord, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(data -> data.getValue().courseCodeProperty());

        TableColumn<StudentScheduleRecord, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(data -> data.getValue().courseNameProperty());

        TableColumn<StudentScheduleRecord, String> lecturerCol = new TableColumn<>("Lecturer");
        lecturerCol.setCellValueFactory(data -> data.getValue().lecturerProperty());

        TableColumn<StudentScheduleRecord, String> roomCol = new TableColumn<>("Hall / Room");
        roomCol.setCellValueFactory(data -> data.getValue().roomProperty());

        TableColumn<StudentScheduleRecord, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(data -> data.getValue().dayProperty());

        TableColumn<StudentScheduleRecord, String> timeCol = new TableColumn<>("Time Slot");
        timeCol.setCellValueFactory(data -> data.getValue().timeProperty());

        table.getColumns().addAll(codeCol, nameCol, lecturerCol, roomCol, dayCol, timeCol);

        ObservableList<StudentScheduleRecord> masterData = FXCollections.observableArrayList();
        loadStudentTimetable(masterData);

        FilteredList<StudentScheduleRecord> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(record -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String query = newVal.toLowerCase().trim();
                return record.getCourseCode().toLowerCase().contains(query) ||
                        record.getCourseName().toLowerCase().contains(query);
            });
        });

        table.setItems(filteredData);

        refreshBtn.setOnAction(e -> {
            searchField.clear();
            loadStudentTimetable(masterData);
        });

        exportBtn.setOnAction(e -> exportScheduleToCSV(stage, filteredData));

        box.getChildren().addAll(label, new Separator(), actionControls, table);
        return box;
    }

    private void loadStudentTimetable(ObservableList<StudentScheduleRecord> list) {
        list.clear();
        String query = "SELECT c.department, c.level, c.course_code, c.course_name, " +
                "l.lecturer_name, r.room_name, s.day_of_week, s.time_slot " +
                "FROM schedules s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "JOIN lecturers l ON s.lecturer_id = l.lecturer_id " +
                "JOIN rooms r ON s.room_id = r.room_id " +
                "WHERE c.department = ? AND c.level = ? " +
                "ORDER BY FIELD(s.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'), s.time_slot";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentDepartment);
            pstmt.setInt(2, Integer.parseInt(studentLevel));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentScheduleRecord(
                            rs.getString("department"),
                            String.valueOf(rs.getInt("level")),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("lecturer_name"),
                            rs.getString("room_name"),
                            rs.getString("day_of_week"),
                            rs.getString("time_slot")
                    ));
                }
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Could not load student timetable: " + ex.getMessage());
        }
    }

    // --- Tab 2: Attached Lecturers Directory ---
    private VBox createLecturerDirectoryView() {
        VBox box = createCardLayout();
        Label label = new Label("Lecturers Attached to My Timetable");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2c3e50;");

        Label infoLabel = new Label("Showing only professors and instructors assigned to your current departmental timetable.");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d;");

        TableView<LecturerContactRecord> table = new TableView<>();
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<LecturerContactRecord, String> nameCol = new TableColumn<>("Lecturer Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());

        TableColumn<LecturerContactRecord, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> data.getValue().departmentProperty());

        TableColumn<LecturerContactRecord, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());

        TableColumn<LecturerContactRecord, String> phoneCol = new TableColumn<>("Phone Contact");
        phoneCol.setCellValueFactory(data -> data.getValue().phoneProperty());

        table.getColumns().addAll(nameCol, deptCol, emailCol, phoneCol);

        ObservableList<LecturerContactRecord> lecturerList = FXCollections.observableArrayList();
        loadAttachedLecturersFromDB(lecturerList);
        table.setItems(lecturerList);

        Button refreshBtn = new Button("🔄 Refresh Attached Lecturers");
        styleBlueButton(refreshBtn);
        refreshBtn.setPrefHeight(38);
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> loadAttachedLecturersFromDB(lecturerList));

        box.getChildren().addAll(label, infoLabel, new Separator(), table, refreshBtn);
        return box;
    }

    private void loadAttachedLecturersFromDB(ObservableList<LecturerContactRecord> list) {
        list.clear();
        String query = "SELECT DISTINCT l.lecturer_name, l.department, l.email, l.phone " +
                "FROM schedules s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "JOIN lecturers l ON s.lecturer_id = l.lecturer_id " +
                "WHERE c.department = ? AND c.level = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentDepartment);
            pstmt.setInt(2, Integer.parseInt(studentLevel));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new LecturerContactRecord(
                            rs.getString("lecturer_name"),
                            rs.getString("department"),
                            rs.getString("email"),
                            rs.getString("phone")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exportScheduleToCSV(Stage stage, FilteredList<StudentScheduleRecord> records) {
        if (records.isEmpty()) {
            showAlert("Export Notice", "There are no schedule records to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Timetable Schedule");
        fileChooser.setInitialFileName("My_Class_Schedule.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Course Code,Course Name,Lecturer,Room,Day,Time Slot");
                for (StudentScheduleRecord rec : records) {
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            rec.getCourseCode(),
                            rec.getCourseName(),
                            rec.getLecturer(),
                            rec.getRoom(),
                            rec.getDay(),
                            rec.getTime());
                }
                showAlert("Success", "Schedule successfully exported to:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                showAlert("Export Error", "Failed to export file: " + ex.getMessage());
            }
        }
    }

    private VBox createCardLayout() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 2);");
        return card;
    }

    private void styleGreenButton(Button btn) {
        btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-cursor: hand;");
        btn.setMaxWidth(Double.MAX_VALUE);
    }

    private void styleRedButton(Button btn) {
        btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    private void styleBlueButton(Button btn) {
        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-cursor: hand;");
        btn.setMaxWidth(Double.MAX_VALUE);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Data Models ---

    public static class StudentScheduleRecord {
        private final SimpleStringProperty department;
        private final SimpleStringProperty level;
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty courseName;
        private final SimpleStringProperty lecturer;
        private final SimpleStringProperty room;
        private final SimpleStringProperty day;
        private final SimpleStringProperty time;

        public StudentScheduleRecord(String department, String level, String courseCode, String courseName, String lecturer, String room, String day, String time) {
            this.department = new SimpleStringProperty(department != null ? department : "");
            this.level = new SimpleStringProperty(level != null ? level : "");
            this.courseCode = new SimpleStringProperty(courseCode != null ? courseCode : "");
            this.courseName = new SimpleStringProperty(courseName != null ? courseName : "");
            this.lecturer = new SimpleStringProperty(lecturer != null ? lecturer : "");
            this.room = new SimpleStringProperty(room != null ? room : "");
            this.day = new SimpleStringProperty(day != null ? day : "");
            this.time = new SimpleStringProperty(time != null ? time : "");
        }

        public String getDepartment() { return department.get(); }
        public SimpleStringProperty departmentProperty() { return department; }

        public String getLevel() { return level.get(); }
        public SimpleStringProperty levelProperty() { return level; }

        public String getCourseCode() { return courseCode.get(); }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }

        public String getCourseName() { return courseName.get(); }
        public SimpleStringProperty courseNameProperty() { return courseName; }

        public String getLecturer() { return lecturer.get(); }
        public SimpleStringProperty lecturerProperty() { return lecturer; }

        public String getRoom() { return room.get(); }
        public SimpleStringProperty roomProperty() { return room; }

        public String getDay() { return day.get(); }
        public SimpleStringProperty dayProperty() { return day; }

        public String getTime() { return time.get(); }
        public SimpleStringProperty timeProperty() { return time; }
    }

    public static class LecturerContactRecord {
        private final SimpleStringProperty name;
        private final SimpleStringProperty department;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;

        public LecturerContactRecord(String name, String department, String email, String phone) {
            this.name = new SimpleStringProperty(name != null ? name : "");
            this.department = new SimpleStringProperty(department != null ? department : "Not Provided");
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
}