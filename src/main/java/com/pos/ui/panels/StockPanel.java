package com.pos.ui.panels;

import com.pos.model.Stock;
import com.pos.model.StockHistory;
import com.pos.service.StockService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.StyledTable;
import com.pos.ui.components.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StockPanel extends JPanel {

    private final StockService stockService = new StockService();

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Stock> currentStocks;

    public StockPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel title = new JLabel("Stock Management");
        title.setFont(UIConstants.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);

        JToggleButton lowStockBtn = new JToggleButton("Show Low Stock Only");
        lowStockBtn.setFont(UIConstants.FONT_BODY);
        lowStockBtn.addActionListener(e -> {
            if (lowStockBtn.isSelected()) {
                populateTable(stockService.getLowStock());
            } else {
                populateTable(currentStocks);
            }
        });

        RoundedButton refreshBtn = new RoundedButton("Refresh", RoundedButton.Style.SECONDARY);
        refreshBtn.addActionListener(e -> refresh());

        rightBar.add(lowStockBtn);
        rightBar.add(refreshBtn);
        topBar.add(rightBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Product", "Quantity", "Low Stock Alert", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Color low-stock rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected && row < currentStocks.size()) {
                    Stock s = currentStocks.get(row);
                    if (s.isLowStock()) {
                        c.setBackground(new Color(254, 242, 242));
                        setForeground(UIConstants.DANGER);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.BG_LIGHT);
                        setForeground(UIConstants.TEXT_PRIMARY);
                    }
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        tableWrapper.setBackground(UIConstants.BG_LIGHT);
        tableWrapper.add(StyledTable.inScrollPane((StyledTable) table), BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        // Bottom buttons
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionBar.setBackground(UIConstants.BG_LIGHT);

        RoundedButton stockInBtn = new RoundedButton("Stock In", RoundedButton.Style.SUCCESS);
        stockInBtn.addActionListener(e -> showAdjustDialog("IN"));

        RoundedButton adjustBtn = new RoundedButton("Adjust", RoundedButton.Style.WARNING);
        adjustBtn.addActionListener(e -> showAdjustDialog("ADJUST"));

        RoundedButton historyBtn = new RoundedButton("History", RoundedButton.Style.SECONDARY);
        historyBtn.addActionListener(e -> showHistory());

        actionBar.add(stockInBtn);
        actionBar.add(adjustBtn);
        actionBar.add(historyBtn);
        add(actionBar, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        currentStocks = stockService.getAll();
        populateTable(currentStocks);
    }

    private void populateTable(List<Stock> stocks) {
        tableModel.setRowCount(0);
        for (Stock s : stocks) {
            String status = s.isLowStock() ? "⚠ Low Stock" : "OK";
            tableModel.addRow(new Object[]{s.getProductName(), s.getQuantity(), s.getLowStockAlert(), status});
        }
    }

    private void showAdjustDialog(String mode) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a product first"); return; }
        Stock stock = currentStocks.get(row);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                mode.equals("IN") ? "Stock In" : "Adjust Stock",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(360, 260);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 4, 6, 4);

        c.gridy = 0; c.gridwidth = 2;
        JLabel productLbl = new JLabel("Product: " + stock.getProductName());
        productLbl.setFont(UIConstants.FONT_HEADER);
        form.add(productLbl, c);

        c.gridy = 1;
        JLabel currentLbl = new JLabel("Current quantity: " + stock.getQuantity());
        currentLbl.setFont(UIConstants.FONT_BODY);
        currentLbl.setForeground(UIConstants.TEXT_MUTED);
        form.add(currentLbl, c);

        c.gridy = 2; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel(mode.equals("IN") ? "Add Quantity:" : "New Quantity:"), c);
        JTextField qtyField = new JTextField(String.valueOf(mode.equals("IN") ? 1 : stock.getQuantity()), 8);
        qtyField.setFont(UIConstants.FONT_BODY);
        c.gridx = 1; c.weightx = 1.0;
        form.add(qtyField, c);

        c.gridx = 0; c.gridy = 3; c.weightx = 0;
        form.add(new JLabel("Note:"), c);
        JTextField noteField = new JTextField("Manual adjustment", 16);
        noteField.setFont(UIConstants.FONT_BODY);
        c.gridx = 1; c.weightx = 1.0;
        form.add(noteField, c);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(Color.WHITE);
        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dialog.dispose());
        RoundedButton saveBtn = new RoundedButton("Save", RoundedButton.Style.SUCCESS);
        saveBtn.addActionListener(e -> {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                String note = noteField.getText().trim();
                if (mode.equals("IN")) {
                    stockService.stockIn(stock.getProductId(), qty, note);
                } else {
                    stockService.stockAdjust(stock.getProductId(), qty, note);
                }
                dialog.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid quantity");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showHistory() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a product first"); return; }
        Stock stock = currentStocks.get(row);

        List<StockHistory> history = stockService.getHistory(stock.getProductId());

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Stock History — " + stock.getProductName(),
                Dialog.ModalityType.APPLICATION_MODAL);

        String[] cols = {"Date/Time", "Type", "Quantity", "Note"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (StockHistory h : history) {
            model.addRow(new Object[]{
                    h.getCreatedAt() != null ? h.getCreatedAt().toString().replace("T", " ").substring(0, 16) : "",
                    h.getType(),
                    h.getQuantity(),
                    h.getNote()
            });
        }
        JTable histTable = new StyledTable(model);
        JScrollPane scroll = new JScrollPane(histTable);

        dialog.setLayout(new BorderLayout());
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
