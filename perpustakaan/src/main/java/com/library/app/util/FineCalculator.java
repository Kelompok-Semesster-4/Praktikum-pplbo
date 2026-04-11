package com.library.app.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.library.app.model.FineRule;

public class FineCalculator {
    public BigDecimal calculateTotalFine(long daysLate, FineRule rule) {
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(daysLate * rule.getFinePerDay());
    }

    public BigDecimal processCalculate(LocalDate dueDate, LocalDate returnDate, FineRule rule) {
        if (!DateUtil.isLoanLate(dueDate, returnDate)) {
            return BigDecimal.ZERO;
        }

        long daysLate = DateUtil.calculateDaysDifference(dueDate, returnDate);
        return calculateTotalFine(daysLate, rule);
    }
}
