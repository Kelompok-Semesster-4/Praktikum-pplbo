package com.library.app.util;

import java.time.Year;

public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static void requireNotBlank(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireNumeric(String value, String message) {
        requireNotBlank(value, message);
        if (!value.matches("\\d+")) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireStudentOrLecturerCode(String value, String message) {
        requireNumeric(value, message);
        if (value.length() < 5 || value.length() > 20) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requirePublicationYear(int year) {
        int currentYear = Year.now().getValue();
        if (year < 1900 || year > currentYear + 1) {
            throw new IllegalArgumentException("Tahun terbit tidak valid.");
        }
    }

    public static void requirePositive(int number, String message) {
        if (number <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
