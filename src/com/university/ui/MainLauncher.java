package com.university.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainLauncher extends Application {
    @Override
    public void start(Stage primaryStage) {
        new LoginApp().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}