package com.pos.ui.dialogs;

import com.pos.model.*;
import com.pos.service.OrderService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class PaymentDialog extends JDialog {

    private final BigDecimal finalAmount;
    private final List<OrderItem> items;
    private final Discount discount;
    private final OrderService orderService;

    private boolean completed = false;
    private String receiptNumber;

    private JTextField paidAmountField;
    private JLabel changeLabel;
    private JComboBox<String> methodCombo;

    public PaymentDialog(Window parent, BigDecimal finalAmount,
                         List<OrderItem> items, Discount discount,
                         OrderService orderService) {
        super(parent, "Payment", ModalityType.APPLICATION_MODAL);
        this.finalAmount = finalAmount;
        this.items = items;
        this.discount = discount;
        this.orderService = orderService;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        header.setBackground(UIConstants.SIDEBAR_BG);
        JLabel title = new JLabel("Process Payment");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        c.gridx = 0; c.gridwidth = 2;

        // Total due
        JLabel dueLabel = new JLabel("Amount Due");
        dueLabel.setFont(UIConstants.FONT_HEADING);
        dueLabel.setForeground(UIConstants.TEXT_MUTED);
        c.gridy = 0;
        body.add(dueLabel, c);

        JLabel amountLabel = new JLabel(String.format("$%.2f", finalAmount));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        amountLabel.setForeground(UIConstants.ACCENT);
        c.gridy = 1;
        body.add(amountLabel, c);

        JSeparator sep = new JSeparator();
        c.gridy = 2; c.insets = new Insets(10, 0, 10, 0);
        body.add(sep, c);
        c.insets = new Insets(6, 0, 6, 0);

        // Payment method
        c.gridy = 3; c.gridwidth = 1; c.weightx = 0;
        body.add(new JLabel("Method:"), c);
        methodCombo = new JComboBox<>(new String[]{"CASH", "CARD", "QR"});
        methodCombo.setFont(UIConstants.FONT_BODY);
        methodCombo.setPreferredSize(new Dimension(200, 32));
        c.gridx = 1; c.weightx = 1.0;
        body.add(methodCombo, c);

        // Paid amount
        c.gridx = 0; c.gridy = 4; c.weightx = 0;
        body.add(new JLabel("Paid:"), c);
        paidAmountField = new JTextField(String.format("%.2f", finalAmount), 12);
        paidAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        paidAmountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        paidAmountField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { calcChange(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { calcChange(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calcChange(); }
        });
        c.gridx = 1; c.weightx = 1.0;
        body.add(paidAmountField, c);

        // Change
        c.gridx = 0; c.gridy = 5; c.weightx = 0;
        body.add(new JLabel("Change:"), c);
        changeLabel = new JLabel("$0.00");
        changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        changeLabel.setForeground(UIConstants.SUCCESS);
        c.gridx = 1; c.weightx = 1.0;
        body.add(changeLabel, c);

        root.add(body, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 28, 20, 28));

        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dispose());

        RoundedButton confirmBtn = new RoundedButton("Confirm Payment", RoundedButton.Style.SUCCESS);
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmBtn.addActionListener(e -> processPayment());

        btnPanel.add(cancelBtn);
        btnPanel.add(confirmBtn);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
        setPreferredSize(new Dimension(400, 440));
    }

    private void calcChange() {
        try {
            BigDecimal paid = new BigDecimal(paidAmountField.getText().trim());
            BigDecimal change = paid.subtract(finalAmount);
            if (change.compareTo(BigDecimal.ZERO) < 0) {
                changeLabel.setText(String.format("-$%.2f", change.abs()));
                changeLabel.setForeground(UIConstants.DANGER);
            } else {
                changeLabel.setText(String.format("$%.2f", change));
                changeLabel.setForeground(UIConstants.SUCCESS);
            }
        } catch (NumberFormatException ex) {
            changeLabel.setText("—");
            changeLabel.setForeground(UIConstants.TEXT_MUTED);
        }
    }

    private void processPayment() {
        BigDecimal paid;
        try {
            paid = new BigDecimal(paidAmountField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid paid amount");
            return;
        }

        String method = (String) methodCombo.getSelectedItem();
        if ("CASH".equals(method) && paid.compareTo(finalAmount) < 0) {
            JOptionPane.showMessageDialog(this, "Paid amount is less than total due");
            return;
        }

        Payment payment = new Payment();
        payment.setMethod(method);
        payment.setPaidAmount(paid);

        try {
            Order order = orderService.processOrder(items, discount, payment);
            receiptNumber = order.getReceiptNumber();
            completed = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Payment Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCompleted() { return completed; }
    public String getReceiptNumber() { return receiptNumber; }
}
