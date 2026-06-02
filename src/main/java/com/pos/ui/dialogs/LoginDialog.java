package com.pos.ui.dialogs;

import com.pos.model.User;
import com.pos.service.AuthService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginDialog extends JDialog {

    private final AuthService authService = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private boolean loggedIn = false;

    public LoginDialog(Frame parent) {
        super(parent, "Login — POS System", true);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel();
        header.setBackground(UIConstants.SIDEBAR_BG);
        header.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        JLabel title = new JLabel("POS System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Point of Sale");
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(new Color(148, 163, 184));
        JPanel headerContent = new JPanel();
        headerContent.setOpaque(false);
        headerContent.setLayout(new BoxLayout(headerContent, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerContent.add(title);
        headerContent.add(Box.createVerticalStrut(4));
        headerContent.add(subtitle);
        header.add(headerContent);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        c.gridx = 0; c.weightx = 1.0;

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UIConstants.FONT_HEADING);
        c.gridy = 0;
        form.add(userLabel, c);

        usernameField = new JTextField(20);
        usernameField.setFont(UIConstants.FONT_BODY);
        usernameField.setPreferredSize(new Dimension(280, 38));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        c.gridy = 1;
        form.add(usernameField, c);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UIConstants.FONT_HEADING);
        c.gridy = 2; c.insets = new Insets(14, 0, 6, 0);
        form.add(passLabel, c);

        passwordField = new JPasswordField(20);
        passwordField.setFont(UIConstants.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(280, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        c.gridy = 3; c.insets = new Insets(6, 0, 6, 0);
        form.add(passwordField, c);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIConstants.FONT_SMALL);
        errorLabel.setForeground(UIConstants.DANGER);
        c.gridy = 4;
        form.add(errorLabel, c);

        RoundedButton loginBtn = new RoundedButton("Sign In", RoundedButton.Style.PRIMARY);
        loginBtn.setPreferredSize(new Dimension(280, 42));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        c.gridy = 5; c.insets = new Insets(10, 0, 0, 0);
        form.add(loginBtn, c);

        loginBtn.addActionListener(this::doLogin);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin(null);
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocus();
            }
        });

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
        setPreferredSize(new Dimension(380, 480));
    }

    private void doLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            User user = authService.login(username, password);
            loggedIn = true;
            dispose();
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
        }
    }

    public boolean isLoggedIn() { return loggedIn; }
}
