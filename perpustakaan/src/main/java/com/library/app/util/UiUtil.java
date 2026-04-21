package com.library.app.util;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class UiUtil {

    public static final Color BACKGROUND = new Color(245, 247, 251);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(45, 108, 223);
    public static final Color TEXT_DARK = new Color(34, 44, 69);
    public static final Color TEXT_MUTED = new Color(128, 137, 157);
    public static final Color BORDER = new Color(232, 236, 243);

    private UiUtil() {
    }

    public static Font titleFont() {
        return new Font("SansSerif", Font.BOLD, 24);
    }

    public static Font headingFont() {
        return new Font("SansSerif", Font.BOLD, 18);
    }

    public static Font normalFont() {
        return new Font("SansSerif", Font.PLAIN, 13);
    }

    public static Font boldFont() {
        return new Font("SansSerif", Font.BOLD, 13);
    }

    public static Font smallFont() {
        return new Font("SansSerif", Font.PLAIN, 12);
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(boldFont());
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFont(boldFont());
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}