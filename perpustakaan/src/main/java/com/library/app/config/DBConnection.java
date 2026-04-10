package com.library.app.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private DBConnection() {
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                    "MySQL JDBC Driver tidak ditemukan. Pastikan dependency mysql-connector-j berhasil diunduh oleh Maven.",
                    exception);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBConfig.jdbcUrl(), DBConfig.USERNAME, DBConfig.PASSWORD);
    }
}
