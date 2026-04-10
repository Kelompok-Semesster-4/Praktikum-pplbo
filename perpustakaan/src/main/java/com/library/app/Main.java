package com.library.app;

import com.library.app.bootstrap.DatabaseInitializer;
import com.library.app.ui.fx.LibraryFxApp;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        javafx.application.Application.launch(LibraryFxApp.class, args);
    }
}
