package com.pos.ui;

import com.pos.ui.components.UIConstants;
import com.pos.ui.panels.*;
import com.pos.util.AppContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebar;

    private DashboardPanel dashboardPanel;
    private SalesPanel salesPanel;
    private ProductPanel productPanel;
    private StockPanel stockPanel;
    private OrderPanel orderPanel;
    private UserPanel userPanel;
    private SettingsPanel settingsPanel;

    public MainFrame() {
        setTitle("POS System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        salesPanel    = new SalesPanel();
        productPanel  = new ProductPanel();
        stockPanel    = new StockPanel();
        orderPanel    = new OrderPanel();
        userPanel     = new UserPanel();
        settingsPanel = new SettingsPanel();
        dashboardPanel = new DashboardPanel();

        if (AppContext.isAdmin()) {
            contentPanel.add(dashboardPanel, "DASHBOARD");
        }
        contentPanel.add(salesPanel,    "SALES");
        contentPanel.add(productPanel,  "PRODUCTS");
        contentPanel.add(stockPanel,    "STOCK");
        contentPanel.add(orderPanel,    "ORDERS");
        if (AppContext.isAdmin()) {
            contentPanel.add(userPanel,     "USERS");
            contentPanel.add(settingsPanel, "SETTINGS");
        }

        add(contentPanel, BorderLayout.CENTER);
        if (AppContext.isAdmin()) {
            showPanel("DASHBOARD");
        } else {
            showPanel("SALES");
        }
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.SIDEBAR_BG);
        panel.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 20));
        logo.setOpaque(false);
        JLabel logoLabel = new JLabel("POS");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(Color.WHITE);
        logo.add(logoLabel);
        panel.add(logo);

        // User info
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        userInfo.setOpaque(false);
        JLabel userLabel = new JLabel(AppContext.getCurrentUser().getFullName());
        userLabel.setFont(UIConstants.FONT_SMALL);
        userLabel.setForeground(new Color(148, 163, 184));
        userInfo.add(userLabel);
        panel.add(userInfo);
        panel.add(Box.createVerticalStrut(16));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(51, 65, 85));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(8));

        // Nav items
        if (AppContext.isAdmin()) {
            addNavItem(panel, "Dashboard", "DASHBOARD");
        }
        addNavItem(panel, "Sales",     "SALES");
        addNavItem(panel, "Products",  "PRODUCTS");
        addNavItem(panel, "Stock",     "STOCK");
        addNavItem(panel, "Orders",    "ORDERS");
        if (AppContext.isAdmin()) {
            addNavItem(panel, "Users",   "USERS");
            addNavItem(panel, "Settings","SETTINGS");
        }

        panel.add(Box.createVerticalGlue());

        // Logout button
        JLabel logoutLabel = new JLabel("Log out");
        logoutLabel.setFont(UIConstants.FONT_BODY);
        logoutLabel.setForeground(new Color(248, 113, 113));
        logoutLabel.setBorder(BorderFactory.createEmptyBorder(12, 16, 20, 16));
        logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int choice = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Log out?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    AppContext.logout();
                    dispose();
                    System.exit(0);
                }
            }
        });
        panel.add(logoutLabel);
        return panel;
    }

    private void addNavItem(JPanel parent, String label, String panelKey) {
        JLabel item = new JLabel("  " + label);
        item.setFont(UIConstants.FONT_BODY);
        item.setForeground(UIConstants.SIDEBAR_TEXT);
        item.setOpaque(true);
        item.setBackground(UIConstants.SIDEBAR_BG);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 40));
        item.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.putClientProperty("panelKey", panelKey);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { showPanel(panelKey); }
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(30, 41, 59));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                String active = (String) item.getClientProperty("active");
                item.setBackground("true".equals(active) ? UIConstants.SIDEBAR_ACTIVE : UIConstants.SIDEBAR_BG);
            }
        });
        parent.add(item);
    }

    public void showPanel(String panelKey) {
        cardLayout.show(contentPanel, panelKey);
        // Highlight active nav item
        for (Component c : sidebar.getComponents()) {
            if (c instanceof JLabel lbl && lbl.getClientProperty("panelKey") != null) {
                boolean active = panelKey.equals(lbl.getClientProperty("panelKey"));
                lbl.putClientProperty("active", active ? "true" : "false");
                lbl.setBackground(active ? UIConstants.SIDEBAR_ACTIVE : UIConstants.SIDEBAR_BG);
            }
        }
        // Refresh panels on show
        switch (panelKey) {
            case "PRODUCTS" -> productPanel.refresh();
            case "STOCK"    -> stockPanel.refresh();
            case "ORDERS"   -> orderPanel.refresh();
            case "USERS"    -> userPanel.refresh();
            case "SETTINGS" -> settingsPanel.loadSettings();
            case "DASHBOARD" -> {
                if (dashboardPanel != null) dashboardPanel.refresh();
            }
        }
    }
}
