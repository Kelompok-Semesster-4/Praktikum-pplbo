package com.library.app.ui.fx;

import com.library.app.ui.KioskFrame;
import javafx.application.Application;
import javafx.stage.Stage;

public class LibraryFxApp extends Application {
    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setFullScreen(true);
        KioskFrame kioskFrame = new KioskFrame();
        kioskFrame.showOn(stage);
    }
}
