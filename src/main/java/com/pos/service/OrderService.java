package com.pos.service;

import com.pos.dao.DiscountDAO;
import com.pos.dao.OrderDAO;
import com.pos.model.*;
import com.pos.util.AppContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OrderService {

    private final OrderDAO orderDAO = new OrderDAO();
    private final DiscountDAO discountDAO = new DiscountDAO();

    private static final AtomicLong receiptCounter = new AtomicLong(
            System.currentTimeMillis() % 100000
    );

    public Order processOrder(List<OrderItem> items, Discount discount, Payment payment) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Order must have at least one item");
        if (payment == null)
            throw new IllegalArgumentException("Payment is required");

        // Validate stock
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be positive");
        }

        Order order = new Order();
        order.setUserId(AppContext.getCurrentUser().getId());
        order.setItems(items);

        // Calculate totals
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        BigDecimal discountAmt = BigDecimal.ZERO;
        if (discount != null) {
            discountAmt = discount.calculate(total);
            order.setDiscountId(discount.getId());
            order.setDiscountName(discount.getName());
        }
        order.setDiscountAmount(discountAmt);

        BigDecimal finalAmount = total.subtract(discountAmt);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) finalAmount = BigDecimal.ZERO;
        order.setFinalAmount(finalAmount);

        // Validate payment
        if ("CASH".equals(payment.getMethod()) &&
                payment.getPaidAmount().compareTo(finalAmount) < 0) {
            throw new IllegalArgumentException("Paid amount is insufficient");
        }

        BigDecimal change = payment.getPaidAmount().subtract(finalAmount);
        if (change.compareTo(BigDecimal.ZERO) < 0) change = BigDecimal.ZERO;
        payment.setChangeAmount(change);

        order.setPayment(payment);
        order.setReceiptNumber(generateReceiptNumber());

        return orderDAO.save(order);
    }

    public List<Order> getOrdersByDateRange(LocalDate from, LocalDate to) {
        return orderDAO.findAll(from, to);
    }

    public Order findById(Long id) {
        return orderDAO.findById(id);
    }

    public List<Discount> getActiveDiscounts() {
        return discountDAO.findActive();
    }

    private String generateReceiptNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "RCP-" + date + "-" + String.format("%05d", receiptCounter.incrementAndGet());
    }
    public Order findByReceiptNumber(String receiptNumber) {
        return orderDAO.findByReceiptNumber(receiptNumber);
    }
}
