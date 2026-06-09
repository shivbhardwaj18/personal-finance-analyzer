package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton DB connection manager.
 * Auto-creates the finance_app database and transactions table on first run.
 */
public class DBConnection {

    // ── Credentials (update if your MySQL setup differs) ──────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DB_NAME  = "finance_app";
    private static final String USER     = "root";
    private static final String PASSWORD = "Sh!vBh@rdw@j#18";

    // Root URL (no DB specified) — used to create the schema if missing
    private static final String ROOT_URL = "jdbc:mysql://" + HOST + ":" + PORT
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // App URL (DB specified)
    private static final String APP_URL  = "jdbc:mysql://" + HOST + ":" + PORT
            + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static DBConnection instance;
    private Connection connection;

    // ── Private constructor — bootstraps DB schema ─────────────────────────
    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            bootstrapSchema();
            connection = DriverManager.getConnection(APP_URL, USER, PASSWORD);
            System.out.println("[DBConnection] Connected to finance_app successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-j to lib/.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to MySQL: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the database and the transactions table if they do not already exist.
     */
    private void bootstrapSchema() throws SQLException {
        try (Connection rootConn = DriverManager.getConnection(ROOT_URL, USER, PASSWORD);
             Statement stmt = rootConn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id          INT AUTO_INCREMENT PRIMARY KEY,
                    amount      DOUBLE       NOT NULL,
                    category    VARCHAR(50)  NOT NULL,
                    type        VARCHAR(10)  NOT NULL,
                    date        DATE         NOT NULL,
                    description VARCHAR(255)
                )
            """);
            System.out.println("[DBConnection] Schema bootstrapped (DB + table ready).");
        }
    }

    // ── Singleton accessor ─────────────────────────────────────────────────
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /** Returns the live JDBC Connection. */
    public Connection getConnection() {
        return connection;
    }

    /** Call this when the app shuts down. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DBConnection] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
