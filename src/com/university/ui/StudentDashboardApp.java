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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentDashboardApp extends Application {

    private TableView<StudentScheduleRecord> table;
    private ObservableList<StudentScheduleRecord> masterData;
    private final String initialDepartment;
    private final String initialLevel;

    // Default constructor
    public StudentDashboardApp() {
        this.initialDepartment = null;
        this.initialLevel = null;
    }

    // Parameterized constructor receiving student selections from login
    public StudentDashboardApp(String department, String level) {
        this.initialDepartment = department;
        this.initialLevel = level;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Timetable System - Student Dashboard");

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
        Label titleLabel = new Label("Student Class Schedule Portal");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label subtitleLabel = new Label("Viewing schedule for Department: " +
                (initialDepartment != null ? initialDepartment : "All") +
                " | Level: " + (initialLevel != null ? initialLevel : "All"));
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        titleTitles.getChildren().addAll(titleLabel, subtitleLabel);

        topNavBar.getChildren().addAll(backBtn, titleTitles);

        // Filter and Search Area Card
        VBox filterCard = createCardLayout();
        Label filterTitle = new Label("🔍 Timetable Filters & Search");
        filterTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        HBox filterControls = new HBox(12);
        filterControls.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> deptFilterCb = new ComboBox<>();
        deptFilterCb.setPromptText("Filter Department");
        deptFilterCb.getItems().add("All Departments");
        deptFilterCb.setPrefWidth(170);

        ComboBox<String> levelFilterCb = new ComboBox<>();
        levelFilterCb.setPromptText("Filter Level");
        levelFilterCb.getItems().addAll("All Levels", "100", "200", "300", "400");
        levelFilterCb.setPrefWidth(130);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Course Code / Name...");
        searchField.setPrefWidth(220);

        Button refreshBtn = new Button("🔄 Reset Filters");
        styleBlueButton(refreshBtn);

        Button exportBtn = new Button("📄 Export My Schedule");
        styleGreenButton(exportBtn);

        filterControls.getChildren().addAll(deptFilterCb, levelFilterCb, searchField, refreshBtn, exportBtn);
        filterCard.getChildren().addAll(filterTitle, new Separator(), filterControls);

        // Populate Department dropdown dynamically from DB
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT department FROM courses WHERE department IS NOT NULL")) {
            while (rs.next()) {
                deptFilterCb.getItems().add(rs.getString("department"));
            }
        } catch (SQLException ignored) {}

        // Apply initial login values if provided
        if (initialDepartment != null) {
            deptFilterCb.setValue(initialDepartment);
        } else {
            deptFilterCb.setValue("All Departments");
        }

        if (initialLevel != null) {
            levelFilterCb.setValue(initialLevel);
        } else {
            levelFilterCb.setValue("All Levels");
        }

        // Schedule Table View Card
        VBox tableCard = createCardLayout();
        table = new TableView<>();
        table.setPrefHeight(380);

        TableColumn<StudentScheduleRecord, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> data.getValue().departmentProperty());
        deptCol.setPrefWidth(110);

        TableColumn<StudentScheduleRecord, String> levelCol = new TableColumn<>("Level");
        levelCol.setCellValueFactory(data -> data.getValue().levelProperty());
        levelCol.setPrefWidth(65);

        TableColumn<StudentScheduleRecord, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(data -> data.getValue().courseCodeProperty());
        codeCol.setPrefWidth(100);

        TableColumn<StudentScheduleRecord, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(data -> data.getValue().courseNameProperty());
        nameCol.setPrefWidth(180);

        TableColumn<StudentScheduleRecord, String> lecturerCol = new TableColumn<>("Lecturer");
        lecturerCol.setCellValueFactory(data -> data.getValue().lecturerProperty());
        lecturerCol.setPrefWidth(140);

        TableColumn<StudentScheduleRecord, String> roomCol = new TableColumn<>("Hall / Room");
        roomCol.setCellValueFactory(data -> data.getValue().roomProperty());
        roomCol.setPrefWidth(100);

        TableColumn<StudentScheduleRecord, String> dayCol = new TableColumn<>("Day");
        dayCol.setCellValueFactory(data -> data.getValue().dayProperty());
        dayCol.setPrefWidth(90);

        TableColumn<StudentScheduleRecord, String> timeCol = new TableColumn<>("Time Slot");
        timeCol.setCellValueFactory(data -> data.getValue().timeProperty());
        timeCol.setPrefWidth(130);

        table.getColumns().addAll(deptCol, levelCol, codeCol, nameCol, lecturerCol, roomCol, dayCol, timeCol);

        masterData = FXCollections.observableArrayList();
        FilteredList<StudentScheduleRecord> filteredData = new FilteredList<>(masterData, p -> true);

        // Filter Logic
        Runnable updateFilter = () -> {
            String selectedDept = deptFilterCb.getValue();
            String selectedLevel = levelFilterCb.getValue();
            String keyword = searchField.getText().toLowerCase().trim();

            filteredData.setPredicate(record -> {
                boolean matchesDept = (selectedDept == null || selectedDept.equals("All Departments") || record.getDepartment().equalsIgnoreCase(selectedDept));
                boolean matchesLevel = (selectedLevel == null || selectedLevel.equals("All Levels") || record.getLevel().equals(selectedLevel));
                boolean matchesSearch = (keyword.isEmpty() ||
                        record.getCourseCode().toLowerCase().contains(keyword) ||
                        record.getCourseName().toLowerCase().contains(keyword));

                return matchesDept && matchesLevel && matchesSearch;
            });
        };

        deptFilterCb.setOnAction(e -> updateFilter.run());
        levelFilterCb.setOnAction(e -> updateFilter.run());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter.run());

        table.setItems(filteredData);
        loadStudentTimetable();
        updateFilter.run(); // Apply initial filter match

        refreshBtn.setOnAction(e -> {
            searchField.clear();
            deptFilterCb.setValue("All Departments");
            levelFilterCb.setValue("All Levels");
            loadStudentTimetable();
        });

        exportBtn.setOnAction(e -> exportScheduleToCSV(primaryStage, filteredData));

        tableCard.getChildren().add(table);

        mainLayout.getChildren().addAll(topNavBar, filterCard, tableCard);

        Scene scene = new Scene(mainLayout, 960, 680);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadStudentTimetable() {
        masterData.clear();
        String query = "SELECT c.department, c.level, c.course_code, c.course_name, " +
                "l.lecturer_name, r.room_name, s.day_of_week, s.time_slot " +
                "FROM schedules s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "JOIN lecturers l ON s.lecturer_id = l.lecturer_id " +
                "JOIN rooms r ON s.room_id = r.room_id " +
                "ORDER BY FIELD(s.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'), s.time_slot";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                masterData.add(new StudentScheduleRecord(
                        rs.getString("department") != null ? rs.getString("department") : "N/A",
                        String.valueOf(rs.getInt("level")),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("lecturer_name"),
                        rs.getString("room_name"),
                        rs.getString("day_of_week"),
                        rs.getString("time_slot")
                ));
            }
        } catch (SQLException ex) {
            showAlert("Database Error", "Could not load timetable: " + ex.getMessage());
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
                writer.println("Department,Level,Course Code,Course Name,Lecturer,Room,Day,Time Slot");
                for (StudentScheduleRecord rec : records) {
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            rec.getDepartment(),
                            rec.getLevel(),
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
    }

    private void styleRedButton(Button btn) {
        btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    private void styleBlueButton(Button btn) {
        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-cursor: hand;");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
            this.department = new SimpleStringProperty(department);
            this.level = new SimpleStringProperty(level);
            this.courseCode = new SimpleStringProperty(courseCode);
            this.courseName = new SimpleStringProperty(courseName);
            this.lecturer = new SimpleStringProperty(lecturer);
            this.room = new SimpleStringProperty(room);
            this.day = new SimpleStringProperty(day);
            this.time = new SimpleStringProperty(time);
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
}