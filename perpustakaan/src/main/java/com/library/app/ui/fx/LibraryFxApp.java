package com.library.app.ui.fx;

import com.library.app.ui.LoginFrame;

import javafx.application.Application;
import javafx.stage.*;;

public class LibraryFxApp extends Application {
    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setFullScreen(true);
        // KioskFrame kioskFrame = new KioskFrame();
        LoginFrame loginFrame=new LoginFrame();
        loginFrame.showOn(stage);
    }
}
