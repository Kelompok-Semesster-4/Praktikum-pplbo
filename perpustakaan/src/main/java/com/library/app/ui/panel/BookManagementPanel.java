package com.library.app.ui.panel;

import com.library.app.model.BookCatalogItem;
import com.library.app.service.BookService;
import com.library.app.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookManagementPanel extends JPanel implements RefreshablePanel {
    private final BookService bookService = new BookService();

    private final JTextField isbnField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField publisherField = new JTextField();
    private final JTextField yearField = new JTextField();
    private final JTextField categoryField = new JTextField();
    private final JTextField shelfField = new JTextField();
    private final JTextField copiesField = new JTextField("1");
    private final JTextField searchField = new JTextField();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ISBN", "Judul", "Penulis", "Kategori", "Rak", "Total", "Tersedia"}, 0);

    public BookManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        add(buildFormPanel(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        refreshData();
    }

    private JComponent buildFormPanel() {
        JPanel container = new JPanel(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Tambah Buku & Eksemplar"));
        form.add(new JLabel("ISBN"));
        form.add(isbnField);
        form.add(new JLabel("Judul"));
        form.add(titleField);
        form.add(new JLabel("Penulis"));
        form.add(authorField);
        form.add(new JLabel("Penerbit"));
        form.add(publisherField);
        form.add(new JLabel("Tahun"));
        form.add(yearField);
        form.add(new JLabel("Kategori"));
        form.add(categoryField);
        form.add(new JLabel("Kode Rak"));
        form.add(shelfField);
        form.add(new JLabel("Jumlah Eksemplar"));
        form.add(copiesField);

        JButton saveButton = new JButton("Simpan Buku");
        saveButton.addActionListener(event -> saveBook());

        JPanel searchPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Cari Buku"));
        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(event -> refreshData());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        container.add(form, BorderLayout.CENTER);
        container.add(saveButton, BorderLayout.EAST);
        container.add(searchPanel, BorderLayout.SOUTH);
        return container;
    }

    private void saveBook() {
        try {
            bookService.addBook(
                    isbnField.getText(),
                    titleField.getText(),
                    authorField.getText(),
                    publisherField.getText(),
                    Integer.parseInt(yearField.getText().trim()),
                    categoryField.getText(),
                    shelfField.getText(),
                    Integer.parseInt(copiesField.getText().trim())
            );
            UiUtil.showInfo(this, "Buku berhasil disimpan.");
            clearForm();
            refreshData();
        } catch (Exception exception) {
            UiUtil.showError(this, exception.getMessage());
        }
    }

    private void clearForm() {
        isbnField.setText("");
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        yearField.setText("");
        categoryField.setText("");
        shelfField.setText("");
        copiesField.setText("1");
    }

    @Override
    public void refreshData() {
        tableModel.setRowCount(0);
        List<BookCatalogItem> items = bookService.searchCatalog(searchField.getText());
        for (BookCatalogItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getIsbn(),
                    item.getTitle(),
                    item.getAuthor(),
                    item.getCategory(),
                    item.getShelfCode(),
                    item.getTotalCopies(),
                    item.getAvailableCopies()
            });
        }
    }
}
