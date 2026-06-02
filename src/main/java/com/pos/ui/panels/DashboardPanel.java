package com.pos.ui.panels;

import com.pos.model.DashboardStats;
import com.pos.service.DashboardService;
import com.pos.ui.components.RoundedButton;
import com.pos.ui.components.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final DashboardService dashboardService;

    private StatCard productsCard;
    private StatCard ordersCard;
    private StatCard revenueCard;
    private StatCard lowStockCard;

    private LineChartPanel revenueChart;
    private BarChartPanel  categoryChart;

    public DashboardPanel() {
        this.dashboardService = new DashboardService();
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_LIGHT);
        buildUI();
        refresh();
    }

    private void buildUI() {
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel sub = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        sub.setFont(UIConstants.FONT_SMALL);
        sub.setForeground(UIConstants.TEXT_MUTED);

        left.add(title);
        left.add(sub);

        RoundedButton refreshBtn = new RoundedButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        bar.add(left,       BorderLayout.WEST);
        bar.add(refreshBtn, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(UIConstants.BG_LIGHT);
        content.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        content.add(buildStatRow(),  BorderLayout.NORTH);
        content.add(buildChartRow(), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildStatRow() {
        productsCard = new StatCard("Total Products", "0",     UIConstants.PRIMARY,
                new Color(37,  99, 235), new Color(99, 151, 255), "P");
        ordersCard   = new StatCard("Total Orders",   "0",     UIConstants.SUCCESS,
                new Color(16, 185, 129), new Color(52, 225, 165), "O");
        revenueCard  = new StatCard("Today Revenue",  "$0.00", UIConstants.WARNING,
                new Color(245,158,  11), new Color(251,200,  70), "$");
        lowStockCard = new StatCard("Low Stock Items","0",     UIConstants.DANGER,
                new Color(220, 38,  38), new Color(248, 95,  95), "!");

        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setBackground(UIConstants.BG_LIGHT);
        row.add(productsCard);
        row.add(ordersCard);
        row.add(revenueCard);
        row.add(lowStockCard);
        return row;
    }

    private JPanel buildChartRow() {
        revenueChart  = new LineChartPanel("Revenue — Last 7 Days");
        categoryChart = new BarChartPanel("Products by Category");

        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setBackground(UIConstants.BG_LIGHT);
        row.add(wrapChart(revenueChart));
        row.add(wrapChart(categoryChart));
        return row;
    }

    private JPanel wrapChart(JPanel chart) {
        JPanel w = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Subtle bottom-right shadow simulation
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(3, 3, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        w.setBackground(Color.WHITE);
        w.setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(Color.WHITE);
        inner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        inner.add(chart, BorderLayout.CENTER);

        w.add(inner, BorderLayout.CENTER);
        w.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 3));
        return w;
    }

    public void refresh() {
        DashboardStats s = dashboardService.getStats();
        productsCard.setValue(String.valueOf(s.getTotalProducts()));
        ordersCard.setValue(String.valueOf(s.getTotalOrders()));
        revenueCard.setValue(String.format("$%.2f", s.getTodayRevenue()));
        lowStockCard.setValue(String.valueOf(s.getLowStockProducts()));

        // Fill all 7 days so the line chart always renders — missing days get $0
        Map<String, BigDecimal> raw    = s.getRevenueByDay();
        Map<String, BigDecimal> filled = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            filled.put(date, raw.getOrDefault(date, BigDecimal.ZERO));
        }
        revenueChart.setData(filled);
        categoryChart.setData(s.getProductsByCategory());
    }

    // =========================================================================
    // Inner classes
    // =========================================================================

    /**
     * Stat card with a gradient icon circle, large value, and uppercase title.
     * Demonstrates ENCAPSULATION: all internal components are private; only
     * setValue() is exposed to callers.
     */
    private static class StatCard extends JPanel {

        private final Color   circleTop;
        private final Color   circleBottom;
        private final String  iconLetter;
        private final JLabel  valueLabel;

        StatCard(String title, String initial, Color textColor,
                 Color circleTop, Color circleBottom, String iconLetter) {
            this.circleTop    = circleTop;
            this.circleBottom = circleBottom;
            this.iconLetter   = iconLetter;

            setLayout(new BorderLayout(16, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIConstants.BORDER),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)));

            // Gradient icon circle — painted via anonymous JPanel
            JPanel circle = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    int d  = Math.min(getWidth(), getHeight());
                    int cx = (getWidth()  - d) / 2;
                    int cy = (getHeight() - d) / 2;

                    // Gradient fill
                    g2.setPaint(new GradientPaint(cx, cy, circleTop, cx + d, cy + d, circleBottom));
                    g2.fillOval(cx, cy, d, d);

                    // Translucent shine on top-left
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.fillOval(cx + d / 6, cy + d / 8, d / 2, d / 3);

                    // Icon letter
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, d * 2 / 5));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(iconLetter,
                            cx + (d - fm.stringWidth(iconLetter)) / 2,
                            cy + (d + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            circle.setOpaque(false);
            circle.setPreferredSize(new Dimension(56, 56));

            // Text area: value + title
            JPanel text = new JPanel(new GridLayout(2, 1, 0, 6));
            text.setOpaque(false);

            valueLabel = new JLabel(initial);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
            valueLabel.setForeground(textColor);

            JLabel titleLabel = new JLabel(title.toUpperCase());
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            titleLabel.setForeground(UIConstants.TEXT_MUTED);

            text.add(valueLabel);
            text.add(titleLabel);

            add(circle, BorderLayout.WEST);
            add(text,   BorderLayout.CENTER);
        }

        /** Updates the displayed metric value. */
        void setValue(String text) { valueLabel.setText(text); }
    }

    /**
     * Abstract base for all chart panels.
     * Demonstrates ABSTRACTION: the final paintComponent() implements the shared
     * rendering contract (title, separator, grid, empty state) and forces subclasses
     * to supply isEmpty() and drawChart() via abstract methods.
     */
    private static abstract class ChartPanel extends JPanel {

        protected final String title;

        protected static final int LEFT  = 56;
        protected static final int RIGHT = 16;
        protected static final int TOP   = 44;
        protected static final int GRID  =  4;

        ChartPanel(String title) {
            this.title = title;
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 290));
        }

        /** Returns true when no data is available to paint. */
        protected abstract boolean isEmpty();

        /**
         * Renders chart data into the given Graphics2D context.
         * Demonstrates POLYMORPHISM: LineChartPanel and BarChartPanel each supply
         * their own implementation.
         */
        protected abstract void drawChart(Graphics2D g2, int w, int h);

        @Override
        protected final void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            // Chart title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(UIConstants.TEXT_PRIMARY);
            g2.drawString(title, 0, 18);

            // Separator line
            g2.setColor(new Color(243, 244, 246));
            g2.fillRect(0, 26, getWidth(), 2);

            if (isEmpty()) {
                g2.setFont(UIConstants.FONT_BODY);
                g2.setColor(UIConstants.TEXT_MUTED);
                g2.drawString("No data available", LEFT, TOP + 40);
            } else {
                drawChart(g2, getWidth(), getHeight());
            }
            g2.dispose();
        }

        /**
         * Draws Y-axis labels, dashed horizontal grid lines, and solid axes.
         * Shared by both subclasses, demonstrating the template-method pattern.
         */
        protected void drawGrid(Graphics2D g2, int cx, int cy, int cw, int ch, double maxVal) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 1f, new float[]{3f, 5f}, 0f);

            for (int i = 0; i <= GRID; i++) {
                int y = cy + ch - i * ch / GRID;
                if (i > 0) {
                    Stroke old = g2.getStroke();
                    g2.setStroke(dashed);
                    g2.setColor(new Color(243, 244, 246));
                    g2.drawLine(cx, y, cx + cw, y);
                    g2.setStroke(old);
                }
                String lbl = axisLabel(maxVal * i / GRID);
                g2.setColor(UIConstants.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, cx - fm.stringWidth(lbl) - 5,
                        y + fm.getAscent() / 2 - 1);
            }

            g2.setColor(new Color(226, 232, 240));
            g2.drawLine(cx, cy, cx, cy + ch);
            g2.drawLine(cx, cy + ch, cx + cw, cy + ch);
        }

        private static String axisLabel(double v) {
            if (v >= 1000) return String.format("%.0fk", v / 1000);
            if (v == (int) v) return String.valueOf((int) v);
            return String.format("%.1f", v);
        }
    }

    /**
     * Line chart using smooth cubic bezier curves and a gradient-faded fill.
     * Demonstrates INHERITANCE: extends ChartPanel and overrides isEmpty() + drawChart().
     */
    private static class LineChartPanel extends ChartPanel {

        private Map<String, BigDecimal> data;

        LineChartPanel(String title) { super(title); }

        void setData(Map<String, BigDecimal> data) { this.data = data; repaint(); }

        @Override protected boolean isEmpty() { return data == null || data.isEmpty(); }

        @Override
        protected void drawChart(Graphics2D g2, int w, int h) {
            int bottom = 40;
            int chartW = w - LEFT - RIGHT;
            int chartH = h - TOP - bottom;

            BigDecimal max = data.values().stream()
                    .max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

            drawGrid(g2, LEFT, TOP, chartW, chartH, max.doubleValue());

            int n = data.size();
            float[] xs = new float[n];
            float[] ys = new float[n];
            int i = 0;
            for (Map.Entry<String, BigDecimal> e : data.entrySet()) {
                double ratio = e.getValue().doubleValue() / max.doubleValue();
                xs[i] = LEFT + (n == 1 ? chartW / 2f : (float) i * chartW / (n - 1));
                ys[i] = TOP  + chartH - (float)(ratio * chartH);
                i++;
            }

            // Build smooth bezier line path
            Path2D.Float linePath = new Path2D.Float();
            linePath.moveTo(xs[0], ys[0]);
            for (int j = 1; j < n; j++) {
                float cpX = (xs[j - 1] + xs[j]) / 2f;
                linePath.curveTo(cpX, ys[j - 1], cpX, ys[j], xs[j], ys[j]);
            }

            // Gradient fill under the curve
            Path2D.Float fill = new Path2D.Float(linePath);
            fill.lineTo(xs[n - 1], TOP + chartH);
            fill.lineTo(xs[0],     TOP + chartH);
            fill.closePath();

            g2.setPaint(new GradientPaint(0, TOP,
                    new Color(UIConstants.PRIMARY.getRed(),
                              UIConstants.PRIMARY.getGreen(),
                              UIConstants.PRIMARY.getBlue(), 55),
                    0, TOP + chartH,
                    new Color(UIConstants.PRIMARY.getRed(),
                              UIConstants.PRIMARY.getGreen(),
                              UIConstants.PRIMARY.getBlue(), 0)));
            g2.fill(fill);

            // Draw the smooth line
            g2.setPaint(null);
            g2.setColor(UIConstants.PRIMARY);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(linePath);
            g2.setStroke(new BasicStroke(1f));

            // Markers (white inner dot, colored border)
            i = 0;
            for (Map.Entry<String, BigDecimal> e : data.entrySet()) {
                int mx = Math.round(xs[i]);
                int my = Math.round(ys[i]);

                g2.setColor(new Color(UIConstants.PRIMARY.getRed(),
                        UIConstants.PRIMARY.getGreen(), UIConstants.PRIMARY.getBlue(), 25));
                g2.fillOval(mx - 7, my - 7, 15, 15);

                g2.setColor(Color.WHITE);
                g2.fillOval(mx - 4, my - 4, 9, 9);

                g2.setColor(UIConstants.PRIMARY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(mx - 4, my - 4, 9, 9);
                g2.setStroke(new BasicStroke(1f));

                // X-axis date label
                String lbl = e.getKey().length() >= 7 ? e.getKey().substring(5) : e.getKey();
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(UIConstants.TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, mx - fm.stringWidth(lbl) / 2, TOP + chartH + 18);
                i++;
            }
        }
    }

    /**
     * Bar chart with a per-bar color palette and gradient fills.
     * Demonstrates INHERITANCE: extends ChartPanel with its own drawChart().
     */
    private static class BarChartPanel extends ChartPanel {

        private static final Color[] PALETTE = {
            new Color( 37,  99, 235), new Color( 16, 185, 129),
            new Color(245, 158,  11), new Color(239,  68,  68),
            new Color(168,  85, 247), new Color( 20, 184, 166),
            new Color(234,  88,  12), new Color( 99, 102, 241),
            new Color(236,  72, 153), new Color( 14, 165, 233),
        };

        private Map<String, Integer> data;

        BarChartPanel(String title) { super(title); }

        void setData(Map<String, Integer> data) { this.data = data; repaint(); }

        @Override protected boolean isEmpty() { return data == null || data.isEmpty(); }

        @Override
        protected void drawChart(Graphics2D g2, int w, int h) {
            int bottom = 72;
            int chartW = w - LEFT - RIGHT;
            int chartH = h - TOP - bottom;

            int max = data.values().stream().max(Integer::compareTo).orElse(1);
            if (max == 0) max = 1;

            drawGrid(g2, LEFT, TOP, chartW, chartH, max);

            int count = data.size();
            int gap   = Math.max(6, (chartW / Math.max(count, 1)) / 5);
            int barW  = Math.max(14, (chartW - gap * (count + 1)) / count);
            int i     = 0;

            for (Map.Entry<String, Integer> e : data.entrySet()) {
                Color base    = PALETTE[i % PALETTE.length];
                Color lighter = new Color(
                        Math.min(255, base.getRed()   + 60),
                        Math.min(255, base.getGreen() + 60),
                        Math.min(255, base.getBlue()  + 60));

                int value = e.getValue();
                int barH  = Math.max(4, (int)((value / (double) max) * chartH));
                int x     = LEFT + gap + i * (barW + gap);
                int y     = TOP  + chartH - barH;

                // Gradient bar
                g2.setPaint(new GradientPaint(x, y, base, x, y + barH, lighter));
                g2.fillRoundRect(x, y, barW, barH, 6, 6);

                // Shine on top portion
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRoundRect(x, y, barW, barH / 3, 6, 6);

                g2.setPaint(null);

                // Value label above bar
                String val = String.valueOf(value);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.setColor(UIConstants.TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 5);

                // Category label — rotated 40° so 10 labels don't overlap
                String lbl = e.getKey();
                if (lbl.length() > 11) lbl = lbl.substring(0, 10) + "…";
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(UIConstants.TEXT_MUTED);
                Graphics2D gr = (Graphics2D) g2.create();
                gr.translate(x + barW / 2, TOP + chartH + 10);
                gr.rotate(Math.toRadians(-40));
                gr.drawString(lbl, -g2.getFontMetrics().stringWidth(lbl), 0);
                gr.dispose();

                i++;
            }
        }
    }
}
