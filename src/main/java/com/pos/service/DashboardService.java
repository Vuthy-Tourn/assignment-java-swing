package com.pos.service;

import com.pos.dao.DashboardDAO;
import com.pos.model.DashboardStats;

public class DashboardService {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public DashboardStats getStats() {
        return dashboardDAO.getStats();
    }
}

