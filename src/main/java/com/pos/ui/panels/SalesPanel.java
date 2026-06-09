package com.pos.ui.panels;

import com.pos.model.*;

import com.pos.service.OrderService;
import com.pos.service.ProductService;
import com.pos.service.SettingsService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.StyledTable;
import com.pos.ui.components.UIConstants;
import com.pos.ui.dialogs.PaymentDialog;
import java.math.RoundingMode;
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
    private JLabel taxLabel;
    private JLabel totalLabel;

    private Discount selectedDiscount;
    private JComboBox<Discount> discountCombo;

    public SalesPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIConstants.CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        buildUI();
    }

    private void buildUI() {
        add(buildTopBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(430);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setOpaque(false);

        split.setLeftComponent(buildProductBrowser());
        split.setRightComponent(buildCartPanel());

        add(split, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setBackground(UIConstants.CARD_BG);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Sales / Checkout");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Scan barcode, add products, and checkout orders");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(UIConstants.TEXT_MUTED);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel barcodePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barcodePanel.setOpaque(false);

        JLabel barcodeLabel = new JLabel("Barcode");
        barcodeLabel.setFont(UIConstants.FONT_BODY);
        barcodeLabel.setForeground(UIConstants.TEXT_SECONDARY);

        barcodeField = new JTextField(18);
        styleTextField(barcodeField, "Scan barcode or press Enter");
        barcodeField.addActionListener(e -> scanBarcode());

        barcodePanel.add(barcodeLabel);
        barcodePanel.add(barcodeField);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(barcodePanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel buildProductBrowser() {
        JPanel panel = createCardPanel(new BorderLayout(0, 12));

        JLabel title = new JLabel("Products");
        title.setFont(UIConstants.FONT_HEADING);
        title.setForeground(UIConstants.TEXT_PRIMARY);

        searchField = new JTextField();
        styleTextField(searchField, "Search products...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
        });

        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(searchField, BorderLayout.CENTER);

        panel.add(header, BorderLayout.NORTH);

        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setFont(UIConstants.FONT_BODY);
        productList.setFixedCellHeight(96);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.setCellRenderer(new ProductCellRenderer());
        productList.setBackground(UIConstants.CARD_BG);
        productList.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        productList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) addToCartSelected();
            }
        });

        JScrollPane scroll = new JScrollPane(productList);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(UIConstants.CARD_BG);

        panel.add(scroll, BorderLayout.CENTER);

        RoundedButton addBtn = new RoundedButton("Add to Cart", RoundedButton.Style.SUCCESS);
        addBtn.setPreferredSize(new Dimension(0, 42));
        addBtn.addActionListener(e -> addToCartSelected());

        panel.add(addBtn, BorderLayout.SOUTH);

        loadProducts();
        return panel;
    }

    private JPanel buildCartPanel() {
        JPanel panel = createCardPanel(new BorderLayout(0, 12));

        JLabel cartTitle = new JLabel("Cart");
        cartTitle.setFont(UIConstants.FONT_HEADING);
        cartTitle.setForeground(UIConstants.TEXT_PRIMARY);

        panel.add(cartTitle, BorderLayout.NORTH);

        String[] cols = {"Product", "Qty", "Price", "Subtotal", ""};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }
        };

        cartTable = new StyledTable(cartModel);
        cartTable.setRowHeight(38);
        cartTable.setFont(UIConstants.FONT_BODY);
        cartTable.getTableHeader().setFont(UIConstants.FONT_SUBHEADING);

        cartTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(55);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(35);

        cartTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 1) updateQty(e.getFirstRow());
        });

        cartTable.getColumnModel().getColumn(4).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JButton btn = new JButton("×");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btn.setForeground(UIConstants.DANGER);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
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
        panel.add(buildBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 12));
        bottomPanel.setOpaque(false);

        JPanel discountPanel = new JPanel(new BorderLayout(8, 0));
        discountPanel.setOpaque(false);

        JLabel discountLabelText = new JLabel("Discount");
        discountLabelText.setFont(UIConstants.FONT_BODY);
        discountLabelText.setForeground(UIConstants.TEXT_SECONDARY);

        discountCombo = new JComboBox<>();
        discountCombo.setFont(UIConstants.FONT_BODY);
        discountCombo.setPreferredSize(new Dimension(220, 36));
        discountCombo.addItem(null);

        orderService.getActiveDiscounts().forEach(discountCombo::addItem);

        discountCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value == null ? "— No discount —" : value.toString());
            lbl.setFont(UIConstants.FONT_BODY);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            return lbl;
        });

        discountCombo.addActionListener(e -> {
            selectedDiscount = (Discount) discountCombo.getSelectedItem();
            updateTotals();
        });

        discountPanel.add(discountLabelText, BorderLayout.WEST);
        discountPanel.add(discountCombo, BorderLayout.EAST);

        JPanel summary = new JPanel(new GridLayout(3, 2, 10, 8));
        summary.setOpaque(false);
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 0, 12, 0)
        ));
        summary.add(label("Tax")); 
        taxLabel = valueLabel("$0.00"); 
        summary.add(taxLabel);
        
        summary.add(label("Subtotal"));
        subtotalLabel = valueLabel("$0.00");
        summary.add(subtotalLabel);

        summary.add(label("Discount"));
        discountLabel = valueLabel("$0.00");
        discountLabel.setForeground(UIConstants.DANGER);
        summary.add(discountLabel);

        summary.add(totalLbl("Total"));
        totalLabel = totalValLbl("$0.00");
        summary.add(totalLabel);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);

        RoundedButton clearBtn = new RoundedButton("Clear Cart", RoundedButton.Style.SECONDARY);
        clearBtn.setPreferredSize(new Dimension(0, 44));
        clearBtn.addActionListener(e -> clearCart());

        RoundedButton checkoutBtn = new RoundedButton("Checkout", RoundedButton.Style.SUCCESS);
        checkoutBtn.setPreferredSize(new Dimension(0, 44));
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutBtn.addActionListener(e -> checkout());

        btnPanel.add(clearBtn);
        btnPanel.add(checkoutBtn);

        bottomPanel.add(discountPanel, BorderLayout.NORTH);
        bottomPanel.add(summary, BorderLayout.CENTER);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        return bottomPanel;
    }

    private JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(UIConstants.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setFont(UIConstants.FONT_BODY);
        field.setPreferredSize(new Dimension(0, 38));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
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
        l.setForeground(UIConstants.TEXT_SECONDARY);
        return l;
    }

    private JLabel totalLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIConstants.FONT_HEADING);
        l.setForeground(UIConstants.TEXT_PRIMARY);
        return l;
    }

    private JLabel totalValLbl(String text) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
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
            JOptionPane.showMessageDialog(
                    this,
                    "Product is out of stock!",
                    "Stock Empty",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

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
                ? selectedDiscount.calculate(subtotal)
                : BigDecimal.ZERO;
        String taxRateStr = SettingsService.getSetting("tax_rate", "0.00");
        BigDecimal taxRate = new BigDecimal(taxRateStr);
        BigDecimal discountedTotal = subtotal.subtract(discount);
        BigDecimal tax = discountedTotal.multiply(taxRate).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = discountedTotal.add(tax);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }


        subtotalLabel.setText(String.format("$%.2f", subtotal));
        discountLabel.setText(String.format("-$%.2f", discount));
        taxLabel.setText(String.format("$%.2f", tax));     
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
        BigDecimal taxRate = new BigDecimal(SettingsService.getSetting("tax_rate", "0.00"));
        BigDecimal discountedTotal = subtotal.subtract(discount);
        BigDecimal tax = discountedTotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = discountedTotal.add(tax);

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

        if (dialog.isCompleted()) {
            clearCart();
            loadProducts();

            JOptionPane.showMessageDialog(
                    this,
                    "Order #" + dialog.getReceiptNumber() + " completed!",
                    "Sale Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private static class ProductCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            if (!(value instanceof Product p)) {
                return super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );
            }

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

            JPanel card = new JPanel(new BorderLayout(14, 0));
            card.setBackground(isSelected ? UIConstants.SIDEBAR_ACTIVE : Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            isSelected ? UIConstants.ACCENT : UIConstants.BORDER_COLOR,
                            isSelected ? 2 : 1
                    ),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));

            JLabel imageLabel = new JLabel();
            imageLabel.setPreferredSize(new Dimension(66, 66));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            imageLabel.setOpaque(true);
            imageLabel.setBackground(new Color(0xF9FAFB));
            imageLabel.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));

            if (p.getImageUrl() != null && !p.getImageUrl().isBlank()) {
                ImageIcon icon = new ImageIcon(p.getImageUrl());
                Image img = icon.getImage().getScaledInstance(58, 58, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
            } else {
                imageLabel.setText("📦");
                imageLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            }

            JLabel name = new JLabel(p.getName());
            name.setFont(new Font("Segoe UI", Font.BOLD, 14));
            name.setForeground(UIConstants.TEXT_PRIMARY);

            int qty = p.getStockQuantity() != null ? p.getStockQuantity() : 0;

            JLabel stock = new JLabel("Stock: " + qty);
            stock.setFont(UIConstants.FONT_SMALL);
            stock.setForeground(qty <= 5 ? UIConstants.DANGER : UIConstants.TEXT_MUTED);

            JLabel barcode = new JLabel(
                    p.getBarcode() == null ? "No barcode" : p.getBarcode()
            );
            barcode.setFont(UIConstants.FONT_SMALL);
            barcode.setForeground(UIConstants.TEXT_MUTED);

            JPanel info = new JPanel(new GridLayout(3, 1, 0, 3));
            info.setOpaque(false);
            info.add(name);
            info.add(stock);
            info.add(barcode);

            JLabel price = new JLabel(String.format("$%.2f", p.getSellingPrice()));
            price.setFont(new Font("Segoe UI", Font.BOLD, 18));
            price.setForeground(UIConstants.ACCENT);
            price.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel pricePanel = new JPanel(new BorderLayout());
            pricePanel.setOpaque(false);
            pricePanel.setPreferredSize(new Dimension(90, 0));
            pricePanel.add(price, BorderLayout.CENTER);

            card.add(imageLabel, BorderLayout.WEST);
            card.add(info, BorderLayout.CENTER);
            card.add(pricePanel, BorderLayout.EAST);

            wrapper.add(card, BorderLayout.CENTER);
            return wrapper;
        }
    }
}