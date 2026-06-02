package com.pos.ui.panels;

import com.pos.model.*;
import com.pos.service.ProductService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.StyledTable;
import com.pos.ui.components.UIConstants;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductService productService;

    private ProductTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JLabel            countLabel;
    private JPanel            filterBar;
    private String            activeCategory = null;

    private List<Product> currentProducts;
    private List<Product> displayedProducts;

    public ProductPanel() {
        this.productService = new ProductService();
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(buildTopBar(),    BorderLayout.NORTH);
        north.add(buildFilterBar(), BorderLayout.SOUTH);

        add(north,            BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
        refresh();
    }

    // ── Top bar ──────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(248, 250, 252)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel title = new JLabel("Products");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        countLabel = new JLabel("Loading…");
        countLabel.setFont(UIConstants.FONT_SMALL);
        countLabel.setForeground(UIConstants.TEXT_MUTED);

        left.add(title);
        left.add(countLabel);

        searchField = new JTextField(22);
        searchField.putClientProperty("JTextField.placeholderText", "Search name, barcode, category…");
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterTable(); }
            public void removeUpdate(DocumentEvent e)  { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        RoundedButton addBtn = new RoundedButton("+ Add Product");
        addBtn.addActionListener(e -> openDialog(null));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(searchField);
        right.add(addBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Category filter bar ───────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 10));
        filterBar.setBackground(new Color(250, 251, 253));
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        populateFilterBar();
        return filterBar;
    }

    private void populateFilterBar() {
        filterBar.removeAll();
        addFilterPill("All", null);
        List<Category> cats = productService.getCategories();
        for (Category c : cats) addFilterPill(c.getName(), c.getName());
        filterBar.revalidate();
        filterBar.repaint();
    }

    private int countForCategory(String cat) {
        if (currentProducts == null) return 0;
        if (cat == null) return currentProducts.size();
        return (int) currentProducts.stream()
                .filter(p -> cat.equalsIgnoreCase(p.getCategoryName()))
                .count();
    }

    private void addFilterPill(String label, String category) {
        boolean active  = (category == null && activeCategory == null)
                || (category != null && category.equals(activeCategory));
        int     count   = countForCategory(category);
        String  display = label + " (" + count + ")";

        JButton pill = new JButton(display) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setPaint(new GradientPaint(0, 0, UIConstants.PRIMARY,
                            0, getHeight(), UIConstants.PRIMARY.darker()));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                if (!active) {
                    g2.setColor(new Color(209, 213, 219));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        pill.setForeground(active ? Color.WHITE : UIConstants.TEXT_PRIMARY);
        pill.setContentAreaFilled(false);
        pill.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pill.setFocusPainted(false);
        pill.addActionListener(e -> {
            activeCategory = category;
            populateFilterBar();
            filterTable();
        });
        filterBar.add(pill);
    }

    // ── Table area ────────────────────────────────────────────────────────────

    private JPanel buildTableArea() {
        tableModel = new ProductTableModel();
        table      = new StyledTable(tableModel);
        table.setRowHeight(62);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        // ID — centered
        AlternatingRenderer idRenderer = new AlternatingRenderer();
        idRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(idRenderer);

        // Image — custom circle avatar
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());

        // Text columns
        AlternatingRenderer textRenderer = new AlternatingRenderer();
        for (int col : new int[]{2, 3, 4, 5, 6}) {
            table.getColumnModel().getColumn(col).setCellRenderer(textRenderer);
        }

        // Stock — number + progress bar
        table.getColumnModel().getColumn(7).setCellRenderer(new StockRenderer());

        // Status — pill badge
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        JScrollPane sp = StyledTable.inScrollPane((StyledTable) table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 16, 0, 16));
        wrapper.setBackground(UIConstants.BG_LIGHT);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Action bar ────────────────────────────────────────────────────────────

    private JPanel buildActionBar() {
        RoundedButton editBtn = new RoundedButton("Edit", RoundedButton.Style.SECONDARY);
        editBtn.addActionListener(e -> {
            Product p = getSelectedProduct();
            if (p != null) openDialog(p);
        });

        RoundedButton deleteBtn = new RoundedButton("Delete", RoundedButton.Style.DANGER);
        deleteBtn.addActionListener(e -> {
            Product p = getSelectedProduct();
            if (p == null) return;
            int ok = JOptionPane.showConfirmDialog(this,
                    "Deactivate \"" + p.getName() + "\"?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) { productService.delete(p.getId()); refresh(); }
        });

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        bar.setBackground(UIConstants.BG_LIGHT);
        bar.add(editBtn);
        bar.add(deleteBtn);
        return bar;
    }

    // ── Data helpers ──────────────────────────────────────────────────────────

    public void refresh() {
        currentProducts = productService.getAll();
        populateFilterBar();
        filterTable();
    }

    private void filterTable() {
        if (currentProducts == null) return;
        String kw = searchField == null ? "" : searchField.getText().trim().toLowerCase();

        displayedProducts = currentProducts.stream()
                .filter(p -> activeCategory == null
                        || activeCategory.equalsIgnoreCase(p.getCategoryName()))
                .filter(p -> kw.isEmpty()
                        || safeLower(p.getName()).contains(kw)
                        || safeLower(p.getBarcode()).contains(kw)
                        || safeLower(p.getCategoryName()).contains(kw)
                        || safeLower(p.getStatus()).contains(kw))
                .toList();

        populateTable(displayedProducts);
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        if (displayedProducts == null || modelRow >= displayedProducts.size()) {
            JOptionPane.showMessageDialog(this, "Selected product not found.");
            return null;
        }
        return displayedProducts.get(modelRow);
    }

    private void populateTable(List<Product> products) {
        tableModel.setRowCount(0);
        int n = products == null ? 0 : products.size();
        if (countLabel != null)
            countLabel.setText(n + (n == 1 ? " product" : " products"));
        if (products == null) return;
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getImageUrl(),                // path string — ImageRenderer loads & caches it
                    p.getName(),
                    p.getBarcode()      != null ? p.getBarcode()      : "",
                    p.getCategoryName() != null ? p.getCategoryName() : "",
                    String.format("$%.2f", p.getCostPrice()),
                    String.format("$%.2f", p.getSellingPrice()),
                    p.getStockQuantity() != null ? p.getStockQuantity() : 0,
                    p.getStatus()
            });
        }
    }

    private void openDialog(Product existing) {
        new ProductDialog(SwingUtilities.getWindowAncestor(this), existing, productService, this::refresh)
                .setVisible(true);
    }

    private static String safeLower(String v) { return v == null ? "" : v.toLowerCase(); }

    // =========================================================================
    // Inner classes — each demonstrates a distinct OOP principle
    // =========================================================================

    /**
     * Non-editable table model with typed column definitions.
     * Demonstrates ENCAPSULATION + INHERITANCE (extends DefaultTableModel).
     */
    private static class ProductTableModel extends DefaultTableModel {
        private static final String[] COLUMNS = {
                "ID", "Image", "Name", "Barcode",
                "Category", "Cost", "Price", "Stock", "Status"
        };
        ProductTableModel() { super(COLUMNS, 0); }
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c)       { return Object.class; }
    }

    /**
     * Renders the image column as a circular avatar.
     * When a product image is available it is clipped to a circle; otherwise a
     * gradient-filled circle with the product's initial letter is shown.
     * Supports three path-loading strategies: absolute file → relative to working
     * directory → classpath resource (so teammates see images after a git pull).
     * Demonstrates ABSTRACTION + POLYMORPHISM via the TableCellRenderer interface.
     */
    private static class ImageRenderer extends JPanel implements TableCellRenderer {

        private static final Color[] AVATAR_COLORS = {
            new Color(37,  99,  235), new Color(16,  185, 129),
            new Color(245, 158, 11),  new Color(239, 68,  68),
            new Color(168, 85,  247), new Color(20,  184, 166),
            new Color(234, 88,  12),  new Color(99,  102, 241),
            new Color(236, 72,  153), new Color(14,  165, 233),
        };

        private ImageIcon cachedIcon;
        private String    initial     = "?";
        private Color     circleColor = AVATAR_COLORS[0];

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            String path     = value != null ? value.toString() : "";
            int    modelRow = table.convertRowIndexToModel(row);
            Object nameObj  = table.getModel().getValueAt(modelRow, 2);
            String name     = nameObj != null ? nameObj.toString() : "";

            cachedIcon = loadIcon(path, 44, 44);
            if (cachedIcon == null) {
                initial     = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
                circleColor = AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.length];
            }
            setBackground(isSelected
                    ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,   RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int size = Math.min(getWidth(), getHeight()) - 16;
            int x    = (getWidth()  - size) / 2;
            int y    = (getHeight() - size) / 2;

            if (cachedIcon != null) {
                // Clip image to circle
                Shape clip = new Ellipse2D.Float(x, y, size, size);
                g2.setClip(clip);
                g2.drawImage(cachedIcon.getImage(), x, y, size, size, this);
                g2.setClip(null);
                // Subtle ring
                g2.setColor(new Color(0, 0, 0, 20));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, size - 1, size - 1);
            } else {
                // Gradient-filled avatar with initial letter
                g2.setPaint(new GradientPaint(x, y, circleColor.brighter(),
                        x + size, y + size, circleColor.darker()));
                g2.fillOval(x, y, size, size);
                // Shine overlay
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(x + 2, y + 2, size - 4, size / 2);
                // Initial letter
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size * 4 / 10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial,
                        x + (size - fm.stringWidth(initial)) / 2,
                        y + (size + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }

        static ImageIcon loadIcon(String path, int w, int h) {
            if (path == null || path.isBlank()) return null;
            String clean = path.trim();

            File file = new File(clean);
            if (!file.exists()) file = new File(System.getProperty("user.dir"), clean);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                if (icon.getIconWidth() > 0)
                    return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }

            String cp = "/" + clean.replace('\\', '/');
            try (InputStream is = ImageRenderer.class.getResourceAsStream(cp)) {
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    if (img != null)
                        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            } catch (Exception ignored) {}

            return null;
        }
    }

    /**
     * Renders the Stock column as a bold number with a colored mini progress bar.
     * Bar color: red (≤ 5 critical), amber (≤ 20 low), green (> 20 healthy).
     * Demonstrates INHERITANCE + POLYMORPHISM: extends DefaultTableCellRenderer,
     * overrides paintComponent() to add custom multi-layer drawing.
     */
    private static class StockRenderer extends DefaultTableCellRenderer {

        private static final int   MAX_STOCK = 100;
        private static final Color RED       = new Color(239, 68,  68);
        private static final Color AMBER     = new Color(245, 158, 11);
        private static final Color GREEN     = new Color(16,  185, 129);

        private int   quantity;
        private Color barColor;

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            quantity = value instanceof Number ? ((Number) value).intValue() : 0;
            barColor = quantity <= 5 ? RED : (quantity <= 20 ? AMBER : GREEN);
            setText("");
            setBackground(isSelected
                    ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            setOpaque(true);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Quantity number — upper half
            String text = String.valueOf(quantity);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(UIConstants.TEXT_PRIMARY);
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2 - 1);

            // Mini bar — lower half
            int barH  = 5;
            int barW  = getWidth() - 24;
            int barX  = 12;
            int barY  = getHeight() / 2 + 6;
            int filled = (int) (barW * Math.min(quantity, MAX_STOCK) / (double) MAX_STOCK);

            g2.setColor(new Color(229, 231, 235));
            g2.fillRoundRect(barX, barY, barW, barH, barH, barH);

            if (filled > 0) {
                g2.setPaint(new GradientPaint(barX, 0, barColor.brighter(), barX + barW, 0, barColor));
                g2.fillRoundRect(barX, barY, filled, barH, barH, barH);
            }
            g2.dispose();
        }
    }

    /**
     * Renders the Status column as a colored pill badge with a dot indicator.
     * Demonstrates INHERITANCE + POLYMORPHISM: extends DefaultTableCellRenderer,
     * overrides paintComponent() to draw a custom pill shape.
     */
    private static class StatusRenderer extends DefaultTableCellRenderer {

        private String status = "";

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            status = value != null ? value.toString() : "";
            setText("");
            setBackground(isSelected
                    ? table.getSelectionBackground()
                    : (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
            setOpaque(true);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean active = "ACTIVE".equalsIgnoreCase(status);
            Color bg  = active ? new Color(220, 252, 231) : new Color(254, 226, 226);
            Color fg  = active ? new Color(22,  163, 74)  : new Color(220, 38,  38);
            String lbl = active ? "Active" : "Inactive";

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int pillW = fm.stringWidth(lbl) + 30;
            int pillH = 22;
            int pillX = (getWidth()  - pillW) / 2;
            int pillY = (getHeight() - pillH) / 2;

            g2.setColor(bg);
            g2.fillRoundRect(pillX, pillY, pillW, pillH, pillH, pillH);

            // Dot
            g2.setColor(fg);
            g2.fillOval(pillX + 8, pillY + (pillH - 7) / 2, 7, 7);

            g2.drawString(lbl, pillX + 19, pillY + pillH - 7);
            g2.dispose();
        }
    }

    /**
     * Applies alternating white / light-gray row backgrounds to text columns.
     * Demonstrates INHERITANCE: extends DefaultTableCellRenderer with minimal override.
     */
    private static class AlternatingRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    /**
     * Modal dialog for adding or editing a product.
     * Demonstrates ENCAPSULATION + INHERITANCE: all form state is private;
     * extends JDialog to own its window lifecycle and modal behavior.
     */
    private static class ProductDialog extends JDialog {

        private final Product        existing;
        private final ProductService productService;
        private final Runnable       onSaved;

        private JTextField          nameField;
        private JTextField          barcodeField;
        private JTextField          imageUrlField;
        private JTextField          costField;
        private JTextField          priceField;
        private JComboBox<Category> catCombo;
        private JComboBox<Supplier> supCombo;
        private JComboBox<String>   statusCombo;
        private JLabel              imagePreview;

        ProductDialog(Window owner, Product existing,
                      ProductService productService, Runnable onSaved) {
            super(owner, existing == null ? "Add Product" : "Edit Product",
                    ModalityType.APPLICATION_MODAL);
            this.existing       = existing;
            this.productService = productService;
            this.onSaved        = onSaved;
            buildUI();
            setSize(720, 530);
            setLocationRelativeTo(owner);
        }

        private void buildUI() {
            setLayout(new BorderLayout());

            // Gradient dialog header
            JPanel header = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(new GradientPaint(0, 0, new Color(37, 99, 235),
                            getWidth(), 0, new Color(99, 102, 241)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            header.setPreferredSize(new Dimension(0, 54));
            header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

            JLabel headerTitle = new JLabel(existing == null ? "Add New Product" : "Edit Product");
            headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
            headerTitle.setForeground(Color.WHITE);
            header.add(headerTitle, BorderLayout.CENTER);

            JPanel main = new JPanel(new BorderLayout(16, 0));
            main.setBackground(Color.WHITE);
            main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
            main.add(buildForm(),        BorderLayout.CENTER);
            main.add(buildImagePanel(),  BorderLayout.EAST);

            add(header,             BorderLayout.NORTH);
            add(main,               BorderLayout.CENTER);
            add(buildButtonPanel(), BorderLayout.SOUTH);
        }

        private JPanel buildForm() {
            List<Category> cats = productService.getCategories();
            List<Supplier> sups = productService.getSuppliers();

            nameField     = new JTextField(existing != null ? existing.getName() : "", 20);
            barcodeField  = new JTextField(
                    existing != null && existing.getBarcode()  != null ? existing.getBarcode()  : "", 20);
            imageUrlField = new JTextField(
                    existing != null && existing.getImageUrl() != null ? existing.getImageUrl() : "", 20);
            costField     = new JTextField(
                    existing != null ? existing.getCostPrice().toPlainString() : "0.00", 10);
            priceField    = new JTextField(
                    existing != null ? existing.getSellingPrice().toPlainString() : "0.00", 10);

            catCombo    = buildCatCombo(cats);
            supCombo    = buildSupCombo(sups);
            statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
            if (existing != null) statusCombo.setSelectedItem(existing.getStatus());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            GridBagConstraints c = new GridBagConstraints();
            c.fill   = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(6, 4, 6, 4);

            int row = 0;
            addRow(form, c, row++, "Product Name *",  nameField);
            addRow(form, c, row++, "Barcode",         barcodeField);
            addRow(form, c, row++, "Image Path",      imageUrlField);
            addRow(form, c, row++, "Cost Price *",    costField);
            addRow(form, c, row++, "Selling Price *", priceField);
            addRow(form, c, row++, "Category",        catCombo);
            addRow(form, c, row++, "Supplier",        supCombo);
            addRow(form, c, row,   "Status",          statusCombo);
            return form;
        }

        private JComboBox<Category> buildCatCombo(List<Category> cats) {
            JComboBox<Category> cb = new JComboBox<>();
            cb.addItem(null);
            cats.forEach(cb::addItem);
            cb.setRenderer((l, v, i, s, f) -> new JLabel(v == null ? "— None —" : v.getName()));
            if (existing != null && existing.getCategoryId() != null)
                cats.stream().filter(c -> c.getId().equals(existing.getCategoryId()))
                        .findFirst().ifPresent(cb::setSelectedItem);
            return cb;
        }

        private JComboBox<Supplier> buildSupCombo(List<Supplier> sups) {
            JComboBox<Supplier> cb = new JComboBox<>();
            cb.addItem(null);
            sups.forEach(cb::addItem);
            cb.setRenderer((l, v, i, s, f) -> new JLabel(v == null ? "— None —" : v.getName()));
            if (existing != null && existing.getSupplierId() != null)
                sups.stream().filter(s -> s.getId().equals(existing.getSupplierId()))
                        .findFirst().ifPresent(cb::setSelectedItem);
            return cb;
        }

        private JPanel buildImagePanel() {
            imagePreview = new JLabel("No Image", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(248, 250, 252));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.setColor(new Color(209, 213, 219));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            imagePreview.setPreferredSize(new Dimension(165, 165));
            imagePreview.setOpaque(false);
            imagePreview.setFont(UIConstants.FONT_SMALL);
            imagePreview.setForeground(UIConstants.TEXT_MUTED);
            refreshPreview(imageUrlField.getText());

            RoundedButton browseBtn = new RoundedButton("Browse Image", RoundedButton.Style.SECONDARY);
            browseBtn.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter(
                        "Image Files", "jpg", "jpeg", "png", "gif", "webp"));
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String p = fc.getSelectedFile().getAbsolutePath();
                    imageUrlField.setText(p);
                    refreshPreview(p);
                }
            });

            RoundedButton previewBtn = new RoundedButton("Preview", RoundedButton.Style.PRIMARY);
            previewBtn.addActionListener(e -> refreshPreview(imageUrlField.getText()));

            JPanel btns = new JPanel(new GridLayout(2, 1, 0, 6));
            btns.setBackground(Color.WHITE);
            btns.add(browseBtn);
            btns.add(previewBtn);

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBackground(Color.WHITE);
            panel.add(imagePreview, BorderLayout.CENTER);
            panel.add(btns,         BorderLayout.SOUTH);
            return panel;
        }

        private JPanel buildButtonPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER));

            RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
            cancelBtn.addActionListener(e -> dispose());

            RoundedButton saveBtn = new RoundedButton("Save Product", RoundedButton.Style.SUCCESS);
            saveBtn.addActionListener(e -> save());

            panel.add(cancelBtn);
            panel.add(saveBtn);
            return panel;
        }

        private void save() {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Product name is required.");

                BigDecimal cost  = new BigDecimal(costField.getText().trim());
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (cost.compareTo(BigDecimal.ZERO)  < 0)
                    throw new IllegalArgumentException("Cost price cannot be negative.");
                if (price.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Selling price cannot be negative.");

                Product p = existing != null ? existing : new Product();
                p.setName(name);
                p.setBarcode(emptyToNull(barcodeField.getText()));
                p.setImageUrl(emptyToNull(imageUrlField.getText()));
                p.setCostPrice(cost);
                p.setSellingPrice(price);
                p.setCategoryId(catCombo.getSelectedItem() instanceof Category c ? c.getId() : null);
                p.setSupplierId(supCombo.getSelectedItem() instanceof Supplier s ? s.getId() : null);
                p.setStatus((String) statusCombo.getSelectedItem());

                if (existing == null) productService.save(p);
                else                  productService.update(p);

                dispose();
                onSaved.run();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Cost and selling price must be valid numbers.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void refreshPreview(String path) {
            ImageIcon icon = ImageRenderer.loadIcon(path, 153, 153);
            if (icon != null) { imagePreview.setText(""); imagePreview.setIcon(icon); }
            else              { imagePreview.setIcon(null); imagePreview.setText("No Image"); }
        }

        private void addRow(JPanel form, GridBagConstraints c,
                            int row, String label, JComponent field) {
            c.gridy = row; c.gridx = 0; c.weightx = 0;
            JLabel lbl = new JLabel(label);
            lbl.setFont(UIConstants.FONT_BODY);
            lbl.setForeground(UIConstants.TEXT_PRIMARY);
            lbl.setPreferredSize(new Dimension(130, 28));
            form.add(lbl, c);

            c.gridx = 1; c.weightx = 1.0;
            field.setFont(UIConstants.FONT_BODY);
            form.add(field, c);
        }

        private static String emptyToNull(String v) {
            return (v == null || v.trim().isEmpty()) ? null : v.trim();
        }
    }
}
