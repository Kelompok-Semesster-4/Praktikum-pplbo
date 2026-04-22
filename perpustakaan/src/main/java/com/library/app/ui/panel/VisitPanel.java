package com.library.app.ui.panel;

import com.library.app.model.Visit;
import com.library.app.model.enums.VisitPresenceStatus;
import com.library.app.service.VisitService;
import com.library.app.util.DateUtil;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VisitPanel extends JPanel implements RefreshablePanel {
    private final VisitService visitService = new VisitService();

    private final JTextField memberCodeField = new JTextField();
    private final JTextField guestNameField = new JTextField();
    private final JTextField guestInstitutionField = new JTextField();
    private final JTextField guestPurposeField = new JTextField();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Tanggal", "Tipe", "Status", "Nama", "Identitas", "Instansi", "Keperluan", "Masuk", "Keluar"}, 0);

    public VisitPanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildHeader(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        refreshData();
    }

    private JComponent buildHeader() {
        JPanel container = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel memberPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        memberPanel.setBorder(BorderFactory.createTitledBorder("Absen Member (Masuk/Keluar)"));
        memberPanel.add(new JLabel("Masukkan NIM/NIS/NIDN (scan 2x: masuk lalu keluar)"));
        memberPanel.add(memberCodeField);
        JButton memberButton = new JButton("Proses Absen Masuk/Keluar");
        memberButton.addActionListener(event -> recordMemberVisit());
        memberPanel.add(memberButton);

        JPanel guestPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        guestPanel.setBorder(BorderFactory.createTitledBorder("Absen Guest oleh Admin"));
        guestPanel.add(new JLabel("Nama Guest"));
        guestPanel.add(guestNameField);
        guestPanel.add(new JLabel("Instansi / Asal"));
        guestPanel.add(guestInstitutionField);
        guestPanel.add(new JLabel("Keperluan"));
        guestPanel.add(guestPurposeField);
        JButton guestButton = new JButton("Proses Absen Tamu");
        guestButton.addActionListener(event -> recordGuestVisit());
        guestPanel.add(guestButton);

        container.add(memberPanel);
        container.add(guestPanel);
        return container;
    }

    private void recordMemberVisit() {
        try {
            String message = visitService.recordMemberVisit(memberCodeField.getText());
            UiUtil.showInfo(this, message);
            memberCodeField.setText("");
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    private void recordGuestVisit() {
        try {
            String message = visitService.recordGuestVisit(
                    guestNameField.getText(),
                    guestInstitutionField.getText(),
                    guestPurposeField.getText()
            );
            UiUtil.showInfo(this, message);
            guestNameField.setText("");
            guestInstitutionField.setText("");
            guestPurposeField.setText("");
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void refreshData() {
        tableModel.setRowCount(0);
        List<Visit> visits = visitService.getRecentVisits();
        for (Visit visit : visits) {
            tableModel.addRow(new Object[]{
                    DateUtil.format(visit.getVisitDate()),
                    visit.getVisitType(),
                    formatVisitStatus(visit.getVisitStatus()),
                    visit.getVisitorName(),
                    visit.getVisitorIdentifier(),
                    visit.getInstitution(),
                    visit.getPurpose(),
                    visit.getCheckInTime() == null ? "-" : visit.getCheckInTime().toString(),
                    visit.getCheckOutTime() == null ? "-" : visit.getCheckOutTime().toString()
            });
        }
    }

    private String formatVisitStatus(VisitPresenceStatus status) {
        if (status == VisitPresenceStatus.DI_DALAM) {
            return "Di dalam";
        }
        return "Selesai";
    }
}
