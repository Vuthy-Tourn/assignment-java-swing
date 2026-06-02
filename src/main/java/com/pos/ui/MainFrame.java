package com.pos.ui;

import com.pos.model.Order;
import com.pos.ui.components.UIConstants;
import com.pos.ui.panels.*;
import com.pos.util.AppContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebar;

    private final List<JPanel> navItems = new ArrayList<>();

    private DashboardPanel dashboardPanel;
    private SalesPanel     salesPanel;
    private ProductPanel   productPanel;
    private StockPanel     stockPanel;
    private OrderPanel     orderPanel;
    private UserPanel      userPanel;
    private SettingsPanel  settingsPanel;
    private ReceiptPanel receiptPanel;

    // Unicode icons that render reliably in Segoe UI Symbol / common system fonts
    private static final Font ICON_FONT = new Font("Segoe UI Symbol", Font.PLAIN, 15);

    public MainFrame() {
        setTitle("POS System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildUI();
    }

    // ── Root layout ──────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());
        sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.CONTENT_BG);

        salesPanel     = new SalesPanel();
        productPanel   = new ProductPanel();
        stockPanel     = new StockPanel();
        orderPanel     = new OrderPanel();
        userPanel      = new UserPanel();
        settingsPanel  = new SettingsPanel();
        dashboardPanel = new DashboardPanel();
        receiptPanel   = new ReceiptPanel();

        if (AppContext.isAdmin()) contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(salesPanel,   "SALES");
        contentPanel.add(productPanel, "PRODUCTS");
        contentPanel.add(stockPanel,   "STOCK");
        contentPanel.add(orderPanel,   "ORDERS");
        contentPanel.add(receiptPanel, "RECEIPT");
        if (AppContext.isAdmin()) {
            contentPanel.add(userPanel,    "USERS");
            contentPanel.add(settingsPanel,"SETTINGS");
        }

        add(contentPanel, BorderLayout.CENTER);
        showPanel(AppContext.isAdmin() ? "DASHBOARD" : "SALES");
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConstants.SIDEBAR_BG);
        panel.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.BORDER_COLOR));

        panel.add(buildLogoArea());
        panel.add(Box.createVerticalStrut(8));
        panel.add(buildSidebarDivider());
        panel.add(Box.createVerticalStrut(8));

        if (AppContext.isAdmin())
            addNavItem(panel, "\uD83D\uDCCA", "Dashboard", "DASHBOARD"); // 📊
        addNavItem(panel, "\uD83D\uDCB0", "Sales",    "SALES");     // 💰
        addNavItem(panel, "\uD83D\uDCE6", "Products", "PRODUCTS");  // 📦
        addNavItem(panel, "\uD83D\uDCC8", "Stock",    "STOCK");     // 📈
        addNavItem(panel, "\uD83D\uDCCB", "Orders",   "ORDERS");    // 📋
        if (AppContext.isAdmin()) {
            addNavItem(panel, "\uD83D\uDC65", "Users",    "USERS");     // 👥
            addNavItem(panel, "\u2699\uFE0F",  "Settings", "SETTINGS"); // ⚙️
        }

        panel.add(Box.createVerticalGlue());

        panel.add(buildSidebarDivider());
        panel.add(Box.createVerticalStrut(10));

        panel.add(buildUserArea());

        panel.add(Box.createVerticalStrut(6));
        panel.add(buildLogoutArea());
        return panel;
    }
    public void openReceipt(Order order) {

        receiptPanel.setOrder(order);

        cardLayout.show(contentPanel, "RECEIPT");
    }

    // ── Logo area ─────────────────────────────────────────────────────────────

    private JPanel buildLogoArea() {

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Load logo
        ImageIcon icon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/images/ミニストップ　～街角のあなたの憩いの場～ copy.png"))
        );

        // Resize logo
        Image scaledImage = icon.getImage().getScaledInstance(
                50,
                50,
                Image.SCALE_SMOOTH
        );

        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));

        JLabel title = new JLabel("POS System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel subtitle = new JLabel("Management");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(UIConstants.TEXT_MUTED);

        textPanel.add(title);
        textPanel.add(subtitle);

        row.add(logoLabel);
        row.add(textPanel);

        return row;
    }

    // ── User area ─────────────────────────────────────────────────────────────

    private JPanel buildUserArea() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        String fullName = AppContext.getCurrentUser().getFullName();
        String initials = deriveInitials(fullName);

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.AVATAR_BG);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.setColor(UIConstants.AVATAR_FG);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials,
                        (getWidth()  - fm.stringWidth(initials)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(fullName);
        nameLabel.setFont(UIConstants.FONT_SMALL);
        nameLabel.setForeground(UIConstants.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(AppContext.isAdmin() ? "Administrator" : "Staff");
        roleLabel.setFont(UIConstants.FONT_CAPTION);
        roleLabel.setForeground(UIConstants.ROLE_TEXT);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textCol.add(nameLabel);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(roleLabel);

        row.add(avatar);
        row.add(textCol);
        return row;
    }

    // ── Divider ───────────────────────────────────────────────────────────────

    private JPanel buildSidebarDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIConstants.SIDEBAR_DIVIDER);
                g.fillRect(16, 0, getWidth() - 32, 1);
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 1));
        return d;
    }

    // ── Nav item with icon ────────────────────────────────────────────────────

    private void addNavItem(JPanel parent, String icon, String label, String panelKey) {

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        wrapper.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 42));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        wrapper.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wrapper.putClientProperty("panelKey", panelKey);

        JPanel pill = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = "true".equals(wrapper.getClientProperty("active"));
                boolean hover  = "true".equals(wrapper.getClientProperty("hover"));
                if (active) {
                    g2.setColor(UIConstants.SIDEBAR_ACTIVE);
                } else if (hover) {
                    g2.setColor(UIConstants.SIDEBAR_HOVER);
                } else {
                    g2.dispose();
                    super.paintComponent(g);
                    return;
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        // Icon label
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(ICON_FONT);
        iconLbl.setForeground(UIConstants.SIDEBAR_TEXT);
        iconLbl.setPreferredSize(new Dimension(24, 24));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // Text label
        JLabel textLbl = new JLabel(label);
        textLbl.setFont(UIConstants.FONT_BODY);
        textLbl.setForeground(UIConstants.SIDEBAR_TEXT);
        textLbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        pill.add(iconLbl, BorderLayout.WEST);
        pill.add(textLbl, BorderLayout.CENTER);

        wrapper.add(pill, BorderLayout.CENTER);
        wrapper.putClientProperty("iconLbl", iconLbl);
        wrapper.putClientProperty("textLbl", textLbl);
        wrapper.putClientProperty("pill",    pill);

        wrapper.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showPanel(panelKey); }
            @Override public void mouseEntered(MouseEvent e) {
                if (!"true".equals(wrapper.getClientProperty("active"))) {
                    wrapper.putClientProperty("hover", "true");
                    iconLbl.setForeground(UIConstants.TEXT_PRIMARY);
                    textLbl.setForeground(UIConstants.TEXT_PRIMARY);
                    pill.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                wrapper.putClientProperty("hover", "false");
                if (!"true".equals(wrapper.getClientProperty("active"))) {
                    iconLbl.setForeground(UIConstants.SIDEBAR_TEXT);
                    textLbl.setForeground(UIConstants.SIDEBAR_TEXT);
                }
                pill.repaint();
            }
        });

        parent.add(wrapper);
        navItems.add(wrapper);
    }

    // ── Logout row ────────────────────────────────────────────────────────────

    private JPanel buildLogoutArea() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLbl = new JLabel("\u21B6"); // ↶ curved arrow
        iconLbl.setFont(ICON_FONT);
        iconLbl.setForeground(new Color(0xEF4444));

        JLabel textLbl = new JLabel("Log out");
        textLbl.setFont(UIConstants.FONT_BODY);
        textLbl.setForeground(new Color(0xEF4444));
        textLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter logoutHover = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int ok = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Are you sure you want to log out?",
                        "Confirm Logout", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (ok == JOptionPane.YES_OPTION) {
                    AppContext.logout();
                    dispose();
                    System.exit(0);
                }
            }
            @Override public void mouseEntered(MouseEvent e) {
                iconLbl.setForeground(new Color(0xDC2626));
                textLbl.setForeground(new Color(0xDC2626));
            }
            @Override public void mouseExited(MouseEvent e) {
                iconLbl.setForeground(new Color(0xEF4444));
                textLbl.setForeground(new Color(0xEF4444));
            }
        };
        iconLbl.addMouseListener(logoutHover);
        textLbl.addMouseListener(logoutHover);

        row.add(iconLbl);
        row.add(textLbl);
        return row;
    }

    // ── Panel switching ───────────────────────────────────────────────────────

    public void showPanel(String panelKey) {
        cardLayout.show(contentPanel, panelKey);

        for (JPanel wrapper : navItems) {
            boolean active = panelKey.equals(wrapper.getClientProperty("panelKey"));
            wrapper.putClientProperty("active", active ? "true" : "false");
            wrapper.putClientProperty("hover",  "false");

            JLabel iconLbl = (JLabel) wrapper.getClientProperty("iconLbl");
            JLabel textLbl = (JLabel) wrapper.getClientProperty("textLbl");
            JPanel pill    = (JPanel) wrapper.getClientProperty("pill");

            Color fg = active ? UIConstants.SIDEBAR_TEXT_ACTIVE : UIConstants.SIDEBAR_TEXT;
            Font  tf = active ? UIConstants.FONT_SUBHEADING : UIConstants.FONT_BODY;

            if (iconLbl != null) iconLbl.setForeground(fg);
            if (textLbl != null) { textLbl.setForeground(fg); textLbl.setFont(tf); }
            if (pill    != null) pill.repaint();
        }

        switch (panelKey) {
            case "PRODUCTS"  -> productPanel.refresh();
            case "STOCK"     -> stockPanel.refresh();
            case "ORDERS"    -> orderPanel.refresh();
            case "USERS"     -> userPanel.refresh();
            case "DASHBOARD" -> { if (dashboardPanel != null) dashboardPanel.refresh(); }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String deriveInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}