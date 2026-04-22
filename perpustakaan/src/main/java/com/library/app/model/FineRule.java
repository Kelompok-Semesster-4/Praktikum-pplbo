package com.library.app.model;

public class FineRule {
    private static FineRule instance;
    private int defaultLoanDays;
    private long dailyFine;

    private FineRule() {
        this.defaultLoanDays = 7;
        this.dailyFine = 5000;
    }

    public static FineRule getInstance() {
        if (instance == null) {
            instance = new FineRule();
        }

        return instance;
    }

    public void setMaximumDueDays(int maximumDueDays) {
        this.defaultLoanDays = maximumDueDays;
    }

    public void setFinePerDay(long finePerDay) {
        this.dailyFine = finePerDay;
    }

    public int getMaximumDueDays() {
        return defaultLoanDays;
    }

    public long getFinePerDay() {
        return dailyFine;
    }
}
