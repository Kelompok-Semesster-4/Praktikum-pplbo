package com.library.app;

import com.library.app.bootstrap.DatabaseInitializer;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        javax.swing.SwingUtilities.invokeLater(() -> {
            new com.library.app.ui.LoginFrame().setVisible(true);
        });
    }
}
