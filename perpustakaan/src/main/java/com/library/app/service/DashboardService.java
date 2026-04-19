package com.library.app.service;

import com.library.app.dao.DashboardDAO;
import com.library.app.model.DashboardSummary;
import com.library.app.model.OverdueLoanReportItem;
import com.library.app.model.ReportSummary;

import java.util.LinkedHashMap;
import java.util.List;

public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public DashboardSummary getSummary() {
        return dashboardDAO.getSummary();
    }

    public LinkedHashMap<String, Integer> getMonthlyVisits(int monthCount) {
        return dashboardDAO.findMonthlyVisits(monthCount);
    }

    public LinkedHashMap<String, int[]> getMonthlyLoanReturnTrend(int monthCount) {
        return dashboardDAO.findMonthlyLoanReturnTrend(monthCount);
    }

    public List<String[]> getRecentLoans(int limit) {
        return dashboardDAO.findRecentLoans(limit);
    }

    public List<String[]> getTodayVisits(int limit) {
        return dashboardDAO.findTodayVisits(limit);
    }

    public ReportSummary getReportSummary() {
        return dashboardDAO.getReportSummary();
    }

    public List<OverdueLoanReportItem> getOverdueLoans(int limit) {
        return dashboardDAO.findOverdueLoans(limit);
    }
}
