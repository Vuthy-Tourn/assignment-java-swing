package com.pos.model;

import java.math.BigDecimal;

public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public OrderItem() {}

    public OrderItem(Product product, int quantity) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.price = product.getSellingPrice();
        this.quantity = quantity;
        this.subtotal = product.getSellingPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public void recalculate() {
        if (price != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
