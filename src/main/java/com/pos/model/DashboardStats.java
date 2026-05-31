package com.pos.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    private int totalProducts;
    private int totalOrders;
    private int lowStockProducts;
    private BigDecimal todayRevenue = BigDecimal.ZERO;

    private Map<String, BigDecimal> revenueByDay = new LinkedHashMap<>();
    private Map<String, Integer> productsByCategory = new LinkedHashMap<>();

    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(int lowStockProducts) { this.lowStockProducts = lowStockProducts; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public Map<String, BigDecimal> getRevenueByDay() { return revenueByDay; }
    public void setRevenueByDay(Map<String, BigDecimal> revenueByDay) { this.revenueByDay = revenueByDay; }

    public Map<String, Integer> getProductsByCategory() { return productsByCategory; }
    public void setProductsByCategory(Map<String, Integer> productsByCategory) { this.productsByCategory = productsByCategory; }
}
