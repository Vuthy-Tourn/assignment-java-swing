package com.pos.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {

    public enum Style { PRIMARY, SUCCESS, DANGER, SECONDARY, WARNING }

    private Color bgColor;
    private Color hoverColor;
    private int radius = 8;

    public RoundedButton(String text, Style style) {
        super(text);
        applyStyle(style);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(UIConstants.FONT_BODY);
        setForeground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width + 20, getPreferredSize().height + 20));

        Color finalHoverColor = hoverColor;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setBackground(finalHoverColor); repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { setBackground(bgColor); repaint(); }
        });
        setBackground(bgColor);
    }

    public RoundedButton(String text) {
        this(text, Style.PRIMARY);
    }

    private void applyStyle(Style style) {
        switch (style) {
            case SUCCESS  -> { bgColor = UIConstants.SUCCESS; hoverColor = new Color(21, 128, 61); }
            case DANGER   -> { bgColor = UIConstants.DANGER; hoverColor = new Color(185, 28, 28); }
            case SECONDARY -> { bgColor = new Color(107, 114, 128); hoverColor = new Color(75, 85, 99); }
            case WARNING  -> { bgColor = UIConstants.WARNING; hoverColor = new Color(161, 98, 7); }
            default       -> { bgColor = UIConstants.ACCENT; hoverColor = UIConstants.ACCENT; }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        super.paintComponent(g);
        g2.dispose();
    }
}
