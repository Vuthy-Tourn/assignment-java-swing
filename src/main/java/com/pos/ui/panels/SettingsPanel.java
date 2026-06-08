package com.pos.ui.panels;

import com.pos.dao.SettingsDAO;
import com.pos.model.Settings;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.UIConstants;
import com.pos.util.AppContext;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class SettingsPanel extends JPanel {

    private final SettingsDAO settingsDAO = new SettingsDAO();

    private JTextField storeNameField;
    private JTextField storePhoneField;
    private JTextField storeAddressField;
    private JTextField currencyField;
    private JTextField taxField;

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        buildUI();
    }

    private void buildUI() {

        // ================= TOP BAR =================

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0,
                        UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(
                        14, 20, 14, 20)));

        JLabel title = new JLabel("Settings");
        title.setFont(UIConstants.FONT_TITLE);

        topBar.add(title, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);

        // ================= FORM CARD =================

        JPanel formCard = createFormCard();

        // ================= CONTENT =================

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIConstants.CONTENT_BG);

        JPanel leftWrapper = new JPanel(new FlowLayout(
                FlowLayout.LEFT,
                30,
                25));

        leftWrapper.setOpaque(false);

        leftWrapper.add(formCard);

        contentPanel.add(leftWrapper, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);

        scrollPane.setBorder(null);
        scrollPane.getViewport()
                .setBackground(UIConstants.CONTENT_BG);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        loadSettings();
    }
    private JPanel createFormCard() {

        JPanel card = new JPanel(new GridBagLayout());

        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(
                        25, 30, 25, 30)));

        card.setMaximumSize(
                new Dimension(650, Integer.MAX_VALUE));

        card.setPreferredSize(
                new Dimension(650, 400));

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 5, 8, 5);

        storeNameField = field();
        storePhoneField = field();
        storeAddressField = field();
        currencyField = field();
        taxField = field();

        int row = 0;

        addSection(card, c, row++, "Store Information");

        addRow(card, c, row++,
                "Store Name",
                storeNameField);

        addRow(card, c, row++,
                "Phone",
                storePhoneField);

        addRow(card, c, row++,
                "Address",
                storeAddressField);

        addSection(card, c, row++,
                "Financial Settings");

        addRow(card, c, row++,
                "Currency",
                currencyField);

        addRow(card, c, row++,
                "Tax %",
                taxField);

        c.gridy = row++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(20, 0, 0, 0);

        JPanel buttonPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER));

        buttonPanel.setOpaque(false);

        RoundedButton saveButton =
                new RoundedButton(
                        "Save Settings",
                        RoundedButton.Style.SUCCESS);

        saveButton.setPreferredSize(
                new Dimension(180, 30));

        saveButton.addActionListener(
                e -> saveSettings());

        buttonPanel.add(saveButton);

        card.add(buttonPanel, c);

        return card;
    }

    private JTextField field() {

        JTextField field =
                new JTextField();

        field.setFont(UIConstants.FONT_BODY);

        field.setPreferredSize(
                new Dimension(250, 36));

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                UIConstants.BORDER_COLOR),
                        BorderFactory.createEmptyBorder(
                                6, 10, 6, 10)));

        return field;
    }

    private void addSection(
            JPanel form,
            GridBagConstraints c,
            int row,
            String title) {

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;

        JLabel label = new JLabel(title);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14));

        label.setForeground(
                UIConstants.ACCENT);

        form.add(label, c);

        c.gridwidth = 1;
    }

    private void addRow(
            JPanel form,
            GridBagConstraints c,
            int row,
            String text,
            JComponent field) {

        c.gridy = row;

        c.gridx = 0;
        c.weightx = 0;

        JLabel label =
                new JLabel(text + ":");

        label.setPreferredSize(
                new Dimension(120, 32));

        form.add(label, c);

        c.gridx = 1;
        c.weightx = 1.0;

        form.add(field, c);
    }

    private void loadSettings() {

        Settings s = settingsDAO.get();

        storeNameField.setText(
                s.getStoreName() == null
                        ? ""
                        : s.getStoreName());

        storePhoneField.setText(
                s.getStorePhone() == null
                        ? ""
                        : s.getStorePhone());

        storeAddressField.setText(
                s.getStoreAddress() == null
                        ? ""
                        : s.getStoreAddress());

        currencyField.setText(
                s.getCurrency() == null
                        ? "USD"
                        : s.getCurrency());

        taxField.setText(
                s.getTaxPercentage() == null
                        ? "0"
                        : s.getTaxPercentage().toPlainString());
    }

    private void saveSettings() {

        try {

            Settings settings =
                    new Settings();

            settings.setStoreName(
                    storeNameField.getText().trim());

            settings.setStorePhone(
                    storePhoneField.getText().trim());

            settings.setStoreAddress(
                    storeAddressField.getText().trim());

            settings.setCurrency(
                    currencyField.getText().trim());

            settings.setTaxPercentage(
                    new BigDecimal(
                            taxField.getText().trim()));

            settingsDAO.save(settings);

            AppContext.setCurrency(
                    settings.getCurrency());

            JOptionPane.showMessageDialog(
                    this,
                    "Settings saved successfully!");

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
