package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.Book;
import com.library.app.model.BookCatalogItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public void save(Book book) {
        String sql = "INSERT INTO books(isbn, title, author, publisher, publication_year, category, shelf_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, book.getIsbn());
            statement.setString(2, book.getTitle());
            statement.setString(3, book.getAuthor());
            statement.setString(4, book.getPublisher());
            statement.setInt(5, book.getPublicationYear());
            statement.setString(6, book.getCategory());
            statement.setString(7, book.getShelfCode());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getLong(1));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menyimpan buku.", exception);
        }
    }

    public List<BookCatalogItem> searchCatalog(String keyword) {
        List<BookCatalogItem> items = new ArrayList<>();
        String sql = "SELECT b.id, b.isbn, b.title, b.author, b.category, b.shelf_code, b.publication_year, " +
                "COUNT(c.id) AS total_copies, " +
                "SUM(CASE WHEN c.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies " +
                "FROM books b LEFT JOIN book_copies c ON c.book_id = b.id " +
                "WHERE b.title LIKE ? OR b.author LIKE ? OR b.isbn LIKE ? " +
                "GROUP BY b.id, b.isbn, b.title, b.author, b.category, b.shelf_code, b.publication_year " +
                "ORDER BY b.title";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String value = "%" + keyword + "%";
            statement.setString(1, value);
            statement.setString(2, value);
            statement.setString(3, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    BookCatalogItem item = new BookCatalogItem();
                    item.setBookId(resultSet.getLong("id"));
                    item.setIsbn(resultSet.getString("isbn"));
                    item.setTitle(resultSet.getString("title"));
                    item.setAuthor(resultSet.getString("author"));
                    item.setCategory(resultSet.getString("category"));
                    item.setShelfCode(resultSet.getString("shelf_code"));
                    item.setPublicationYear(resultSet.getInt("publication_year"));
                    item.setTotalCopies(resultSet.getInt("total_copies"));
                    item.setAvailableCopies(resultSet.getInt("available_copies"));
                    items.add(item);
                }
            }
            return items;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil katalog buku.", exception);
        }
    }

    public int countBooks() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM books");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung buku.", exception);
        }
    }
}
