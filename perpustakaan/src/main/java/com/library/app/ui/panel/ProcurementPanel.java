package com.library.app.ui.panel;

import com.library.app.model.ProcurementRequest;
import com.library.app.model.enums.RequestStatus;
import com.library.app.service.ProcurementService;
import com.library.app.util.DateUtil;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProcurementPanel extends JPanel implements RefreshablePanel {
    private final ProcurementService procurementService = new ProcurementService();
    private final boolean adminMode;

    private final JTextField memberCodeField = new JTextField();
    private final JTextField requesterNameField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextArea noteArea = new JTextArea(3, 20);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Pemohon", "Judul", "Penulis", "Status", "Waktu", "Respon"}, 0);
    private final JTable table = new JTable(tableModel);

    public ProcurementPanel(boolean adminMode) {
        this.adminMode = adminMode;
        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.NORTH);
        if (adminMode) {
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(buildAdminActions(), BorderLayout.SOUTH);
        }
        refreshData();
    }

    private JComponent buildForm() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder(adminMode ? "Input & Monitoring Request Pengadaan" : "Request Pengadaan"));
        form.add(new JLabel("Kode Anggota (opsional)"));
        form.add(memberCodeField);
        form.add(new JLabel("Nama Pengaju"));
        form.add(requesterNameField);
        form.add(new JLabel("Judul Buku"));
        form.add(titleField);
        form.add(new JLabel("Penulis"));
        form.add(authorField);
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(noteArea), BorderLayout.CENTER);

        JButton submitButton = new JButton("Kirim Request");
        submitButton.addActionListener(event -> submitRequest());
        panel.add(submitButton, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildAdminActions() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approveButton = new JButton("Approve");
        approveButton.addActionListener(event -> reviewSelected(RequestStatus.APPROVED));
        JButton rejectButton = new JButton("Reject");
        rejectButton.addActionListener(event -> reviewSelected(RequestStatus.REJECTED));
        panel.add(approveButton);
        panel.add(rejectButton);
        return panel;
    }

    private void submitRequest() {
        try {
            procurementService.submitRequest(
                    memberCodeField.getText(),
                    requesterNameField.getText(),
                    titleField.getText(),
                    authorField.getText(),
                    noteArea.getText()
            );
            UiUtil.showInfo(this, "Request pengadaan berhasil dikirim.");
            memberCodeField.setText("");
            requesterNameField.setText("");
            titleField.setText("");
            authorField.setText("");
            noteArea.setText("");
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    private void reviewSelected(RequestStatus status) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            UiUtil.showError(this, "Pilih request terlebih dahulu.");
            return;
        }
        String response = JOptionPane.showInputDialog(this, "Catatan respon admin:");
        if (response == null) {
            return;
        }
        long requestId = Long.parseLong(tableModel.getValueAt(selectedRow, 0).toString());
        try {
            procurementService.reviewRequest(requestId, status, response);
            UiUtil.showInfo(this, "Request berhasil diperbarui.");
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
        List<ProcurementRequest> requests = procurementService.getAllRequests();
        for (ProcurementRequest request : requests) {
            tableModel.addRow(new Object[]{
                    request.getId(),
                    request.getRequesterName(),
                    request.getTitle(),
                    request.getAuthor(),
                    request.getStatus(),
                    DateUtil.format(request.getCreatedAt()),
                    request.getResponseNote()
            });
        }
    }
}
