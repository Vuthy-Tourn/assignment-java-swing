package com.pos.service;

import com.pos.dao.StockDAO;
import com.pos.model.Stock;
import com.pos.model.StockHistory;

import java.util.List;

public class StockService {

    private final StockDAO stockDAO = new StockDAO();

    public List<Stock> getAll() { return stockDAO.findAll(); }
    public List<Stock> getLowStock() { return stockDAO.findLowStock(); }
    public List<StockHistory> getHistory(Long productId) { return stockDAO.findHistory(productId); }

    public void stockIn(Long productId, int qty, String note) {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
        stockDAO.adjustStock(productId, qty, "IN", note);
    }

    public void stockAdjust(Long productId, int newQty, String note) {
        // This sets quantity to newQty by calculating delta
        // We need to fetch current qty first
        List<Stock> all = stockDAO.findAll();
        int currentQty = all.stream()
                .filter(s -> s.getProductId().equals(productId))
                .findFirst()
                .map(Stock::getQuantity)
                .orElse(0);
        int delta = newQty - currentQty;
        stockDAO.adjustStock(productId, delta, "ADJUSTMENT", note);
    }

    public void updateAlertLevel(Long productId, int level) {
        stockDAO.updateLowStockAlert(productId, level);
    }
}
