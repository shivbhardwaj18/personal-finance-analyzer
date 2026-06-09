package ui;

import model.Transaction;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Premium styled JTable with INCOME/EXPENSE badge rendering and gradient header.
 */
public class TransactionTable extends JPanel {

    private static final Color BASE     = MainFrame.BASE;
    private static final Color MANTLE   = MainFrame.MANTLE;
    private static final Color SURFACE0 = MainFrame.SURFACE0;
    private static final Color SURFACE1 = MainFrame.SURFACE1;
    private static final Color TEXT     = MainFrame.TEXT;
    private static final Color SUBTEXT0 = MainFrame.SUBTEXT0;
    private static final Color BLUE     = MainFrame.BLUE;
    private static final Color GREEN    = MainFrame.GREEN;
    private static final Color RED      = MainFrame.RED;
    private static final Color OVERLAY0 = MainFrame.OVERLAY0;

    private static final Color ROW_INCOME  = new Color(20, 55, 32);
    private static final Color ROW_EXPENSE = new Color(60, 22, 32);
    private static final Color ROW_ALT     = new Color(36, 36, 54);
    private static final Color SELECTION   = new Color(70, 90, 140);

    private static final String[] COLUMNS = {"  #", "Amount (₹)", "Category", "Type", "Date", "Description"};

    private final DefaultTableModel model;
    private final JTable table;

    public TransactionTable() {
        setLayout(new BorderLayout());
        setBackground(BASE);

        model = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Integer.class;
                if (c == 1) return Double.class;
                return String.class;
            }
        };

        table = new JTable(model);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(MANTLE);
        scroll.getViewport().setBackground(MANTLE);
        scroll.setBorder(BorderFactory.createLineBorder(SURFACE0, 1));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        add(scroll, BorderLayout.CENTER);
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setBackground(MANTLE);
        table.setForeground(TEXT);
        table.setGridColor(SURFACE0);
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        // Styled header
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel l = new JLabel(v == null ? "" : v.toString());
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setForeground(BLUE);
                l.setBackground(new Color(20, 20, 35));
                l.setOpaque(true);
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, SURFACE0),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                l.setHorizontalAlignment(c == 1 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                return l;
            }
        });
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {45, 120, 120, 100, 105, 0};
        for (int i = 0; i < widths.length - 1; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (i < 5) table.getColumnModel().getColumn(i).setMaxWidth(i == 5 ? Integer.MAX_VALUE : widths[i] + 60);
        }

        // Custom cell renderer
        TableCellRenderer mainRenderer = createMainRenderer();
        TableCellRenderer typeRenderer = createTypeRenderer();

        for (int i = 0; i < 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(i == 3 ? typeRenderer : mainRenderer);
        }
    }

    private TableCellRenderer createMainRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                int modelRow = tbl.convertRowIndexToModel(row);

                if (isSelected) {
                    setBackground(SELECTION);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? MANTLE : BASE);
                    setForeground(TEXT);
                }

                if (col == 1 && value instanceof Double) {
                    String type2 = (String) tbl.getModel().getValueAt(modelRow, 3);
                    boolean isIncome = "INCOME".equalsIgnoreCase(type2);
                    setText((isIncome ? "+" : "-") + " ₹ " + String.format("%,.2f", (Double) value));
                    setForeground(isSelected ? Color.WHITE : (isIncome ? GREEN : RED));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setForeground(isSelected ? Color.WHITE : OVERLAY0);
                    setFont(new Font("Segoe UI", Font.PLAIN, 11));
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }

                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };
    }

    // Badge-style renderer for the Type column
    private TableCellRenderer createTypeRenderer() {
        return new TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

                String type = value == null ? "" : value.toString();
                boolean isIncome = "INCOME".equalsIgnoreCase(type);

                JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                if (isSelected) {
                    cell.setBackground(SELECTION);
                } else {
                    cell.setBackground(row % 2 == 0 ? MANTLE : BASE);
                }

                JLabel badge = new JLabel((isIncome ? "▲  " : "▼  ") + type) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(isIncome
                            ? new Color(166, 227, 161, 30)
                            : new Color(243, 139, 168, 30));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setForeground(isIncome ? GREEN : RED);
                badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                badge.setOpaque(false);

                cell.add(badge);
                return cell;
            }
        };
    }

    public void loadData(List<Transaction> transactions) {
        model.setRowCount(0);
        for (Transaction t : transactions) {
            model.addRow(new Object[]{
                t.getId(), t.getAmount(), t.getCategory(),
                t.getType(), t.getDate().toString(), t.getDescription()
            });
        }
    }

    public int getSelectedId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        return (int) model.getValueAt(table.convertRowIndexToModel(viewRow), 0);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getModel() { return model; }

    // ── Dark Scrollbar ─────────────────────────────────────────────────────
    public static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(80, 80, 110);
            trackColor = new Color(24, 24, 37);
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
