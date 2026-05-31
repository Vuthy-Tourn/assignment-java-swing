package com.pos.ui.panels;

import com.pos.model.*;
import com.pos.service.ProductService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.StyledTable;
import com.pos.ui.components.UIConstants;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductService productService = new ProductService();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    private List<Product> currentProducts;
    private List<Product> displayedProducts;

    public ProductPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Products");
        title.setFont(UIConstants.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Search...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        RoundedButton addBtn = new RoundedButton("+ Add Product");
        addBtn.addActionListener(e -> showProductDialog(null));

        rightBar.add(searchField);
        rightBar.add(addBtn);

        topBar.add(rightBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {
                "ID", "Image", "Name", "Barcode",
                "Category", "Cost", "Price", "Stock", "Status"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return ImageIcon.class;
                }
                return Object.class;
            }
        };

        table = new StyledTable(tableModel);
        table.setRowHeight(58);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(60);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);

        table.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        tableWrapper.setBackground(UIConstants.BG_LIGHT);
        tableWrapper.add(StyledTable.inScrollPane((StyledTable) table), BorderLayout.CENTER);

        add(tableWrapper, BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionBar.setBackground(UIConstants.BG_LIGHT);

        RoundedButton editBtn = new RoundedButton("Edit", RoundedButton.Style.SECONDARY);
        editBtn.addActionListener(e -> {
            Product selected = getSelectedProduct();
            if (selected == null) return;
            showProductDialog(selected);
        });

        RoundedButton deleteBtn = new RoundedButton("Delete", RoundedButton.Style.DANGER);
        deleteBtn.addActionListener(e -> {
            Product selected = getSelectedProduct();
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Deactivate product: " + selected.getName() + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                productService.delete(selected.getId());
                refresh();
            }
        });

        actionBar.add(editBtn);
        actionBar.add(deleteBtn);

        add(actionBar, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        currentProducts = productService.getAll();
        displayedProducts = currentProducts;
        populateTable(displayedProducts);
    }

    private void filterTable() {
        if (currentProducts == null) return;

        String kw = searchField.getText().trim().toLowerCase();

        if (kw.isEmpty()) {
            displayedProducts = currentProducts;
        } else {
            displayedProducts = currentProducts.stream()
                    .filter(p ->
                            safeLower(p.getName()).contains(kw)
                                    || safeLower(p.getBarcode()).contains(kw)
                                    || safeLower(p.getImageUrl()).contains(kw)
                                    || safeLower(p.getCategoryName()).contains(kw)
                                    || safeLower(p.getStatus()).contains(kw)
                    )
                    .toList();
        }

        populateTable(displayedProducts);
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first");
            return null;
        }

        int modelRow = table.convertRowIndexToModel(row);

        if (displayedProducts == null || modelRow >= displayedProducts.size()) {
            JOptionPane.showMessageDialog(this, "Selected product not found");
            return null;
        }

        return displayedProducts.get(modelRow);
    }

    private void populateTable(List<Product> products) {
        tableModel.setRowCount(0);

        if (products == null) return;

        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    loadImageIcon(p.getImageUrl(), 46, 46),
                    p.getName(),
                    p.getBarcode() != null ? p.getBarcode() : "",
                    p.getCategoryName() != null ? p.getCategoryName() : "",
                    String.format("$%.2f", p.getCostPrice()),
                    String.format("$%.2f", p.getSellingPrice()),
                    p.getStockQuantity() != null ? p.getStockQuantity() : 0,
                    p.getStatus()
            });
        }
    }

    private void showProductDialog(Product existing) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add Product" : "Edit Product",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(16, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 4, 5, 4);
        c.weightx = 1.0;

        List<Category> categories = productService.getCategories();
        List<Supplier> suppliers = productService.getSuppliers();

        JTextField nameField = new JTextField(existing != null ? existing.getName() : "", 20);
        JTextField barcodeField = new JTextField(existing != null && existing.getBarcode() != null ? existing.getBarcode() : "", 20);
        JTextField imageUrlField = new JTextField(existing != null && existing.getImageUrl() != null ? existing.getImageUrl() : "", 20);
        JTextField costField = new JTextField(existing != null ? existing.getCostPrice().toPlainString() : "0.00", 10);
        JTextField priceField = new JTextField(existing != null ? existing.getSellingPrice().toPlainString() : "0.00", 10);

        JLabel imagePreview = new JLabel("No Image", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(140, 140));
        imagePreview.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER));
        imagePreview.setOpaque(true);
        imagePreview.setBackground(new Color(245, 245, 245));

        updatePreview(imagePreview, imageUrlField.getText());

        RoundedButton browseBtn = new RoundedButton("Browse Image", RoundedButton.Style.SECONDARY);
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Image Files",
                    "jpg", "jpeg", "png", "gif", "webp"
            ));

            int result = chooser.showOpenDialog(dialog);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                imageUrlField.setText(selectedFile.getAbsolutePath());
                updatePreview(imagePreview, selectedFile.getAbsolutePath());
            }
        });

        RoundedButton previewBtn = new RoundedButton("Preview", RoundedButton.Style.PRIMARY);
        previewBtn.addActionListener(e -> updatePreview(imagePreview, imageUrlField.getText()));

        JPanel imagePanel = new JPanel(new BorderLayout(0, 8));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.add(imagePreview, BorderLayout.CENTER);

        JPanel imageButtons = new JPanel(new GridLayout(2, 1, 0, 6));
        imageButtons.setBackground(Color.WHITE);
        imageButtons.add(browseBtn);
        imageButtons.add(previewBtn);

        imagePanel.add(imageButtons, BorderLayout.SOUTH);

        JComboBox<Category> catCombo = new JComboBox<>();
        catCombo.addItem(null);
        categories.forEach(catCombo::addItem);
        catCombo.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "— None —" : value.getName())
        );

        if (existing != null && existing.getCategoryId() != null) {
            categories.stream()
                    .filter(ct -> ct.getId().equals(existing.getCategoryId()))
                    .findFirst()
                    .ifPresent(catCombo::setSelectedItem);
        }

        JComboBox<Supplier> supCombo = new JComboBox<>();
        supCombo.addItem(null);
        suppliers.forEach(supCombo::addItem);
        supCombo.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "— None —" : value.getName())
        );

        if (existing != null && existing.getSupplierId() != null) {
            suppliers.stream()
                    .filter(sp -> sp.getId().equals(existing.getSupplierId()))
                    .findFirst()
                    .ifPresent(supCombo::setSelectedItem);
        }

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        if (existing != null) {
            statusCombo.setSelectedItem(existing.getStatus());
        }

        int row = 0;
        addFormRow(form, c, row++, "Product Name *", nameField);
        addFormRow(form, c, row++, "Barcode", barcodeField);
        addFormRow(form, c, row++, "Image Path", imageUrlField);
        addFormRow(form, c, row++, "Cost Price *", costField);
        addFormRow(form, c, row++, "Selling Price *", priceField);
        addFormRow(form, c, row++, "Category", catCombo);
        addFormRow(form, c, row++, "Supplier", supCombo);
        addFormRow(form, c, row, "Status", statusCombo);

        mainPanel.add(form, BorderLayout.CENTER);
        mainPanel.add(imagePanel, BorderLayout.EAST);

        dialog.add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(Color.WHITE);

        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dialog.dispose());

        RoundedButton saveBtn = new RoundedButton("Save Product", RoundedButton.Style.SUCCESS);
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();

                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Product name is required");
                }

                BigDecimal cost = new BigDecimal(costField.getText().trim());
                BigDecimal price = new BigDecimal(priceField.getText().trim());

                if (cost.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Cost price cannot be negative");
                }

                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Selling price cannot be negative");
                }

                Product p = existing != null ? existing : new Product();

                p.setName(name);
                p.setBarcode(emptyToNull(barcodeField.getText()));
                p.setImageUrl(emptyToNull(imageUrlField.getText()));
                p.setCostPrice(cost);
                p.setSellingPrice(price);

                Category cat = (Category) catCombo.getSelectedItem();
                p.setCategoryId(cat != null ? cat.getId() : null);

                Supplier sup = (Supplier) supCombo.getSelectedItem();
                p.setSupplierId(sup != null ? sup.getId() : null);

                p.setStatus((String) statusCombo.getSelectedItem());

                if (existing == null) {
                    productService.save(p);
                } else {
                    productService.update(p);
                }

                dialog.dispose();
                refresh();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Cost price and selling price must be valid numbers",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        dialog,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setSize(680, 470);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updatePreview(JLabel imagePreview, String imagePath) {
        ImageIcon icon = loadImageIcon(imagePath, 130, 130);

        if (icon != null) {
            imagePreview.setText("");
            imagePreview.setIcon(icon);
        } else {
            imagePreview.setIcon(null);
            imagePreview.setText("No Image");
        }
    }

    private ImageIcon loadImageIcon(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        File file = new File(imagePath.trim());

        if (!file.exists()) {
            return null;
        }

        ImageIcon originalIcon = new ImageIcon(file.getAbsolutePath());

        if (originalIcon.getIconWidth() <= 0 || originalIcon.getIconHeight() <= 0) {
            return null;
        }

        Image scaledImage = originalIcon.getImage().getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(scaledImage);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private void addFormRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setPreferredSize(new Dimension(120, 28));
        form.add(lbl, c);

        c.gridx = 1;
        c.weightx = 1.0;
        field.setFont(UIConstants.FONT_BODY);
        form.add(field, c);
    }

    private static class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            if (value instanceof ImageIcon) {
                setText("");
                setIcon((ImageIcon) value);
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setIcon(null);
                setText("No Image");
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        }
    }
}
