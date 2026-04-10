package com.library.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DBConfig {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = DBConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                PROPERTIES.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Gagal membaca file db.properties.", exception);
        }
    }

    private DBConfig() {
    }

    public static final String HOST = property("db.host", "127.0.0.1");
    public static final int PORT = Integer.parseInt(property("db.port", "3306"));
    public static final String DATABASE = property("db.name", "library_management_native_maven");
    public static final String USERNAME = property("db.username", "root");
    public static final String PASSWORD = property("db.password", "");

    public static String jdbcUrl() {
        return String.format(
                "jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta",
                HOST, PORT, DATABASE);
    }

    private static String property(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        String fileValue = PROPERTIES.getProperty(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }
        return defaultValue;
    }
}
