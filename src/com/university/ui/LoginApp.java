package com.university.ui;

import com.university.database.DatabaseConnection;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginApp {

    public void start(Stage primaryStage) {
        primaryStage.setTitle("University of Education, Winneba - System Login Portal");

        BorderPane rootPane = new BorderPane();
        StackPane centerStack = new StackPane();
        centerStack.setStyle("-fx-background-color: #1a252f;");

        // Load local campus image safely from your Pictures folder (.jpg format)
        Image campusImage;
        try {
            File imageFile = Paths.get("C:", "Users", "BENICE.A", "Pictures", "winneba-campus.jpg").toFile();
            if (imageFile.exists()) {
                campusImage = new Image(imageFile.toURI().toURL().toExternalForm(), true);
            } else {
                campusImage = new Image("https://images.unsplash.com/photo-1541339907198-e08756dedf3f?auto=format&fit=crop&w=1920&q=80", true);
            }
        } catch (Exception e) {
            campusImage = new Image("https://images.unsplash.com/photo-1541339907198-e08756dedf3f?auto=format&fit=crop&w=1920&q=80", true);
        }

        BackgroundImage campusBg = new BackgroundImage(
                campusImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        Pane bgPane = new Pane();
        bgPane.setBackground(new Background(campusBg));

        Pane overlayPane = new Pane();
        overlayPane.setStyle("-fx-background-color: rgba(26, 37, 47, 0.65);");

        VBox loginCard = new VBox(15);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(25));
        loginCard.setMaxWidth(380);
        loginCard.setMaxHeight(560);
        loginCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);");

        Pane tickerContainer = new Pane();
        tickerContainer.setPrefHeight(35);
        tickerContainer.setStyle("-fx-background-color: #2c3e50;");

        Label marqueeLabel = new Label("🎓 Welcome to the University of Education, Winneba School Timetable System - Education for Service");
        marqueeLabel.setTextFill(Color.WHITE);
        marqueeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        marqueeLabel.setLayoutY(9);

        tickerContainer.getChildren().add(marqueeLabel);

        TranslateTransition transition = new TranslateTransition(Duration.seconds(14), marqueeLabel);
        transition.setFromX(950);
        transition.setToX(-900);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.play();

        // Load logo-default.png to appear big right above the title inside the form
        ImageView logoView = new ImageView();
        try {
            File logoFile = Paths.get("C:", "Users", "BENICE.A", "Pictures", "logo-default.png").toFile();
            if (logoFile.exists()) {
                Image logoImg = new Image(logoFile.toURI().toURL().toExternalForm(), true);
                logoView.setImage(logoImg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logoView.setFitWidth(85);
        logoView.setFitHeight(85);
        logoView.setPreserveRatio(true);

        Label titleLabel = new Label("UEW Timetable Portal");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("ADMIN", "LECTURER", "STUDENT");
        roleComboBox.setValue("ADMIN");
        roleComboBox.setMaxWidth(250);
        roleComboBox.setStyle("-fx-font-size: 14px;");

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

        VBox lecturerContainer = new VBox(8);
        lecturerContainer.setMaxWidth(250);
        lecturerContainer.setAlignment(Pos.CENTER_LEFT);
        lecturerContainer.setVisible(false);
        lecturerContainer.setManaged(false);

        Label lecturerLabel = new Label("Select Lecturer Profile:");
        lecturerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> lecturerNameCb = new ComboBox<>();
        lecturerNameCb.setPromptText("-- Choose Lecturer --");
        lecturerNameCb.setMaxWidth(250);

        Hyperlink createPassLink = new Hyperlink("🔒 Set Private Password");
        createPassLink.setStyle("-fx-font-size: 11px;");
        createPassLink.setOnAction(e -> {
            String chosen = lecturerNameCb.getValue();
            if (chosen == null || chosen.isEmpty()) {
                showAlert("Selection Required", "Please select your lecturer profile from the dropdown first.");
                return;
            }
            showPasswordCreationDialog(chosen);
        });

        Hyperlink forgotPassLink = new Hyperlink("❓ Forgot Password?");
        forgotPassLink.setStyle("-fx-font-size: 11px;");
        forgotPassLink.setOnAction(e -> showPasswordRecoveryDialog(lecturerNameCb.getValue()));

        HBox lecturerLinksBox = new HBox(8, createPassLink, forgotPassLink);
        lecturerContainer.getChildren().addAll(lecturerLabel, lecturerNameCb, lecturerLinksBox);

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

        loadLoginDropdownOptions(studentDeptCb, studentLevelCb, lecturerNameCb);

        Button loginBtn = new Button("Login / Proceed");
        loginBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 20px; -fx-cursor: hand;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            messageLabel.setText("");
            if ("STUDENT".equals(newValue)) {
                usernameField.setVisible(false);
                usernameField.setManaged(false);
                passwordContainer.setVisible(false);
                passwordContainer.setManaged(false);
                lecturerContainer.setVisible(false);
                lecturerContainer.setManaged(false);
                studentContainer.setVisible(true);
                studentContainer.setManaged(true);
                loadLoginDropdownOptions(studentDeptCb, studentLevelCb, lecturerNameCb);
            } else if ("LECTURER".equals(newValue)) {
                usernameField.setVisible(false);
                usernameField.setManaged(false);
                passwordContainer.setVisible(true);
                passwordContainer.setManaged(true);
                lecturerContainer.setVisible(true);
                lecturerContainer.setManaged(true);
                studentContainer.setVisible(false);
                studentContainer.setManaged(false);
                loadLoginDropdownOptions(studentDeptCb, studentLevelCb, lecturerNameCb);
            } else {
                usernameField.setVisible(true);
                usernameField.setManaged(true);
                passwordContainer.setVisible(true);
                passwordContainer.setManaged(true);
                studentContainer.setVisible(false);
                studentContainer.setManaged(false);
                lecturerContainer.setVisible(false);
                lecturerContainer.setManaged(false);
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
                String chosenLecturer = lecturerNameCb.getValue();
                String password = passwordField.isVisible() ? passwordField.getText() : visiblePasswordField.getText();

                if (chosenLecturer == null || chosenLecturer.isEmpty()) {
                    messageLabel.setText("Please select your lecturer profile.");
                    return;
                }
                if (password.isEmpty()) {
                    messageLabel.setText("Please enter your private password.");
                    return;
                }

                if (authenticateLecturer(chosenLecturer, password)) {
                    showAlert("Success", "Login successfully!");
                    primaryStage.close();
                    Stage lecturerStage = new Stage();
                    new LecturerDashboardApp(chosenLecturer).start(lecturerStage);
                } else {
                    messageLabel.setText("Invalid password or password not set yet.\nUse 'Set Private Password' if first time.");
                }
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

        loginCard.getChildren().addAll(
                logoView,
                titleLabel,
                new Label("Select User Role:"),
                roleComboBox,
                usernameField,
                passwordContainer,
                lecturerContainer,
                studentContainer,
                loginBtn,
                messageLabel
        );

        centerStack.getChildren().addAll(bgPane, overlayPane, loginCard);

        rootPane.setTop(tickerContainer);
        rootPane.setCenter(centerStack);

        Scene scene = new Scene(rootPane, 950, 640);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadLoginDropdownOptions(ComboBox<String> deptCb, ComboBox<String> levelCb, ComboBox<String> lecturerCb) {
        deptCb.getItems().clear();
        levelCb.getItems().clear();
        lecturerCb.getItems().clear();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT DISTINCT department FROM courses WHERE department IS NOT NULL")) {
                while (rs.next()) {
                    deptCb.getItems().add(rs.getString("department"));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT DISTINCT level FROM courses WHERE level IS NOT NULL ORDER BY level")) {
                while (rs.next()) {
                    levelCb.getItems().add(String.valueOf(rs.getInt("level")));
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT lecturer_name FROM lecturers WHERE lecturer_name IS NOT NULL")) {
                while (rs.next()) {
                    lecturerCb.getItems().add(rs.getString("lecturer_name"));
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

    private boolean authenticateLecturer(String lecturerName, String password) {
        String query = "SELECT lecturer_id FROM lecturers WHERE lecturer_name = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, lecturerName);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // --- Strict Password Validation Rule ---
    public static boolean isValidLecturerPassword(String password) {
        if (password == null || password.length() < 5 || password.length() > 10) {
            return false;
        }
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");

        return hasLetter && hasDigit && hasSymbol;
    }

    private void showPasswordCreationDialog(String lecturerName) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Lecturer Private Password Setup");
        dialog.setHeaderText("Set Password for: " + lecturerName + "\n(5-10 chars, combining letters, numbers & symbols)");

        ButtonType saveButtonType = new ButtonType("Save Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        PasswordField passField = new PasswordField();
        passField.setPromptText("New password");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");

        VBox content = new VBox(10, new Label("New Password:"), passField, new Label("Confirm Password:"), confirmField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String pass = passField.getText();
                String confirm = confirmField.getText();

                if (!pass.equals(confirm)) {
                    showAlert("Error", "Passwords do not match!");
                    return null;
                }
                if (!isValidLecturerPassword(pass)) {
                    showAlert("Invalid Password", "Password must be 5-10 characters long and contain letters, numbers, and symbols.");
                    return null;
                }
                return pass;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            String updateQuery = "UPDATE lecturers SET password = ? WHERE lecturer_name = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, lecturerName);
                int affected = pstmt.executeUpdate();
                if (affected > 0) {
                    showAlert("Success", "Private password created/updated successfully! You can now log in.");
                } else {
                    showAlert("Error", "Lecturer profile not found.");
                }
            } catch (SQLException e) {
                showAlert("Database Error", e.getMessage());
            }
        });
    }

    private void showPasswordRecoveryDialog(String preselectedName) {
        TextInputDialog recoveryDialog = new TextInputDialog(preselectedName != null ? preselectedName : "");
        recoveryDialog.setTitle("Lecturer Password Recovery");
        recoveryDialog.setHeaderText("Forgot your password?");
        recoveryDialog.setContentText("Enter your Lecturer Name, Email, or Phone:");

        recoveryDialog.showAndWait().ifPresent(identifier -> {
            String checkQuery = "SELECT lecturer_name FROM lecturers WHERE lecturer_name = ? OR email = ? OR phone = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, identifier);
                pstmt.setString(2, identifier);
                pstmt.setString(3, identifier);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String foundName = rs.getString("lecturer_name");
                    showAlert("Account Verified", "Welcome, " + foundName + ".\nProceed to create a new secure password.");
                    showPasswordCreationDialog(foundName);
                } else {
                    showAlert("Not Found", "No lecturer account found matching: " + identifier);
                }
            } catch (SQLException e) {
                showAlert("Database Error", e.getMessage());
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}