package com.library.app.ui;

import com.library.app.session.UserSession;

import javax.swing.*;
import java.awt.*;

public abstract class BaseMainFrame extends JFrame {
   protected final UserSession session;
   protected final JTabbedPane tabbedPane = new JTabbedPane();

   protected BaseMainFrame(String title, UserSession session) {
      super(title);
      this.session = session;
      initializeFrame();
   }

   private void initializeFrame() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(1100, 700);
      setLocationRelativeTo(null);
      setLayout(new BorderLayout());

      JLabel headerLabel = new JLabel("Login sebagai: " + session.getUsername() + " (" + session.getRole() + ")");
      headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      add(headerLabel, BorderLayout.NORTH);
      add(tabbedPane, BorderLayout.CENTER);
   }
}
