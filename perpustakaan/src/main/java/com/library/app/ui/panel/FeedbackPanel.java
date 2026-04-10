package com.library.app.ui.panel;

import com.library.app.model.Feedback;
import com.library.app.service.FeedbackService;
import com.library.app.util.DateUtil;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FeedbackPanel extends JPanel implements RefreshablePanel {
    private final FeedbackService feedbackService = new FeedbackService();
    private final boolean adminMode;

    private final JTextField memberCodeField = new JTextField();
    private final JTextField senderNameField = new JTextField();
    private final JTextArea messageArea = new JTextArea(4, 20);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Waktu", "Pengirim", "Pesan"}, 0);

    public FeedbackPanel(boolean adminMode) {
        this.adminMode = adminMode;
        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.NORTH);
        if (adminMode) {
            add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        }
        refreshData();
    }

    private JComponent buildForm() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new GridLayout(0, 2, 8, 8));
        top.setBorder(BorderFactory.createTitledBorder(adminMode ? "Input & Monitoring Feedback" : "Input Feedback"));

        top.add(new JLabel("Kode Anggota (opsional)"));
        top.add(memberCodeField);
        top.add(new JLabel("Nama Pengirim"));
        top.add(senderNameField);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JButton submitButton = new JButton("Kirim Feedback");
        submitButton.addActionListener(event -> submitFeedback());
        panel.add(submitButton, BorderLayout.SOUTH);
        return panel;
    }

    private void submitFeedback() {
        try {
            feedbackService.submitFeedback(memberCodeField.getText(), senderNameField.getText(), messageArea.getText());
            UiUtil.showInfo(this, "Feedback berhasil dikirim.");
            memberCodeField.setText("");
            senderNameField.setText("");
            messageArea.setText("");
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void refreshData() {
        if (!adminMode) {
            return;
        }
        tableModel.setRowCount(0);
        List<Feedback> feedbacks = feedbackService.getAllFeedback();
        for (Feedback feedback : feedbacks) {
            tableModel.addRow(new Object[]{
                    DateUtil.format(feedback.getCreatedAt()),
                    feedback.getSenderName(),
                    feedback.getMessage()
            });
        }
    }
}
