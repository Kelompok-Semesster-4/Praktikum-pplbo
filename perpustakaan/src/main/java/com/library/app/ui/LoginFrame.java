package com.library.app.ui;

import com.library.app.model.User;
import com.library.app.model.enums.Role;
import com.library.app.service.AuthService;
import com.library.app.session.UserSession;
import com.library.app.util.UiUtil;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
   private final AuthService authService = new AuthService();
   private final JTextField usernameField = new JTextField();
   private final JPasswordField passwordField = new JPasswordField();

   public LoginFrame() {
      super("Library Management System - Login");
      initialize();
   }

   private void initialize() {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(420, 240);
      setLocationRelativeTo(null);

      JPanel formPanel = new JPanel(new GridLayout(0, 1, 8, 8));
      formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

      formPanel.add(new JLabel("Username"));
      formPanel.add(usernameField);
      formPanel.add(new JLabel("Password"));
      formPanel.add(passwordField);

      JButton loginButton = new JButton("Login");
      loginButton.addActionListener(event -> performLogin());

      JLabel hintLabel = new JLabel("Default akun: admin/admin123 atau kiosk/kiosk123");
      hintLabel.setFont(hintLabel.getFont().deriveFont(Font.PLAIN, 12f));

      JPanel container = new JPanel(new BorderLayout(10, 10));
      container.add(formPanel, BorderLayout.CENTER);
      container.add(loginButton, BorderLayout.SOUTH);
      container.add(hintLabel, BorderLayout.NORTH);

      setContentPane(container);
   }

   private void performLogin() {
      try {
         User user = authService.login(usernameField.getText(), new String(passwordField.getPassword()));
         UserSession session = new UserSession(user);
         if (user.hasRole(Role.ADMIN)) {
            new AdminFrame(session).setVisible(true);
         } else {
            new KioskFrame(session).setVisible(true);
         }
         dispose();
      } catch (Exception exception) {
         UiUtil.showError(this, exception.getMessage());
      }
   }
}
