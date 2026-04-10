package com.library.app.ui.panel;

import com.library.app.model.Loan;
import com.library.app.service.LoanService;
import com.library.app.util.DateUtil;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LoanPanel extends JPanel implements RefreshablePanel {
    private final LoanService loanService = new LoanService();

    private final JTextField memberCodeField = new JTextField();
    private final JTextField copyCodeField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode Anggota", "Nama", "Kode Eksemplar", "Judul", "Pinjam", "Jatuh Tempo"}, 0);

    public LoanPanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        refreshData();
    }

    private JComponent buildForm() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Proses Peminjaman"));
        panel.add(new JLabel("Kode Anggota"));
        panel.add(memberCodeField);
        panel.add(new JLabel("Kode Eksemplar"));
        panel.add(copyCodeField);
        JButton borrowButton = new JButton("Pinjam Buku");
        borrowButton.addActionListener(event -> borrowBook());
        panel.add(new JLabel());
        panel.add(borrowButton);
        return panel;
    }

    private void borrowBook() {
        try {
            loanService.borrowBook(memberCodeField.getText(), copyCodeField.getText());
            UiUtil.showInfo(this, "Transaksi peminjaman berhasil.");
            memberCodeField.setText("");
            copyCodeField.setText("");
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void refreshData() {
        tableModel.setRowCount(0);
        List<Loan> loans = loanService.getActiveLoans();
        for (Loan loan : loans) {
            tableModel.addRow(new Object[]{
                    loan.getMemberCode(),
                    loan.getMemberName(),
                    loan.getCopyCode(),
                    loan.getBookTitle(),
                    DateUtil.format(loan.getLoanDate()),
                    DateUtil.format(loan.getDueDate())
            });
        }
    }
}
