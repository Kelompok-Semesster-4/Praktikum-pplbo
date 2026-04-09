package com.library.app;

import com.library.app.bootstrap.DatabaseInitializer;
import com.library.app.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
