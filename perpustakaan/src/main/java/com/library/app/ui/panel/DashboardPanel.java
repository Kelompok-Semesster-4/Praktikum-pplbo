package com.library.app.ui.panel;

import com.library.app.model.DashboardSummary;
import com.library.app.service.DashboardService;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel implements RefreshablePanel {
    private final DashboardService dashboardService = new DashboardService();

    private final JLabel totalBooksLabel = new JLabel();
    private final JLabel totalCopiesLabel = new JLabel();
    private final JLabel availableCopiesLabel = new JLabel();
    private final JLabel totalMembersLabel = new JLabel();
    private final JLabel visitsTodayLabel = new JLabel();
    private final JLabel activeLoansLabel = new JLabel();
    private final JLabel pendingRequestsLabel = new JLabel();

    public DashboardPanel() {
        setLayout(new GridLayout(0, 2, 12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(createCard("Total Judul Buku", totalBooksLabel));
        add(createCard("Total Eksemplar", totalCopiesLabel));
        add(createCard("Stok Tersedia", availableCopiesLabel));
        add(createCard("Total Anggota", totalMembersLabel));
        add(createCard("Kunjungan Hari Ini", visitsTodayLabel));
        add(createCard("Pinjaman Aktif", activeLoansLabel));
        add(createCard("Request Pending", pendingRequestsLabel));
        refreshData();
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 24f));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refreshData() {
        DashboardSummary summary = dashboardService.getSummary();
        totalBooksLabel.setText(String.valueOf(summary.getTotalBooks()));
        totalCopiesLabel.setText(String.valueOf(summary.getTotalCopies()));
        availableCopiesLabel.setText(String.valueOf(summary.getAvailableCopies()));
        totalMembersLabel.setText(String.valueOf(summary.getTotalMembers()));
        visitsTodayLabel.setText(String.valueOf(summary.getVisitsToday()));
        activeLoansLabel.setText(String.valueOf(summary.getActiveLoans()));
        pendingRequestsLabel.setText(String.valueOf(summary.getPendingRequests()));
    }
}
