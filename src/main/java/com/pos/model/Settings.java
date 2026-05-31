package com.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Settings {
    private Long id;
    private String storeName;
    private String storePhone;
    private String storeAddress;
    private String currency;
    private BigDecimal taxPercentage;
    private LocalDateTime updatedAt;

    public Settings() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStorePhone() { return storePhone; }
    public void setStorePhone(String storePhone) { this.storePhone = storePhone; }
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
