package com.pos.dao;

import com.pos.model.*;
import com.pos.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public Order save(Order order) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert order
                String orderSql = """
                    INSERT INTO orders (user_id, total_amount, discount_amount, discount_id,
                        final_amount, receipt_number, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, NOW())
                    """;
                try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, order.getUserId());
                    ps.setBigDecimal(2, order.getTotalAmount());
                    ps.setBigDecimal(3, order.getDiscountAmount());
                    if (order.getDiscountId() != null) ps.setLong(4, order.getDiscountId());
                    else ps.setNull(4, Types.BIGINT);
                    ps.setBigDecimal(5, order.getFinalAmount());
                    ps.setString(6, order.getReceiptNumber());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) order.setId(keys.getLong(1));
                    }
                }

                // Insert order items
                String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    for (OrderItem item : order.getItems()) {
                        ps.setLong(1, order.getId());
                        ps.setLong(2, item.getProductId());
                        ps.setInt(3, item.getQuantity());
                        ps.setBigDecimal(4, item.getPrice());
                        ps.setBigDecimal(5, item.getSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Insert payment
                if (order.getPayment() != null) {
                    Payment pay = order.getPayment();
                    String paySql = "INSERT INTO payments (order_id, method, paid_amount, change_amount, created_at) VALUES (?, ?, ?, ?, NOW())";
                    try (PreparedStatement ps = conn.prepareStatement(paySql)) {
                        ps.setLong(1, order.getId());
                        ps.setString(2, pay.getMethod());
                        ps.setBigDecimal(3, pay.getPaidAmount());
                        ps.setBigDecimal(4, pay.getChangeAmount());
                        ps.executeUpdate();
                    }
                }

                // Deduct stock
                String stockSql = "UPDATE stocks SET quantity = quantity - ?, updated_at = NOW() WHERE product_id = ?";
                String histSql = "INSERT INTO stock_history (product_id, type, quantity, note, created_at) VALUES (?, 'OUT', ?, ?, NOW())";
                try (PreparedStatement stockPs = conn.prepareStatement(stockSql);
                     PreparedStatement histPs = conn.prepareStatement(histSql)) {
                    for (OrderItem item : order.getItems()) {
                        stockPs.setInt(1, item.getQuantity());
                        stockPs.setLong(2, item.getProductId());
                        stockPs.addBatch();

                        histPs.setLong(1, item.getProductId());
                        histPs.setInt(2, item.getQuantity());
                        histPs.setString(3, "Sale - Receipt #" + order.getReceiptNumber());
                        histPs.addBatch();
                    }
                    stockPs.executeBatch();
                    histPs.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving order", e);
        }
        return order;
    }

    public List<Order> findAll(LocalDate from, LocalDate to) {
        String sql = """
            SELECT o.*, u.full_name AS user_name
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.id
            WHERE DATE(o.created_at) BETWEEN ? AND ?
            ORDER BY o.created_at DESC
            """;
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrderRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching orders", e);
        }
        return list;
    }

    public Order findById(Long id) {
        String sql = """
            SELECT o.*, u.full_name AS user_name
            FROM orders o
            LEFT JOIN users u ON o.user_id = u.id
            WHERE o.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(conn, id));
                    order.setPayment(findPaymentByOrderId(conn, id));
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding order", e);
        }
        return null;
    }

    private List<OrderItem> findItemsByOrderId(Connection conn, Long orderId) throws SQLException {
        String sql = """
            SELECT oi.*, p.name AS product_name
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            WHERE oi.order_id = ?
            """;
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Payment findPaymentByOrderId(Connection conn, Long orderId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM payments WHERE order_id = ?")) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment pay = new Payment();
                    pay.setId(rs.getLong("id"));
                    pay.setOrderId(rs.getLong("order_id"));
                    pay.setMethod(rs.getString("method"));
                    pay.setPaidAmount(rs.getBigDecimal("paid_amount"));
                    pay.setChangeAmount(rs.getBigDecimal("change_amount"));
                    return pay;
                }
            }
        }
        return null;
    }

    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setUserId(rs.getLong("user_id"));
        o.setUserName(rs.getString("user_name"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        o.setFinalAmount(rs.getBigDecimal("final_amount"));
        o.setReceiptNumber(rs.getString("receipt_number"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
        return o;
    }
    public Order findByReceiptNumber(String receiptNumber) {

        String sql = """
        SELECT o.*, u.full_name AS user_name
        FROM orders o
        LEFT JOIN users u
        ON o.user_id = u.id
        WHERE o.receipt_number = ?
        """;

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, receiptNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Order order =
                            mapOrderRow(rs);

                    order.setItems(
                            findItemsByOrderId(
                                    conn,
                                    order.getId()
                            )
                    );

                    order.setPayment(
                            findPaymentByOrderId(
                                    conn,
                                    order.getId()
                            )
                    );

                    return order;
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error finding receipt",
                    e
            );
        }

        return null;
    }
}
