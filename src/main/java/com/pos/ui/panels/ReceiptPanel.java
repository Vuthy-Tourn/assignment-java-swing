package com.pos.ui.panels;

import com.pos.model.Order;
import com.pos.model.OrderItem;
import com.pos.model.Payment;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.UIConstants;
import com.pos.util.ReceiptPdfExporter;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ReceiptPanel extends JPanel {

    private JPanel contentPanel;

    public ReceiptPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        buildUI();
    }

    private void buildUI() {

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0,
                        UIConstants.BORDER_COLOR
                ),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("Receipt");
        title.setFont(UIConstants.FONT_TITLE);

        topBar.add(title, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(UIConstants.CONTENT_BG);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIConstants.CONTENT_BG);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setOrder(Order order) {

        contentPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        contentPanel.add(createReceiptCard(order), gbc);

        revalidate();
        repaint();
    }

    private JPanel createReceiptCard(Order order) {

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        UIConstants.BORDER_COLOR
                ),
                BorderFactory.createEmptyBorder(
                        30, 30, 30, 30
                )
        ));

        card.setPreferredSize(new Dimension(500, 780));
        card.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));

        // Logo
        card.add(createHeader());

        card.add(Box.createVerticalStrut(20));
        card.add(createDivider());
        card.add(Box.createVerticalStrut(20));

        // Success

        JLabel success = new JLabel(" Payment Successful");
        success.setFont(new Font("Segoe UI", Font.BOLD, 20));
        success.setForeground(UIConstants.SUCCESS);
        success.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(success);

        JLabel receiptNo =
                new JLabel(order.getReceiptNumber());

        receiptNo.setFont(UIConstants.FONT_BODY);
        receiptNo.setForeground(
                UIConstants.TEXT_MUTED
        );
        receiptNo.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.add(Box.createVerticalStrut(6));
        card.add(receiptNo);

        card.add(Box.createVerticalStrut(25));

        // Payment section
        card.add(sectionTitle("Payment Details"));
        card.add(Box.createVerticalStrut(10));

        JPanel paymentPanel = new JPanel();
        paymentPanel.setOpaque(false);
        paymentPanel.setLayout(
                new BoxLayout(
                        paymentPanel,
                        BoxLayout.Y_AXIS
                )
        );

        Payment payment = order.getPayment();

        DateTimeFormatter fmt =
                DateTimeFormatter.ofPattern(
                        "dd MMM yyyy hh:mm a"
                );

        addInfoRow(
                paymentPanel,
                "Receipt",
                order.getReceiptNumber()
        );

        addInfoRow(
                paymentPanel,
                "Date",
                order.getCreatedAt() != null
                        ? order.getCreatedAt().format(fmt)
                        : "-"
        );

        addInfoRow(
                paymentPanel,
                "Payment",
                payment != null
                        ? payment.getMethod()
                        : "-"
        );

        addInfoRow(
                paymentPanel,
                "Cashier",
                order.getUserName()
        );

        card.add(paymentPanel);

        card.add(Box.createVerticalStrut(25));

        // Product section
        card.add(sectionTitle("Products"));
        card.add(Box.createVerticalStrut(12));

        for (OrderItem item : order.getItems()) {

            card.add(productRow(item));
            card.add(Box.createVerticalStrut(10));
        }

        card.add(Box.createVerticalStrut(12));
        card.add(createDivider());
        card.add(Box.createVerticalStrut(15));

        // Totals
        card.add(totalRow(
                "Subtotal",
                formatMoney(order.getTotalAmount())
        ));

        card.add(Box.createVerticalStrut(6));

        card.add(totalRow(
                "Discount",
                "- " + formatMoney(order.getDiscountAmount())
        ));

        card.add(Box.createVerticalStrut(15));

        card.add(createDivider());

        card.add(Box.createVerticalStrut(15));

        card.add(totalRowBold(
                "Final Amount",
                formatMoney(order.getFinalAmount())
        ));

        card.add(Box.createVerticalStrut(30));

        // Buttons
        JPanel buttons =
                new JPanel(new GridLayout(
                        1, 2, 10, 0
                ));

        buttons.setOpaque(false);
        buttons.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        44
                )
        );

        RoundedButton pdfBtn = new RoundedButton("Download PDF", RoundedButton.Style.PRIMARY);

        pdfBtn.addActionListener(e -> {
            // Let the user choose where to save
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Receipt");
            chooser.setSelectedFile(new java.io.File("receipt_" + order.getReceiptNumber() + ".pdf"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                // Ensure .pdf extension
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new java.io.File(file.getAbsolutePath() + ".pdf");
                }
                final java.io.File dest = file;
                // Run export off the EDT
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        ReceiptPdfExporter.export(order, dest);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get(); // rethrow any exception
                            JOptionPane.showMessageDialog(
                                    ReceiptPanel.this,
                                    "PDF saved to:\n" + dest.getAbsolutePath(),
                                    "Success", JOptionPane.INFORMATION_MESSAGE
                            );
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    ReceiptPanel.this,
                                    "Failed to save PDF:\n" + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }.execute();
            }
        });

        RoundedButton doneBtn =
                new RoundedButton(
                        "Done",
                        RoundedButton.Style.SECONDARY
                );

        doneBtn.addActionListener(e -> {
            Window w =
                    SwingUtilities.getWindowAncestor(this);

            if (w instanceof JDialog dialog) {
                dialog.dispose();
            }
        });

        buttons.add(pdfBtn);
        buttons.add(doneBtn);

        card.add(buttons);

        return card;
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        // CHANGE THIS IMAGE PATH
        ImageIcon logo = new ImageIcon(
                Objects.requireNonNull(
                        getClass().getResource(
                                "/images/logo_pos.png"
                        )
                )
        );

        Image scaled =
                logo.getImage()
                        .getScaledInstance(
                                80,
                                80,
                                Image.SCALE_SMOOTH
                        );

        JLabel logoLabel =
                new JLabel(new ImageIcon(scaled));

        logoLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel title =
                new JLabel("POS SYSTEM");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22
                )
        );

        title.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel sub =
                new JLabel("Payment Receipt");

        sub.setForeground(
                UIConstants.TEXT_MUTED
        );

        sub.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(title);
        panel.add(sub);

        return panel;
    }

    private JPanel sectionTitle(String text) {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setOpaque(false);

        JLabel label =
                new JLabel(text);

        label.setFont(
                UIConstants.FONT_HEADING
        );

        panel.add(label);

        return panel;
    }

    private JPanel productRow(OrderItem item) {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );

        panel.setOpaque(false);

        JPanel left =
                new JPanel();

        left.setOpaque(false);
        left.setLayout(
                new BoxLayout(
                        left,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel name =
                new JLabel(
                        item.getProductName()
                );

        name.setFont(
                UIConstants.FONT_BODY
        );

        JLabel qty =
                new JLabel(
                        "Qty: "
                                + item.getQuantity()
                );

        qty.setForeground(
                UIConstants.TEXT_MUTED
        );

        left.add(name);
        left.add(qty);

        JLabel price =
                new JLabel(
                        formatMoney(
                                item.getSubtotal()
                        )
                );

        price.setFont(
                UIConstants.FONT_HEADING
        );

        panel.add(left, BorderLayout.WEST);
        panel.add(price, BorderLayout.EAST);

        return panel;
    }

    private JPanel totalRow(
            String label,
            String value
    ) {

        JPanel row =
                new JPanel(
                        new BorderLayout()
                );

        row.setOpaque(false);

        row.add(new JLabel(label),
                BorderLayout.WEST);

        row.add(new JLabel(value),
                BorderLayout.EAST);

        return row;
    }

    private JPanel totalRowBold(
            String label,
            String value
    ) {

        JPanel row =
                totalRow(label, value);

        ((JLabel) row.getComponent(0))
                .setFont(
                        UIConstants.FONT_HEADING
                );

        JLabel right =
                (JLabel)
                        row.getComponent(1);

        right.setFont(
                UIConstants.FONT_HEADING
        );

        right.setForeground(
                UIConstants.SUCCESS
        );

        return row;
    }

    private void addInfoRow(
            JPanel panel,
            String key,
            String value
    ) {

        JPanel row =
                new JPanel(
                        new BorderLayout()
                );

        row.setOpaque(false);

        JLabel left =
                new JLabel(key);

        left.setForeground(
                UIConstants.TEXT_MUTED
        );

        JLabel right =
                new JLabel(
                        value != null
                                ? value
                                : "-"
                );

        row.add(left,
                BorderLayout.WEST);

        row.add(right,
                BorderLayout.EAST);

        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }

    private JSeparator createDivider() {
        return new JSeparator();
    }

    private String formatMoney(
            BigDecimal amount
    ) {

        if (amount == null)
            return "$0.00";

        return String.format(
                "$%.2f",
                amount
        );
    }
}