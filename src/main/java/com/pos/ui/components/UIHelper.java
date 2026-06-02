package com.pos.ui.components;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Factory helpers for consistently styled Swing components.
 * Use these in panel classes to keep the look cohesive.
 */
public final class UIHelper {

    private UIHelper() {}

    // ─────────────────────────────────────────────────────────────────────────
    //  Buttons
    // ─────────────────────────────────────────────────────────────────────────

    /** Solid blue primary button. */
    public static JButton primaryButton(String text) {
        return makeButton(text, UIConstants.ACCENT, UIConstants.ACCENT_HOVER, Color.WHITE);
    }

    /** Outline secondary button. */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                g2.setColor(hover ? new Color(0xF3F4F6) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleFlatButton(btn, UIConstants.TEXT_PRIMARY);
        return btn;
    }

    /** Red danger / delete button. */
    public static JButton dangerButton(String text) {
        return makeButton(text, UIConstants.DANGER, new Color(0xB91C1C), Color.WHITE);
    }

    /** Ghost button — no border, subtle hover. */
    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(0xF3F4F6));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleFlatButton(btn, UIConstants.TEXT_SECONDARY);
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Cards
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * White card panel with rounded corners and a subtle shadow.
     * Add your content to this panel.
     */
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Drop shadow (paint slightly below/right)
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(2, 3, getWidth() - 3, getHeight() - 3, 12, 12);
                // White card surface
                g2.setColor(UIConstants.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return p;
    }

    /**
     * Stat / KPI card with coloured left accent stripe.
     * @param accentColor left-border colour (e.g. UIConstants.SUCCESS)
     */
    public static JPanel statCard(Color accentColor) {
        JPanel p = card();
        p.setBorder(BorderFactory.createCompoundBorder(
                new LeftAccentBorder(accentColor, 4, 12),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Badges / tags
    // ─────────────────────────────────────────────────────────────────────────

    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(UIConstants.FONT_CAPTION);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return lbl;
    }

    public static JLabel successBadge(String text) {
        return badge(text, UIConstants.SUCCESS_BG, UIConstants.SUCCESS_FG);
    }
    public static JLabel warningBadge(String text) {
        return badge(text, UIConstants.WARNING_BG, UIConstants.WARNING_FG);
    }
    public static JLabel dangerBadge(String text) {
        return badge(text, UIConstants.DANGER_BG, UIConstants.DANGER_FG);
    }
    public static JLabel infoBadge(String text) {
        return badge(text, UIConstants.INFO_BG, UIConstants.INFO_FG);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Page header  — title + optional subtitle on the content-area background
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a header row you can add to the top of a panel:
     *   [Title]
     *   [subtitle]   (omitted if blank/null)
     */
    public static JPanel pageHeader(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_TITLE);
        titleLbl.setForeground(UIConstants.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(titleLbl);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel subLbl = new JLabel(subtitle);
            subLbl.setFont(UIConstants.FONT_BODY);
            subLbl.setForeground(UIConstants.TEXT_SECONDARY);
            subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(Box.createVerticalStrut(3));
            p.add(subLbl);
        }
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Styled table
    // ─────────────────────────────────────────────────────────────────────────

    /** Apply consistent styling to a JTable (call once after creation). */
    public static void styleTable(JTable table) {
        table.setFont(UIConstants.FONT_BODY);
        table.setForeground(UIConstants.TEXT_PRIMARY);
        table.setBackground(UIConstants.CARD_BG);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setSelectionBackground(UIConstants.ACCENT_LIGHT);
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setFocusable(false);
        table.getTableHeader().setFont(UIConstants.FONT_SUBHEADING);
        table.getTableHeader().setForeground(UIConstants.TEXT_SECONDARY);
        table.getTableHeader().setBackground(new Color(0xF9FAFB));
        table.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    /** Wraps a table in a scroll pane styled to match cards. */
    public static JScrollPane tableScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        sp.getViewport().setBackground(UIConstants.CARD_BG);
        return sp;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Styled text field
    // ─────────────────────────────────────────────────────────────────────────

    public static JTextField styledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UIConstants.FONT_BODY);
        tf.setForeground(UIConstants.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JComboBox<String> styledComboBox(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(UIConstants.FONT_BODY);
        cb.setForeground(UIConstants.TEXT_PRIMARY);
        cb.setBackground(Color.WHITE);
        return cb;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static JButton makeButton(String text, Color normal, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : normal);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        styleFlatButton(btn, fg);
        return btn;
    }

    private static void styleFlatButton(JButton btn, Color fgColor) {
        btn.setFont(UIConstants.FONT_SUBHEADING);
        btn.setForeground(fgColor);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LeftAccentBorder — coloured stripe on the left edge of a panel
    // ─────────────────────────────────────────────────────────────────────────

    public static class LeftAccentBorder extends AbstractBorder {
        private final Color color;
        private final int   stripeWidth;
        private final int   radius;

        public LeftAccentBorder(Color color, int stripeWidth, int radius) {
            this.color       = color;
            this.stripeWidth = stripeWidth;
            this.radius      = radius;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Card background with drop shadow
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(x + 2, y + 3, w - 3, h - 3, radius, radius);
            g2.setColor(UIConstants.CARD_BG);
            g2.fillRoundRect(x, y, w - 2, h - 2, radius, radius);
            // Coloured stripe
            g2.setColor(color);
            g2.fillRoundRect(x, y, stripeWidth * 2, h - 2, radius, radius);
            g2.fillRect(x + stripeWidth, y, stripeWidth, h - 2);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(0, stripeWidth + 2, 0, 0);
        }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(0, stripeWidth + 2, 0, 0);
            return insets;
        }
    }
}