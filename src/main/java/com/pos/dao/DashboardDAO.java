package com.pos.dao;

import com.pos.model.DashboardStats;
import com.pos.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAO {

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();

        try (Connection conn = DatabaseConnection.getConnection()) {
            stats.setTotalProducts(count(conn,
                    "SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'"));

            stats.setTotalOrders(count(conn,
                    "SELECT COUNT(*) FROM orders"));

            stats.setLowStockProducts(count(conn,
                    "SELECT COUNT(*) FROM stocks WHERE quantity <= low_stock_alert"));

            stats.setTodayRevenue(sum(conn,
                    "SELECT COALESCE(SUM(final_amount), 0) FROM orders WHERE DATE(created_at) = CURDATE()"));

            stats.setRevenueByDay(getRevenueByDay(conn));
            stats.setProductsByCategory(getProductsByCategory(conn));

        } catch (SQLException e) {
            throw new RuntimeException("Error loading dashboard stats", e);
        }

        return stats;
    }

    private int count(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private BigDecimal sum(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    private Map<String, BigDecimal> getRevenueByDay(Connection conn) throws SQLException {
        String sql = """
            SELECT DATE(created_at) AS order_date,
                   COALESCE(SUM(final_amount), 0) AS revenue
            FROM orders
            WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
            GROUP BY DATE(created_at)
            ORDER BY order_date
            """;

        Map<String, BigDecimal> map = new LinkedHashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("order_date"), rs.getBigDecimal("revenue"));
            }
        }

        return map;
    }

    private Map<String, Integer> getProductsByCategory(Connection conn) throws SQLException {
        String sql = """
            SELECT COALESCE(c.name, 'Uncategorized') AS category_name,
                   COUNT(p.id) AS total
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.status = 'ACTIVE'
            GROUP BY COALESCE(c.name, 'Uncategorized')
            ORDER BY total DESC
            """;

        Map<String, Integer> map = new LinkedHashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("category_name"), rs.getInt("total"));
            }
        }

        return map;
    }
}
