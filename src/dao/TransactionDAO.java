package dao;

import db.DBConnection;
import model.Transaction;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data Access Object for the transactions table.
 *
 * Data flow:
 *   [UI] → [TransactionDAO] → [DBConnection.getConnection()] → [MySQL]
 */
public class TransactionDAO {

    private static final List<Transaction> demoTransactions = new ArrayList<>();

    static {
        // Pre-populate with clean demo data so the app starts with data immediately for the recruiter
        demoTransactions.add(new Transaction(1, 55000.0, "Other", "INCOME", LocalDate.now().minusDays(10), "Monthly Salary Credit"));
        demoTransactions.add(new Transaction(2, 12000.0, "Bills", "EXPENSE", LocalDate.now().minusDays(8), "Apartment Rent"));
        demoTransactions.add(new Transaction(3, 3500.0, "Food", "EXPENSE", LocalDate.now().minusDays(5), "Zomato dinner & grocery order"));
        demoTransactions.add(new Transaction(4, 4500.0, "Shopping", "EXPENSE", LocalDate.now().minusDays(3), "Shopping at H&M"));
        demoTransactions.add(new Transaction(5, 1800.0, "Travel", "EXPENSE", LocalDate.now().minusDays(2), "Uber ride & fuel refuel"));
        demoTransactions.add(new Transaction(6, 1200.0, "Entertainment", "EXPENSE", LocalDate.now().minusDays(1), "Movie tickets & snacks"));
    }

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    // ── INSERT ─────────────────────────────────────────────────────────────
    /**
     * Inserts a new transaction.
     * @param t Transaction without id (id is auto-assigned by DB)
     */
    public void insert(Transaction t) {
        if (DBConnection.getInstance().isDemoMode()) {
            int nextId = demoTransactions.stream().mapToInt(Transaction::getId).max().orElse(0) + 1;
            t.setId(nextId);
            demoTransactions.add(0, t); // add to top
            return;
        }
        String sql = "INSERT INTO transactions (amount, category, type, date, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, t.getAmount());
            ps.setString(2, t.getCategory());
            ps.setString(3, t.getType());
            ps.setDate(4, Date.valueOf(t.getDate()));
            ps.setString(5, t.getDescription());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed: " + e.getMessage(), e);
        }
    }

    // ── FETCH ALL ──────────────────────────────────────────────────────────
    /** Returns all transactions ordered newest first. */
    public List<Transaction> fetchAll() {
        if (DBConnection.getInstance().isDemoMode()) {
            return new ArrayList<>(demoTransactions);
        }
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC, id DESC";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("FetchAll failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    /** Updates an existing transaction by its id. */
    public void update(Transaction t) {
        if (DBConnection.getInstance().isDemoMode()) {
            for (int i = 0; i < demoTransactions.size(); i++) {
                if (demoTransactions.get(i).getId() == t.getId()) {
                    demoTransactions.set(i, t);
                    return;
                }
            }
            return;
        }
        String sql = "UPDATE transactions SET amount=?, category=?, type=?, date=?, description=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, t.getAmount());
            ps.setString(2, t.getCategory());
            ps.setString(3, t.getType());
            ps.setDate(4, Date.valueOf(t.getDate()));
            ps.setString(5, t.getDescription());
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update failed: " + e.getMessage(), e);
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    /** Deletes a transaction by id. */
    public void delete(int id) {
        if (DBConnection.getInstance().isDemoMode()) {
            demoTransactions.removeIf(t -> t.getId() == id);
            return;
        }
        String sql = "DELETE FROM transactions WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }

    // ── FETCH BY CATEGORY (for Pie Chart) ─────────────────────────────────
    /**
     * Returns total EXPENSE amount grouped by category.
     * Used for the dashboard pie chart.
     *
     * @return Map: category → total expense amount
     */
    public Map<String, Double> fetchByCategory() {
        if (DBConnection.getInstance().isDemoMode()) {
            return demoTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = """
            SELECT category, SUM(amount) AS total
            FROM transactions
            WHERE type = 'EXPENSE'
            GROUP BY category
            ORDER BY total DESC
        """;
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("fetchByCategory failed: " + e.getMessage(), e);
        }
        return map;
    }

    // ── FETCH BY MONTH (for Bar Chart) ────────────────────────────────────
    /**
     * Returns total EXPENSE amount grouped by month (last 6 months).
     * Used for the dashboard bar chart.
     *
     * @return Map: "YYYY-MM" → total expense amount (sorted oldest→newest)
     */
    public Map<String, Double> fetchByMonth() {
        if (DBConnection.getInstance().isDemoMode()) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            return demoTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()) && !t.getDate().isBefore(sixMonthsAgo))
                .collect(Collectors.groupingBy(t -> t.getDate().format(fmt), Collectors.summingDouble(Transaction::getAmount)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = """
            SELECT DATE_FORMAT(date, '%Y-%m') AS month, SUM(amount) AS total
            FROM transactions
            WHERE type = 'EXPENSE'
              AND date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
            GROUP BY month
            ORDER BY month ASC
        """;
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("month"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("fetchByMonth failed: " + e.getMessage(), e);
        }
        return map;
    }

    // ── Private mapper ─────────────────────────────────────────────────────
    private Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("id"),
            rs.getDouble("amount"),
            rs.getString("category"),
            rs.getString("type"),
            rs.getDate("date").toLocalDate(),
            rs.getString("description")
        );
    }
}
