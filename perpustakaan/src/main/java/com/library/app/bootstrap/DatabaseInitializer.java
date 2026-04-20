package com.library.app.bootstrap;

import com.library.app.config.DBConnection;
import com.library.app.dao.UserDAO;
import com.library.app.model.User;
import com.library.app.model.enums.Role;
import com.library.app.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        createTables();
        migrateTables();
        seedDefaultUsers();
    }

    private static void createTables() {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS members (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_code VARCHAR(30) NOT NULL UNIQUE,
                        name VARCHAR(100) NOT NULL,
                        member_type VARCHAR(20) NOT NULL,
                        major VARCHAR(100),
                        phone VARCHAR(30),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS books (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        isbn VARCHAR(30) NOT NULL UNIQUE,
                        title VARCHAR(200) NOT NULL,
                        author VARCHAR(150) NOT NULL,
                        publisher VARCHAR(150),
                        publication_year INT NOT NULL,
                        category VARCHAR(100),
                        shelf_code VARCHAR(30),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS book_copies (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        book_id BIGINT NOT NULL,
                        copy_code VARCHAR(50) NOT NULL UNIQUE,
                        status VARCHAR(20) NOT NULL,
                        FOREIGN KEY (book_id) REFERENCES books(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS visits (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_id BIGINT NULL,
                        visitor_name VARCHAR(100) NOT NULL,
                        visitor_identifier VARCHAR(50),
                        visit_type VARCHAR(20) NOT NULL,
                        visit_status VARCHAR(20) NOT NULL DEFAULT 'SELESAI',
                        institution VARCHAR(150),
                        purpose VARCHAR(255),
                        visit_date DATE NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (member_id) REFERENCES members(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS loans (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_id BIGINT NOT NULL,
                        copy_id BIGINT NOT NULL,
                        loan_date DATE NOT NULL,
                        due_date DATE NOT NULL,
                        return_date DATE NULL,
                        fine_amount DECIMAL(12,2) DEFAULT 0,
                        status VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (member_id) REFERENCES members(id),
                        FOREIGN KEY (copy_id) REFERENCES book_copies(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS feedbacks (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_id BIGINT NULL,
                        sender_name VARCHAR(100) NOT NULL,
                        subject VARCHAR(150) NULL,
                        rating INT NOT NULL DEFAULT 0,
                        message TEXT NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'NEW',
                        response_note TEXT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        responded_at TIMESTAMP NULL,
                        FOREIGN KEY (member_id) REFERENCES members(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS procurement_requests (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_id BIGINT NULL,
                        requester_name VARCHAR(100) NOT NULL,
                        title VARCHAR(200) NOT NULL,
                        author VARCHAR(150),
                        publisher VARCHAR(150),
                        publication_year INT NULL,
                        isbn VARCHAR(30) NULL,
                        note TEXT,
                        status VARCHAR(20) NOT NULL,
                        response_note TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        responded_at TIMESTAMP NULL,
                        FOREIGN KEY (member_id) REFERENCES members(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        notification_key VARCHAR(120) NOT NULL UNIQUE,
                        type VARCHAR(40) NOT NULL,
                        title VARCHAR(200) NOT NULL,
                        message TEXT NOT NULL,
                        target_key VARCHAR(60) NOT NULL,
                        priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
                        is_read BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        read_at TIMESTAMP NULL
                    )
                    """);

        } catch (SQLException exception) {
            throw new IllegalStateException("Gagal membuat tabel database.", exception);
        }
    }

    private static void migrateTables() {
        try (Connection connection = DBConnection.getConnection()) {
            ensureColumnExists(connection, "feedbacks", "subject", "ALTER TABLE feedbacks ADD COLUMN subject VARCHAR(150) NULL AFTER sender_name");
            ensureColumnExists(connection, "feedbacks", "rating", "ALTER TABLE feedbacks ADD COLUMN rating INT NOT NULL DEFAULT 0 AFTER subject");
            ensureColumnExists(connection, "feedbacks", "status", "ALTER TABLE feedbacks ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'NEW' AFTER message");
            ensureColumnExists(connection, "feedbacks", "response_note", "ALTER TABLE feedbacks ADD COLUMN response_note TEXT NULL AFTER status");
            ensureColumnExists(connection, "feedbacks", "responded_at", "ALTER TABLE feedbacks ADD COLUMN responded_at TIMESTAMP NULL AFTER created_at");
            ensureColumnExists(connection, "visits", "visit_status", "ALTER TABLE visits ADD COLUMN visit_status VARCHAR(20) NOT NULL DEFAULT 'SELESAI' AFTER visit_type");
            ensureColumnExists(connection, "procurement_requests", "publisher", "ALTER TABLE procurement_requests ADD COLUMN publisher VARCHAR(150) NULL AFTER author");
            ensureColumnExists(connection, "procurement_requests", "publication_year", "ALTER TABLE procurement_requests ADD COLUMN publication_year INT NULL AFTER publisher");
            ensureColumnExists(connection, "procurement_requests", "isbn", "ALTER TABLE procurement_requests ADD COLUMN isbn VARCHAR(30) NULL AFTER publication_year");
        } catch (SQLException exception) {
            throw new IllegalStateException("Gagal memperbarui struktur tabel database.", exception);
        }
    }

    private static void ensureColumnExists(Connection connection, String tableName, String columnName, String alterSql)
            throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(alterSql);
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    private static void seedDefaultUsers() {
        UserDAO userDAO = new UserDAO();

        if (userDAO.findByUsername("admin").isEmpty()) {
            userDAO.save(new User(null, "admin", PasswordUtil.hash("admin123"), Role.ADMIN));
        }

        if (userDAO.findByUsername("kiosk").isEmpty()) {
            userDAO.save(new User(null, "kiosk", PasswordUtil.hash("kiosk123"), Role.KIOSK));
        }
    }
}