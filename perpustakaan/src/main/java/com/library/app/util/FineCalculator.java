package com.library.app.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.library.app.model.FineRule;

public class FineCalculator {
    public static BigDecimal calculateTotalFine(long daysLate) {
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(daysLate * FineRule.getInstance().getFinePerDay());
    }

    public static BigDecimal processFine(LocalDate dueDate, LocalDate returnDate) {
        if (!DateUtil.isOverdue(dueDate, returnDate)) {
            return BigDecimal.ZERO;
        }

        long daysLate = DateUtil.calculateDaysDifference(dueDate, returnDate);
        return calculateTotalFine(daysLate);
    }
}
