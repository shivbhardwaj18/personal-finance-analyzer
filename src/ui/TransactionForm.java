package ui;

import dao.TransactionDAO;
import model.Transaction;
import util.AIEngine;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Premium transaction form with emoji labels, styled inputs, gradient buttons.
 */
public class TransactionForm extends JPanel {

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

    private static final String[] CATEGORIES = {"🍕 Food","✈️ Travel","🛍️ Shopping","🧾 Bills","🎬 Entertainment","📦 Other"};
    private static final String[] CAT_VALUES  = {"Food","Travel","Shopping","Bills","Entertainment","Other"};
    private static final String[] TYPES       = {"💸 EXPENSE","💰 INCOME"};
    private static final String[] TYPE_VALUES = {"EXPENSE","INCOME"};

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Font LABEL_FONT = new Font("Segoe UI Emoji", Font.BOLD, 12);
    private static final Font INPUT_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 14);
    private static final Font BTN_FONT   = new Font("Segoe UI Emoji", Font.BOLD, 13);

    private JTextField amountField, dateField, descField;
    private JComboBox<String> categoryBox, typeBox;
    private JButton addBtn, editBtn, deleteBtn, clearBtn;
    private TransactionTable txTable;
    private JLabel formTitle;

    private final TransactionDAO dao = new TransactionDAO();
    private final Consumer<String> onChangeCallback;
    private int editingId = -1;

    private boolean isProgrammaticChange = false;
    private boolean categoryManuallyChanged = false;

    public TransactionForm(Consumer<String> onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
        setLayout(new BorderLayout(0, 0));
        setBackground(BASE);

        add(buildFormSection(), BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);

        loadTable();
    }

    // ── Form Section ──────────────────────────────────────────────────────
    private JPanel buildFormSection() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BASE);
        outer.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        // Card
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, MANTLE, 0, getHeight(), new Color(28, 28, 42));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(
            new DashboardPanel.RoundedBorder(SURFACE0, 12, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(4, 6, 4, 6);

        // Section heading
        formTitle = new JLabel("➕  Add New Transaction");
        formTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        formTitle.setForeground(BLUE);
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        card.add(formTitle, gbc);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(SURFACE0);
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 3; gbc.insets = new Insets(2, 6, 8, 6);
        card.add(sep, gbc);
        gbc.insets = new Insets(4, 6, 4, 6);

        // Row 1 Labels
        gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.weightx = 0.3; card.add(fieldLabel("💲 Amount"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; card.add(fieldLabel("🔄 Type"), gbc);
        gbc.gridx = 2; gbc.weightx = 0.4; card.add(fieldLabel("📅 Date"), gbc);

        // Row 1 Fields
        gbc.gridy = 3;
        amountField = styledField("e.g. 500");
        gbc.gridx = 0; gbc.weightx = 0.3; card.add(amountField, gbc);

        typeBox = styledCombo(TYPES);
        gbc.gridx = 1; gbc.weightx = 0.3; card.add(typeBox, gbc);

        dateField = styledField(LocalDate.now().format(DATE_FMT));
        dateField.setText(LocalDate.now().format(DATE_FMT));
        gbc.gridx = 2; gbc.weightx = 0.4; card.add(dateField, gbc);

        // Row 2 Labels
        gbc.gridy = 4;
        gbc.gridx = 0; gbc.weightx = 0.3; gbc.gridwidth = 1; card.add(fieldLabel("🏷️ Category"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; gbc.gridwidth = 2; card.add(fieldLabel("📝 Description (auto-categorizes on Tab)"), gbc);

        // Row 2 Fields
        gbc.gridy = 5;
        categoryBox = styledCombo(CATEGORIES);
        categoryBox.addActionListener(e -> {
            if (!isProgrammaticChange) {
                categoryManuallyChanged = true;
            }
        });
        gbc.gridx = 0; gbc.weightx = 0.3; gbc.gridwidth = 1; card.add(categoryBox, gbc);

        descField = styledField("e.g. Zomato order, Uber ride...");
        descField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { autoFillCategory(); }
        });
        gbc.gridx = 1; gbc.weightx = 0.7; gbc.gridwidth = 2; card.add(descField, gbc);

        // Buttons row
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 3; gbc.insets = new Insets(14, 6, 4, 6); gbc.weightx = 1.0;
        addBtn    = actionBtn("➕  Add",        GREEN,  new Color(20, 60, 30));
        editBtn   = actionBtn("✏️  Update",    YELLOW, new Color(60, 50, 15));
        deleteBtn = actionBtn("🗑️  Delete",    RED,    new Color(70, 20, 30));
        clearBtn  = actionBtn("✖  Clear",      SURFACE1, SURFACE0);

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(addBtn); btnRow.add(editBtn); btnRow.add(deleteBtn);
        btnRow.add(Box.createHorizontalStrut(10)); btnRow.add(clearBtn);

        card.add(btnRow, gbc);

        // Listeners
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e -> clearForm());

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ── Table Section ─────────────────────────────────────────────────────
    private JPanel buildTableSection() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setBackground(BASE);
        outer.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel title = new JLabel("📋  Transaction History");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        title.setForeground(BLUE);
        headerRow.add(title, BorderLayout.WEST);

        JLabel hint = new JLabel("🖱️ Click a row to select for edit/delete");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(SURFACE1);
        headerRow.add(hint, BorderLayout.EAST);

        outer.add(headerRow, BorderLayout.NORTH);

        txTable = new TransactionTable();
        txTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromSelection();
        });
        outer.add(txTable, BorderLayout.CENTER);
        return outer;
    }

    // ── Actions ────────────────────────────────────────────────────────────
    private void onAdd() {
        Transaction t = buildFromForm();
        if (t == null) return;
        dao.insert(t);
        loadTable();
        clearForm();
        fireChange("✅ Transaction added — ID #" + t.getId());
    }

    private void onEdit() {
        if (editingId < 0) { showErr("No row selected."); return; }
        Transaction t = buildFromForm();
        if (t == null) return;
        t.setId(editingId);
        dao.update(t);
        loadTable();
        clearForm();
        fireChange("✏️ Transaction #" + editingId + " updated.");
    }

    private void onDelete() {
        if (editingId < 0) { showErr("No row selected."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
            "🗑️  Delete transaction #" + editingId + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        dao.delete(editingId);
        loadTable();
        clearForm();
        fireChange("🗑️ Transaction #" + editingId + " deleted.");
    }

    // ── Validation ─────────────────────────────────────────────────────────
    private Transaction buildFromForm() {
        String amtStr  = amountField.getText().trim();
        String cat     = CAT_VALUES[categoryBox.getSelectedIndex()];
        String type    = TYPE_VALUES[typeBox.getSelectedIndex()];
        String dateStr = dateField.getText().trim();
        String desc    = descField.getText().trim();

        if (amtStr.isEmpty() || dateStr.isEmpty() || desc.isEmpty()) {
            showErr("⚠️  All fields are required."); return null;
        }
        double amt;
        try { amt = Double.parseDouble(amtStr); }
        catch (NumberFormatException ex) { showErr("⚠️  Amount must be a valid number."); return null; }
        if (amt <= 0) { showErr("⚠️  Amount must be greater than zero."); return null; }

        LocalDate date;
        try { date = LocalDate.parse(dateStr, DATE_FMT); }
        catch (DateTimeParseException ex) { showErr("⚠️  Date must be yyyy-MM-dd format."); return null; }

        return new Transaction(amt, cat, type, date, desc);
    }

    private void autoFillCategory() {
        if (categoryManuallyChanged) return;
        String desc = descField.getText().trim();
        if (!desc.isEmpty()) {
            String cat = AIEngine.autoCategorize(desc);
            for (int i = 0; i < CAT_VALUES.length; i++) {
                if (CAT_VALUES[i].equals(cat)) {
                    isProgrammaticChange = true;
                    categoryBox.setSelectedIndex(i);
                    isProgrammaticChange = false;
                    break;
                }
            }
        }
    }

    private void populateFormFromSelection() {
        int id = txTable.getSelectedId();
        if (id < 0) { setEditMode(false); return; }

        DefaultTableModel m = txTable.getModel();
        int viewRow  = txTable.getTable().getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = txTable.getTable().convertRowIndexToModel(viewRow);

        amountField.setText(String.valueOf(m.getValueAt(modelRow, 1)));
        String cat = m.getValueAt(modelRow, 2).toString();
        isProgrammaticChange = true;
        for (int i = 0; i < CAT_VALUES.length; i++) {
            if (CAT_VALUES[i].equals(cat)) { categoryBox.setSelectedIndex(i); break; }
        }
        String type = m.getValueAt(modelRow, 3).toString();
        for (int i = 0; i < TYPE_VALUES.length; i++) {
            if (TYPE_VALUES[i].equals(type)) { typeBox.setSelectedIndex(i); break; }
        }
        isProgrammaticChange = false;
        categoryManuallyChanged = false;
        dateField.setText(m.getValueAt(modelRow, 4).toString());
        descField.setText(m.getValueAt(modelRow, 5).toString());

        editingId = id;
        setEditMode(true);
        formTitle.setText("✏️  Editing Transaction #" + id);
    }

    private void setEditMode(boolean editing) {
        addBtn.setEnabled(!editing);
        editBtn.setEnabled(editing);
        deleteBtn.setEnabled(editing);
        if (!editing) { editingId = -1; formTitle.setText("➕  Add New Transaction"); }
    }

    private void clearForm() {
        amountField.setText("");
        isProgrammaticChange = true;
        categoryBox.setSelectedIndex(0);
        typeBox.setSelectedIndex(0);
        isProgrammaticChange = false;
        categoryManuallyChanged = false;
        dateField.setText(LocalDate.now().format(DATE_FMT));
        descField.setText("");
        txTable.getTable().clearSelection();
        setEditMode(false);
    }

    public void loadTable() { txTable.loadData(dao.fetchAll()); }
    private void fireChange(String msg) { if (onChangeCallback != null) onChangeCallback.accept(msg); }

    // ── Helpers ────────────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL_FONT);
        l.setForeground(SUBTEXT0);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(90, 90, 120));
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        f.setFont(INPUT_FONT);
        f.setBackground(CRUST);
        f.setForeground(TEXT);
        f.setCaretColor(BLUE);
        f.setBorder(new CompoundBorder(
            new DashboardPanel.RoundedBorder(SURFACE0, 8, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new DashboardPanel.RoundedBorder(BLUE, 8, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new DashboardPanel.RoundedBorder(SURFACE0, 8, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(INPUT_FONT);
        c.setBackground(CRUST);
        c.setForeground(TEXT);
        c.setBorder(BorderFactory.createLineBorder(SURFACE0, 1));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int idx, boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
                l.setBackground(sel ? SURFACE0 : CRUST);
                l.setForeground(TEXT);
                l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                l.setFont(INPUT_FONT);
                return l;
            }
        });
        return c;
    }

    private JButton actionBtn(String text, Color fg, Color bgDark) {
        JButton b = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isEnabled()
                    ? (hovered ? bgDark.brighter() : bgDark)
                    : new Color(40, 40, 55);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(BTN_FONT);
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(
            new DashboardPanel.RoundedBorder(fg.darker(), 10, 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        b.setOpaque(false);
        return b;
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}
