package ui;

import dao.TransactionDAO;
import model.Transaction;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

/**
 * Premium Dashboard with gradient summary cards + styled JFreeChart charts.
 */
public class DashboardPanel extends JPanel {

    private static final Color BASE     = MainFrame.BASE;
    private static final Color MANTLE   = MainFrame.MANTLE;
    private static final Color CRUST    = MainFrame.CRUST;
    private static final Color SURFACE0 = MainFrame.SURFACE0;
    private static final Color SURFACE1 = MainFrame.SURFACE1;
    private static final Color TEXT     = MainFrame.TEXT;
    private static final Color SUBTEXT0 = MainFrame.SUBTEXT0;
    private static final Color BLUE     = MainFrame.BLUE;
    private static final Color GREEN    = MainFrame.GREEN;
    private static final Color RED      = MainFrame.RED;
    private static final Color YELLOW   = MainFrame.YELLOW;
    private static final Color MAUVE    = MainFrame.MAUVE;
    private static final Color TEAL     = MainFrame.TEAL;
    private static final Color PEACH    = MainFrame.PEACH;
    private static final Color LAVENDER = MainFrame.LAVENDER;

    private static final Color[] PIE_COLORS = {BLUE, RED, GREEN, YELLOW, MAUVE, TEAL, PEACH, LAVENDER};

    private JLabel incomeVal, expenseVal, balanceVal;
    private JLabel incomeSub, expenseSub, balanceSub;
    private JPanel chartArea;
    private final TransactionDAO dao = new TransactionDAO();

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BASE);

        // Scrollable content
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(BASE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Top Row (Cards)
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 16, 0);
        content.add(buildTopRow(), gbc);

        // Chart Area
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        chartArea = new JPanel(new GridLayout(1, 2, 16, 0));
        chartArea.setBackground(BASE);
        content.add(chartArea, gbc);

        // Vertical Spacer to consume remaining height
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        content.add(spacer, gbc);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(BASE);
        scroll.getViewport().setBackground(BASE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new TransactionTable.DarkScrollBarUI());
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    // ── Refresh ────────────────────────────────────────────────────────────
    public void refresh() {
        List<Transaction> all = dao.fetchAll();

        double income  = all.stream().filter(t -> "INCOME".equals(t.getType()))
                            .mapToDouble(Transaction::getAmount).sum();
        double expense = all.stream().filter(t -> "EXPENSE".equals(t.getType()))
                            .mapToDouble(Transaction::getAmount).sum();
        double balance = income - expense;

        long incomeCount  = all.stream().filter(t -> "INCOME".equals(t.getType())).count();
        long expenseCount = all.stream().filter(t -> "EXPENSE".equals(t.getType())).count();

        incomeVal.setText(String.format("₹ %,.0f", income));
        expenseVal.setText(String.format("₹ %,.0f", expense));
        balanceVal.setText(String.format("₹ %,.0f", balance));
        balanceVal.setForeground(balance >= 0 ? GREEN : RED);

        incomeSub.setText(incomeCount + " transactions");
        expenseSub.setText(expenseCount + " transactions");
        boolean isPos = balance >= 0;
        balanceSub.setIcon(new TrendIcon(isPos, isPos ? GREEN : RED));
        balanceSub.setText(isPos ? "Positive" : "Negative");
        balanceSub.setForeground(isPos ? GREEN : RED);
        balanceSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        balanceSub.setIconTextGap(5);

        chartArea.removeAll();
        chartArea.add(wrapChart("📊", "Spending by Category", buildPieChart(dao.fetchByCategory())));
        chartArea.add(wrapChart("📅", "Monthly Expenses (Last 6 Months)", buildBarChart(dao.fetchByMonth())));
        chartArea.revalidate();
        chartArea.repaint();
    }

    // ── Top Row: Summary Cards ─────────────────────────────────────────────
    private JPanel buildTopRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setBackground(BASE);
        row.setPreferredSize(new Dimension(0, 110));

        incomeVal  = bigNumLabel("₹ 0", GREEN);
        expenseVal = bigNumLabel("₹ 0", RED);
        balanceVal = bigNumLabel("₹ 0", GREEN);

        incomeSub  = subLabel("0 transactions");
        expenseSub = subLabel("0 transactions");
        balanceSub = subLabel("—");

        row.add(buildGradientCard("💰", "TOTAL INCOME",  incomeVal,  incomeSub, GREEN));
        row.add(buildGradientCard("💸", "TOTAL EXPENSE", expenseVal, expenseSub, RED));
        row.add(buildGradientCard("🏦", "NET BALANCE",   balanceVal, balanceSub, BLUE));

        return row;
    }

    private JPanel buildGradientCard(String emoji, String title, JLabel valueLabel,
                                     JLabel subLabel, Color accent) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Solid premium dark background
                g2.setColor(MANTLE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                
                // Left vertical accent line
                g2.setColor(accent);
                g2.fillRoundRect(0, 12, 4, getHeight()-24, 4, 4);
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
            new RoundedBorder(SURFACE0, 12, 1),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titlePanel.setOpaque(false);

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        emojiLbl.setForeground(accent);

        JLabel textLbl = new JLabel(title);
        textLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        textLbl.setForeground(SUBTEXT0);

        titlePanel.add(emojiLbl);
        titlePanel.add(textLbl);

        gbc.gridy = 0; card.add(titlePanel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(6, 0, 4, 0);
        card.add(valueLabel, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        card.add(subLabel, gbc);

        return card;
    }

    private JLabel bigNumLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(color);
        return l;
    }

    private JLabel subLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(SUBTEXT0);
        return l;
    }

    // ── Chart wrapper card ─────────────────────────────────────────────────
    private JPanel wrapChart(String emoji, String title, ChartPanel cp) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MANTLE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
            new RoundedBorder(SURFACE0, 12, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);

        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        emojiLbl.setForeground(BLUE);

        JLabel textLbl = new JLabel(title);
        textLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        textLbl.setForeground(BLUE);

        titlePanel.add(emojiLbl);
        titlePanel.add(textLbl);

        card.add(titlePanel, BorderLayout.NORTH);

        // Limit the charts height to exactly 280px
        cp.setPreferredSize(new Dimension(450, 280));

        card.add(cp, BorderLayout.CENTER);
        return card;
    }

    // ── Pie Chart ─────────────────────────────────────────────────────────
    private ChartPanel buildPieChart(Map<String, Double> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (data.isEmpty()) dataset.setValue("No Data", 1);
        else data.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        chart.setBackgroundPaint(MANTLE);
        chart.setBorderVisible(false);

        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(MANTLE);
        plot.setOutlinePaint(null);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(TEXT);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelLinkPaint(SURFACE1);
        plot.setShadowPaint(null);
        plot.setSimpleLabels(true);
        plot.setInteriorGap(0.08);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(MANTLE);
            chart.getLegend().setItemPaint(TEXT);
            chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
            chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        }

        int i = 0;
        for (String key : data.keySet()) {
            plot.setSectionPaint(key, PIE_COLORS[i % PIE_COLORS.length]);
            plot.setSectionOutlinePaint(key, MANTLE);
            i++;
        }
        if (data.isEmpty()) plot.setSectionPaint("No Data", SURFACE0);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(MANTLE);
        cp.setOpaque(false);
        return cp;
    }

    // ── Bar Chart ─────────────────────────────────────────────────────────
    private ChartPanel buildBarChart(Map<String, Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (data.isEmpty()) dataset.addValue(0, "₹ Expenses", "No Data");
        else data.forEach((month, total) -> dataset.addValue(total, "₹ Expenses", month));

        JFreeChart chart = ChartFactory.createBarChart(
            "", "Month", "Amount (₹)", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(MANTLE);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(MANTLE);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(SURFACE0);
        plot.setRangeGridlineStroke(new BasicStroke(0.6f));
        plot.setOutlineVisible(false);

        CategoryAxis da = plot.getDomainAxis();
        da.setTickLabelPaint(SUBTEXT0);
        da.setLabelPaint(SUBTEXT0);
        da.setAxisLinePaint(SURFACE0);
        da.setTickMarkPaint(SURFACE0);
        da.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

        NumberAxis ra = (NumberAxis) plot.getRangeAxis();
        ra.setTickLabelPaint(SUBTEXT0);
        ra.setLabelPaint(SUBTEXT0);
        ra.setAxisLinePaint(SURFACE0);
        ra.setTickMarkPaint(SURFACE0);
        ra.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        ra.setNumberFormatOverride(new java.text.DecimalFormat("₹#,##0"));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, BLUE);
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.20);
        renderer.setItemMargin(0.1);
        renderer.setDefaultToolTipGenerator((ds, row, col) ->
            String.format("₹ %,.0f", ds.getValue(row, col)));

        // Subtle modern gradient bars
        GradientPaint barGrad = new GradientPaint(0, 0, BLUE, 0, 280, new Color(137, 180, 250, 40));
        renderer.setSeriesPaint(0, barGrad);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(MANTLE);
        cp.setOpaque(false);
        return cp;
    }

    // ── Rounded border helper ──────────────────────────────────────────────
    static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius, thickness;
        RoundedBorder(Color color, int radius, int thickness) {
            this.color = color; this.radius = radius; this.thickness = thickness;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Double(x+0.5, y+0.5, w-1, h-1, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    }

    // ── Trend indicator icon (green ▲ up / red ▼ down) ─────────────────────
    static class TrendIcon implements Icon {
        private final boolean up;
        private final Color color;

        TrendIcon(boolean up, Color color) {
            this.up = up;
            this.color = color;
        }

        @Override public int getIconWidth() { return 10; }
        @Override public int getIconHeight() { return 10; }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            Polygon p = new Polygon();
            if (up) {
                p.addPoint(x + 5, y + 1);
                p.addPoint(x + 1, y + 8);
                p.addPoint(x + 9, y + 8);
            } else {
                p.addPoint(x + 1, y + 2);
                p.addPoint(x + 9, y + 2);
                p.addPoint(x + 5, y + 9);
            }
            g2.fillPolygon(p);
            g2.dispose();
        }
    }
}
