package com.pos.service;

import com.pos.dao.CategoryDAO;
import com.pos.dao.ProductDAO;
import com.pos.dao.SupplierDAO;
import com.pos.model.Category;
import com.pos.model.Product;
import com.pos.model.Supplier;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<Product> getAll() { return productDAO.findAll(); }
    public List<Product> getActive() { return productDAO.findActive(); }
    public List<Product> search(String keyword) { return productDAO.search(keyword); }
    public Product findByBarcode(String barcode) { return productDAO.findByBarcode(barcode); }
    public Product findById(Long id) { return productDAO.findById(id); }

    public Product save(Product product) {
        validate(product);
        return productDAO.save(product);
    }

    public void update(Product product) {
        validate(product);
        productDAO.update(product);
    }

    public void delete(Long id) { productDAO.delete(id); }

    public List<Category> getCategories() { return categoryDAO.findAll(); }
    public List<Supplier> getSuppliers() { return supplierDAO.findAll(); }

    public Category saveCategory(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required");
        return categoryDAO.save(new Category(name.trim()));
    }

    private void validate(Product p) {
        if (p.getName() == null || p.getName().isBlank())
            throw new IllegalArgumentException("Product name is required");
        if (p.getSellingPrice() == null || p.getSellingPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Selling price must be >= 0");
        if (p.getCostPrice() == null || p.getCostPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Cost price must be >= 0");
    }
}
