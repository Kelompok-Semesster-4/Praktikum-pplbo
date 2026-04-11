package com.library.app.model;

public class FineRule {
    private int maximumDueDays = 7;
    private long finePerDay = 5000L;

    public FineRule() {}

    public FineRule(int maximumDueDays, long finePerDay) {
        this.maximumDueDays = maximumDueDays;
        this.finePerDay = finePerDay;
    }

    public void setMaximumDueDays(int maximumDueDays) {
        this.maximumDueDays = maximumDueDays;
    }

    public void setFinePerDay(long finePerDay) {
        this.finePerDay = finePerDay;
    }

    public int getMaximumDueDays() {
        return maximumDueDays;
    }

    public long getFinePerDay() {
        return finePerDay;
    }
}
