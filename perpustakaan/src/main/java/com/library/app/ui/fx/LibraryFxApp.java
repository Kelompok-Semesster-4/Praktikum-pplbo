package com.library.app.ui.fx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LibraryFxApp extends Application {
    @Override
    public void start(Stage stage) {
        Label title = new Label("Library Management System");
        Label subtitle = new Label("JavaFX is configured and ready.");

        VBox root = new VBox(10, title, subtitle);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 480, 180);
        stage.setTitle("Library System - JavaFX");
        stage.setScene(scene);
        stage.show();
    }
}
