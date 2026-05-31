package com.pos.model;

import java.time.LocalDateTime;

public class Stock {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private int lowStockAlert;
    private LocalDateTime updatedAt;

    public Stock() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getLowStockAlert() { return lowStockAlert; }
    public void setLowStockAlert(int lowStockAlert) { this.lowStockAlert = lowStockAlert; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isLowStock() { return quantity <= lowStockAlert; }
}
