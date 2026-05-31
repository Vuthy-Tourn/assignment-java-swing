package com.pos.dao;

import com.pos.model.Stock;
import com.pos.model.StockHistory;
import com.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public List<Stock> findAll() {
        String sql = """
            SELECT s.*, p.name AS product_name
            FROM stocks s
            JOIN products p ON s.product_id = p.id
            WHERE p.status = 'ACTIVE'
            ORDER BY p.name
            """;
        List<Stock> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching stocks", e);
        }
        return list;
    }

    public List<Stock> findLowStock() {
        String sql = """
            SELECT s.*, p.name AS product_name
            FROM stocks s
            JOIN products p ON s.product_id = p.id
            WHERE p.status = 'ACTIVE' AND s.quantity <= s.low_stock_alert
            ORDER BY s.quantity ASC
            """;
        List<Stock> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching low stocks", e);
        }
        return list;
    }

    public void adjustStock(Long productId, int delta, String type, String note) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update stock quantity
                String updateSql = "UPDATE stocks SET quantity = quantity + ?, updated_at = NOW() WHERE product_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, delta);
                    ps.setLong(2, productId);
                    ps.executeUpdate();
                }
                // Log history
                String histSql = "INSERT INTO stock_history (product_id, type, quantity, note, created_at) VALUES (?, ?, ?, ?, NOW())";
                try (PreparedStatement ps = conn.prepareStatement(histSql)) {
                    ps.setLong(1, productId);
                    ps.setString(2, type);
                    ps.setInt(3, Math.abs(delta));
                    ps.setString(4, note);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adjusting stock", e);
        }
    }

    public void updateLowStockAlert(Long productId, int alertLevel) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE stocks SET low_stock_alert=? WHERE product_id=?")) {
            ps.setInt(1, alertLevel);
            ps.setLong(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating alert level", e);
        }
    }

    public List<StockHistory> findHistory(Long productId) {
        String sql = """
            SELECT sh.*, p.name AS product_name
            FROM stock_history sh
            JOIN products p ON sh.product_id = p.id
            WHERE sh.product_id = ?
            ORDER BY sh.created_at DESC
            LIMIT 100
            """;
        List<StockHistory> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapHistoryRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching stock history", e);
        }
        return list;
    }

    private Stock mapRow(ResultSet rs) throws SQLException {
        Stock s = new Stock();
        s.setId(rs.getLong("id"));
        s.setProductId(rs.getLong("product_id"));
        s.setProductName(rs.getString("product_name"));
        s.setQuantity(rs.getInt("quantity"));
        s.setLowStockAlert(rs.getInt("low_stock_alert"));
        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) s.setUpdatedAt(ts.toLocalDateTime());
        return s;
    }

    private StockHistory mapHistoryRow(ResultSet rs) throws SQLException {
        StockHistory h = new StockHistory();
        h.setId(rs.getLong("id"));
        h.setProductId(rs.getLong("product_id"));
        h.setProductName(rs.getString("product_name"));
        h.setType(rs.getString("type"));
        h.setQuantity(rs.getInt("quantity"));
        h.setNote(rs.getString("note"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) h.setCreatedAt(ts.toLocalDateTime());
        return h;
    }
}
