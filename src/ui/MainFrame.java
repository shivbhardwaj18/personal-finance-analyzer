package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Premium redesigned MainFrame with gradient header, icon sidebar, animated nav buttons.
 */
public class MainFrame extends JFrame {

    // ── Palette (Catppuccin Mocha) ─────────────────────────────────────────
    static final Color BASE      = new Color(30,  30,  46);
    static final Color MANTLE    = new Color(24,  24,  37);
    static final Color CRUST     = new Color(17,  17,  27);
    static final Color SURFACE0  = new Color(49,  50,  68);
    static final Color SURFACE1  = new Color(69,  71,  90);
    static final Color OVERLAY0  = new Color(108, 112, 134);
    static final Color TEXT      = new Color(205, 214, 244);
    static final Color SUBTEXT1  = new Color(166, 173, 200);
    static final Color SUBTEXT0  = new Color(148, 163, 184);
    static final Color BLUE      = new Color(137, 180, 250);
    static final Color GREEN     = new Color(166, 227, 161);
    static final Color RED       = new Color(243, 139, 168);
    static final Color YELLOW    = new Color(249, 226, 175);
    static final Color MAUVE     = new Color(203, 166, 247);
    static final Color TEAL      = new Color(148, 226, 213);
    static final Color PEACH     = new Color(250, 179, 135);
    static final Color LAVENDER  = new Color(180, 190, 254);

    private static final String CARD_DASHBOARD    = "DASHBOARD";
    private static final String CARD_TRANSACTIONS = "TRANSACTIONS";
    private static final String CARD_AI           = "AI_INSIGHTS";

    private CardLayout cardLayout;
    private JPanel     cardPanel;
    private JLabel     statusLabel;
    private JLabel     clockLabel;

    private DashboardPanel   dashboardPanel;
    private TransactionForm  transactionForm;
    private AIInsightsPanel  aiInsightsPanel;

    private JButton activeSideBtn;

    public MainFrame() {
        super("💰 Personal Finance Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setBackground(BASE);

        // Custom title bar color hint (works on some systems)
        getRootPane().putClientProperty("apple.awt.brushMetalLook", true);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BASE);

        getContentPane().add(buildHeader(),  BorderLayout.NORTH);
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(buildCards(),   BorderLayout.CENTER);
        getContentPane().add(buildStatus(),  BorderLayout.SOUTH);

        startClock();
        setVisible(true);
    }

    // ── Gradient Header ───────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, CRUST, getWidth(), 0, new Color(35, 30, 55));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(0, 70));
        h.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SURFACE0),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));

        // Left: Logo + Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JLabel coin = new JLabel("💰");
        coin.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel title = new JLabel("Personal Finance Analyzer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(BLUE);

        JLabel sub = new JLabel("Track · Analyze · Predict · Grow");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(OVERLAY0);

        titleStack.add(title);
        titleStack.add(sub);
        left.add(coin);
        left.add(titleStack);

        // Right: clock + badge
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JLabel badge = makeBadge("● OFFLINE", GREEN);
        clockLabel = new JLabel("--:--:--");
        clockLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clockLabel.setForeground(SUBTEXT0);

        right.add(badge);
        right.add(clockLabel);

        h.add(left,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private JLabel makeBadge(String text, Color color) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color.darker().darker());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(color);
        l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        l.setOpaque(false);
        return l;
    }

    // ── Premium Sidebar ───────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(MANTLE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right edge glow
                GradientPaint glow = new GradientPaint(getWidth()-4, 0, new Color(137,180,250,30),
                    getWidth(), 0, new Color(137,180,250,0));
                g2.setPaint(glow);
                g2.fillRect(getWidth()-8, 0, 8, getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, SURFACE0),
            BorderFactory.createEmptyBorder(24, 12, 24, 12)
        ));

        // Section label
        JLabel navLabel = new JLabel("  M E N U");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navLabel.setForeground(OVERLAY0);
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(navLabel);
        sidebar.add(Box.createVerticalStrut(14));

        JButton dashBtn = navBtn("📊", "Dashboard",    CARD_DASHBOARD,  BLUE);
        JButton txBtn   = navBtn("💳", "Transactions", CARD_TRANSACTIONS, GREEN);
        JButton aiBtn   = navBtn("🤖", "AI Insights",  CARD_AI,          MAUVE);

        sidebar.add(dashBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(txBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(aiBtn);
        sidebar.add(Box.createVerticalGlue());

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(SURFACE0);
        sep.setMaximumSize(new Dimension(196, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(12));

        // Footer info
        JLabel ver = buildSideFooter("⚡ v1.0.0  |  Java 17+");
        JLabel db  = buildSideFooter("🗄 MySQL · finance_app");
        sidebar.add(ver);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(db);

        activateSideBtn(dashBtn);
        return sidebar;
    }

    private JLabel buildSideFooter(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(80, 80, 110));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        return l;
    }

    private JButton navBtn(String emoji, String label, String card, Color accent) {
        JButton b = new JButton() {
            private float hoverAlpha = 0f;
            private boolean hovered = false;

            {
                setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setMaximumSize(new Dimension(196, 46));
                setPreferredSize(new Dimension(196, 46));

                JLabel emojiLbl = new JLabel(emoji);
                emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

                JLabel textLbl = new JLabel(label);
                textLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                textLbl.setForeground(TEXT);

                add(emojiLbl);
                add(textLbl);

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = (this == activeSideBtn);
                if (isActive) {
                    GradientPaint gp = new GradientPaint(0, 0,
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40),
                        getWidth(), 0,
                        new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 10));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    // Left accent bar
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 6, 4, getHeight()-12, 4, 4);
                } else if (hovered) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        b.addActionListener(e -> {
            activateSideBtn(b);
            cardLayout.show(cardPanel, card);
            if (card.equals(CARD_DASHBOARD))    dashboardPanel.refresh();
            if (card.equals(CARD_AI))           aiInsightsPanel.refresh();
            if (card.equals(CARD_TRANSACTIONS)) transactionForm.loadTable();
            setStatus("📍 Viewing: " + label);
        });
        return b;
    }

    private void activateSideBtn(JButton b) {
        if (activeSideBtn != null) activeSideBtn.repaint();
        activeSideBtn = b;
        b.repaint();
    }

    // ── Card Content ───────────────────────────────────────────────────────
    private JPanel buildCards() {
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BASE);

        dashboardPanel   = new DashboardPanel();
        transactionForm  = new TransactionForm(msg -> {
            setStatus(msg);
            dashboardPanel.refresh();
        });
        aiInsightsPanel  = new AIInsightsPanel();

        cardPanel.add(dashboardPanel,  CARD_DASHBOARD);
        cardPanel.add(transactionForm, CARD_TRANSACTIONS);
        cardPanel.add(aiInsightsPanel, CARD_AI);

        cardLayout.show(cardPanel, CARD_DASHBOARD);
        return cardPanel;
    }

    // ── Status Bar ─────────────────────────────────────────────────────────
    private JPanel buildStatus() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(CRUST);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bar.setPreferredSize(new Dimension(0, 32));
        bar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, SURFACE0),
            BorderFactory.createEmptyBorder(0, 16, 0, 16)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel dot = new JLabel("●");
        dot.setForeground(GREEN);
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        statusLabel = new JLabel("Ready — Connected to finance_app database.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(SUBTEXT0);

        left.add(dot);
        left.add(statusLabel);

        JLabel hint = new JLabel("Click a row to edit  ·  ESC to clear");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(70, 70, 100));

        bar.add(left,  BorderLayout.WEST);
        bar.add(hint,  BorderLayout.EAST);
        return bar;
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    // ── Live Clock ─────────────────────────────────────────────────────────
    private void startClock() {
        Timer t = new Timer(1000, e -> {
            java.time.LocalTime now = java.time.LocalTime.now();
            clockLabel.setText(String.format("%02d:%02d:%02d",
                now.getHour(), now.getMinute(), now.getSecond()));
        });
        t.start();
    }
}
