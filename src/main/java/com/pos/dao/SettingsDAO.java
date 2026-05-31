package com.pos.dao;

import com.pos.model.Settings;
import com.pos.util.DatabaseConnection;

import java.sql.*;

public class SettingsDAO {

    public Settings get() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM settings LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching settings", e);
        }
        // Return defaults if no settings row exists
        Settings defaults = new Settings();
        defaults.setStoreName("My Store");
        defaults.setCurrency("USD");
        defaults.setTaxPercentage(java.math.BigDecimal.ZERO);
        return defaults;
    }

    public void save(Settings s) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Upsert: delete then insert
            conn.prepareStatement("DELETE FROM settings").executeUpdate();
            String sql = "INSERT INTO settings (store_name, store_phone, store_address, currency, tax_percentage, updated_at) VALUES (?, ?, ?, ?, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, s.getStoreName());
                ps.setString(2, s.getStorePhone());
                ps.setString(3, s.getStoreAddress());
                ps.setString(4, s.getCurrency());
                ps.setBigDecimal(5, s.getTaxPercentage());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving settings", e);
        }
    }

    private Settings mapRow(ResultSet rs) throws SQLException {
        Settings s = new Settings();
        s.setId(rs.getLong("id"));
        s.setStoreName(rs.getString("store_name"));
        s.setStorePhone(rs.getString("store_phone"));
        s.setStoreAddress(rs.getString("store_address"));
        s.setCurrency(rs.getString("currency"));
        s.setTaxPercentage(rs.getBigDecimal("tax_percentage"));
        return s;
    }
}
