package com.library.app.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.library.app.model.FineRule;

public class FineCalculator {
    public boolean isLate(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate == null) {
            return false;
        }
        return returnDate.isAfter(dueDate);
    }

    public long calculateDaysLate(LocalDate dueDate, LocalDate returnDate) {
        if (!isLate(dueDate, returnDate)) {
            return 0;
        }
        return dueDate.until(returnDate, ChronoUnit.DAYS);
    }

    public BigDecimal calculateTotalFine(long daysLate, FineRule rule) {
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(daysLate * rule.getFinePerDay());
    }

    public BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate, FineRule rule) {
        long daysLate = calculateDaysLate(dueDate, returnDate);
        return calculateTotalFine(daysLate, rule);
    }
}
