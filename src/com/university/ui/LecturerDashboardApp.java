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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LecturerDashboardApp extends Application {

    private TableView<LecturerScheduleRecord> table;
    private ObservableList<LecturerScheduleRecord> masterData;
    private final String lecturerName;

    // Default constructor
    public LecturerDashboardApp() {
        this.lecturerName = "Unknown Lecturer";
    }

    // Parameterized constructor receiving the logged-in lecturer's name
    public LecturerDashboardApp(String lecturerName) {
        this.lecturerName = lecturerName != null ? lecturerName : "Unknown Lecturer";
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Timetable System - Lecturer Dashboard");

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
        backBtn.setOnAction(e -> {
            primaryStage.close();
            Stage loginStage = new Stage();
            new LoginApp().start(loginStage);
        });

        VBox titleTitles = new VBox(3);
        Label titleLabel = new Label("Faculty Member Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label subtitleLabel = new Label("Logged in as: " + lecturerName);
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        titleTitles.getChildren().addAll(titleLabel, subtitleLabel);

        topNavBar.getChildren().addAll(backBtn, titleTitles);

        // REQUIREMENT 1 & 3: Profile, Admin Contact & Assigned Courses Summary Card (Before Table View)
        VBox profileCard = createCardLayout();
        Label profileTitle = new Label("👤 Lecturer Profile & Administrative Support");
        profileTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        Label nameInfo = new Label("Full Name: " + lecturerName);
        Label emailInfo = new Label("My Email: " + fetchLecturerEmail(lecturerName));

        // Admin Contact Info for Lecturers to reach out
        Label adminContactInfo = new Label("📢 Reach out to Admin for changes/info: " + fetchAdminContactInfo());
        adminContactInfo.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label assignedDetails = new Label("Assigned Courses, Departments & Levels:\n" + fetchLecturerAssignedDetails(lecturerName));
        assignedDetails.setWrapText(true);
        assignedDetails.setStyle("-fx-text-fill: #34495e; -fx-font-size: 12px;");

        profileCard.getChildren().addAll(profileTitle, new Separator(), nameInfo, emailInfo, adminContactInfo, new Separator(), assignedDetails);

        // Filter and Search Area Card
        VBox filterCard = createCardLayout();
        Label filterTitle = new Label("🔍 Timetable Search & Export");
        filterTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        HBox filterControls = new HBox(12);
        filterControls.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Course Code / Name...");
        searchField.setPrefWidth(280);

        Button exportBtn = new Button("📄 Export My Teaching Schedule");
        styleGreenButton(exportBtn);

        filterControls.getChildren().addAll(searchField, exportBtn);
        filterCard.getChildren().addAll(filterTitle, new Separator(), filterControls);

        // Schedule Table View Card (REQUIREMENT 4: Strict to this lecturer only)
        VBox tableCard = createCardLayout();
        table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<LecturerScheduleRecord, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(data -> data.getValue().courseCodeProperty());
        codeCol.setPrefWidth(110);

        TableColumn<LecturerScheduleRecord, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(data -> data.getValue().courseNameProperty());
        nameCol.setPrefWidth(190);

        TableColumn<LecturerScheduleRecord, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> data.getValue().departmentProperty());
        deptCol.setPrefWidth(110);

        TableColumn<LecturerScheduleRecord, String> levelCol = new TableColumn<>("Level");
        levelCol.setCellValueFactory(data -> data.getValue().levelProperty());
        levelCol.setPrefWidth(65);

        TableColumn<LecturerScheduleRecord, String> roomCol = new TableColumn<>("Hall / Room");
        roomCol.setCellValueFactory(data -> data.getValue().roomProperty());
        roomCol.setPrefWidth(110);

        TableColumn<LecturerScheduleRecord, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(data -> data.getValue().dayProperty());
        dayCol.setPrefWidth(100);

        TableColumn<LecturerScheduleRecord, String> timeCol = new TableColumn<>("Time Slot");
        timeCol.setCellValueFactory(data -> data.getValue().timeProperty());
        timeCol.setPrefWidth(140);

        table.getColumns().addAll(codeCol, nameCol, deptCol, levelCol, roomCol, dayCol, timeCol);

        masterData = FXCollections.observableArrayList();
        FilteredList<LecturerScheduleRecord> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.toLowerCase().trim();
            filteredData.setPredicate(record -> {
                return keyword.isEmpty() ||
                        record.getCourseCode().toLowerCase().contains(keyword) ||
                        record.getCourseName().toLowerCase().contains(keyword);
            });
        });

        table.setItems(filteredData);
        loadLecturerTimetable(lecturerName);

        exportBtn.setOnAction(e -> exportScheduleToCSV(primaryStage, filteredData));

        tableCard.getChildren().add(table);

        mainLayout.getChildren().addAll(topNavBar, profileCard, filterCard, tableCard);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 980, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String fetchLecturerEmail(String name) {
        String query = "SELECT email FROM lecturers WHERE lecturer_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String email = rs.getString("email");
                return (email != null && !email.isEmpty()) ? email : "Not Provided (Contact Admin)";
            }
        } catch (SQLException ignored) {}
        return "Not Available";
    }

    private String fetchAdminContactInfo() {
        String query = "SELECT admin_name, email, phone FROM admin_info LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return String.format("%s | Email: %s | Phone: %s",
                        rs.getString("admin_name"),
                        rs.getString("email"),
                        rs.getString("phone"));
            }
        } catch (SQLException ignored) {}
        return "Admin Email: admin.timetable@university.edu";
    }

    private String fetchLecturerAssignedDetails(String name) {
        StringBuilder details = new StringBuilder();
        String query = "SELECT DISTINCT c.course_code, c.course_name, c.department, c.level " +
                "FROM schedules s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "JOIN lecturers l ON s.lecturer_id = l.lecturer_id " +
                "WHERE l.lecturer_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                details.append(" • ")
                        .append(rs.getString("course_code")).append(" - ")
                        .append(rs.getString("course_name"))
                        .append(" (Dept: ").append(rs.getString("department"))
                        .append(", Level: ").append(rs.getInt("level")).append(")\n");
            }
            if (!found) return " No courses currently assigned.";
        } catch (SQLException ex) {
            return " Error loading assigned courses.";
        }
        return details.toString();
    }

    private void loadLecturerTimetable(String targetLecturer) {
        masterData.clear();
        // Strict restriction: Only query records belonging to this logged-in lecturer
        String query = "SELECT c.course_code, c.course_name, c.department, c.level, " +
                "r.room_name, s.day_of_week, s.time_slot " +
                "FROM schedules s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "JOIN lecturers l ON s.lecturer_id = l.lecturer_id " +
                "JOIN rooms r ON s.room_id = r.room_id " +
                "WHERE l.lecturer_name = ? " +
                "ORDER BY FIELD(s.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'), s.time_slot";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, targetLecturer);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                masterData.add(new LecturerScheduleRecord(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("department") != null ? rs.getString("department") : "N/A",
                        String.valueOf(rs.getInt("level")),
                        rs.getString("room_name"),
                        rs.getString("day_of_week"),
                        rs.getString("time_slot")
                ));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Could not load teaching schedule: " + ex.getMessage());
        }
    }

    private void exportScheduleToCSV(Stage stage, FilteredList<LecturerScheduleRecord> records) {
        if (records.isEmpty()) {
            showAlert("Export Notice", "There are no schedule records to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Teaching Schedule");
        fileChooser.setInitialFileName("My_Teaching_Schedule.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Course Code,Course Name,Department,Level,Room,Day,Time Slot");
                for (LecturerScheduleRecord rec : records) {
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            rec.getCourseCode(),
                            rec.getCourseName(),
                            rec.getDepartment(),
                            rec.getLevel(),
                            rec.getRoom(),
                            rec.getDay(),
                            rec.getTime());
                }
                showAlert("Success", "Teaching schedule successfully exported to:\n" + file.getAbsolutePath());
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
    }

    private void styleRedButton(Button btn) {
        btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class LecturerScheduleRecord {
        private final SimpleStringProperty courseCode;
        private final SimpleStringProperty courseName;
        private final SimpleStringProperty department;
        private final SimpleStringProperty level;
        private final SimpleStringProperty room;
        private final SimpleStringProperty day;
        private final SimpleStringProperty time;

        public LecturerScheduleRecord(String courseCode, String courseName, String department, String level, String room, String day, String time) {
            this.courseCode = new SimpleStringProperty(courseCode);
            this.courseName = new SimpleStringProperty(courseName);
            this.department = new SimpleStringProperty(department);
            this.level = new SimpleStringProperty(level);
            this.room = new SimpleStringProperty(room);
            this.day = new SimpleStringProperty(day);
            this.time = new SimpleStringProperty(time);
        }

        public String getCourseCode() { return courseCode.get(); }
        public SimpleStringProperty courseCodeProperty() { return courseCode; }

        public String getCourseName() { return courseName.get(); }
        public SimpleStringProperty courseNameProperty() { return courseName; }

        public String getDepartment() { return department.get(); }
        public SimpleStringProperty departmentProperty() { return department; }

        public String getLevel() { return level.get(); }
        public SimpleStringProperty levelProperty() { return level; }

        public String getRoom() { return room.get(); }
        public SimpleStringProperty roomProperty() { return room; }

        public String getDay() { return day.get(); }
        public SimpleStringProperty dayProperty() { return day; }

        public String getTime() { return time.get(); }
        public SimpleStringProperty timeProperty() { return time; }
    }
}