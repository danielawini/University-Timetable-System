package com.university.ui;

import com.university.database.DatabaseConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginApp {

    public void start(Stage primaryStage) {
        primaryStage.setTitle("University Timetable System - Login Portal");

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: #f4f6f9;");

        Label titleLabel = new Label("System Login Portal");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Role Selection Menu (ComboBox)
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("ADMIN", "LECTURER", "STUDENT");
        roleComboBox.setValue("ADMIN");
        roleComboBox.setMaxWidth(250);
        roleComboBox.setStyle("-fx-font-size: 14px;");

        // Input fields for Admin username and password
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Password");
        visiblePasswordField.setMaxWidth(250);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        CheckBox showPasswordCheckBox = new CheckBox("Show Password");
        showPasswordCheckBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");

        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        showPasswordCheckBox.setOnAction(e -> {
            if (showPasswordCheckBox.isSelected()) {
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
            } else {
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);
            }
        });

        VBox passwordContainer = new VBox(5);
        passwordContainer.setMaxWidth(250);
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        passwordContainer.getChildren().addAll(passwordField, visiblePasswordField, showPasswordCheckBox);

        // Student Selection Container (Department & Level)
        VBox studentContainer = new VBox(8);
        studentContainer.setMaxWidth(250);
        studentContainer.setAlignment(Pos.CENTER_LEFT);
        studentContainer.setVisible(false);
        studentContainer.setManaged(false);

        Label deptLabel = new Label("Select Department:");
        deptLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> studentDeptCb = new ComboBox<>();
        studentDeptCb.setPromptText("-- Choose Department --");
        studentDeptCb.setMaxWidth(250);

        Label levelLabel = new Label("Select Year Level:");
        levelLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> studentLevelCb = new ComboBox<>();
        studentLevelCb.setPromptText("-- Choose Level --");
        studentLevelCb.setMaxWidth(250);

        studentContainer.getChildren().addAll(deptLabel, studentDeptCb, levelLabel, studentLevelCb);

        // Populate Student dropdowns dynamically from database records
        loadStudentOptions(studentDeptCb, studentLevelCb);

        Button loginBtn = new Button("Login / Proceed");
        loginBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 20px; -fx-cursor: hand;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        // Role change listener to toggle UI views
        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            messageLabel.setText("");
            if ("STUDENT".equals(newValue)) {
                usernameField.setVisible(false);
                usernameField.setManaged(false);
                passwordContainer.setVisible(false);
                passwordContainer.setManaged(false);
                studentContainer.setVisible(true);
                studentContainer.setManaged(true);
                // Refresh list in case admin added new courses
                loadStudentOptions(studentDeptCb, studentLevelCb);
            } else if ("LECTURER".equals(newValue)) {
                usernameField.setVisible(false);
                usernameField.setManaged(false);
                passwordContainer.setVisible(false);
                passwordContainer.setManaged(false);
                studentContainer.setVisible(false);
                studentContainer.setManaged(false);
            } else {
                usernameField.setVisible(true);
                usernameField.setManaged(true);
                passwordContainer.setVisible(true);
                passwordContainer.setManaged(true);
                studentContainer.setVisible(false);
                studentContainer.setManaged(false);
            }
        });

        loginBtn.setOnAction(e -> {
            String selectedRole = roleComboBox.getValue();

            if ("ADMIN".equals(selectedRole)) {
                String username = usernameField.getText();
                String password = passwordField.isVisible() ? passwordField.getText() : visiblePasswordField.getText();

                if (validateAdminLogin(username, password)) {
                    showAlert("Success", "Login successfully!");
                    primaryStage.close();
                    Stage dashboardStage = new Stage();
                    new AdminDashboardApp().start(dashboardStage);
                } else {
                    messageLabel.setText("Invalid admin credentials!");
                }
            } else if ("LECTURER".equals(selectedRole)) {
                showAlert("Lecturer Portal", "Redirecting to Lecturer Dashboard...");
            } else if ("STUDENT".equals(selectedRole)) {
                String chosenDept = studentDeptCb.getValue();
                String chosenLevel = studentLevelCb.getValue();

                if (chosenDept == null || chosenLevel == null) {
                    messageLabel.setText("Please select your Department and Level.");
                    return;
                }

                primaryStage.close();
                Stage studentStage = new Stage();
                new StudentDashboardApp(chosenDept, chosenLevel).start(studentStage);
            }
        });

        layout.getChildren().addAll(
                titleLabel,
                new Label("Select User Role:"),
                roleComboBox,
                usernameField,
                passwordContainer,
                studentContainer,
                loginBtn,
                messageLabel
        );

        Scene scene = new Scene(layout, 400, 430);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadStudentOptions(ComboBox<String> deptCb, ComboBox<String> levelCb) {
        deptCb.getItems().clear();
        levelCb.getItems().clear();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Load distinct departments from courses
            try (ResultSet rs = stmt.executeQuery("SELECT DISTINCT department FROM courses WHERE department IS NOT NULL")) {
                while (rs.next()) {
                    deptCb.getItems().add(rs.getString("department"));
                }
            }

            // Load distinct levels from courses
            try (ResultSet rs = stmt.executeQuery("SELECT DISTINCT level FROM courses WHERE level IS NOT NULL ORDER BY level")) {
                while (rs.next()) {
                    levelCb.getItems().add(String.valueOf(rs.getInt("level")));
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateAdminLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password_hash = ? AND role = 'ADMIN'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}