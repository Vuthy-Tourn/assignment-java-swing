package com.pos.dao;

import com.pos.model.Category;
import com.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching categories", e);
        }
        return list;
    }

    public Category save(Category c) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO categories (name, created_at) VALUES (?, NOW())",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving category", e);
        }
        return c;
    }

    public void update(Category c) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE categories SET name=?, updated_at=NOW() WHERE id=?")) {
            ps.setString(1, c.getName());
            ps.setLong(2, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating category", e);
        }
    }

    public void delete(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category", e);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        return c;
    }
}
