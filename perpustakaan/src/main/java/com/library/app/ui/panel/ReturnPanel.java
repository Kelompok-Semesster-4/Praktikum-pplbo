package com.library.app.ui.panel;

import com.library.app.model.Loan;
import com.library.app.service.LoanService;
import com.library.app.util.DateUtil;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReturnPanel extends JPanel implements RefreshablePanel {
    private final LoanService loanService = new LoanService();

    private final JTextField copyCodeField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode Anggota", "Kode Eksemplar", "Judul", "Tanggal Kembali", "Denda"}, 0);

    public ReturnPanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        refreshData();
    }

    private JComponent buildForm() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Proses Pengembalian"));
        panel.add(new JLabel("Kode Eksemplar"));
        panel.add(copyCodeField);
        JButton returnButton = new JButton("Kembalikan Buku");
        returnButton.addActionListener(event -> returnBook());
        panel.add(new JLabel());
        panel.add(returnButton);
        return panel;
    }

    private void returnBook() {
        try {
            Loan loan = loanService.returnBook(copyCodeField.getText());
            UiUtil.showInfo(this, "Buku berhasil dikembalikan. Denda: Rp" + loan.getFineAmount());
            copyCodeField.setText("");
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    @Override
    public void refreshData() {
        tableModel.setRowCount(0);
        List<Loan> loans = loanService.getReturnedLoans();
        for (Loan loan : loans) {
            tableModel.addRow(new Object[]{
                    loan.getMemberCode(),
                    loan.getCopyCode(),
                    loan.getBookTitle(),
                    DateUtil.format(loan.getReturnDate()),
                    loan.getFineAmount()
            });
        }
    }
}
