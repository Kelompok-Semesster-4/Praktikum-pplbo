package com.library.app.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.library.app.model.FineRule;

public final class DateUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private DateUtil() {
    }

    public static String format(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FORMATTER);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "-" : dateTime.format(DATE_TIME_FORMATTER);
    }

    public static boolean isLoanLate(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate == null) {
            return false;
        }
        return returnDate.isAfter(dueDate);
    }

    public static long calculateDaysDifference(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return startDate.until(endDate, ChronoUnit.DAYS);
    }

    public static LocalDate calculateDueDate(LocalDate borrowDate, FineRule rule) {
        return borrowDate.plusDays(rule.getMaximumDueDays());
    }
}