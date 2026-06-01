package com.pos.ui.panels;

import com.pos.model.DashboardStats;
import com.pos.service.DashboardService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService = new DashboardService();

    private JLabel productsValue;
    private JLabel ordersValue;
    private JLabel revenueValue;
    private JLabel lowStockValue;

    private LineChartPanel revenueChart;
    private BarChartPanel categoryChart;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.CONTENT_BG);
        buildUI();
        refresh();
    }

    private void buildUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Dashboard");
        title.setFont(UIConstants.FONT_TITLE);

        RoundedButton refreshBtn = new RoundedButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(UIConstants.CONTENT_BG);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 16));
        cards.setBackground(UIConstants.CONTENT_BG);

        productsValue = new JLabel("0");
        ordersValue = new JLabel("0");
        revenueValue = new JLabel("$0.00");
        lowStockValue = new JLabel("0");

        cards.add(createCard("Total Products", productsValue, UIConstants.ACCENT));
        cards.add(createCard("Total Orders", ordersValue, UIConstants.SUCCESS));
        cards.add(createCard("Today Revenue", revenueValue, UIConstants.WARNING));
        cards.add(createCard("Low Stock", lowStockValue, UIConstants.DANGER));

        content.add(cards, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 16));
        charts.setBackground(UIConstants.CONTENT_BG);

        revenueChart = new LineChartPanel("Revenue - Last 7 Days");
        categoryChart = new BarChartPanel("Products by Category");

        charts.add(wrapChart(revenueChart));
        charts.add(wrapChart(categoryChart));

        content.add(charts, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.CONTENT_BG
                ),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel wrapChart(JPanel chart) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.add(chart, BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        DashboardStats stats = dashboardService.getStats();

        productsValue.setText(String.valueOf(stats.getTotalProducts()));
        ordersValue.setText(String.valueOf(stats.getTotalOrders()));
        revenueValue.setText(String.format("$%.2f", stats.getTodayRevenue()));
        lowStockValue.setText(String.valueOf(stats.getLowStockProducts()));

        revenueChart.setData(stats.getRevenueByDay());
        categoryChart.setData(stats.getProductsByCategory());
    }

    private static class LineChartPanel extends JPanel {
        private final String title;
        private Map<String, BigDecimal> data;

        public LineChartPanel(String title) {
            this.title = title;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 300));
        }

        public void setData(Map<String, BigDecimal> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setFont(UIConstants.FONT_HEADING);
            g2.setColor(UIConstants.TEXT_PRIMARY);
            g2.drawString(title, 12, 22);

            if (data == null || data.isEmpty()) {
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.drawString("No revenue data", 12, 60);
                g2.dispose();
                return;
            }

            int left = 45;
            int right = 20;
            int top = 45;
            int bottom = 45;

            int chartW = w - left - right;
            int chartH = h - top - bottom;

            BigDecimal max = data.values().stream()
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);

            if (max.compareTo(BigDecimal.ZERO) == 0) {
                max = BigDecimal.ONE;
            }

            g2.setColor(UIConstants.BORDER_COLOR);
            g2.drawLine(left, top, left, top + chartH);
            g2.drawLine(left, top + chartH, left + chartW, top + chartH);

            int size = data.size();
            int i = 0;
            int prevX = -1;
            int prevY = -1;

            for (Map.Entry<String, BigDecimal> entry : data.entrySet()) {
                double value = entry.getValue().doubleValue();
                double maxValue = max.doubleValue();

                int x = left + (size == 1 ? chartW / 2 : i * chartW / (size - 1));
                int y = top + chartH - (int) ((value / maxValue) * chartH);

                g2.setColor(UIConstants.ACCENT);

                if (prevX != -1) {
                    g2.drawLine(prevX, prevY, x, y);
                }

                g2.fillOval(x - 4, y - 4, 8, 8);

                g2.setColor(UIConstants.TEXT_MUTED);
                g2.setFont(UIConstants.FONT_SMALL);
                g2.drawString(entry.getKey().substring(5), x - 18, top + chartH + 18);

                prevX = x;
                prevY = y;
                i++;
            }

            g2.dispose();
        }
    }

    private static class BarChartPanel extends JPanel {
        private final String title;
        private Map<String, Integer> data;

        public BarChartPanel(String title) {
            this.title = title;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 300));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setFont(UIConstants.FONT_HEADING);
            g2.setColor(UIConstants.TEXT_PRIMARY);
            g2.drawString(title, 12, 22);

            if (data == null || data.isEmpty()) {
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.drawString("No category data", 12, 60);
                g2.dispose();
                return;
            }

            int left = 45;
            int right = 20;
            int top = 45;
            int bottom = 55;

            int chartW = w - left - right;
            int chartH = h - top - bottom;

            int max = data.values().stream().max(Integer::compareTo).orElse(1);
            if (max == 0) max = 1;

            g2.setColor(UIConstants.BORDER_COLOR);
            g2.drawLine(left, top, left, top + chartH);
            g2.drawLine(left, top + chartH, left + chartW, top + chartH);

            int count = data.size();
            int gap = 12;
            int barW = Math.max(20, (chartW - gap * (count + 1)) / count);

            int i = 0;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int value = entry.getValue();
                int barH = (int) ((value / (double) max) * chartH);

                int x = left + gap + i * (barW + gap);
                int y = top + chartH - barH;

                g2.setColor(UIConstants.SUCCESS);
                g2.fillRoundRect(x, y, barW, barH, 8, 8);

                g2.setColor(UIConstants.TEXT_PRIMARY);
                g2.setFont(UIConstants.FONT_SMALL);
                g2.drawString(String.valueOf(value), x + barW / 2 - 4, y - 5);

                String label = entry.getKey();
                if (label.length() > 8) {
                    label = label.substring(0, 8) + "...";
                }

                g2.setColor(UIConstants.TEXT_MUTED);
                g2.drawString(label, x, top + chartH + 18);

                i++;
            }

            g2.dispose();
        }
    }
}