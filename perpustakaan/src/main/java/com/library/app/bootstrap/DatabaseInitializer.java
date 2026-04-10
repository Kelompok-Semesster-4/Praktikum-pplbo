package com.library.app.bootstrap;

import com.library.app.config.DBConnection;
import com.library.app.dao.UserDAO;
import com.library.app.model.User;
import com.library.app.model.enums.Role;
import com.library.app.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initialize() {
        createTables();
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
                        message TEXT NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
                        note TEXT,
                        status VARCHAR(20) NOT NULL,
                        response_note TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        responded_at TIMESTAMP NULL,
                        FOREIGN KEY (member_id) REFERENCES members(id)
                    )
                    """);

        } catch (SQLException exception) {
            throw new IllegalStateException("Gagal membuat tabel database.", exception);
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
