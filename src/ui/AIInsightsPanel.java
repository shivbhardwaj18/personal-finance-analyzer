package ui;

import dao.TransactionDAO;
import model.Transaction;
import util.AIEngine;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

/**
 * Premium AI Insights panel with emoji alert cards, gradient backgrounds,
 * and a styled next-month prediction table.
 */
public class AIInsightsPanel extends JPanel {

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

    private final TransactionDAO dao = new TransactionDAO();
    private JPanel insightCards;
    private JPanel predictionSection;
    private JLabel lastRefreshLabel;

    public AIInsightsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BASE);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BASE);
        content.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        insightCards     = new JPanel();
        insightCards.setLayout(new BoxLayout(insightCards, BoxLayout.Y_AXIS));
        insightCards.setBackground(BASE);

        predictionSection = new JPanel();
        predictionSection.setLayout(new BoxLayout(predictionSection, BoxLayout.Y_AXIS));
        predictionSection.setBackground(BASE);

        content.add(insightCards);
        content.add(Box.createVerticalStrut(24));
        content.add(predictionSection);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBackground(BASE);
        scroll.getViewport().setBackground(BASE);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new TransactionTable.DarkScrollBarUI());
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    // ── Header ─────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(MANTLE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        h.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SURFACE0),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("🤖");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel title = new JLabel("AI Financial Insights");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(MAUVE);

        lastRefreshLabel = new JLabel("Powered by rule-based engine · No ML required");
        lastRefreshLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lastRefreshLabel.setForeground(SURFACE1);

        titleStack.add(title);
        titleStack.add(lastRefreshLabel);
        left.add(icon); left.add(titleStack);

        h.add(left, BorderLayout.WEST);
        h.add(buildRefreshButton(), BorderLayout.EAST);
        return h;
    }

    // ── Refresh ────────────────────────────────────────────────────────────
    public void refresh() {
        List<Transaction> all = dao.fetchAll();
        List<String>       insights    = AIEngine.generateInsights(all);
        Map<String, Double> predictions = AIEngine.predictNextMonth(all);

        // ── Insights section ──────────────────────────────────────────────
        insightCards.removeAll();

        insightCards.add(sectionHeader("📊", "Insights & Smart Alerts", BLUE));
        insightCards.add(Box.createVerticalStrut(12));

        for (String insight : insights) {
            insightCards.add(buildInsightCard(insight));
            insightCards.add(Box.createVerticalStrut(8));
        }

        // ── Predictions section ───────────────────────────────────────────
        predictionSection.removeAll();

        predictionSection.add(sectionHeader("🔮", "Next Month Prediction", TEAL));
        predictionSection.add(Box.createVerticalStrut(6));

        JLabel sub = new JLabel("  Based on rolling 3-month average per category");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        sub.setForeground(SURFACE1);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        predictionSection.add(sub);
        predictionSection.add(Box.createVerticalStrut(12));
        predictionSection.add(buildPredictionCards(predictions));

        insightCards.revalidate(); insightCards.repaint();
        predictionSection.revalidate(); predictionSection.repaint();

        lastRefreshLabel.setText("Last refreshed: " +
            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // ── Insight Card ───────────────────────────────────────────────────────
    private JPanel buildInsightCard(String text) {
        Color accent;
        if      (text.startsWith("🚨")) { accent = RED; }
        else if (text.startsWith("⚠️")) { accent = YELLOW; }
        else if (text.startsWith("✅")) { accent = GREEN; }
        else if (text.startsWith("💡")) { accent = TEAL; }
        else if (text.startsWith("📅")) { accent = BLUE; }
        else                             { accent = MAUVE; }

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MANTLE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        card.setPreferredSize(new Dimension(0, 54));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(new CompoundBorder(
            new DashboardPanel.RoundedBorder(SURFACE0, 10, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Left glow bar
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 2, getWidth(), getHeight()-4, 4, 4);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(4, 0));

        JLabel label = new JLabel("<html><body style='width:680px'>" + escHtml(text) + "</body></html>");
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        label.setForeground(TEXT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        card.add(bar, BorderLayout.WEST);
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    // ── Prediction Cards (horizontal grid) ────────────────────────────────
    private JPanel buildPredictionCards(Map<String, Double> predictions) {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(BASE);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (predictions.isEmpty()) {
            wrapper.setLayout(new FlowLayout(FlowLayout.LEFT));
            JPanel empty = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(MANTLE);
                    g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                    g2.dispose();
                }
            };
            empty.setOpaque(false);
            empty.setPreferredSize(new Dimension(500, 60));
            empty.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));
            JLabel l = new JLabel("📭  Add transactions for 3+ months to see predictions.");
            l.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            l.setForeground(SUBTEXT0);
            empty.add(l);
            empty.setBorder(new DashboardPanel.RoundedBorder(SURFACE0, 12, 1));
            wrapper.add(empty);
            return wrapper;
        }

        wrapper.setLayout(new WrapLayout(FlowLayout.LEFT, 12, 10));
        String[] catEmoji = {"🍕","✈️","🛍️","🧾","🎬","📦"};
        String[] catNames = {"Food","Travel","Shopping","Bills","Entertainment","Other"};
        Color[] catColors = {YELLOW, TEAL, MAUVE, RED, BLUE, SUBTEXT0};

        for (Map.Entry<String, Double> e : predictions.entrySet()) {
            String cat = e.getKey();
            double val = e.getValue();
            String emoji = "📦";
            Color color = BLUE;
            for (int i = 0; i < catNames.length; i++) {
                if (catNames[i].equals(cat)) { emoji = catEmoji[i]; color = catColors[i]; break; }
            }
            wrapper.add(buildPredCard(emoji, cat, val, color));
        }
        return wrapper;
    }

    private JPanel buildPredCard(String emoji, String cat, double val, Color accent) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                    new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30),
                    0, getHeight(), MANTLE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(160, 90));
        card.setBorder(new CompoundBorder(
            new DashboardPanel.RoundedBorder(accent.darker(), 14, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;

        JLabel emojiLbl = new JLabel(emoji + "  " + cat);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        emojiLbl.setForeground(accent);
        card.add(emojiLbl, gbc);

        JLabel amtLbl = new JLabel(String.format("₹ %,.0f", val));
        amtLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amtLbl.setForeground(TEXT);
        gbc.gridy = 1; gbc.insets = new Insets(6, 0, 0, 0);
        card.add(amtLbl, gbc);

        return card;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JPanel sectionHeader(String emoji, String text, Color accent) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(4, 22));

        JLabel emojiLbl = new JLabel("  " + emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        emojiLbl.setForeground(accent);

        JLabel textLbl = new JLabel(" " + text);
        textLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        textLbl.setForeground(accent);

        row.add(bar); row.add(emojiLbl); row.add(textLbl);
        return row;
    }

    private JButton buildRefreshButton() {
        JButton b = new JButton("🔄  Refresh") {
            private boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hov = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(35, 50, 70) : new Color(25, 35, 55));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(TEAL);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(
            new DashboardPanel.RoundedBorder(TEAL.darker(), 10, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        b.setOpaque(false);
        b.addActionListener(e -> refresh());
        return b;
    }

    private String escHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ── Wrap Layout (auto-wraps child components like CSS flexbox wrap) ─────
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;
                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.height += rowHeight + vgap;
                            rowWidth = 0; rowHeight = 0;
                        }
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                dim.height += rowHeight + insets.top + insets.bottom + vgap * 2;
                dim.width  = maxWidth;
                return dim;
            }
        }
    }
}
