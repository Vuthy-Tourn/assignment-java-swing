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
        setBackground(UIConstants.BG_LIGHT);
        buildUI();
    }

    private void buildUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel title = new JLabel("Settings");
        title.setFont(UIConstants.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 4, 8, 4);
        c.weightx = 1.0;

        storeNameField    = field();
        storePhoneField   = field();
        storeAddressField = field();
        currencyField     = field();
        taxField          = field();

        int row = 0;
        addSection(formCard, c, row++, "Store Information");
        addRow(formCard, c, row++, "Store Name", storeNameField);
        addRow(formCard, c, row++, "Phone", storePhoneField);
        addRow(formCard, c, row++, "Address", storeAddressField);
        addSection(formCard, c, row++, "Financial");
        addRow(formCard, c, row++, "Currency", currencyField);
        addRow(formCard, c, row, "Tax %", taxField);

        loadSettings();

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setBackground(UIConstants.BG_LIGHT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));
        formCard.setMaximumSize(new Dimension(500, 500));
        JPanel sized = new JPanel(new BorderLayout());
        sized.setBackground(UIConstants.BG_LIGHT);
        sized.setBorder(BorderFactory.createEmptyBorder(24, 80, 0, 80));
        sized.add(formCard, BorderLayout.CENTER);
        add(sized, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        btnPanel.setBackground(UIConstants.BG_LIGHT);
        RoundedButton saveBtn = new RoundedButton("Save Settings", RoundedButton.Style.SUCCESS);
        saveBtn.setPreferredSize(new Dimension(180, 38));
        saveBtn.addActionListener(e -> saveSettings());
        btnPanel.add(saveBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JTextField field() {
        JTextField f = new JTextField(24);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private void addSection(JPanel form, GridBagConstraints c, int row, String text) {
        c.gridy = row; c.gridx = 0; c.gridwidth = 2;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UIConstants.PRIMARY);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER));
        form.add(lbl, c);
        c.gridwidth = 1;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row; c.gridx = 0; c.weightx = 0;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setPreferredSize(new Dimension(120, 32));
        form.add(lbl, c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(field, c);
    }

    private void loadSettings() {
        Settings s = settingsDAO.get();
        storeNameField.setText(s.getStoreName() != null ? s.getStoreName() : "");
        storePhoneField.setText(s.getStorePhone() != null ? s.getStorePhone() : "");
        storeAddressField.setText(s.getStoreAddress() != null ? s.getStoreAddress() : "");
        currencyField.setText(s.getCurrency() != null ? s.getCurrency() : "USD");
        taxField.setText(s.getTaxPercentage() != null ? s.getTaxPercentage().toPlainString() : "0");
    }

    private void saveSettings() {
        try {
            Settings s = new Settings();
            s.setStoreName(storeNameField.getText().trim());
            s.setStorePhone(storePhoneField.getText().trim());
            s.setStoreAddress(storeAddressField.getText().trim());
            s.setCurrency(currencyField.getText().trim().isEmpty() ? "USD" : currencyField.getText().trim());
            s.setTaxPercentage(new BigDecimal(taxField.getText().trim()));
            settingsDAO.save(s);
            AppContext.setCurrency(s.getCurrency());
            JOptionPane.showMessageDialog(this, "Settings saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid tax percentage", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
