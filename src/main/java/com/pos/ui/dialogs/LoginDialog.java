package com.pos.ui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class LoginDialog extends JDialog {
    
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private boolean loggedIn = false;

    public LoginDialog(JFrame parent) {
        super(parent, "Login — POS System", true);
        initializeUI();
    }

    private void initializeUI() {
        setSize(750, 460); 
        setLocationRelativeTo(getParent());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Let FlatLaf know this specific dialog should use custom window title decorations
        getRootPane().putClientProperty(FlatClientProperties.STYLE, "background: #0F172A; foreground: #FFFFFF;");
        
        JPanel splitContainer = new JPanel(new GridLayout(1, 2, 0, 0));
        splitContainer.setBackground(Color.WHITE);
        
        // ==========================================
        // LEFT SIDE: IMAGE/LOGO PANEL
        // ==========================================
        JPanel leftImagePanel = new JPanel(new BorderLayout());
        leftImagePanel.setBackground(new Color(241, 245, 249)); // Balanced soft light gray canvas
        
        JLabel lblIllustration = new JLabel("", SwingConstants.CENTER);
        lblIllustration.setHorizontalAlignment(SwingConstants.CENTER);
        lblIllustration.setVerticalAlignment(SwingConstants.CENTER);
        
        try {
            String basePath = "src/main/resources/images/";
            File imageFile = new File(basePath + "side_banner.png");
            
            if (!imageFile.exists()) {
                File dir = new File(basePath);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().toLowerCase().startsWith("side_banner")) {
                                imageFile = f;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (imageFile.exists()) {
                ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
                Image scaledImg = originalIcon.getImage().getScaledInstance(350, 280, Image.SCALE_SMOOTH);
                lblIllustration.setIcon(new ImageIcon(scaledImg));
            } else {
                // Bold typography title styled fallback layout
                lblIllustration.setText("<html><center><font size='7' color='#1E293B'><b>POS System</b></font><br><font size='4' color='#64748B'>Point of Sale</font></center></html>");
                lblIllustration.setFont(new Font("Segoe UI", Font.BOLD, 24));
            }
        } catch (Exception e) {
            System.err.println("Error rendering sidebar image: " + e.getMessage());
        }
        
        leftImagePanel.add(lblIllustration, BorderLayout.CENTER);
        
        // ==========================================
        // RIGHT SIDE: CLEAN WHITE LOGIN FORM
        // ==========================================
        JPanel rightFormPanel = new JPanel(new BorderLayout());
        rightFormPanel.setBackground(Color.WHITE);
        rightFormPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Header Text
        JPanel textHeader = new JPanel(new GridLayout(2, 1, 4, 4));
        textHeader.setBackground(Color.WHITE);
        
        JLabel lblBrand = new JLabel("Login", SwingConstants.LEFT);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBrand.setForeground(new Color(30, 41, 59)); 
        
        JLabel lblSubtitle = new JLabel("Point of Sales", SwingConstants.LEFT);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
        
        textHeader.add(lblBrand);
        textHeader.add(lblSubtitle);
        rightFormPanel.add(textHeader, BorderLayout.NORTH);
        
        // Form Fields
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsername.setForeground(new Color(71, 85, 105));
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); 
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
        txtUsername.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPassword.setForeground(new Color(71, 85, 105));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        
        fieldsPanel.add(lblUsername);
        fieldsPanel.add(Box.createVerticalStrut(6)); 
        fieldsPanel.add(txtUsername);
        fieldsPanel.add(Box.createVerticalStrut(18)); 
        fieldsPanel.add(lblPassword);
        fieldsPanel.add(Box.createVerticalStrut(6));
        fieldsPanel.add(txtPassword);
        
        rightFormPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        // Login Button
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(Color.WHITE);
        
        btnLogin = new JButton("Log in");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(0, 42)); 
        btnLogin.putClientProperty(FlatClientProperties.STYLE, "background: #2563EB; foreground: #FFFFFF;");
        
        actionPanel.add(btnLogin, BorderLayout.CENTER);
        rightFormPanel.add(actionPanel, BorderLayout.SOUTH);
        
        splitContainer.add(leftImagePanel);
        splitContainer.add(rightFormPanel);
        setContentPane(splitContainer);
        
        btnLogin.addActionListener(this::handleLogin);
        getRootPane().setDefaultButton(btnLogin); 
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both fields.", "Missing Credentials", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.equals("admin") && password.equals("sql1234")) {
            loggedIn = true;
            dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}