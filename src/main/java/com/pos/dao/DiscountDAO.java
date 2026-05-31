package com.pos.dao;

import com.pos.model.Discount;
import com.pos.model.Settings;
import com.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiscountDAO {

    public List<Discount> findActive() {
        List<Discount> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM discounts WHERE active = true ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching discounts", e);
        }
        return list;
    }

    public List<Discount> findAll() {
        List<Discount> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM discounts ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching discounts", e);
        }
        return list;
    }

    public Discount save(Discount d) {
        String sql = "INSERT INTO discounts (name, type, value, active) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getType());
            ps.setBigDecimal(3, d.getValue());
            ps.setBoolean(4, d.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving discount", e);
        }
        return d;
    }

    public void update(Discount d) {
        String sql = "UPDATE discounts SET name=?, type=?, value=?, active=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getType());
            ps.setBigDecimal(3, d.getValue());
            ps.setBoolean(4, d.isActive());
            ps.setLong(5, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating discount", e);
        }
    }

    private Discount mapRow(ResultSet rs) throws SQLException {
        Discount d = new Discount();
        d.setId(rs.getLong("id"));
        d.setName(rs.getString("name"));
        d.setType(rs.getString("type"));
        d.setValue(rs.getBigDecimal("value"));
        d.setActive(rs.getBoolean("active"));
        return d;
    }
}
