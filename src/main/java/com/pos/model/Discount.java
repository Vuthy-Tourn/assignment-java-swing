package com.pos.model;

import java.math.BigDecimal;

public class Discount {
    public enum Type { PERCENTAGE, FIXED }

    private Long id;
    private String name;
    private String type;
    private BigDecimal value;
    private boolean active;

    public Discount() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public BigDecimal calculate(BigDecimal totalAmount) {
        if ("PERCENTAGE".equals(type)) {
            return totalAmount.multiply(value).divide(BigDecimal.valueOf(100));
        }
        return value;
    }

    @Override
    public String toString() {
        return name + " (" + ("PERCENTAGE".equals(type) ? value + "%" : "$" + value) + ")";
    }
}
