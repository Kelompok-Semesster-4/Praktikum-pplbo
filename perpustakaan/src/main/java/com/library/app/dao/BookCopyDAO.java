package com.library.app.dao;

import com.library.app.config.DBConnection;
import com.library.app.model.BookCopy;
import com.library.app.model.enums.CopyStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookCopyDAO {
    public void saveCopies(long bookId, int totalCopies) {
        String sql = "INSERT INTO book_copies(book_id, copy_code, status) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 1; index <= totalCopies; index++) {
                statement.setLong(1, bookId);
                statement.setString(2, generateCopyCode(bookId, index));
                statement.setString(3, CopyStatus.AVAILABLE.name());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal membuat eksemplar buku.", exception);
        }
    }

    public Optional<BookCopy> findByCopyCode(String copyCode) {
        String sql = "SELECT id, book_id, copy_code, status FROM book_copies WHERE copy_code = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, copyCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil eksemplar buku.", exception);
        }
    }

    public void updateStatus(long copyId, CopyStatus status) {
        String sql = "UPDATE book_copies SET status = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, copyId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal memperbarui status eksemplar.", exception);
        }
    }

    public int countAll() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM book_copies");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung eksemplar buku.", exception);
        }
    }

    public int countAvailable() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM book_copies WHERE status = 'AVAILABLE'");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghitung stok tersedia.", exception);
        }
    }

    public List<BookCopy> findAllByBookId(long bookId) {
        List<BookCopy> copies = new ArrayList<>();
        String sql = "SELECT id, book_id, copy_code, status FROM book_copies WHERE book_id = ? ORDER BY copy_code";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    copies.add(map(resultSet));
                }
            }
            return copies;
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil daftar eksemplar.", exception);
        }
    }

    public Optional<String> findFirstAvailableCopyCode(long bookId) {
        String sql = "SELECT copy_code FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' ORDER BY copy_code LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString("copy_code"));
                }
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal mengambil eksemplar tersedia.", exception);
        }
    }

    public void deleteByBookId(long bookId) {
        String sql = "DELETE FROM book_copies WHERE book_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, bookId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Gagal menghapus eksemplar buku.", exception);
        }
    }

    private String generateCopyCode(long bookId, int index) {
        return String.format("CPY-%05d-%03d", bookId, index);
    }

    private BookCopy map(ResultSet resultSet) throws SQLException {
        return new BookCopy(
                resultSet.getLong("id"),
                resultSet.getLong("book_id"),
                resultSet.getString("copy_code"),
                CopyStatus.valueOf(resultSet.getString("status"))
        );
    }
}
