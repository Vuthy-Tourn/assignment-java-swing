package com.pos.dao;

import com.pos.model.User;
import com.pos.model.Role;
import com.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsername(String username) {
        String sql = """
            SELECT u.*, r.name AS role_name
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            WHERE u.username = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
        return null;
    }

    public List<User> findAll() {
        String sql = """
            SELECT u.*, r.name AS role_name
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            ORDER BY u.full_name
            """;
        List<User> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users", e);
        }
        return list;
    }

    public User save(User u) {
        String sql = "INSERT INTO users (username, password, full_name, role_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            if (u.getRoleId() != null) ps.setLong(4, u.getRoleId());
            else ps.setNull(4, Types.BIGINT);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
        return u;
    }

    public void update(User u) {
        String sql = "UPDATE users SET full_name=?, role_id=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            if (u.getRoleId() != null) ps.setLong(2, u.getRoleId());
            else ps.setNull(2, Types.BIGINT);
            ps.setLong(3, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    public void updatePassword(Long userId, String hashedPassword) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET password=?, updated_at=NOW() WHERE id=?")) {
            ps.setString(1, hashedPassword);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password", e);
        }
    }

    public void delete(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public List<Role> findAllRoles() {
        List<Role> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM roles ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Role(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching roles", e);
        }
        return list;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setRoleId(rs.getLong("role_id"));
        u.setRoleName(rs.getString("role_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
