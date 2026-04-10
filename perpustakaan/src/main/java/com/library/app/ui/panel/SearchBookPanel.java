package com.library.app.ui.panel;

import com.library.app.model.BookCatalogItem;
import com.library.app.service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SearchBookPanel extends JPanel implements RefreshablePanel {
    private final BookService bookService = new BookService();
    private final JTextField searchField = new JTextField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ISBN", "Judul", "Penulis", "Kategori", "Rak", "Total", "Tersedia"}, 0);

    public SearchBookPanel() {
        setLayout(new BorderLayout(10, 10));
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createTitledBorder("Pencarian Buku"));
        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(event -> refreshData());
        top.add(searchField, BorderLayout.CENTER);
        top.add(searchButton, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        refreshData();
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
