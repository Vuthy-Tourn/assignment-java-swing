package com.pos.ui.panels;

import com.pos.model.*;
import com.pos.service.OrderService;
import com.pos.service.ProductService;
import com.pos.ui.MainFrame;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.StyledTable;
import com.pos.ui.components.UIConstants;
import com.pos.ui.dialogs.PaymentDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SalesPanel extends JPanel {

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();

    private final List<OrderItem> cartItems = new ArrayList<>();

    private JTextField searchField;
    private JTextField barcodeField;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private DefaultListModel<Product> productListModel;
    private JList<Product> productList;

    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;

    private Discount selectedDiscount;
    private JComboBox<Discount> discountCombo;

    public SalesPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        buildUI();
    }

    private void buildUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel title = new JLabel("Sales / Checkout");
        title.setFont(UIConstants.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        // Barcode entry on top bar
        JPanel barcodePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barcodePanel.setOpaque(false);
        barcodeField = new JTextField(16);
        barcodeField.setFont(UIConstants.FONT_BODY);
        barcodeField.setToolTipText("Scan barcode or type and press Enter");
        JLabel barcodeLabel = new JLabel("Barcode:");
        barcodeLabel.setFont(UIConstants.FONT_BODY);
        barcodePanel.add(barcodeLabel);
        barcodePanel.add(barcodeField);
        topBar.add(barcodePanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        barcodeField.addActionListener(e -> scanBarcode());

        // Main content: left = product browser, right = cart
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(420);
        split.setDividerSize(4);
        split.setBorder(null);

        split.setLeftComponent(buildProductBrowser());
        split.setRightComponent(buildCartPanel());

        add(split, BorderLayout.CENTER);
    }

    private JPanel buildProductBrowser() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 8));

        // Search
        searchField = new JTextField();
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "Search products...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
        });
        panel.add(searchField, BorderLayout.NORTH);

        // Product list
        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setFont(UIConstants.FONT_BODY);
        productList.setFixedCellHeight(48);
        productList.setCellRenderer(new ProductCellRenderer());
        productList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) addToCartSelected();
            }
        });

        JScrollPane scroll = new JScrollPane(productList);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        RoundedButton addBtn = new RoundedButton("Add to Cart", RoundedButton.Style.SUCCESS);
        addBtn.addActionListener(e -> addToCartSelected());
        panel.add(addBtn, BorderLayout.SOUTH);

        loadProducts();
        return panel;
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 12));

        JLabel cartTitle = new JLabel("Cart");
        cartTitle.setFont(UIConstants.FONT_HEADING);
        panel.add(cartTitle, BorderLayout.NORTH);

        // Cart table
        String[] cols = {"Product", "Qty", "Price", "Subtotal", ""};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 1; }
        };
        cartTable = new StyledTable(cartModel);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        cartTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 1) updateQty(e.getFirstRow());
        });

        // Remove button in last column
        cartTable.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JButton btn = new JButton("×");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setForeground(UIConstants.DANGER);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            return btn;
        });
        cartTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = cartTable.columnAtPoint(e.getPoint());
                int row = cartTable.rowAtPoint(e.getPoint());
                if (col == 4 && row >= 0) removeFromCart(row);
            }
        });

        panel.add(StyledTable.inScrollPane((StyledTable) cartTable), BorderLayout.CENTER);

        // Totals + discount
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);

        // Discount
        JPanel discountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        discountPanel.setOpaque(false);
        discountPanel.add(new JLabel("Discount:  "));
        discountCombo = new JComboBox<>();
        discountCombo.setFont(UIConstants.FONT_BODY);
        discountCombo.setPreferredSize(new Dimension(200, 30));
        discountCombo.addItem(null); // No discount
        orderService.getActiveDiscounts().forEach(discountCombo::addItem);
        discountCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value == null ? "— No discount —" : value.toString());
            lbl.setFont(UIConstants.FONT_BODY);
            return lbl;
        });
        discountCombo.addActionListener(e -> {
            selectedDiscount = (Discount) discountCombo.getSelectedItem();
            updateTotals();
        });
        discountPanel.add(discountCombo);
        bottomPanel.add(discountPanel, BorderLayout.NORTH);

        // Summary
        JPanel summary = new JPanel(new GridLayout(3, 2, 8, 4));
        summary.setOpaque(false);
        summary.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        summary.add(label("Subtotal:"));   subtotalLabel = valueLabel("$0.00"); summary.add(subtotalLabel);
        summary.add(label("Discount:"));   discountLabel = valueLabel("$0.00"); summary.add(discountLabel);
        summary.add(totalLbl("Total:"));   totalLabel    = totalValLbl("$0.00"); summary.add(totalLabel);
        bottomPanel.add(summary, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setOpaque(false);
        RoundedButton clearBtn = new RoundedButton("Clear", RoundedButton.Style.SECONDARY);
        clearBtn.addActionListener(e -> clearCart());
        RoundedButton checkoutBtn = new RoundedButton("Checkout", RoundedButton.Style.SUCCESS);
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutBtn.addActionListener(e -> checkout());
        btnPanel.add(clearBtn);
        btnPanel.add(checkoutBtn);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_BODY);
        l.setForeground(UIConstants.TEXT_MUTED);
        return l;
    }

    private JLabel valueLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(UIConstants.FONT_BODY);
        return l;
    }

    private JLabel totalLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_HEADING);
        return l;
    }

    private JLabel totalValLbl(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(UIConstants.ACCENT);
        return l;
    }

    private void loadProducts() {
        productListModel.clear();
        productService.getActive().forEach(productListModel::addElement);
    }

    private void filterProducts() {
        String keyword = searchField.getText().trim();
        productListModel.clear();
        List<Product> products = keyword.isEmpty()
                ? productService.getActive()
                : productService.search(keyword);
        products.forEach(productListModel::addElement);
    }

    private void scanBarcode() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) return;
        Product p = productService.findByBarcode(barcode);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Product not found: " + barcode);
        } else {
            addToCart(p);
        }
        barcodeField.setText("");
        barcodeField.requestFocus();
    }

    private void addToCartSelected() {
        Product p = productList.getSelectedValue();
        if (p != null) addToCart(p);
    }

    private void addToCart(Product product) {
        if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
            JOptionPane.showMessageDialog(this, "Product is out of stock!", "Stock Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Check if already in cart
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProductId().equals(product.getId())) {
                int qty = cartItems.get(i).getQuantity() + 1;
                cartItems.get(i).setQuantity(qty);
                cartItems.get(i).recalculate();
                refreshCartTable();
                return;
            }
        }
        cartItems.add(new OrderItem(product, 1));
        refreshCartTable();
    }

    private void removeFromCart(int row) {
        if (row >= 0 && row < cartItems.size()) {
            cartItems.remove(row);
            refreshCartTable();
        }
    }

    private void updateQty(int row) {
        if (row < 0 || row >= cartItems.size()) return;
        Object val = cartModel.getValueAt(row, 1);
        try {
            int qty = Integer.parseInt(val.toString().trim());
            if (qty <= 0) {
                removeFromCart(row);
            } else {
                cartItems.get(row).setQuantity(qty);
                cartItems.get(row).recalculate();
                refreshCartTable();
            }
        } catch (NumberFormatException e) {
            refreshCartTable();
        }
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        for (OrderItem item : cartItems) {
            cartModel.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    String.format("$%.2f", item.getPrice()),
                    String.format("$%.2f", item.getSubtotal()),
                    "×"
            });
        }
        updateTotals();
    }

    private void updateTotals() {
        BigDecimal subtotal = cartItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = selectedDiscount != null
                ? selectedDiscount.calculate(subtotal) : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        discountLabel.setText(String.format("-$%.2f", discount));
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void clearCart() {
        cartItems.clear();
        selectedDiscount = null;
        discountCombo.setSelectedIndex(0);
        refreshCartTable();
    }

    private void checkout() {

        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty");
            return;
        }

        BigDecimal subtotal = cartItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = selectedDiscount != null
                ? selectedDiscount.calculate(subtotal)
                : BigDecimal.ZERO;

        BigDecimal finalAmount = subtotal.subtract(discount);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        PaymentDialog dialog = new PaymentDialog(
                SwingUtilities.getWindowAncestor(this),
                finalAmount,
                cartItems,
                selectedDiscount,
                orderService
        );

        dialog.setVisible(true);

        // payment success
        if (dialog.isCompleted()) {

            try {

                // load saved order from DB
                Order savedOrder =
                        orderService.findByReceiptNumber(
                                dialog.getReceiptNumber()
                        );

                // open receipt panel
                Window window =
                        SwingUtilities.getWindowAncestor(this);

                if (window instanceof MainFrame frame
                        && savedOrder != null) {

                    frame.openReceipt(savedOrder);
                }

                clearCart();
                loadProducts();

            } catch (Exception ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load receipt: "
                                + ex.getMessage()
                );
            }
        }
    }

    // Cell renderer for product list
    private static class ProductCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Product p) {
                JPanel cell = new JPanel(new BorderLayout(8, 0));
                cell.setBackground(isSelected ? new Color(219, 234, 254) : Color.WHITE);
                cell.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

                JLabel name = new JLabel(p.getName());
                name.setFont(UIConstants.FONT_BODY);

                JPanel right = new JPanel(new GridLayout(2, 1));
                right.setOpaque(false);
                JLabel price = new JLabel("$" + p.getSellingPrice(), SwingConstants.RIGHT);
                price.setFont(UIConstants.FONT_HEADING);
                price.setForeground(UIConstants.ACCENT);
                int qty = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                JLabel stock = new JLabel("Stock: " + qty, SwingConstants.RIGHT);
                stock.setFont(UIConstants.FONT_SMALL);
                stock.setForeground(qty <= 5 ? UIConstants.DANGER : UIConstants.TEXT_MUTED);
                right.add(price);
                right.add(stock);

                cell.add(name, BorderLayout.CENTER);
                cell.add(right, BorderLayout.EAST);
                return cell;
            }
            return this;
        }
    }
}
