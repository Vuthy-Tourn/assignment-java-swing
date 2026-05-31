package com.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    public enum Method { CASH, CARD, QR }

    private Long id;
    private Long orderId;
    private String method;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private LocalDateTime createdAt;

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
