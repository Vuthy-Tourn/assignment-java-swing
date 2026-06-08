package com.pos.ui.panels;

import com.pos.model.Role;
import com.pos.model.User;
import com.pos.service.UserService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.StyledTable;
import com.pos.ui.components.UIConstants;
import com.pos.util.AppContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {

    private final UserService userService = new UserService();

    private DefaultTableModel tableModel;
    private List<User> currentUsers;

    public UserPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        buildUI();
    }

    private void buildUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel title = new JLabel("User Management");
        title.setFont(UIConstants.FONT_TITLE);
        topBar.add(title, BorderLayout.WEST);

        RoundedButton addBtn = new RoundedButton("+ Add User");
        addBtn.addActionListener(e -> showUserDialog(null));
        topBar.add(addBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Full Name", "Role", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new StyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        tableWrapper.setBackground(UIConstants.CONTENT_BG);
        tableWrapper.add(StyledTable.inScrollPane((StyledTable) table), BorderLayout.CENTER);
        add(tableWrapper, BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actionBar.setBackground(UIConstants.CONTENT_BG);

        RoundedButton editBtn = new RoundedButton("Edit", RoundedButton.Style.SECONDARY);
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first"); return; }
            showUserDialog(currentUsers.get(row));
        });

        RoundedButton pwdBtn = new RoundedButton("Change Password", RoundedButton.Style.WARNING);
        pwdBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first"); return; }
            showChangePasswordDialog(currentUsers.get(row));
        });

        RoundedButton deleteBtn = new RoundedButton("Delete", RoundedButton.Style.DANGER);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first"); return; }
            User u = currentUsers.get(row);
            if (u.getId().equals(AppContext.getCurrentUser().getId())) {
                JOptionPane.showMessageDialog(this, "Cannot delete yourself");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete user: " + u.getUsername() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                userService.delete(u.getId());
                refresh();
            }
        });

        actionBar.add(editBtn);
        actionBar.add(pwdBtn);
        actionBar.add(deleteBtn);
        add(actionBar, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        currentUsers = userService.getAll();
        tableModel.setRowCount(0);
        for (User u : currentUsers) {
            tableModel.addRow(new Object[]{
                    u.getId(), u.getUsername(), u.getFullName(),
                    u.getRoleName() != null ? u.getRoleName() : "",
                    u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : ""
            });
        }
    }

    private void showUserDialog(User existing) {
        List<Role> roles = userService.getRoles();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add User" : "Edit User",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(380, existing == null ? 340 : 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 4, 6, 4);
        c.weightx = 1.0;

        JTextField usernameField  = new JTextField(existing != null ? existing.getUsername() : "", 16);
        JTextField fullNameField  = new JTextField(existing != null ? existing.getFullName() : "", 16);
        JPasswordField pwdField   = new JPasswordField(16);
        JComboBox<Role> roleCombo = new JComboBox<>();
        roles.forEach(roleCombo::addItem);
        if (existing != null && existing.getRoleId() != null) {
            roles.stream().filter(r -> r.getId().equals(existing.getRoleId()))
                    .findFirst().ifPresent(roleCombo::setSelectedItem);
        }

        int row = 0;
        if (existing == null) {
            addRow(form, c, row++, "Username *", usernameField);
            addRow(form, c, row++, "Password *", pwdField);
        }
        addRow(form, c, row++, "Full Name", fullNameField);
        addRow(form, c, row, "Role", roleCombo);

        if (existing != null) usernameField.setEnabled(false);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(Color.WHITE);
        RoundedButton cancelBtn = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dialog.dispose());
        RoundedButton saveBtn = new RoundedButton("Save", RoundedButton.Style.SUCCESS);
        saveBtn.addActionListener(e -> {
            try {
                Role selectedRole = (Role) roleCombo.getSelectedItem();
                if (existing == null) {
                    userService.create(
                            usernameField.getText(),
                            new String(pwdField.getPassword()),
                            fullNameField.getText(),
                            selectedRole != null ? selectedRole.getId() : null);
                } else {
                    existing.setFullName(fullNameField.getText());
                    existing.setRoleId(selectedRole != null ? selectedRole.getId() : null);
                    userService.update(existing);
                }
                dialog.dispose();
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showChangePasswordDialog(User user) {
        JPasswordField newPwd = new JPasswordField(16);
        JPasswordField confirmPwd = new JPasswordField(16);
        Object[] fields = {"New password:", newPwd, "Confirm:", confirmPwd};
        int result = JOptionPane.showConfirmDialog(this, fields,
                "Change Password — " + user.getUsername(), JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String p1 = new String(newPwd.getPassword());
            String p2 = new String(confirmPwd.getPassword());
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match");
                return;
            }
            try {
                userService.changePassword(user.getId(), p1);
                JOptionPane.showMessageDialog(this, "Password changed successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row; c.gridx = 0; c.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setPreferredSize(new Dimension(110, 28));
        form.add(lbl, c);
        c.gridx = 1; c.weightx = 1.0;
        field.setFont(UIConstants.FONT_BODY);
        form.add(field, c);
    }
}
