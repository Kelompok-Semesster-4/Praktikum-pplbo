package com.library.app.ui.panel;

import com.library.app.dao.LoanDAO;
import com.library.app.model.Loan;
import com.library.app.service.LoanService;
import com.library.app.util.DateUtil;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LoanPanel extends JPanel implements RefreshablePanel {
    private final LoanService loanService = new LoanService();

    private final JTextField memberCodeField = new JTextField();
    private final JTextField copyCodeField = new JTextField();
    private final JLabel subtitleLabel = new JLabel("Data peminjaman aktif");
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Kode Anggota", "Nama", "Kode Eksemplar", "Judul", "Tanggal Pinjam", "Jatuh Tempo"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable loanTable = new JTable(tableModel);

    public LoanPanel() {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 251));

        add(buildTopSection(), BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);

        configureTable();
        refreshData();
    }

    private JComponent buildTopSection() {
        JPanel wrapper = new JPanel(new BorderLayout(16, 16));
        wrapper.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Peminjaman Buku");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(31, 41, 55));

        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(107, 114, 128));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitleLabel);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel memberLabel = createFieldLabel("Kode Anggota");
        JLabel copyLabel = createFieldLabel("Kode Eksemplar");

        styleTextField(memberCodeField, "Masukkan kode anggota");
        styleTextField(copyCodeField, "Masukkan kode eksemplar");

        JButton borrowButton = new JButton("Pinjam Buku");
        stylePrimaryButton(borrowButton);
        borrowButton.addActionListener(event -> borrowBook());

        gbc.gridx = 0;
        gbc.gridy = 0;
        formCard.add(memberLabel, gbc);

        gbc.gridx = 1;
        formCard.add(copyLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formCard.add(memberCodeField, gbc);

        gbc.gridx = 1;
        formCard.add(copyCodeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formCard.add(borrowButton, gbc);

        wrapper.add(titlePanel, BorderLayout.NORTH);
        wrapper.add(formCard, BorderLayout.CENTER);

        return wrapper;
    }

    private JComponent buildTableSection() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel tableTitle = new JLabel("Daftar Peminjaman Aktif");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableTitle.setForeground(new Color(31, 41, 55));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(4, 4, 12, 4));

        JScrollPane scrollPane = new JScrollPane(loanTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(tableTitle, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void configureTable() {
        loanTable.setRowHeight(34);
        loanTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        loanTable.setSelectionBackground(new Color(232, 240, 254));
        loanTable.setSelectionForeground(new Color(31, 41, 55));
        loanTable.setGridColor(new Color(238, 242, 247));
        loanTable.setShowVerticalLines(false);
        loanTable.setFillsViewportHeight(true);
        loanTable.getTableHeader().setReorderingAllowed(false);
        loanTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        loanTable.getTableHeader().setBackground(new Color(249, 250, 251));
        loanTable.getTableHeader().setForeground(new Color(107, 114, 128));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        loanTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        loanTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        loanTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        loanTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }

    private void styleTextField(JTextField field, String tooltip) {
        field.setPreferredSize(new Dimension(220, 40));
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setToolTipText(tooltip);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(new Color(37, 99, 235));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void borrowBook() {
        try {
            loanService.borrowBook(
                    memberCodeField.getText().trim(),
                    copyCodeField.getText().trim()
            );

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
        LoanDAO access = new LoanDAO();

        for (Loan loan : loans) {
            Object[] loanIdentity = access.getLoanIdentity(loan.getId());
            tableModel.addRow(new Object[]{
                    loanIdentity[0],
                    loanIdentity[1],
                    loanIdentity[2],
                    loanIdentity[3],
                    DateUtil.format(loan.getLoanDate()),
                    DateUtil.format(loan.getDueDate())
            });
        }

        subtitleLabel.setText(loans.size() + " peminjaman aktif");
    }
}