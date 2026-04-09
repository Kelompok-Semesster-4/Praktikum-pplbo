package com.library.app.service;

import com.library.app.dao.DashboardDAO;
import com.library.app.model.DashboardSummary;

public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public DashboardSummary getSummary() {
        return dashboardDAO.getSummary();
    }
}
