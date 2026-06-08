package com.pos.ui.panels;

import com.pos.model.Order;
import com.pos.model.OrderItem;
import com.pos.service.OrderService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.StyledTable;
import com.pos.ui.components.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderPanel extends JPanel {

    private final OrderService orderService = new OrderService();

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Order> currentOrders;

    private JSpinner fromSpinner;
    private JSpinner toSpinner;
    private JLabel totalRevenueLabel;

    public OrderPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        buildUI();
    }


    private void buildUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Order History");
        title.setFont(UIConstants.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        SpinnerDateModel fromModel = new SpinnerDateModel();
        fromSpinner = new JSpinner(fromModel);
        fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));
        fromSpinner.setPreferredSize(new Dimension(120, 30));

        SpinnerDateModel toModel = new SpinnerDateModel();
        toSpinner = new JSpinner(toModel);
        toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd"));
        toSpinner.setPreferredSize(new Dimension(120, 30));

        java.util.Date today = new java.util.Date();
        fromSpinner.setValue(today);
        toSpinner.setValue(today);

        RoundedButton searchBtn = new RoundedButton("Search", RoundedButton.Style.PRIMARY);
        searchBtn.addActionListener(e -> refresh());

        filterPanel.add(new JLabel("From:"));
        filterPanel.add(fromSpinner);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(toSpinner);
        filterPanel.add(searchBtn);

        topBar.add(filterPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {
                "ID", "Receipt #", "Date", "Cashier",
                "Items", "Total", "Discount", "Tax", "Final"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new StyledTable(tableModel);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        table.removeColumn(table.getColumnModel().getColumn(0));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        tableWrapper.setBackground(UIConstants.CONTENT_BG);
        tableWrapper.add(StyledTable.inScrollPane((StyledTable) table), BorderLayout.CENTER);

        add(tableWrapper, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(UIConstants.CONTENT_BG);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBottom.setOpaque(false);

        RoundedButton viewBtn = new RoundedButton("View Detail", RoundedButton.Style.SECONDARY);
        viewBtn.addActionListener(e -> viewDetail());

        leftBottom.add(viewBtn);
        bottomBar.add(leftBottom, BorderLayout.WEST);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBottom.setOpaque(false);

        JLabel revLabel = new JLabel("Total Revenue:");
        revLabel.setFont(UIConstants.FONT_HEADING);

        totalRevenueLabel = new JLabel("$0.00");
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalRevenueLabel.setForeground(UIConstants.SUCCESS);

        rightBottom.add(revLabel);
        rightBottom.add(totalRevenueLabel);

        bottomBar.add(rightBottom, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        LocalDate from = getDateFromSpinner(fromSpinner);
        LocalDate to = getDateFromSpinner(toSpinner);

        if (from.isAfter(to)) {
            JOptionPane.showMessageDialog(this, "From date cannot be after To date");
            return;
        }

        currentOrders = orderService.getOrdersByDateRange(from, to);
        populateTable(currentOrders);

        BigDecimal total = currentOrders.stream()
                .map(Order::getFinalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalRevenueLabel.setText(String.format("$%.2f", total));
    }

    private LocalDate getDateFromSpinner(JSpinner spinner) {
        java.util.Date date = (java.util.Date) spinner.getValue();

        return date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }

    private void populateTable(List<Order> orders) {
        tableModel.setRowCount(0);

        if (orders == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Order o : orders) {
            tableModel.addRow(new Object[]{
                    o.getId(),
                    o.getReceiptNumber(),
                    o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : "",
                    o.getUserName() != null ? o.getUserName() : "",
                    o.getItems() != null ? o.getItems().size() : 0,
                    String.format("$%.2f", safeAmount(o.getTotalAmount())),
                    String.format("$%.2f", safeAmount(o.getDiscountAmount())),
                    String.format("$%.2f", safeAmount(o.getTaxAmount())),
                    String.format("$%.2f", safeAmount(o.getFinalAmount()))
            });
        }
    }

    private BigDecimal safeAmount(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    private Long getSelectedOrderId() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order first");
            return null;
        }

        int modelRow = table.convertRowIndexToModel(row);
        Object value = tableModel.getValueAt(modelRow, 0);

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        return Long.parseLong(value.toString());
    }

    private void viewDetail() {
        Long orderId = getSelectedOrderId();
        if (orderId == null) return;

        Order order = orderService.findById(orderId);
        if (order == null) {
            JOptionPane.showMessageDialog(this, "Order not found");
            return;
        }

        ReceiptPanel receiptPanel = new ReceiptPanel();
        receiptPanel.setOrder(order);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Receipt — " + order.getReceiptNumber(),
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setLayout(new BorderLayout());
        dialog.add(receiptPanel, BorderLayout.CENTER);

        dialog.setSize(600, 800);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_MUTED);

        JLabel val = new JLabel(value != null ? value : "");
        val.setFont(UIConstants.FONT_BODY);

        panel.add(lbl);
        panel.add(val);
    }
}