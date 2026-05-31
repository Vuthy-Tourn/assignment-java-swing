package com.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private Long discountId;
    private String discountName;
    private BigDecimal finalAmount;
    private String receiptNumber;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();
    private Payment payment;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public Long getDiscountId() { return discountId; }
    public void setDiscountId(Long discountId) { this.discountId = discountId; }
    public String getDiscountName() { return discountName; }
    public void setDiscountName(String discountName) { this.discountName = discountName; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public void addItem(OrderItem item) { this.items.add(item); }
}
