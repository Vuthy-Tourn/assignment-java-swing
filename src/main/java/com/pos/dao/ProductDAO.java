package com.pos.dao;

import com.pos.model.Product;
import com.pos.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> findAll() {
        String sql = """
            SELECT p.*, c.name AS category_name, s.name AS supplier_name,
                   st.quantity AS stock_qty
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            LEFT JOIN stocks st ON st.product_id = p.id
            ORDER BY p.name
            """;
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching products", e);
        }
        return list;
    }

    public List<Product> findActive() {
        String sql = """
            SELECT p.*, c.name AS category_name, s.name AS supplier_name,
                   st.quantity AS stock_qty
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            LEFT JOIN stocks st ON st.product_id = p.id
            WHERE p.status = 'ACTIVE'
            ORDER BY p.name
            """;
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching active products", e);
        }
        return list;
    }

    public List<Product> search(String keyword) {
        String sql = """
            SELECT p.*, c.name AS category_name, s.name AS supplier_name,
                   st.quantity AS stock_qty
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            LEFT JOIN stocks st ON st.product_id = p.id
            WHERE p.status = 'ACTIVE'
              AND (p.name LIKE ? OR p.barcode LIKE ? OR c.name LIKE ?)
            ORDER BY p.name
            """;
        List<Product> list = new ArrayList<>();
        String q = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching products", e);
        }
        return list;
    }

    public Product findByBarcode(String barcode) {
        String sql = """
            SELECT p.*, c.name AS category_name, s.name AS supplier_name,
                   st.quantity AS stock_qty
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            LEFT JOIN stocks st ON st.product_id = p.id
            WHERE p.barcode = ? AND p.status = 'ACTIVE'
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by barcode", e);
        }
        return null;
    }

    public Product findById(Long id) {
        String sql = """
            SELECT p.*, c.name AS category_name, s.name AS supplier_name,
                   st.quantity AS stock_qty
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            LEFT JOIN stocks st ON st.product_id = p.id
            WHERE p.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product", e);
        }
        return null;
    }

    public Product save(Product p) {
        String sql = """
            INSERT INTO products (name, barcode, image_url, selling_price, cost_price,
                category_id, supplier_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getBarcode());
            ps.setString(3, p.getImageUrl());
            ps.setBigDecimal(4, p.getSellingPrice());
            ps.setBigDecimal(5, p.getCostPrice());
            setNullableLong(ps, 6, p.getCategoryId());
            setNullableLong(ps, 7, p.getSupplierId());
            ps.setString(8, p.getStatus() != null ? p.getStatus() : "ACTIVE");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getLong(1));
            }
            // Initialize stock record
            initStock(conn, p.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product", e);
        }
        return p;
    }

    public void update(Product p) {
        String sql = """
            UPDATE products SET name=?, barcode=?, image_url=?, selling_price=?,
                cost_price=?, category_id=?, supplier_id=?, status=?, updated_at=NOW()
            WHERE id=?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getBarcode());
            ps.setString(3, p.getImageUrl());
            ps.setBigDecimal(4, p.getSellingPrice());
            ps.setBigDecimal(5, p.getCostPrice());
            setNullableLong(ps, 6, p.getCategoryId());
            setNullableLong(ps, 7, p.getSupplierId());
            ps.setString(8, p.getStatus());
            ps.setLong(9, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
    }

    public void delete(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE products SET status='INACTIVE', updated_at=NOW() WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }

    private void initStock(Connection conn, Long productId) throws SQLException {
        String sql = "INSERT IGNORE INTO stocks (product_id, quantity, low_stock_alert) VALUES (?, 0, 5)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.executeUpdate();
        }
    }

    private void setNullableLong(PreparedStatement ps, int idx, Long val) throws SQLException {
        if (val != null) ps.setLong(idx, val);
        else ps.setNull(idx, Types.BIGINT);
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setBarcode(rs.getString("barcode"));
        p.setImageUrl(rs.getString("image_url"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setCostPrice(rs.getBigDecimal("cost_price"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setSupplierId(rs.getLong("supplier_id"));
        p.setSupplierName(rs.getString("supplier_name"));
        p.setStatus(rs.getString("status"));
        int qty = rs.getInt("stock_qty");
        p.setStockQuantity(rs.wasNull() ? 0 : qty);
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        return p;
    }
}
