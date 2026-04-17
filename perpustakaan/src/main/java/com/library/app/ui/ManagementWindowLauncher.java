package com.library.app.ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class ManagementWindowLauncher {
   private ManagementWindowLauncher() {
   }

   public static void show(String title, JPanel panel) {
      SwingUtilities.invokeLater(() -> {
         JFrame frame = new JFrame(title);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.setContentPane(panel);
         frame.setSize(1100, 700);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
      });
   }
}