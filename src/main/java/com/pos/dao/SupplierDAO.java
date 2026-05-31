package com.pos.dao;

import com.pos.model.Supplier;
import com.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM suppliers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching suppliers", e);
        }
        return list;
    }

    public Supplier save(Supplier s) {
        String sql = "INSERT INTO suppliers (name, phone, address, created_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving supplier", e);
        }
        return s;
    }

    public void update(Supplier s) {
        String sql = "UPDATE suppliers SET name=?, phone=?, address=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getAddress());
            ps.setLong(4, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating supplier", e);
        }
    }

    public void delete(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM suppliers WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting supplier", e);
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        return s;
    }
}
